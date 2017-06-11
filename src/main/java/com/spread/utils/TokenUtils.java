package com.spread.utils;

import java.sql.Date;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenUtils {

	public static final String SESSION_ID = "session_id";

	// TODO: design fluent API
	// https://dzone.com/articles/java-fluent-api-design

	public static String createJWT(String secret, String sessionId) {

		Date now = new Date(System.currentTimeMillis());

		SecretKey signingKey = TokenUtils.base64StringToKey(TokenUtils.stringToBase64(secret));
		String jwt = Jwts.builder().setIssuedAt(now).claim(SESSION_ID, sessionId)
				.signWith(SignatureAlgorithm.HS512, signingKey).compact();
		return jwt;
	}

	public static Claims parseJWT(String jwt, String secret) {

		SecretKey signingKey = TokenUtils.base64StringToKey(TokenUtils.stringToBase64(secret));
		Claims claims = Jwts.parser().setSigningKey(signingKey).parseClaimsJws(jwt).getBody();

		return claims;
	}

	public static String getBearerToken(String header) {
		return header.replaceAll("(?i)Bearer", "");
	}

	public static String stringToBase64(String s) {
		return Base64.getEncoder().encodeToString(s.getBytes());
	}

	public static String keyToBase64String(SecretKey key) {
		String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
		return encodedKey;
	}

	public static SecretKey base64StringToKey(String encodedKey) {
		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HS512");
		return key;
	}

}
