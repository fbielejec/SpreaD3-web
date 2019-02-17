package com.spread.loggers;

public abstract class AbstractLogger implements ILogger {

    protected AbstractLogger nextLogger;

    public void setNextLogger(AbstractLogger nextLogger) {
        this.nextLogger = nextLogger;
    }

    public void log(Integer level, String message, String[][] meta) {
        this.doLog(level, message, meta);
        if (nextLogger != null) {
            nextLogger.log(level, message, meta);
        }
    }

}
