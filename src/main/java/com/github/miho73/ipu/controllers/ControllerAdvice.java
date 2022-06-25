package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.library.rest.response.RestfulReponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestControllerAdvice
public class ControllerAdvice
{
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(value = {
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public String handleMethodArgumentTypeMismatchException(Exception ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOGGER.error("Controller advice caught exception: ", ex);
        if(request.getMethod().equals("GET")) {
            response.sendError(400);
            return null;
        }
        else {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(value = {Exception.class})
    public String handleGeneralException(Exception ex, HttpServletResponse response) {
        LOGGER.error("exception. ", ex);
        response.setStatus(500);
        return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}