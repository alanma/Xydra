package com.sonicmetrics.core.shared.util;

import org.xydra.index.impl.TestDataGenerator;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import com.sonicmetrics.core.server.IndexedDay;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent;


public class TestEventsGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(TestEventsGenerator.class);
    
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
            
            String subject = TestDataGenerator.randomFromList(subjects);
            String category = TestDataGenerator.randomFromList(categories);
            String action = TestDataGenerator.randomFromList(actions);
            String label = TestDataGenerator.randomFromList(labels);
            String source = TestDataGenerator.randomFromList(sources);
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
        return generateSampleData(startUtc, endUtc, n,
                TestDataGenerator.generateRandomStrings(20, 15),
                TestDataGenerator.generateRandomStrings(20, 15),
                TestDataGenerator.generateRandomStrings(20, 15),
                TestDataGenerator.generateRandomStrings(2, 30),
                TestDataGenerator.generateRandomStrings(3, 20));
    }
    
}
