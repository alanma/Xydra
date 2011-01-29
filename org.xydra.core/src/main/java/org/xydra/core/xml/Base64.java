package org.xydra.core.xml;

/***
 * Methods to encode and decode binary data using the Base64 encoding scheme.
 * 
 * Based on http://iharder.sourceforge.net/current/java/base64/ (Public Domain),
 * cleaned up so it will run in GWT.
 * 
 * @author dscharrer
 * 
 */
public class Base64 {
	
	/** Maximum line length (76) of Base64 output. */
	private final static int MAX_LINE_LENGTH = 76;
	
	/** The equals sign (=) as a byte. */
	private final static char EQUALS_SIGN = '=';
	
	/** The new line character (\n) as a byte. */
	private final static char NEW_LINE = '\n';
	
	// Indicates white space in encoding
	private final static byte WHITE_SPACE_ENC = -5;
	
	// Indicates equals sign in encoding
	private final static byte EQUALS_SIGN_ENC = -1;
	
	/** The 64 valid Base64 values. */
	private final static char[] ALPHABET = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
	        'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
	        'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
	        't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	        '+', '/' };
	
	/**
	 * Translates a Base64 value to either its 6-bit reconstruction value or a
	 * negative number indicating some other meaning.
	 **/
	private final static byte[] DECODABET = {
	        // illegal characters
	        -9, -9, -9, -9, -9, -9, -9,
	        -9,
	        -9,
	        // Whitespace: Tab and Linefeed
	        WHITE_SPACE_ENC,
	        WHITE_SPACE_ENC,
	        // illegal characters
	        -9,
	        -9,
	        // Whitespace: Carriage Return
	        WHITE_SPACE_ENC,
	        // illegal characters
	        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
	        -9,
	        -9,
	        // Whitespace: Space
	        WHITE_SPACE_ENC,
	        // illegal characters
	        -9, -9, -9, -9, -9, -9, -9, -9,
	        -9,
	        -9,
	        // Plus sign at decimal 43
	        62,
	        // illegal characters
	        -9,
	        -9,
	        -9,
	        // Slash at decimal 47
	        63,
	        // Numbers 0 through 9
	        52, 53, 54, 55, 56, 57, 58, 59, 60,
	        61,
	        // illegal characters
	        -9,
	        -9,
	        -9,
	        // Equals sign at decimal 61
	        EQUALS_SIGN_ENC,
	        // illegal characters
	        -9, -9,
	        -9,
	        // Letters 'A' through 'Z'
	        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
	        24, 25,
	        // illegal characters
	        -9, -9, -9, -9, -9, -9,
	        // Letters 'a' through 'z'
	        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
	        48, 49, 50, 51 };
	
	private static byte decodeChar(char c) {
		if(c >= DECODABET.length) {
			return -9;
		}
		return DECODABET[c];
	}
	
	/** Defeats instantiation. */
	private Base64() {
	}
	
	/**
	 * <p>
	 * Encodes up to three bytes of the array <var>source</var> and writes the
	 * resulting four Base64 bytes to <var>destination</var>. The source and
	 * destination arrays can be manipulated anywhere along their length by
	 * specifying <var>srcOffset</var> and <var>destOffset</var>. This method
	 * does not check to make sure your arrays are large enough to accomodate
	 * <var>srcOffset</var> + 3 for the <var>source</var> array or
	 * <var>destOffset</var> + 4 for the <var>destination</var> array. The
	 * actual number of significant bytes in your array is given by
	 * <var>numSigBytes</var>.
	 * </p>
	 * <p>
	 * This is the lowest level of the encoding methods with all possible
	 * parameters.
	 * </p>
	 * 
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param numSigBytes the number of significant bytes in your array
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 */
	private static void encode3to4(byte[] source, int srcOffset, int numSigBytes,
	        char[] destination, int destOffset) {
		
		// 1 2 3
		// 01234567890123456789012345678901 Bit position
		// --------000000001111111122222222 Array position from threeBytes
		// --------| || || || | Six bit groups to index ALPHABET
		// >>18 >>12 >> 6 >> 0 Right shift necessary
		// 0x3f 0x3f 0x3f Additional AND
		
		// Create buffer with zero-padding if there are only one or two
		// significant bytes passed in the array.
		// We have to shift left 24 in order to flush out the 1's that appear
		// when Java treats a value as negative that is cast from a byte to an
		// int.
		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
		        | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
		        | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);
		
		destination[destOffset] = ALPHABET[(inBuff >>> 18)];
		switch(numSigBytes) {
		case 3:
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
			break;
		
		case 2:
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = EQUALS_SIGN;
			break;
		
		case 1:
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = EQUALS_SIGN;
			destination[destOffset + 3] = EQUALS_SIGN;
			break;
		}
	}
	
	/**
	 * Encodes a byte array into Base64 notation.
	 * 
	 * @param source The data to convert
	 * @return The data in Base64-encoded form
	 * @throws NullPointerException if source array is null
	 */
	public static String encode(byte[] source) {
		return encode(source, 0, source.length, false);
	}
	
	/**
	 * Encodes a byte array into Base64 notation.
	 * 
	 * @param source The data to convert
	 * @param breakLines Break output into multiple lines.
	 * @return The Base64-encoded data as a String
	 * @throws NullPointerException if source array is null
	 */
	public static String encode(byte[] source, boolean breakLines) {
		return encode(source, 0, source.length, breakLines);
	}
	
	/**
	 * Encodes a byte array into Base64 notation.
	 * 
	 * @param source The data to convert
	 * @param off Offset in array where conversion should begin
	 * @param len Length of data to convert
	 * @return The Base64-encoded data as a String
	 * @throws NullPointerException if source array is null
	 * @throws IllegalArgumentException if source array, offset, or length are
	 *             invalid
	 */
	public static String encode(byte[] source, int off, int len) {
		return encode(source, off, len, false);
	}
	
	/**
	 * Encodes a byte array into Base64 notation.
	 * 
	 * @param source The data to convert
	 * @param off Offset in array where conversion should begin
	 * @param len Length of data to convert
	 * @param breakLines Break output into multiple lines
	 * @return The Base64-encoded data as a String
	 * @throws NullPointerException if source array is null
	 * @throws IllegalArgumentException if source array, offset, or length are
	 *             invalid
	 */
	public static String encode(byte[] source, int off, int len, boolean breakLines) {
		return new String(encodeToChars(source, off, len, breakLines));
	}
	
	/**
	 * Similar to {@link #encode(byte[])} but returns a byte array instead of
	 * instantiating a String. This is more efficient if you're working with I/O
	 * streams and have large data sets to encode.
	 * 
	 * 
	 * @param source The data to convert
	 * @return The Base64-encoded data as a byte[] (of ASCII characters)
	 * @throws NullPointerException if source array is null
	 */
	public static char[] encodeToChars(byte[] source) {
		return encodeToChars(source, 0, source.length, false);
	}
	
	/**
	 * Similar to {@link #encode(byte[], int, int, boolean)} but returns a byte
	 * array instead of instantiating a String. This is more efficient if you're
	 * working with I/O streams and have large data sets to encode.
	 * 
	 * 
	 * @param source The data to convert
	 * @param off Offset in array where conversion should begin
	 * @param len Length of data to convert
	 * @param breakLines Break output into multiple lines.
	 * @return The Base64-encoded data as a String
	 * @throws NullPointerException if source array is null
	 * @throws IllegalArgumentException if source array, offset, or length are
	 *             invalid
	 */
	public static char[] encodeToChars(byte[] source, int off, int len, boolean breakLines) {
		
		if(source == null) {
			throw new NullPointerException("Cannot serialize a null array.");
		}
		
		if(off < 0) {
			throw new IllegalArgumentException("Cannot have negative offset: " + off);
		}
		
		if(len < 0) {
			throw new IllegalArgumentException("Cannot have length offset: " + len);
		}
		
		if(off + len > source.length) {
			throw new IllegalArgumentException("Cannot have offset of " + off + " and length of "
			        + len + " with array of length " + source.length);
		}

		else {
			
			// Try to determine more precisely how big the array needs to be.
			// If we get it right, we don't have to do an array copy, and
			// we save a bunch of memory.
			int encLen = (len / 3) * 4 + (len % 3 > 0 ? 4 : 0); // Bytes needed
			// for actual
			// encoding
			if(breakLines) {
				encLen += encLen / MAX_LINE_LENGTH; // Plus extra newline
				// characters
			}
			char[] outBuff = new char[encLen];
			
			int d = 0;
			int e = 0;
			int len2 = len - 2;
			int lineLength = 0;
			for(; d < len2; d += 3, e += 4) {
				encode3to4(source, d + off, 3, outBuff, e);
				
				lineLength += 4;
				if(breakLines && lineLength >= MAX_LINE_LENGTH) {
					outBuff[e + 4] = NEW_LINE;
					e++;
					lineLength = 0;
				}
			}
			
			if(d < len) {
				encode3to4(source, d + off, len - d, outBuff, e);
				e += 4;
			}
			
			// Only resize array if we didn't guess it right.
			if(e <= outBuff.length - 1) {
				// If breaking lines and the last byte falls right at
				// the line length (76 bytes per line), there will be
				// one extra byte, and the array will need to be resized.
				// Not too bad of an estimate on array size, I'd say.
				char[] finalOut = new char[e];
				System.arraycopy(outBuff, 0, finalOut, 0, e);
				return finalOut;
			} else {
				return outBuff;
			}
			
		}
		
	}
	
	/**
	 * Decodes four bytes from array <var>source</var> and writes the resulting
	 * bytes (up to three of them) to <var>destination</var>. The source and
	 * destination arrays can be manipulated anywhere along their length by
	 * specifying <var>srcOffset</var> and <var>destOffset</var>. This method
	 * does not check to make sure your arrays are large enough to accomodate
	 * <var>srcOffset</var> + 4 for the <var>source</var> array or
	 * <var>destOffset</var> + 3 for the <var>destination</var> array. This
	 * method returns the actual number of bytes that were converted from the
	 * Base64 encoding.
	 * <p>
	 * This is the lowest level of the decoding methods with all possible
	 * parameters.
	 * </p>
	 * 
	 * 
	 * @param source the array to convert
	 * @param srcOffset the index where conversion begins
	 * @param destination the array to hold the conversion
	 * @param destOffset the index where output will be put
	 * @return the number of decoded bytes converted
	 * @throws NullPointerException if source or destination arrays are null
	 * @throws IllegalArgumentException if srcOffset or destOffset are invalid
	 *             or there is not enough room in the array.
	 */
	private static int decode4to3(char[] source, int srcOffset, byte[] destination, int destOffset) {
		
		// Example: Dk==
		if(source[srcOffset + 2] == EQUALS_SIGN) {
			// Two ways to do the same thing. Don't know which way I like best.
			// int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
			// )
			// | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
			int outBuff = ((decodeChar(source[srcOffset])) << 18)
			        | ((decodeChar(source[srcOffset + 1])) << 12);
			
			destination[destOffset] = (byte)(outBuff >>> 16);
			return 1;
		}

		// Example: DkL=
		else if(source[srcOffset + 3] == EQUALS_SIGN) {
			// Two ways to do the same thing. Don't know which way I like best.
			// int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
			// )
			// | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
			// | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
			int outBuff = ((decodeChar(source[srcOffset])) << 18)
			        | ((decodeChar(source[srcOffset + 1])) << 12)
			        | ((decodeChar(source[srcOffset + 2])) << 6);
			
			destination[destOffset] = (byte)(outBuff >>> 16);
			destination[destOffset + 1] = (byte)(outBuff >>> 8);
			return 2;
		}

		// Example: DkLE
		else {
			// Two ways to do the same thing. Don't know which way I like best.
			// int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
			// )
			// | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
			// | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
			// | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
			int outBuff = ((decodeChar(source[srcOffset])) << 18)
			        | ((decodeChar(source[srcOffset + 1])) << 12)
			        | ((decodeChar(source[srcOffset + 2])) << 6)
			        | ((decodeChar(source[srcOffset + 3])));
			
			destination[destOffset] = (byte)(outBuff >> 16);
			destination[destOffset + 1] = (byte)(outBuff >> 8);
			destination[destOffset + 2] = (byte)(outBuff);
			
			return 3;
		}
		
	}
	
	/**
	 * Low-level access to decoding ASCII characters in the form of a byte
	 * array. <strong>Ignores GUNZIP option, if it's set.</strong> This is not
	 * generally a recommended method, although it is used internally as part of
	 * the decoding process. Special case: if len = 0, an empty array is
	 * returned. Still, if you need more speed and reduced memory footprint,
	 * consider this method.
	 * 
	 * @param source The Base64 encoded data
	 * @return decoded data
	 */
	public static byte[] decode(char[] source) {
		return decode(source, 0, source.length);
	}
	
	/**
	 * Low-level access to decoding ASCII characters in the form of a byte
	 * array. <strong>Ignores GUNZIP option, if it's set.</strong> This is not
	 * generally a recommended method, although it is used internally as part of
	 * the decoding process. Special case: if len = 0, an empty array is
	 * returned. Still, if you need more speed and reduced memory footprint,
	 * consider this method.
	 * 
	 * @param source The Base64 encoded data
	 * @param off The offset of where to begin decoding
	 * @param len The length of characters to decode
	 * @return decoded data
	 * @throws IllegalArgumentException If bogus characters exist in source data
	 */
	public static byte[] decode(char[] source, int off, int len) {
		
		// Lots of error checking and exception throwing
		if(source == null) {
			throw new NullPointerException("Cannot decode null source array.");
		}
		if(off < 0 || off + len > source.length) {
			throw new IllegalArgumentException("Source array with length " + source.length
			        + " cannot have offset of " + off + " and process " + len + " bytes.");
		}
		
		if(len == 0) {
			return new byte[0];
		} else if(len < 4) {
			throw new IllegalArgumentException(
			        "Base64-encoded string must have at least four characters, but length specified was "
			                + len);
		}
		
		int len34 = len * 3 / 4; // Estimate on array size
		byte[] outBuff = new byte[len34]; // Upper limit on size of output
		int outBuffPosn = 0; // Keep track of where we're writing
		
		char[] b4 = new char[4]; // Four byte buffer from source, eliminating
		// white space
		int b4Posn = 0; // Keep track of four byte input buffer
		int i = 0; // Source array counter
		byte sbiDecode = 0; // Special value from DECODABET
		
		for(i = off; i < off + len; i++) { // Loop through source
		
			sbiDecode = decodeChar(source[i]);
			
			// White space, Equals sign, or legit Base64 character
			// Note the values such as -5 and -9 in the
			// DECODABETs at the top of the file.
			if(sbiDecode < WHITE_SPACE_ENC) {
				// There's a bad input character in the Base64 stream.
				throw new IllegalArgumentException("Bad Base64 input character decimal "
				        + (source[i] & 0xFF) + " in array position " + i);
			}
			if(sbiDecode >= EQUALS_SIGN_ENC) {
				b4[b4Posn++] = source[i]; // Save non-whitespace
				if(b4Posn > 3) { // Time to decode?
					outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
					b4Posn = 0;
					
					// If that was the equals sign, break out of 'for' loop
					if(source[i] == EQUALS_SIGN) {
						break;
					}
				}
			}
		}
		
		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
		return out;
	}
	
	/**
	 * Decodes data from Base64 notation.
	 * 
	 * @param s the string to decode
	 * @return the decoded data
	 * @throws IllegalArgumentException if there is an error
	 * @throws NullPointerException if <tt>s</tt> is null
	 */
	public static byte[] decode(String s) {
		
		if(s == null) {
			throw new NullPointerException("Input string was null.");
		}
		
		char[] source = s.toCharArray();
		
		// Decode
		return decode(source, 0, source.length);
	}
	
}
