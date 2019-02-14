package com.spread.loggers;

import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.API.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultLogger implements ILogger {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void log(String message, Integer level) {
        Match(level).of(Case($(INFO), l -> run(() -> logger.info(message))), //
                        Case($(DEBUG), l -> run(() -> logger.debug(message))), //
                        Case($(WARNING), l -> run(() -> logger.warn(message))), //
                        Case($(ERROR), l -> run(() -> logger.error(message))), //
                        Case($(), l -> run(() -> logger.info(message))));
    }

}
