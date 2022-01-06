package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.Resource;
import com.github.miho73.ipu.exceptions.InvalidInputException;
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
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private ProblemRepository problemRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    public JSONArray queryResource(String hash) throws SQLException {
        JSONArray ret = new JSONArray();

        Connection connection = resourceRepository.openConnection();
        Resource resource = resourceRepository.queryResource(hash, connection);
        resourceRepository.close(connection);
        if(resource == null) return ret;

        JSONObject res = new JSONObject();
        res.put("resource_code", resource.getResource_code());
        res.put("resource_name", resource.getResource_name());
        res.put("registered", resource.getRegistered());
        res.put("registered_by", resource.getRegistered_by());
        ret.put(res);
        return ret;
    }
    public JSONArray queryResourceByName(String name) throws SQLException {
        JSONArray ret = new JSONArray();

        Connection connection = resourceRepository.openConnection();
        Resource resource = resourceRepository.queryResourceByName(name, connection);
        resourceRepository.close(connection);
        if(resource == null) return ret;

        JSONObject res = new JSONObject();
        res.put("resource_code", resource.getResource_code());
        res.put("resource_name", resource.getResource_name());
        res.put("registered", resource.getRegistered());
        res.put("registered_by", resource.getRegistered_by());
        ret.put(res);
        return ret;
    }

    public JSONArray getAllResources() throws SQLException {
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
        return resTot;
    }

    public void changeName(String code, String name) throws SQLException, InvalidInputException {
        Connection connection = resourceRepository.openConnectionForEdit();
        if(!resourceRepository.isResourceExists(code, connection)) {
            resourceRepository.commitAndClose(connection);
            throw new InvalidInputException("resource not found");
        }
        resourceRepository.updateName(code, name, connection);
        resourceRepository.commitAndClose(connection);
    }

    public JSONArray searchProblemUsingResource(String code) throws SQLException {
        JSONArray ret = new JSONArray();

        Connection probConnection = problemRepository.openConnection();
        Connection resConnection = resourceRepository.openConnection();
        if(!resourceRepository.isResourceExists(code, resConnection)) return ret;
        Vector<Problem> searched = problemRepository.searchProblemUsingResource(code, probConnection);
        for (Problem problem : searched) {
            JSONObject probObj = new JSONObject();
            probObj.put("code", problem.getCode());
            probObj.put("name", problem.getName());
            ret.put(probObj);
        }
        return ret;
    }

    public void deleteResource(String code) throws SQLException {
        Connection connection = resourceRepository.openConnectionForEdit();
        resourceRepository.deleteResource(code, connection);
        resourceRepository.commitAndClose(connection);
    }
}