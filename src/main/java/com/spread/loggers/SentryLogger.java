package com.spread.loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.Event.Level;
import io.sentry.event.EventBuilder;

public class SentryLogger extends AbstractLogger {

    private String sentryLogLevel;

    private static final HashMap<Integer, Level> levelToSentryLevel = new HashMap<Integer, Level>() {
            private static final long serialVersionUID = 1L;
            {
                put(INFO, Event.Level.INFO);
                put(DEBUG, Event.Level.DEBUG);
                put(WARN, Event.Level.WARNING);
                put(ERROR, Event.Level.ERROR);
            }};

    public SentryLogger(String activeProfile, String sentryLogLevel, String dsn, HashMap<String, String> opts) {

        String encodedURL = opts.keySet().stream()
            .map(key -> key + "=" + opts.get(key))
            .collect(Collectors.joining("&", dsn + "?", ""));

        this.sentryLogLevel = sentryLogLevel;

        if (activeProfile.equalsIgnoreCase("production"))
            Sentry.init(encodedURL);
    }

    // TODO : doLog exception : https://docs.sentry.io/clients/java/usage/#building-more-complex-events

    @Override
    public void doLog(Integer level, String message, String[][] meta) {
        if(level >= stringToLevel.get(sentryLogLevel)) {

            System.out.println(levelToString.get(level) +"[" + "MIN LEVEL=" + stringToLevel.get(sentryLogLevel)+ "]" + " --- Sending To Sentry: " + message);

            EventBuilder eventBuilder = new EventBuilder();

            Map<String, String> metaMap = Stream.of(meta).collect(Collectors.toMap(k -> k[0], v -> v[1]));

            metaMap.forEach((key, value) -> eventBuilder.withExtra(key, value));

            eventBuilder
                .withMessage(message)
                .withLevel(levelToSentryLevel.get(level))
                .withLogger(this.getClass().getSimpleName());

            Sentry.capture(eventBuilder);
        }
    }

}
