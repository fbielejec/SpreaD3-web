package com.spread.controllers;

import java.util.Collections;

import com.google.gson.GsonBuilder;

public class ControllerUtils {

    public static String jsonResponse (String message) {
             return new GsonBuilder().create().toJson(Collections.singletonMap("response", message));
    }

}
