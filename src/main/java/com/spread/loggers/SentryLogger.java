package com.spread.loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.Event.Level;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;

public class SentryLogger extends AbstractLogger {

    private String sentryLogLevel;

    private final String loggerName;

    private static final HashMap<Integer, Level> levelToSentryLevel = new HashMap<Integer, Level>() {
            private static final long serialVersionUID = 1L;
            {
                put(INFO, Event.Level.INFO);
                put(DEBUG, Event.Level.DEBUG);
                put(WARN, Event.Level.WARNING);
                put(ERROR, Event.Level.ERROR);
            }};

    public SentryLogger(String activeProfile, String sentryLogLevel, String dsn, HashMap<String, String> opts) {

        this.sentryLogLevel = sentryLogLevel;
        this.loggerName = this.getClass().getSimpleName();

        String encodedURL = opts.keySet().stream()
            .map(key -> key + "=" + opts.get(key))
            .collect(Collectors.joining("&", dsn + "?", ""));

        if (activeProfile.equalsIgnoreCase("production"))
            Sentry.init(encodedURL);
    }

    // TODO: if !meta

    @Override
    public void doLog(Integer level, String message, String[][] meta) {

        Integer minLevel = stringToLevel.getOrDefault(sentryLogLevel, ERROR);
        if(level >= minLevel) {

            EventBuilder eventBuilder = new EventBuilder();

            if (!(meta == null)) {
                Map<String, String> metaMap = Stream.of(meta).collect(Collectors.toMap(k -> k[0], v -> v[1]));
                metaMap.forEach((key, value) -> eventBuilder.withExtra(key, value));
            }
            eventBuilder
                .withMessage(message)
                .withLevel(levelToSentryLevel.get(level))
                .withLogger(loggerName);

            Sentry.capture(eventBuilder);
        }
    }

    @Override
    public void doLog(Integer level, String message) {
        doLog(level,  message, null);
    }

    @Override
    public void doLog(Integer level, Exception e, String[][] meta) {

        EventBuilder eventBuilder = new EventBuilder()
            // .withMessage(e.getMessage())
            .withLevel(levelToSentryLevel.get(level))
            .withLogger(loggerName)
            .withSentryInterface(new ExceptionInterface(e));

        if (!(meta == null)) {
            Map<String, String> metaMap = Stream.of(meta).collect(Collectors.toMap(k -> k[0], v -> v[1]));
            metaMap.forEach((key, value) -> eventBuilder.withExtra(key, value));
        }

        Sentry.capture(eventBuilder);
    }

    @Override
    public void doLog(Integer level, Exception e) {
        doLog(level, e, null);
    }


}
