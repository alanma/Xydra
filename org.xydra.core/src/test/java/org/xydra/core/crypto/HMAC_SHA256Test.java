package org.xydra.core.crypto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.core.serialize.Base64;
import org.xydra.sharedutils.XyAssert;


public class HMAC_SHA256Test {

	@Test
	public void testEncodingAndHashing() {
		/* given in signed request */
		final String fbHmacBase64UrlEncStr = "vlXgu64BQGFSQrY0ZcJBZASMvYvTHu9GQ0YM9rjPSso";
		/* given in signed request */
		final String fbBase64UrlEncStr = "eyJhbGdvcml0aG0iOiJITUFDLVNIQTI1NiIsIjAiOiJwYXlsb2FkIn0";

		/* expected decoding of fbBase64urlStr */
		final String fbDecMsgStr = "{\"algorithm\":\"HMAC-SHA256\",\"0\":\"payload\"}";
		XyAssert.xyAssert(Base64.utf8(Base64.urlDecode(fbBase64UrlEncStr)).equals(fbDecMsgStr));
		XyAssert.xyAssert(Base64.urlEncode(Base64.utf8(fbDecMsgStr)).equals(fbBase64UrlEncStr));

		/* known to developer */
		final String fpKeyStr = "secret";

		/* calculate expected HMAC */
		final byte[] fbKey = Base64.utf8(fpKeyStr);
		final byte[] fbBase64url = Base64.utf8(fbBase64UrlEncStr);
		final byte[] expectedHmac = HMAC_SHA256.hmac_sha256(fbKey, fbBase64url);
		final String expectedHmacStr = Base64.utf8(expectedHmac);
		final String expectedHmacUrlEncStr = Base64.urlEncode(expectedHmac);
		assertEquals(fbHmacBase64UrlEncStr, expectedHmacUrlEncStr);
		// check in another way
		final String fbHmacStr = Base64.utf8(Base64.urlDecode(fbHmacBase64UrlEncStr));
		assertEquals(expectedHmacStr, fbHmacStr);
	}

}
