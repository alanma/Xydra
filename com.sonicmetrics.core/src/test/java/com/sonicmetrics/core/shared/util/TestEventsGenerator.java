package com.sonicmetrics.core.shared.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.sonicmetrics.core.server.IndexedDay;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent;


public class TestEventsGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(TestEventsGenerator.class);
	private static final String[] KATAKANA = { "ka", "ko", "ke", "ki", "ku",
	
	"ma", "mo", "me", "mi", "mu",
	
	"sa", "so", "se", "si", "su",
	
	"na", "no", "ne", "ni", "nu",
	
	"ta", "to", "te", "ti", "tu",
	
	"ra", "ro", "re", "ri", "ru",
	
	"ya", "yo", "ye", "yi", "yu",
	
	"wa", "wo", "we", "wi", "wu", };
	
	/**
	 * Categories, actions and labels are not correlated at all (completely
	 * mixed).
	 * 
	 * All events are in an interval [ 01.01.2020, 13 days later ]
	 * 
	 * Warn: Creates all events in memory first.
	 * 
	 * @param startUtc
	 * @param endUtc
	 * 
	 * @param numberOfEvents how many events to generate
	 * @param subjects
	 * @param categories an array of categories to choose from
	 * @param actions an array of actions to choose from
	 * @param labels an array of labels to choose from
	 * @param sources
	 * @return an iterator over test ISonicEvents
	 */
	public static Iterator<SonicEvent> generateSampleData(long startUtc, long endUtc,
	        int numberOfEvents, String[] subjects, String[] categories, String[] actions,
	        String[] labels, String[] sources) {
		log.info("Generate " + numberOfEvents + " sample data in time range ["
		        + IndexedDay.toIsoDateTimeString(startUtc) + ","
		        + IndexedDay.toIsoDateTimeString(endUtc) + "] with categories="
		        + Arrays.toString(categories) + " actions=" + Arrays.toString(actions) + " labels="
		        + Arrays.toString(labels));
		
		// generate events
		LinkedList<SonicEvent> eventList = new LinkedList<SonicEvent>();
		for(int i = 0; i < numberOfEvents; i++) {
			long time = startUtc + (long)(Math.random() * ((double)endUtc - startUtc));
			
			String subject = randomFromList(subjects);
			String category = randomFromList(categories);
			String action = randomFromList(actions);
			String label = randomFromList(labels);
			String source = randomFromList(sources);
			SonicEvent se = SonicEvent.create(time)
			
			.subject(subject)
			
			.category(category)
			
			.action(action)
			
			.labelIgnoreIfNull(label)
			
			.source(source)
			
			.build();
			eventList.add(se);
		}
		
		return eventList.descendingIterator();
	}
	
	public static Iterator<SonicEvent> generateSampleData(long startUtc, long endUtc, int n) {
		return generateSampleData(startUtc, endUtc, n, generateRandomStrings(20, 15),
		        generateRandomStrings(20, 15), generateRandomStrings(20, 15),
		        generateRandomStrings(2, 30), generateRandomStrings(3, 20));
	}
	
	/**
	 * @param n to be generated
	 * @param minLen
	 * @return random strings
	 */
	public static final String[] generateRandomStrings(int n, int minLen) {
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < n; i++) {
			list.add(generateRandomString(minLen));
		}
		return list.toArray(new String[n]);
	}
	
	private static String generateRandomString(int minLen) {
		String s = "";
		while(s.length() < minLen) {
			s += randomFromList(KATAKANA);
		}
		return s;
	}
	
	public static <T> T randomFromList(T[] array) {
		assert array.length > 0;
		int i = (int)(Math.random() * array.length);
		return array[i];
	}
	
	private Random rnd;
	
	public double random() {
		if(this.rnd == null) {
			this.rnd = new Random(0);
		}
		return this.rnd.nextDouble();
	}
	
}
