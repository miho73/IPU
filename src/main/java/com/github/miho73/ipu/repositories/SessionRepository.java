package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.services.SessionService;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpSession;
import java.util.Dictionary;
import java.util.Hashtable;

@Repository("SessionRepository")
public class SessionRepository {
    private final Hashtable<String, HttpSession> sessionTable;

    public SessionRepository() {
        sessionTable = new Hashtable<>();
    }

    public void add(HttpSession session, String id) {
        if(sessionTable.containsKey(id)) sessionTable.replace(id, session);
        else sessionTable.put(id, session);
    }
    public HttpSession get(String id) {
        return sessionTable.get(id);
    }
    public void remove(String id) {
        sessionTable.remove(id);
    }
    public boolean exists(String id) {
        return sessionTable.containsKey(id);
    }

}
