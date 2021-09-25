package com.github.miho73.ipu.services;

import com.github.miho73.ipu.controllers.AuthControl;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.repositories.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service("SessionService")
public class SessionService {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthControl.class);
    private final SessionRepository sessionRepository = new SessionRepository();

    /**
     * Check login
     * @param session session of user to check login.
     * @return true if logged in. Otherwise, false.
     */
    public boolean checkLogin(HttpSession session) {
        if(session == null) return false;
        Object logged = session.getAttribute("isLoggedIn");
        if(logged == null) return false;
        if(!sessionRepository.exists(getId(session))) return false;
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
        LOGGER.debug("Set user to session: "+user.getCode()+": "+user.getId()+"("+user.getName()+") "+user.getPrivilege());
        if(sessionRepository.exists(user.getId())) {
            invalidSession(sessionRepository.get(user.getId()));
            sessionRepository.remove(user.getId());
        }
        LOGGER.debug("Registering session to table. ID="+user.getId());
        sessionRepository.add(session, user.getId());
    }
    public User getUserData(HttpSession session) {
        return new User((String)session.getAttribute("id"), (String)session.getAttribute("name"), (String)session.getAttribute("privilege"));
    }

    public String getName(HttpSession session) {
        return (String)session.getAttribute("name");
    }
    public void setName(HttpSession session, String name) {
        session.setAttribute("name", name);
    }
    public String getId(HttpSession session) {
        return (String)session.getAttribute("id");
    }
    public int getCode(HttpSession session) {
        return (int)session.getAttribute("code");
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
            case INVITE_CODES -> priv.contains("i");
            default -> false;
        };
    }

    public void invalidSession(HttpSession session) {
        LOGGER.debug("Invalidated session ID="+getId(session)+". Drop from table");
        sessionRepository.remove(getId(session));
        session.invalidate();
    }

    public HttpSession getSessonById(String id) {
        return sessionRepository.get(id);
    }
}
