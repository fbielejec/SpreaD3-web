package com.spread.controller;

import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.Test;

import com.spread.controllers.TokenController;
import static org.junit.Assert.assertEquals;

public class TokenControllerTests {

	@Test
	public void testKeyEncodingandDecoding() {

		String secret = "quebahombre";
		String base64EncodedSecret = Base64.getEncoder().encodeToString(secret.getBytes());

		SecretKey key = TokenController.base64StringToKey(base64EncodedSecret);
		String actualBase64EncodedSecret = TokenController.keyToBase64String(key);

		assertEquals(base64EncodedSecret, actualBase64EncodedSecret);

		String actualSecret = new String(Base64.getDecoder().decode(actualBase64EncodedSecret));
		assertEquals(secret, actualSecret);
	}

}
