package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.InviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service("InviteService")
public class InviteService {
    private final InviteRepository inviteRepository;

    @Autowired
    public InviteService(InviteRepository inviteRepository) {
        this.inviteRepository = inviteRepository;
    }

    public boolean checkExists(String code) throws SQLException {
        return inviteRepository.checkExist(code);
    }

    public List<String> listCodes() throws SQLException {
        return inviteRepository.getList();
    }

    public enum DB_ACTION {
        INSERT,
        DELETE,
        UPDATE
    }
    public void IDU(String[] params, DB_ACTION action) throws SQLException {
        switch (action) {
            case INSERT -> inviteRepository.insertCode(params[1]);
            case DELETE -> inviteRepository.deleteCode(params[1]);
            case UPDATE -> inviteRepository.updateCode(params[1], params[2]);
        }
    }

    public boolean inviteCodeValidator(String code) {
        return code.length() >= 4 && code.length() <= 6;
    }
}
