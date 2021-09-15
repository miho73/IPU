package com.github.miho73.ipu.services;

import com.github.miho73.ipu.controllers.UserControl;
import com.github.miho73.ipu.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service("SessionService")
public class SessionService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserControl.class);

    /**
     * Check login
     * @param session session of user to check login.
     * @return true if logged in. Otherwise, false.
     */
    public boolean checkLogin(HttpSession session) {
        if(session == null) return false;
        Object logged = session.getAttribute("isLoggedIn");
        if(logged == null) return false;
        return (boolean)logged;
    }

    public void setAttribute(HttpSession session, String key, Object value) {
        session.setAttribute(key, value);
    }

    public void setUserSession(HttpSession session, User user) {
        session.setAttribute("privilege", user.getPrivilege());
        session.setAttribute("id", user.getId());
        session.setAttribute("code", user.getCode());
        session.setAttribute("name", user.getName());
    }
    public User getUserData(HttpSession session) {
        return new User((String)session.getAttribute("id"), (String)session.getAttribute("name"), (String)session.getAttribute("privilege"));
    }

    public String getName(HttpSession session) {
        return (String)session.getAttribute("name");
    }
    public String getId(HttpSession session) {
        return (String)session.getAttribute("id");
    }
    public long getCode(HttpSession session) {
        return (long)session.getAttribute("code");
    }

    public void loadSessionToModel(HttpSession session, Model model) {
        if(checkLogin(session)) {
            User user = getUserData(session);
            model.addAllAttributes(Map.of(
                    "logged", true,
                    "user", user
            ));
        }
        else {
            model.addAllAttributes(Map.of(
                    "logged", false
            ));
        }
    }

    public enum PRIVILEGES {
        USER,
        PROBLEM_MAKE,
        INVITE_CODES,
        SUPERUSER
    }

    /**
     * Check privilege of user via session privilege
     * @param p required privilege
     * @param session session of user want to check
     * @return True if user don't have sufficient privilege. If user have access, return false
     */
    public boolean hasPrivilege(PRIVILEGES p, HttpSession session) {
        String priv = (String)session.getAttribute("privilege");
        LOGGER.debug("Privilege check: \""+priv+"\". "+p+" required to access.");
        if(priv == null) return true;
        if(priv.contains("s")) return false;
        return !switch (p) {
            case USER -> priv.contains("u");
            case PROBLEM_MAKE -> priv.contains("p");
            case INVITE_CODES -> priv.contains("m");
            default -> false;
        };
    }

    public void invalidSession(HttpSession session) {
        session.invalidate();
    }
}
