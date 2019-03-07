package com.spread.loggers;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Instant;

public class DefaultLogger extends AbstractLogger {

    private final Logger logger = LogManager.getLogger("AppLogger");

    @Override
    public void doLog(Integer level, String message, String[][] ... meta) {

        Map<String, String> map = toMap(new String[][] {
                {"timestamp", Instant.now().toString()},
                {"level" , levelToString.get(level) },
                {"message", message},
                {"thread" , Thread.currentThread().getName()},
            });

        if (!(meta == null)) {
            map = merge (map, merge (meta));
        }

        String json = new GsonBuilder().create().toJson(map);
        logWithLevel(level, json);
    }

    @Override
    public void doLog(Integer level, String message) {
        doLog(level, message, null);
    }

    @Override
    public void doLog(Integer level, Exception e, String[][] ... meta) {

        Map<String, String> map = toMap(new String[][] {
                {"timestamp", Instant.now().toString()},
                {"level" , levelToString.get(level) },
                {"message", Optional.ofNullable(e.getMessage()).orElse("null")},
                {"thread" , Thread.currentThread().getName()},
            });


        if (!(meta == null)) {
            map = merge (map, merge (meta));
        }

        String json = new GsonBuilder().create().toJson(map);
        logWithLevel(level, json);
    }

    @Override
    public void doLog(Integer level, Exception e) {
        doLog(level, e, null);
    }

    private void logWithLevel (Integer level, String json) {
        Match(level).of(Case($(INFO), l -> run(() -> logger.info(json))),
                        Case($(DEBUG), l -> run(() -> logger.debug(json))),
                        Case($(WARN), l -> run(() -> logger.warn(json))),
                        Case($(ERROR), l -> run(() -> logger.error(json))),
                        Case($(), l -> run(() -> logger.info(json))));
    }

    private Map<String, String> toMap (String[][] m) {
        return Stream.of(m).collect(Collectors.toMap(k -> k[0], v -> v[1]));
    }

    private Map<String, String> merge (Map<String, String> m1, Map<String, String> m2) {
        if (m2 == null) {
            return m1;
        }
        m1.forEach((key, value) -> m2.merge(key, value, (v1, v2) -> v1));
        return m2;
    }

}
