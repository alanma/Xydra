package org.xydra.core.crypto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.core.serialize.Base64;


public class HMAC_SHA256Test {
	
	@Test
	public void testEncodingAndHashing() {
		/* given in signed request */
		String fbHmacBase64UrlEncStr = "vlXgu64BQGFSQrY0ZcJBZASMvYvTHu9GQ0YM9rjPSso";
		/* given in signed request */
		String fbBase64UrlEncStr = "eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsIjAiOiJwYXlsb2FkIn0";
		
		/* expected decoding of fbBase64urlStr */
		String fbDecMsgStr = "{\"algorithm\":\"HMAC-SHA256\",\"0\":\"payload\"}";
		assert Base64.utf8(Base64.urlDecode(fbBase64UrlEncStr)).equals(fbDecMsgStr);
		assert Base64.urlEncode(Base64.utf8(fbDecMsgStr)).equals(fbBase64UrlEncStr);
		
		/* known to developer */
		String fpKeyStr = "secret";
		
		/* calculate expected HMAC */
		byte[] fbKey = Base64.utf8(fpKeyStr);
		byte[] fbBase64url = Base64.utf8(fbBase64UrlEncStr);
		byte[] expectedHmac = HMAC_SHA256.hmac_sha256(fbKey, fbBase64url);
		String expectedHmacStr = Base64.utf8(expectedHmac);
		String expectedHmacUrlEncStr = Base64.urlEncode(expectedHmac);
		assertEquals(fbHmacBase64UrlEncStr, expectedHmacUrlEncStr);
		// check in another way
		String fbHmacStr = Base64.utf8(Base64.urlDecode(fbHmacBase64UrlEncStr));
		assertEquals(expectedHmacStr, fbHmacStr);
	}
	
}
