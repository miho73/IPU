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
    public boolean checkLogin(HttpSession session) {
        if(session == null) return true;
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

    public void invalidSession(HttpSession session) {
        session.invalidate();;
    }
}
