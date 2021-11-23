package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service("ResourceService")
public class ResourceService {
    private final ResourceRepository resourceRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public void addResource(byte[] resource, String hash, String adder) throws SQLException {
        Connection connection = resourceRepository.openConnectionForEdit();
        try {
            resourceRepository.addResource(resource, hash, adder, connection);
            resourceRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            resourceRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot add resource", e);
            throw e;
        }
    }
    public byte[] getResource(String hash) throws SQLException {
        Connection connection = resourceRepository.openConnection();
        byte[] dat = resourceRepository.getResource(hash, connection);
        resourceRepository.close(connection);
        return dat;
    }
}