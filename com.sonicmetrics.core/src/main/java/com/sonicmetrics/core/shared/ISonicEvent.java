package com.sonicmetrics.core.shared;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * Each sonic event answers the basic questions of journalism about an event:
 * when, who and what happened?
 * <ol>
 * <li>When is {@link #getWhen()},</li>
 * <li>Who is {@link #getSubject()} and</li>
 * <li>What is defined mostly by {@link #getAction()}. Furthermore, the action
 * is generalised as {@link #getCategory()} and refined as {@link #getLabel()}.</li>
 * </ol>
 * 
 * The canonical order of properties is: Subject, Category, Action, Label,
 * Source
 * 
 * 
 * Sonic events are compatible with Google Analytics, i.e. it is trivial to send
 * all sonic events also to Google Analytics. A Google Analytics events has the
 * structure 'category.action.label.value'. To get meaningful queries, sonic
 * events must have category and action defined.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public interface ISonicEvent extends ISonicPotentialEvent {
	
	/**
	 * @return the time-stamp of the event
	 */
	long getWhen();
	
	/**
	 * @return the database key used to store the event. This helps to get only
	 *         new events when using such a key as a 'lastkey' parameter in a
	 *         REST query.
	 */
	String getKey();
	
	/**
	 * Used by {@link ISonicDB} to set a key. May be set only once. Key must
	 * match internal time encoding.
	 * 
	 * @param key
	 */
	void setKey(@NeverNull String key);
	
	/**
	 * @return a valid JSON string that looks like this
	 *         "{ 'key1'='value1'; 'key2'='value2' }"
	 */
	StringBuilder toJsonObject();
	
}
