package org.xydra.index.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {

	private static final String[] KATAKANA = { "ka", "ko", "ke", "ki", "ku",

	"ma", "mo", "me", "mi", "mu",

	"sa", "so", "se", "si", "su",

	"na", "no", "ne", "ni", "nu",

	"ta", "to", "te", "ti", "tu",

	"ra", "ro", "re", "ri", "ru",

	"ya", "yo", "ye", "yi", "yu",

	"wa", "wo", "we", "wi", "wu", };

	/**
	 * @param n
	 *            number of strings to be generated
	 * @param minLen
	 * @return random strings
	 */
	public static final String[] generateRandomStrings(int n, int minLen) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < n; i++) {
			list.add(generateRandomString(minLen));
		}
		return list.toArray(new String[n]);
	}

	private static String generateRandomString(int minLen) {
		String s = "";
		while (s.length() < minLen) {
			s += randomFromList(KATAKANA);
		}
		return s;
	}

	/**
	 * @param minLen
	 * @param differentSyllabes
	 *            max is 5*8=40
	 * @return a random Katakana-style string
	 */
	public static String generateRandomKatakanaString(int minLen, int differentSyllabes) {
		StringBuilder s = new StringBuilder();
		while (s.length() < minLen) {
			s.append(randomFromList(KATAKANA));
		}
		return s.toString();
	}

	public static <T> T randomFromList(T[] array) {
		return randomFromList(array, array.length);
	}

	/**
	 * @param array
	 * @param maxIndex
	 *            exclusive
	 * @return ...
	 */
	public static <T> T randomFromList(T[] array, int maxIndex) {
		assert array.length > 0;
		assert maxIndex <= array.length;
		int i = (int) (Math.random() * maxIndex);
		return array[i];
	}

	private Random rnd;

	public double random() {
		if (this.rnd == null) {
			this.rnd = new Random(0);
		}
		return this.rnd.nextDouble();
	}
}
