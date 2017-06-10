package com.spread.controller;

import static org.junit.Assert.assertEquals;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.Test;

import com.spread.utils.TokenUtils;

public class TokenUtilsTests {

	@Test
	public void testEncodingAndDecoding() {

		String secret = "quebaHombre";
		String base64EncodedSecret = TokenUtils.stringToBase64(secret);

		SecretKey key = TokenUtils.base64StringToKey(base64EncodedSecret);
		String actualBase64EncodedSecret = TokenUtils.keyToBase64String(key);

		assertEquals(base64EncodedSecret, actualBase64EncodedSecret);

		String actualSecret = new String(Base64.getDecoder().decode(actualBase64EncodedSecret));
		assertEquals(secret, actualSecret);
		
	}

	@Test
	public void testHeaderParsing() {

		
		
//		String secret = "quebaHombre";
//		String expectedSessionId = UUID.randomUUID().toString();
//		
//		String jwt = TokenUtils.createJWT(secret, expectedSessionId);
//		
//		String sessionId = TokenUtils.parseJWT(jwt, secret).get(TokenUtils.SESSION_ID).toString();
//		
//		assertEquals(expectedSessionId, sessionId);
	}
	
	@Test
	public void testJWTParsing() {

		String secret = "quebaHombre";
		String expectedSessionId = UUID.randomUUID().toString();
		
		String jwt = TokenUtils.createJWT(secret, expectedSessionId);
		
		String sessionId = TokenUtils.parseJWT(jwt, secret).get(TokenUtils.SESSION_ID).toString();
		
		assertEquals(expectedSessionId, sessionId);
	}
	
}
