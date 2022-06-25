package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.events.AuthenticationFailureBadCredentialsEvent;
import com.github.miho73.ipu.library.events.AuthenticationSuccessEvent;
import com.github.miho73.ipu.library.exceptions.CaptchaFailureException;
import com.github.miho73.ipu.library.exceptions.DuplicatedException;
import com.github.miho73.ipu.library.exceptions.ForbiddenException;
import com.github.miho73.ipu.library.security.Captcha;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.library.security.SecureTools;
import com.github.miho73.ipu.library.security.bruteforce.LoginAttemptService;
import com.github.miho73.ipu.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

@Service("AuthService")
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private SHA sha;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private LoginAttemptService loginAttemptService;
    @Autowired private Captcha captcha;
    @Autowired private SessionService sessionService;
    @Autowired private InviteService inviteService;

    private final Pattern PasswordValidator = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\]{6,}$");

    @Value("${captcha.v3.sitekey}") private String CAPTCHA_V3_SITE_KEY;
    @Value("${captcha.v2.sitekey}") private String CAPTCHA_V2_SITE_KEY;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AuthService() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
    }

    /**
     * process login
     * @param id        Id to login
     * @param password  Password of user to login
     * @param gToken    Google reCAPTCHA token
     * @param gVers     Google reCAPTCHA version(v2 or v3)
     * @param session   User session to login
     * @param request   HttpServetRequest of login request
     * @return 0 when succeed / 1 when login locked / 2 when id not found / 3 when password is wrong / 4 when user is blocked / 5 when reCAPTCHA failure
     */
    public short completeLogin(String id, String password, String gToken, String gVers, HttpSession session, HttpServletRequest request) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        // return 1 if login is locked
        if(this.checkLocked(request)) {
            return 1;
        }

        // if form is empty, throw InvalidInputException
        if(id.equals("") || password.equals(""))
            return 1;

        // complete captcha test
        boolean captchaFlag = false;
        if(gVers.equals("v3") && captcha.getV3Result(gToken)) captchaFlag = true;
        else if(gVers.equals("v2") && captcha.getV2Result(gToken)) captchaFlag = true;

        // if captcha ok
        if (captchaFlag) {
            Connection connection = userRepository.openConnection();
            User user = userRepository.getUserForAuthentication(id, connection);

            // when user is not found
            if (user == null) {
                LOGGER.debug("Login attempt: id=" + id + ", result=id not found("+id+")");
                reportAuthFailure(request);
                userRepository.close(connection);
                return 2;
            }
            // when user does not have 'u' privilege, block login
            if (!user.getPrivilege().contains("u")) {
                LOGGER.debug("Login attempt: id=" + id + ", result=blocked");
                userRepository.close(connection);
                return 4;
            }
            // check password
            String hash = sha.SHA512(password, user.getSalt());
            // when login succeed
            if (user.getPwd().equals(hash)) {
                LOGGER.debug("Login attempt: id=" + id + ", result=ok");
                user = userRepository.getUserById(id, connection);
                userRepository.updateUserLastLoginById(id, new Timestamp(System.currentTimeMillis()), connection);
                sessionService.setAttribute(session, "isLoggedIn", true);
                sessionService.setUserSession(session, user);
                LOGGER.debug("Set session for session id "+session.getId());
                userRepository.close(connection);
                return 0;
            }
            else {
                LOGGER.debug("Login attempt: id=" + id + ", result=wrong password");
                reportAuthFailure(request);
                userRepository.close(connection);
                return 3;
            }
        }
        // when captcha failure
        else {
            LOGGER.debug("Login attempt: id=" + id + ", result=CAPTCHA failed");
            return 5;
        }
    }

    private boolean checkLocked(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null) {
            ip = request.getRemoteAddr();
        }
        else {
            ip = ip.split(",")[0];
        }
        boolean ban = loginAttemptService.isBlocked(ip);
        if(ban) LOGGER.debug("IP ban: IP="+ip);
        return ban;
    }
    private void reportAuthFailure(HttpServletRequest request) {
        publisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(request));
    }
    private void reportAuthSuccess(HttpServletRequest request) {
        publisher.publishEvent(new AuthenticationSuccessEvent(request));
    }

    /**
     *
     * @param id user id to authenticate
     * @param pwd password that user provided
     * @param request HttpServletRequest of request
     * @return 0 when succeed / 1 when locked / 2 when user not found / 3 when password is wrong / 4 when captcha failure
     * @throws SQLException Sql error
     * @throws InvalidInputException hash error
     * @throws NoSuchAlgorithmException hash error
     */
    public int auth(String id, String pwd, String gToken, String gVers, HttpServletRequest request) throws SQLException, InvalidInputException, NoSuchAlgorithmException, IOException {
        if(checkLocked(request)) {
            return 1;
        }

        boolean captchaFlag = false;
        if(gVers.equals("v3") && captcha.getV3Result(gToken)) captchaFlag = true;
        else if(gVers.equals("v2") && captcha.getV2Result(gToken)) captchaFlag = true;
        if(!captchaFlag) {
            return 4;
        }

        Connection connection = userRepository.openConnection();
        User user = userRepository.getUserForAuthentication(id, connection);
        if (user == null) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=id not found("+id+")");
            userRepository.close(connection);
            return 2;
        }
        String hash = sha.SHA512(pwd, user.getSalt());
        if (user.getPwd().equals(hash)) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=ok");
            userRepository.close(connection);
            reportAuthSuccess(request);
            return 0;
        }
        LOGGER.debug("Authentication: id=" + id + ", result=wrong password");
        reportAuthFailure(request);
        userRepository.close(connection);
        return 3;
    }

    /**
     * check if id is already exists on database
     * @param id id to check
     * @return returns if id is already on database
     */
    private boolean IdDuplicationTest(String id) throws SQLException {
        Connection connection = userRepository.openConnection();
        Object code = userRepository.getUserDataById(id, "user_code", connection);
        userRepository.close(connection);
        return code==null;
    }

    public void addUser(User user, String captchaToken) throws IOException, CaptchaFailureException, SQLException, ForbiddenException, DuplicatedException, NoSuchAlgorithmException, InvalidInputException {

        if(!captcha.getV2Result(captchaToken)) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=CAPTCHA failed");
            throw new CaptchaFailureException("for token "+captchaToken);
        }
        if(!inviteService.checkExists(user.getInviteCode())) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=Invalid invite");
            throw new ForbiddenException("unknown invite code " +user.getInviteCode());
        }
        if(!IdDuplicationTest(user.getId())) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=ID Duplicated");
            throw new DuplicatedException("id "+user.getId()+" already exists");
        }

        // Generate hash salt and hash
        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(user.getPwd(), salt);
        user.setPwd(hash);
        user.setSalt(Base64.getEncoder().encodeToString(salt));

        // Insert into database
        Connection userConnection = null;
        try {
            userConnection = userRepository.openConnectionForEdit();
            userRepository.addUser(user, userConnection);
            int code = (int) userRepository.getUserDataById(user.getId(), "user_code", userConnection);
            userRepository.commitAndClose(userConnection);
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=ok");
        }
        catch (SQLException e) {
            LOGGER.error("Signup request: id="+user.getId()+", name="+user.getName()+", result=error", e);
            if(userConnection != null) userRepository.rollbackAndClose(userConnection);
            throw e;
        }
    }

    /**
     * update user password
     * @param nPwd new password for user
     * @param uCode code of user to change password
     */
    public void updatePassword(String nPwd, int uCode) throws NoSuchAlgorithmException, InvalidInputException, SQLException {
        // check password form
        if(!PasswordValidator.matcher(nPwd).matches()) {
            throw new InvalidInputException("pwd");
        }

        // update salt and hash
        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(nPwd, salt);

        // Update database
        Connection connection = userRepository.openConnectionForEdit();
        try {
            userRepository.updatePassword(hash, Base64.getEncoder().encodeToString(salt), uCode, connection);
            userRepository.commitAndClose(connection);
        }
        catch (SQLException e) {
            userRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot update password", e);
            throw e;
        }
    }

    public void modelCaptchaV3(Model model) {
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V3_SITE_KEY,
                "captcha_version", "v3"
        ));
    }
    public void modelCaptchaV2(Model model) {
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V2_SITE_KEY,
                "captcha_version", "v2"
        ));
    }
}

