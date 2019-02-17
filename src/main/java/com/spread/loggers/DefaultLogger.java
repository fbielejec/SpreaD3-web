package com.spread.loggers;

import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.API.run;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.GsonBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultLogger extends AbstractLogger {

    private final Logger logger = LogManager.getLogger("AppLogger");

    @Override
    public void doLog(Integer level, String message, String[][] meta) {

        Map<String, String> defaultMap = Stream.of(new String[][] {
                {"timestamp", Instant.now().toString()},
                {"level" , levelToString.get(level) },
                { "message", message},
            }).collect(Collectors.toMap(k -> k[0], v -> v[1]));

        Map<String, String> metaMap = Stream.of(meta).collect(Collectors.toMap(k -> k[0], v -> v[1]));

        defaultMap.forEach((key, value) -> metaMap.merge(key, value, (v1, v2) -> v2));

        String json = new GsonBuilder().create().toJson(metaMap);

        Match(level).of(Case($(INFO), l -> run(() -> logger.info(json))), //
                        Case($(DEBUG), l -> run(() -> logger.debug(json))), //
                        Case($(WARN), l -> run(() -> logger.warn(json))), //
                        Case($(ERROR), l -> run(() -> logger.error(json))), //
                        Case($(), l -> run(() -> logger.info(json))));

    }

}
