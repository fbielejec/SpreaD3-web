package com.spread.loggers;

public abstract class AbstractLogger implements ILogger {

    protected AbstractLogger nextLogger;

    public void setNextLogger(AbstractLogger nextLogger) {
        this.nextLogger = nextLogger;
    }

    public void log(Integer level, String message) {
        this.doLog(level, message);
        if (nextLogger != null) {
            nextLogger.log(level, message);
        }
    }

    public void log(Integer level, String message, String[][] meta) {
        this.doLog(level, message, meta);
        if (nextLogger != null) {
            nextLogger.log(level, message, meta);
        }
    }

    public void log(Integer level, Exception e, String[][] meta) {
        this.doLog(level, e, meta);
        if (nextLogger != null) {
            nextLogger.log(level, e, meta);
        }
    }

    public void log(Integer level, Exception e) {
        this.doLog(level, e);
        if (nextLogger != null) {
            nextLogger.log(level, e);
        }
    }

}
