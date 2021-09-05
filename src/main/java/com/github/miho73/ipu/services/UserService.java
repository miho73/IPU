package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.security.Captcha;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service("UserService")
public class UserService {
    private final UserRepository userRepository;
    private final SHA sha;
    private final Captcha captcha;
    private SessionService sessionService;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);
    private TimeZone tz = TimeZone.getTimeZone("UTC");
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    @Autowired
    public UserService(UserRepository userRepository, SHA sha, Captcha captcha, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sha = sha;
        this.captcha = captcha;
        this.sessionService = sessionService;

        df.setTimeZone(tz);
    }

    public User getUserByCode(long code) throws SQLException {
        return userRepository.getUserByCode(code);
    }

    public User getUserById(String id) throws SQLException {
        return userRepository.getUserById(id);
    }

    public enum LOGIN_RESULT {
        OK,
        BAD_PASSWORD,
        ID_NOT_FOUND,
        INVALID_INPUT,
        CAPTCHA_FAILED,
    }

    public LOGIN_RESULT checkLogin(LoginForm form, HttpSession session) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        if(form.getId().equals("") || form.getPassword().equals("")) throw new InvalidInputException("");

        boolean captchaFlag = false;
        if(form.getgVers().equals("v3") && captcha.getV3Result(form.getgToken())) captchaFlag = true;
        else if(form.getgToken().equals("v2") && captcha.getV2Result(form.getgToken())) captchaFlag = true;

        if (captchaFlag) {
            User user = userRepository.getUserForAuthentication(form.getId());
            if (user == null) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=id not found");
                return LOGIN_RESULT.ID_NOT_FOUND;
            }
            String hash = sha.SHA512(form.getPassword(), user.getSalt());
            if (user.getPwd().equals(hash)) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=ok");
                user = userRepository.getUserById(form.getId());
                LOGGER.debug("Queried user data to set session. id "+form.getId());
                userRepository.updateUserStringById(form.getId(), "last_login", df.format(new Date()));
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
}

