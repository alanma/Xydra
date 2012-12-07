package org.xydra.core.crypto;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Computes HMAC-SHA-256 message digest. This is used, e.g., in Facebook
 * authentication code.
 * 
 * Based on code with
 * "Copyright (c) 1999-2004 Brian Wellington (bwelling@xbill.org)" likely
 * bundled in http://sourceforge.net/projects/dnsjava/, BSD License
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class HMAC_SHA256 {
	
	private static final byte IPAD = 0x36;
	private static final byte OPAD = 0x5c;
	private static final byte PADLEN = 64;
	
	/**
	 * @param givenKey The secret key
	 * @param message The message to be signed
	 * @return a HMAC-SHA-256 message digest
	 */
	public static byte[] hmac_sha256(byte[] givenKey, byte[] message) {
		/* hash key if too long */
		byte[] key;
		if(givenKey.length > PADLEN) {
			key = hash(givenKey);
		} else {
			key = givenKey;
		}
		/* compute ipad, opad */
		byte[] ipad = new byte[PADLEN];
		byte[] opad = new byte[PADLEN];
		int i;
		for(i = 0; i < key.length; i++) {
			ipad[i] = (byte)(key[i] ^ IPAD);
			opad[i] = (byte)(key[i] ^ OPAD);
		}
		for(; i < PADLEN; i++) {
			ipad[i] = IPAD;
			opad[i] = OPAD;
		}
		
		/* HMAC(K,m) = H( opad + H(ipad + m) ) */
		byte[] ipad_message = concat(ipad, message);
		byte[] output = hash(ipad_message);
		byte[] opad_output = concat(opad, output);
		byte[] result = hash(opad_output);
		return result;
	}
	
	public static byte[] concat(byte[] a, byte[] b) {
		byte[] ab = new byte[a.length + b.length];
		System.arraycopy(a, 0, ab, 0, a.length);
		System.arraycopy(b, 0, ab, a.length, b.length);
		return ab;
	}
	
	private static byte[] hash(byte[] input) {
		/**
		 * FYI: javax (which does not run easily in GWT) does it like this:
		 * 
		 * <pre>
		 * HMac2 h = new HMac2(&quot;SHA-256&quot;, key);
		 * h.update(message);
		 * return h.sign();
		 * </pre>
		 */
		return SHA2.digest256(input);
	}
	
}
