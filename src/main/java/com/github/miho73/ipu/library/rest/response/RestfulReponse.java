package com.github.miho73.ipu.library.rest.response;

import org.json.JSONObject;

public class RestfulReponse {
    public enum HTTP_CODE {
        OK,
        BAD_REQUEST,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        INTERNAL_SERVER_ERROR,
        NOT_IMPLEMENTED,
        SERVICE_UNAVAILABLE,
    }

    public static String createRestfulResponse(HTTP_CODE status) {
        JSONObject response = new JSONObject();
        int code = switch (status){
            case OK -> 200;
            case BAD_REQUEST -> 400;
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND -> 404;
            case INTERNAL_SERVER_ERROR -> 500;
            case NOT_IMPLEMENTED -> 501;
            case SERVICE_UNAVAILABLE -> 503;
        };
        response.put("code", code);
        response.put("message", status.name());
        return response.toString();
    }

    public static String createRestfulResponse(HTTP_CODE status, JSONObject result) {
        JSONObject response = new JSONObject();
        int code = switch (status){
            case OK -> 200;
            case BAD_REQUEST -> 400;
            case UNAUTHORIZED -> 401;
            case FORBIDDEN -> 403;
            case NOT_FOUND -> 404;
            case INTERNAL_SERVER_ERROR -> 500;
            case NOT_IMPLEMENTED -> 501;
            case SERVICE_UNAVAILABLE -> 503;
        };
        response.put("code", code);
        response.put("message", status.name());
        response.put("result", result);
        return response.toString();
    }
}
