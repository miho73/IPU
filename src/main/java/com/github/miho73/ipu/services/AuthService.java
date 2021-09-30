package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.security.Captcha;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.library.security.SecureTools;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

@Service("AuthService")
public class AuthService {
    private final UserRepository userRepository;
    private final SolutionRepository solutionRepository;
    private final SHA sha;
    private final Captcha captcha;
    private final SessionService sessionService;
    private final InviteService inviteService;

    private final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    @Autowired
    public AuthService(UserRepository userRepository, SHA sha, Captcha captcha, SessionService sessionService, InviteService inviteService, SolutionRepository solutionRepository) {
        this.userRepository = userRepository;
        this.sha = sha;
        this.captcha = captcha;
        this.sessionService = sessionService;
        this.inviteService = inviteService;
        this.solutionRepository = solutionRepository;

        df.setTimeZone(tz);
    }

    public enum LOGIN_RESULT {
        OK,
        BAD_PASSWORD,
        ID_NOT_FOUND,
        INVALID_INPUT,
        CAPTCHA_FAILED,
        BLOCKED
    }

    public LOGIN_RESULT checkLogin(LoginForm form, HttpSession session) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        if(form.getId().equals("") || form.getPassword().equals("")) throw new InvalidInputException("");

        boolean captchaFlag = false;
        if(form.getgVers().equals("v3") && captcha.getV3Result(form.getgToken())) captchaFlag = true;
        else if(form.getgVers().equals("v2") && captcha.getV2Result(form.getgToken())) captchaFlag = true;

        if (captchaFlag) {
            User user = userRepository.getUserForAuthentication(form.getId());
            if (user == null) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=id not found");
                return LOGIN_RESULT.ID_NOT_FOUND;
            }
            if (!user.getPrivilege().contains("u")) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=blocked");
                return LOGIN_RESULT.BLOCKED;
            }
            String hash = sha.SHA512(form.getPassword(), user.getSalt());
            if (user.getPwd().equals(hash)) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=ok");
                user = userRepository.getUserById(form.getId());
                LOGGER.debug("Queried user data to set session. id "+form.getId());
                userRepository.updateUserTSById(form.getId(), "last_login", new Timestamp(System.currentTimeMillis()));
                LOGGER.debug("Updated last login. id "+form.getId());
                sessionService.setAttribute(session, "isLoggedIn", true);
                sessionService.setUserSession(session, user);
                LOGGER.debug("Set session for session id "+session.getId());
                return LOGIN_RESULT.OK;
            }
            LOGGER.debug("Login attempt: id=" + form.getId() + ", result=wrong password");
            return LOGIN_RESULT.BAD_PASSWORD;
        } else {
            LOGGER.debug("Login attempt: id=" + form.getId() + ", result=CAPTCHA failed");
            return LOGIN_RESULT.CAPTCHA_FAILED;
        }
    }

    public boolean auth(String id, String pwd) throws SQLException, InvalidInputException, NoSuchAlgorithmException {
        User user = userRepository.getUserForAuthentication(id);
        if (user == null) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=id not found");
            return false;
        }
        String hash = sha.SHA512(pwd, user.getSalt());
        if (user.getPwd().equals(hash)) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=ok");
            return true;
        }
        LOGGER.debug("Authentication: id=" + id + ", result=wrong password");
        return false;
    }

    private boolean IdDuplicationTest(String id) throws SQLException {
        Object code = userRepository.getUserDataById(id, "user_code");
        return code==null;
    }

    public enum SIGNUP_RESULT {
        OK,
        CAPTCHA_FAILED,
        INVALID_INVITE,
        DUPLICATED_ID
    }

    @Transactional
    //TODO: If one of entire user-add procedure makes error? Transaction required.
    public SIGNUP_RESULT addUser(User user, String captchaToken) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        if(!captcha.getV2Result(captchaToken)) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=CAPTCHA failed");
            return SIGNUP_RESULT.CAPTCHA_FAILED;
        }
        if(!inviteService.checkExists(user.getInviteCode())) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=Invalid invite");
            return SIGNUP_RESULT.INVALID_INVITE;
        }
        if(!IdDuplicationTest(user.getId())) {
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=ID Duplicated");
            return SIGNUP_RESULT.DUPLICATED_ID;
        }

        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(user.getPwd(), salt);
        user.setPwd(hash);
        user.setSalt(Base64.getEncoder().encodeToString(salt));
        userRepository.addUser(user);
        int code = (int) userRepository.getUserDataById(user.getId(), "user_code");
        solutionRepository.addUser(code);
        LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=ok");
        return SIGNUP_RESULT.OK;
    }

    public void updatePassword(String nPwd, int uCode) throws NoSuchAlgorithmException, InvalidInputException, SQLException {
        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(nPwd, salt);
        userRepository.updatePassword(hash, Base64.getEncoder().encodeToString(salt), uCode);
    }
}
