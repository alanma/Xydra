package org.xydra.googleanalytics;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

/*
 * Copyright 2008 Adobe Systems Inc., 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s):
 *   Zwetan Kjukov <zwetan@gmail.com>.
 *   Marc Alcaraz <ekameleon@gmail.com>.
 */
public class Utils {

	private static final Random random = new Random();

	public static int random31bitInteger() {
		return random.nextInt(2147483647) - 1;
	}

	public static int random32bitInteger() {
		return random.nextInt();
	}

	/**
	 * Generate hash for input string. This is a global method, since it does
	 * not need to access any instance variables, and it is being used
	 * everywhere in the GATC module.
	 * 
	 * @param input
	 *            Input string to generate hash value on.
	 * @return Hash value of input string. If input string is undefined, or
	 *         empty, return hash value of 1.
	 */
	public static int generateHash(String input) {
		int hash = 1; // hash buffer
		int leftMost7 = 0; // left-most 7 bits
		int pos; // character position in string
		int current; // current character in string

		// if input is undef or empty, hash value is 1
		if (input != null && input != "") {
			hash = 0;

			// hash function
			for (pos = input.length() - 1; pos >= 0; pos--) {
				current = input.charAt(pos);
				hash = ((hash << 6) & 0xfffffff) + current + (current << 14);
				leftMost7 = hash & 0xfe00000;
				// hash = (leftMost7 != 0) ? (hash ^ (leftMost7 >> 21)) :
				// hash;
				if (leftMost7 != 0) {
					hash ^= leftMost7 >> 21;
				}
			}
		}
		return hash;
	}

	/**
	 * For input 'he he+ho' this method should return 'he%20he+ho', but returns
	 * 'he+he%2Bho'
	 * 
	 * 
	 * @param raw
	 * @return
	 */
	public static String urlencode(String raw) {
		try {
			// encoded will use '+' for space, while JS in Firefox uses "%20"
			String encoded = URLEncoder.encode(raw, "UTF-8");
			String niceEncoded = encoded;
			niceEncoded = niceEncoded.replace("+", "%20");

			// niceEncoded = niceEncoded.replace("%2B", "+");
			niceEncoded = niceEncoded.replace("%28", "(");
			niceEncoded = niceEncoded.replace("%29", ")");
			return niceEncoded;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return hash of the domain name of the web site
	 */
	public static int getDomainhash(String domainName) {
		return Utils.generateHash(domainName);
	}

	public static long getCurrentTimeInSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	public static void main(String[] args) {
		System.out.println(urlencode("he he+ho"));
	}

}
