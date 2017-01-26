package com.spread.controller.loggers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javaslang.API.*;

public class DefaultLogger implements ILogger {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void log(String message, Integer level) {
		Match(level).of(Case($(INFO), run(() -> logger.info(message))), //
				Case($(DEBUG), run(() -> logger.debug(message))), //
				Case($(ERROR), run(() -> logger.error(message))), //
				Case($(), run(() -> logger.info(message))));
	}

}
