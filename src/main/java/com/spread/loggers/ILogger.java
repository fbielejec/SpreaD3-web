package com.spread.loggers;

public interface ILogger {

        public static final int INFO = 1;
        public static final int DEBUG = 2;
        public static final int WARNING = 3;
        public static final int ERROR = 4;

        void log(String message, Integer level);

    // void log(Integer level, String message , String ... meta); //

}
