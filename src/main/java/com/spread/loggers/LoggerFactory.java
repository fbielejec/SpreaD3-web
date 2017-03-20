package com.spread.loggers;

import static javaslang.API.*;

public class LoggerFactory extends AbstractLoggerFactory {

	public static final Integer DEFAULT = 0;

	@Override
	public ILogger getLogger(int loggerType) {
		ILogger logger = Match(loggerType).of(Case($(DEFAULT), new DefaultLogger()), //
				Case($(), new DefaultLogger()));
		return logger;
	}

}
