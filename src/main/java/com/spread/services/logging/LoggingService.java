package com.spread.services.logging;

import java.util.HashMap;

import com.spread.loggers.AbstractLogger;
import com.spread.loggers.DefaultLogger;
import com.spread.loggers.SentryLogger;

import org.springframework.stereotype.Service;

@Service
public class LoggingService {

    private Boolean isInit = false;

    public AbstractLogger init(String activeProfile, String sentryLogLevel, String dsn, HashMap<String, String> opts) {

        DefaultLogger logger1 = new DefaultLogger();
        SentryLogger logger2 = new SentryLogger(activeProfile, sentryLogLevel, dsn, opts);

        logger1.setNextLogger(logger2);
        isInit = true;

        return logger1;
    }


    /**
     * @return the isInit
     */
    public Boolean isInitialized() {
        return isInit;
    }

}
