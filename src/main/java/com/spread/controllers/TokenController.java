package com.spread.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin
public class TokenController {

	// TODO: generate JWT token with unique session id
	@RequestMapping(path = "/token", method = RequestMethod.GET)
	public ResponseEntity<Object> respondWithToken() {
		String uuid = UUID.randomUUID().toString();
		return ResponseEntity.status(HttpStatus.OK).body(uuid);
	}
	
}
