package com.spread.services.sentry;

import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import io.sentry.Sentry;
import io.sentry.servlet.SentryServletContainerInitializer;

@Service
public class SentryLoggingService {

    private Boolean isInit = false;

    public void init(String dsn, HashMap<String, String> opts) {
        
        String encodedURL = opts.keySet().stream()
            .map(key -> key + "=" + opts.get(key))
            .collect(Collectors.joining("&", dsn + "?", ""));            

        Sentry.init(encodedURL);       
        isInit = true;        
    }

    /**
     * @return the isInit
     */
    public Boolean getIsInitialized() {
        return isInit;
    }

    @Bean
    public SentryServletContainerInitializer sentryServletContextInitializer() {
        return new SentryServletContainerInitializer();
    }
    
}
