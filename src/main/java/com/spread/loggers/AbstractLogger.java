package com.spread.loggers;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Merges multiple maps hierarchically left-to-right
     * If a key occurs in more than one map, the mapping from
     * the second map will be the mapping in the result
     *
     * @param  maps
     * @return merged map
     */
    public Map<String, String> merge (String[][] ... maps) {
        return Arrays.stream(maps)
            .flatMap(Arrays::stream)
            .collect(Collectors.toMap(k -> k[0], v -> Optional.ofNullable(v[1]).orElse("null"), (v1, v2) -> v2));
    }

}
