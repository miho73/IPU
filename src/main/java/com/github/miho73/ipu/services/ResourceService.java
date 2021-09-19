package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.repositories.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service("ResourceService")
public class ResourceService {
    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public void addResource(byte[] resource, String hash, String adder) throws SQLException {
        resourceRepository.addResource(resource, hash, adder);
    }
    public byte[] getResource(String hash) throws SQLException {
        return resourceRepository.getResource(hash);
    }
}