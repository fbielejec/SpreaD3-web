package com.spread.loggers;

import java.util.HashMap;

public interface ILogger {

    public static final HashMap<Integer, String> levelToString = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1L;
            {
                put(INFO, "INFO");
                put(DEBUG, "DEBUG");
                put(WARN, "WARN");
                put(ERROR, "ERROR");
            }};

    public static final HashMap<String, Integer> stringToLevel = new HashMap<String, Integer>() {
            private static final long serialVersionUID = 1L;
            {
                put("INFO", INFO);
                put("DEBUG", DEBUG);
                put("WARN", WARN);
                put("ERROR", ERROR);
            }};

    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    void doLog(Integer level, String message, String[][] meta);

    void doLog(Integer level, String message);

    void doLog(Integer level, Exception e, String[][] meta);

    void doLog(Integer level, Exception e);

}
