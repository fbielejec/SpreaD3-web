package com.spread.controllers;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.spread.loggers.ILogger;
import com.spread.loggers.LoggerFactory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Controller
@CrossOrigin
public class TokenController {

	private static final String SESSION_ID = "session_id";
	private final ILogger logger;

	public TokenController() {
		this.logger = new LoggerFactory().getLogger(LoggerFactory.DEFAULT);
	}

	public static String keyToBase64String(SecretKey key) {
		String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
		return encodedKey;
	}

	public static SecretKey base64StringToKey(String encodedKey) {
		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		return key;
	}

	@RequestMapping(path = "/token", method = RequestMethod.GET)
	public ResponseEntity<Object> respondWithToken() {

		try {

			SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();

			String uuid = UUID.randomUUID().toString();

			String jwt = Jwts.builder().claim(SESSION_ID, uuid).signWith(SignatureAlgorithm.HS512, secretKey).compact();

			JSONObject body = new JSONObject().put("token", jwt);

			return ResponseEntity.status(HttpStatus.OK).body(body);
		} catch (JSONException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.log(e.getMessage(), ILogger.ERROR);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}

	}

}
