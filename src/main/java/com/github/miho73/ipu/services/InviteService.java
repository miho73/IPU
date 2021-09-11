package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.InviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

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
}
