package org.xydra.store.base;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Utility to compute an MD5 checksum.
 * 
 * @author voelkel
 */
public class HashUtils {
	
	/**
	 * @param input
	 * @return an MD5 hash of the input padded with "Xydra".
	 */
	public static String getXydraPasswordHash(String input) {
		return getMD5("Xydra" + input);
	}
	
	/**
	 * @param input
	 * @return the MD5 hash of the given input
	 */
	public static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while(hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
}
