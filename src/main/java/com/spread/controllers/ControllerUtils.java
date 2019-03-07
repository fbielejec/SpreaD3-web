package com.spread.controllers;

import java.util.Collections;

import com.google.gson.GsonBuilder;
import com.spread.exceptions.SpreadException;
import com.spread.utils.TokenUtils;

public class ControllerUtils {

    public static String jsonResponse (String message) {
        return new GsonBuilder().create().toJson(Collections.singletonMap("response", message));
    }

    public static String getSessionId(String authorizationHeader, String secret) throws SpreadException {
        String sessionId = null;
        try {
            sessionId = TokenUtils.parseJWT(TokenUtils.getBearerToken(authorizationHeader), secret).get(TokenUtils.SESSION_ID)
                .toString();
        } catch (Exception e) {
            throw new SpreadException(SpreadException.Type.AUTHORIZATION_EXCEPTION,
                                      "Exception when parsing JWT token", new String[][] {
                                          {"authorizationHeader", authorizationHeader},
                                          {"method", new Throwable()
                                           .getStackTrace()[0]
                                           .getMethodName()},
                                      });

        }

        return sessionId;
    }

}
