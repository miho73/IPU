package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.exceptions.CaptchaFailureException;
import com.github.miho73.ipu.library.exceptions.DuplicatedException;
import com.github.miho73.ipu.library.exceptions.ForbiddenException;
import com.github.miho73.ipu.library.security.Captcha;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.library.security.SecureTools;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;

@Service("AuthService")
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private SolutionRepository solutionRepository;
    @Autowired private SHA sha;
    @Autowired private Captcha captcha;
    @Autowired private SessionService sessionService;
    @Autowired private InviteService inviteService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public AuthService() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
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

    public LOGIN_RESULT checkLogin(String id, String password, String gToken, String gVers, HttpSession session) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        if(id.equals("") || password.equals("")) throw new InvalidInputException("");

        boolean captchaFlag = false;
        if(gVers.equals("v3") && captcha.getV3Result(gToken)) captchaFlag = true;
        else if(gVers.equals("v2") && captcha.getV2Result(gToken)) captchaFlag = true;

        if (captchaFlag) {
            Connection connection = userRepository.openConnection();

            User user = userRepository.getUserForAuthentication(id, connection);
            if (user == null) {
                LOGGER.debug("Login attempt: id=" + id + ", result=id not found");
                userRepository.close(connection);
                return LOGIN_RESULT.ID_NOT_FOUND;
            }
            if (!user.getPrivilege().contains("u")) {
                LOGGER.debug("Login attempt: id=" + id + ", result=blocked");
                userRepository.close(connection);
                return LOGIN_RESULT.BLOCKED;
            }
            String hash = sha.SHA512(password, user.getSalt());
            if (user.getPwd().equals(hash)) {
                LOGGER.debug("Login attempt: id=" + id + ", result=ok");
                user = userRepository.getUserById(id, connection);
                LOGGER.debug("Queried user data to set session. id "+id);
                userRepository.updateUserTSById(id, "last_login", new Timestamp(System.currentTimeMillis()), connection);
                LOGGER.debug("Updated last login. id "+id);
                sessionService.setAttribute(session, "isLoggedIn", true);
                sessionService.setUserSession(session, user);
                LOGGER.debug("Set session for session id "+session.getId());
                userRepository.close(connection);
                return LOGIN_RESULT.OK;
            }
            LOGGER.debug("Login attempt: id=" + id + ", result=wrong password");
            userRepository.close(connection);
            return LOGIN_RESULT.BAD_PASSWORD;
        } else {
            LOGGER.debug("Login attempt: id=" + id + ", result=CAPTCHA failed");
            return LOGIN_RESULT.CAPTCHA_FAILED;
        }
    }

    public boolean auth(String id, String pwd) throws SQLException, InvalidInputException, NoSuchAlgorithmException {
        Connection connection = userRepository.openConnection();

        User user = userRepository.getUserForAuthentication(id, connection);
        if (user == null) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=id not found");
            userRepository.close(connection);
            return false;
        }
        String hash = sha.SHA512(pwd, user.getSalt());
        if (user.getPwd().equals(hash)) {
            LOGGER.debug("Authentication attempt: id=" + id + ", result=ok");
            userRepository.close(connection);
            return true;
        }
        LOGGER.debug("Authentication: id=" + id + ", result=wrong password");
        userRepository.close(connection);
        return false;
    }

    private boolean IdDuplicationTest(String id) throws SQLException {
        Connection connection = userRepository.openConnection();
        Object code = userRepository.getUserDataById(id, "user_code", connection);
        userRepository.close(connection);
        return code==null;
    }

    public enum SIGNUP_RESULT {
        OK,
        CAPTCHA_FAILED,
        INVALID_INVITE,
        DUPLICATED_ID,
        ERROR
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
            throw new DuplicatedException(String.format("id %s already exists", user.getId()));
        }

        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(user.getPwd(), salt);
        user.setPwd(hash);
        user.setSalt(Base64.getEncoder().encodeToString(salt));
        Connection userConnection = null;
        try {
            userConnection = userRepository.openConnectionForEdit();
            userRepository.addUser(user, userConnection);
            int code = (int) userRepository.getUserDataById(user.getId(), "user_code", userConnection);
            userRepository.commitAndClose(userConnection);
            LOGGER.debug("Signup request: id="+user.getId()+", name="+user.getName()+", result=ok");
        }
        catch (Exception e) {
            LOGGER.error("Signup request: id="+user.getId()+", name="+user.getName()+", result=Internal error", e);
            if(userConnection != null) userRepository.rollback(userConnection);
            if(userConnection != null) userRepository.close(userConnection);
            throw e;
        }
    }

    public void updatePassword(String nPwd, int uCode) throws NoSuchAlgorithmException, InvalidInputException, SQLException {
        byte[] salt = SecureTools.getSecureRandom(64);
        String hash = sha.SHA512(nPwd, salt);
        Connection connection = userRepository.openConnection();
        try {
            userRepository.updatePassword(hash, Base64.getEncoder().encodeToString(salt), uCode, connection);
            userRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            userRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot update password", e);
        }
    }
}

