package com.spread.loggers;

import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.API.run;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultLogger extends AbstractLogger {

    private final Logger logger = LogManager.getLogger("AppLogger");

    @Override
    public void doLog(Integer level, String message, String[][] meta) {

        Map<String, String> map = merge(
                                        new String[][] {
                                            {"timestamp", Instant.now().toString()},
                                            {"level" , levelToString.get(level) },
                                            { "message", message}
                                        },
                                        meta);

        String json = new GsonBuilder().create().toJson(map);

        Match(level).of(Case($(INFO), l -> run(() -> logger.info(json))),
                        Case($(DEBUG), l -> run(() -> logger.debug(json))),
                        Case($(WARN), l -> run(() -> logger.warn(json))),
                        Case($(ERROR), l -> run(() -> logger.error(json))),
                        Case($(), l -> run(() -> logger.info(json))));

    }

    @Override
    public void doLog(Integer level, String message) {
        doLog(level, message, null);
    }

    @Override
    public void doLog(Integer level, Exception e, String[][] meta) {

        Map<String, String> map = merge(
                                        new String[][] {
                                            {"timestamp", Instant.now().toString()},
                                            {"level" , levelToString.get(level)},
                                            { "message", Optional.ofNullable(e.getMessage()).orElse("null")}
                                        },
                                        meta);

        String json = new GsonBuilder().create().toJson(map);

        logger.error(json);
    }

    @Override
    public void doLog(Integer level, Exception e) {
        doLog(level, e, null);
    }


    /**
     * Merges two maps hierarchically eft-to-right
     * If a key occurs in more than one map, the mapping from
     * the second map will be the mapping in the result
     *
     * @param  map1
     * @param  map2
     * @return joint map
     */
    private Map<String, String> merge (String[][] map1, String[][] map2) {

        Map<String, String> m1 = Stream.of(map1).collect(Collectors.toMap(k -> k[0], v -> v[1]));
        if (map2 == null) {
            return m1;
        }

        Map<String, String> m2 = Stream.of(map2).collect(Collectors.toMap(k -> k[0], v -> v[1]));
        m1.forEach((key, value) -> m2.merge(key, value, (v1, v2) -> v1));
        return m2;
    }

}
