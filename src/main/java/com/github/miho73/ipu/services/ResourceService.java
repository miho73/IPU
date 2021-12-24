package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.Resource;
import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.repositories.ResourceRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

@Service("ResourceService")
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final ProblemRepository problemRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public ResourceService(ResourceRepository resourceRepository, ProblemRepository problemRepository) {
        this.resourceRepository = resourceRepository;
        this.problemRepository = problemRepository;
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

    public boolean isResourceExists(String hash) throws SQLException {
        Connection connection = resourceRepository.openConnection();
        boolean is = resourceRepository.isResourceExists(hash, connection);
        resourceRepository.close(connection);
        return is;
    }

    public String queryResource(String hash) throws SQLException {
        Connection connection = resourceRepository.openConnection();
        Resource resource = resourceRepository.queryResource(hash, connection);
        resourceRepository.close(connection);

        JSONObject res = new JSONObject();
        res.put("resource_code", resource.getResource_code());
        res.put("resource_name", resource.getResource_name());
        res.put("registered", resource.getRegistered());
        res.put("registered_by", resource.getRegistered_by());
        return "["+ res +"]";
    }
    public String queryResourceByName(String name) throws SQLException {
        Connection connection = resourceRepository.openConnection();
        Resource resource = resourceRepository.queryResourceByName(name, connection);
        resourceRepository.close(connection);
        if(resource == null) return "";

        JSONObject res = new JSONObject();
        res.put("resource_code", resource.getResource_code());
        res.put("resource_name", resource.getResource_name());
        res.put("registered", resource.getRegistered());
        res.put("registered_by", resource.getRegistered_by());
        return "["+ res +"]";
    }

    public String getAllResources() throws SQLException {
        Connection connection = resourceRepository.openConnection();
        Vector<Resource> resources = resourceRepository.getAllResources(connection);
        resourceRepository.close(connection);

        JSONArray resTot = new JSONArray();
        for (Resource resource : resources) {
            JSONObject res = new JSONObject();
            res.put("resource_code", resource.getResource_code());
            res.put("resource_name", resource.getResource_name());
            res.put("registered", resource.getRegistered());
            res.put("registered_by", resource.getRegistered_by());
            resTot.put(res);
        }
        return resTot.toString();
    }

    public boolean changeName(String code, String name) throws SQLException {
        Connection connection = resourceRepository.openConnectionForEdit();
        if(!resourceRepository.isResourceExists(code, connection)) {
            resourceRepository.commitAndClose(connection);
            return false;
        }
        resourceRepository.updateName(code, name, connection);
        resourceRepository.commitAndClose(connection);
        return true;
    }

    public String searchProblemUsingResource(String code) throws SQLException {
        Connection probConnection = problemRepository.openConnection();
        Connection resConnection = resourceRepository.openConnection();
        if(!resourceRepository.isResourceExists(code, resConnection)) {
            return "nf";
        }
        Vector<Problem> searched = problemRepository.searchProblemUsingResource(code, probConnection);
        JSONArray ret = new JSONArray();
        for (Problem problem : searched) {
            JSONObject probObj = new JSONObject();
            probObj.put("code", problem.getCode());
            probObj.put("name", problem.getName());
            ret.put(probObj);
        }
        return ret.toString();
    }

    public void deleteResource(String code) throws SQLException {
        Connection connection = resourceRepository.openConnectionForEdit();
        resourceRepository.deleteResource(code, connection);
        resourceRepository.commitAndClose(connection);
    }
}