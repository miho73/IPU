package com.github.miho73.ipu.library.rest.response;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class RestfulReponse {
    public static String createRestfulResponse(HttpStatus status) {
        JSONObject response = new JSONObject();
        response.put("code", status.value());
        response.put("message", status.getReasonPhrase());
        return response.toString();
    }

    public static String createRestfulResponse(HttpStatus status, Object result) {
        JSONObject response = new JSONObject();
        response.put("code", status.value());
        response.put("message", status.getReasonPhrase());
        response.put("result", result);
        return response.toString();
    }
}
