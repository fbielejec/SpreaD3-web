package com.spread.loggers;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggingUtils {

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
