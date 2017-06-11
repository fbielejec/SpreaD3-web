package com.spread.controllers;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.spread.loggers.ILogger;
import com.spread.loggers.LoggerFactory;
import com.spread.repositories.KeyRepository;
import com.spread.utils.TokenUtils;

@Controller
@CrossOrigin
public class TokenController {

	@Autowired
	private KeyRepository keyRepository;
	
	private final ILogger logger;

	public TokenController() {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
	}

	@RequestMapping(path = "/token", method = RequestMethod.GET)
	public ResponseEntity<Object> respondWithToken() {

		try {

			String secret = keyRepository.findFirstByOrderByIdDesc().getKey();
			
			String uuid = UUID.randomUUID().toString();
			String jwt = TokenUtils.createJWT(secret, uuid);
			JSONObject body = new JSONObject().put("token", jwt);

			return ResponseEntity.status(HttpStatus.OK).body(body.toString());
		} catch (JSONException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

	}

}
