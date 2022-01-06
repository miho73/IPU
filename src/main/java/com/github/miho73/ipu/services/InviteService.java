package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.InviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service("InviteService")
public class InviteService {
    @Autowired private InviteRepository inviteRepository;

    public boolean checkExists(String code) throws SQLException {
        Connection connection = inviteRepository.openConnection();
        boolean is = inviteRepository.checkExist(code, connection);
        inviteRepository.close(connection);
        return is;
    }

    public List<String> listCodes() throws SQLException {
        Connection connection = inviteRepository.openConnection();
        List<String> lst = inviteRepository.getList(connection);
        inviteRepository.close(connection);
        return lst;
    }

    public enum DB_ACTION {
        INSERT,
        DELETE,
        UPDATE
    }
    public void IDU(String[] params, DB_ACTION action) throws SQLException {
        Connection connection = inviteRepository.openConnection();
        connection.setAutoCommit(false);
        switch (action) {
            case INSERT -> inviteRepository.insertCode(params[1], connection);
            case DELETE -> inviteRepository.deleteCode(params[1], connection);
            case UPDATE -> inviteRepository.updateCode(params[1], params[2], connection);
        }
        inviteRepository.commitAndClose(connection);
    }

    public boolean inviteCodeValidator(String code) {
        return code.length() < 4 || code.length() > 6;
    }
}
