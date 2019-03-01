package com.spread.controllers;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.spread.loggers.AbstractLogger;
import com.spread.loggers.ILogger;
import com.spread.repositories.KeyRepository;
import com.spread.utils.TokenUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin
public class TokenController {

    private AbstractLogger logger;

    @Autowired
    private KeyRepository keyRepository;

    public TokenController() {
    }

    public void init(AbstractLogger logger) {
        this.logger = logger;
    }

    @RequestMapping(path = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> respondWithToken(HttpServletRequest request) {

        try {

            String secret = keyRepository.findFirstByOrderByIdDesc().getKey();

            String uuid = UUID.randomUUID().toString();
            String jwt = TokenUtils.createJWT(secret, uuid);
            JSONObject body = new JSONObject().put("token", jwt);

            logger.log(ILogger.INFO, "Sending token", new String[][] {
                    {"token", jwt},
                    {"request-ip" , request.getRemoteAddr()}
                });

            return ResponseEntity.status(HttpStatus.OK).body(body.toString());
        } catch (JSONException e) {
            logger.log(ILogger.ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

}
