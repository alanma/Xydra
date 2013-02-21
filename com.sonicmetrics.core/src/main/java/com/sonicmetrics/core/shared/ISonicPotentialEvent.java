package com.sonicmetrics.core.shared;

import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;


/**
 * Each sonic event answers the basic questions of journalism about an event:
 * when, who and what happened?
 * <ol>
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
public interface ISonicPotentialEvent {
	
	public static enum FilterProperty {
		Subject, Category, Action, Label, Value, Source
	}
	
	/**
	 * See {@link ISonicPotentialEvent} for details about
	 * 'category.action.label.value'
	 * 
	 * @return the main action of the event. Like a twitter message in one word.
	 */
	@NeverNull
	String getAction();
	
	/**
	 * See {@link ISonicPotentialEvent} for details about
	 * 'category.action.label.value'
	 * 
	 * @return the category of the action of the event.
	 */
	@NeverNull
	String getCategory();
	
	/**
	 * @return the subject about whom or what the event is. In the Twitter
	 *         world, this would be the twitter user account, such as
	 *         '@sonicmetrics'.
	 */
	@NeverNull
	String getSubject();
	
	/**
	 * @return the technical source of the event, like e.g. TweetDeck as a
	 *         sender for tweets.
	 */
	@NeverNull
	String getSource();
	
	/**
	 * @return a compact string representing the contents of
	 *         'category.action.label.value'. Might turn out to be
	 *         'category.action.label.value', 'category.action.label' or
	 *         'category.action'.
	 */
	@NeverNull
	String getDotString();
	
	/**
	 * See {@link ISonicPotentialEvent} for details about
	 * 'category.action.label.value'
	 * 
	 * @return a more detailed label of the action of the event.
	 */
	@CanBeNull
	String getLabel();
	
	/**
	 * See {@link ISonicPotentialEvent} for details about
	 * 'category.action.label.value'
	 * 
	 * @return a more detailed label of the action of the event.
	 */
	@CanBeNull
	String getValue();
	
	/**
	 * @return true if the label is defined (i.e. not null)
	 */
	boolean hasLabel();
	
	/**
	 * @return an id that allows updating the event. If a new event with the
	 *         same time-stamp and this unique key is sent, the existing event
	 *         is updated. This allows sending the same data several times (by
	 *         using the original data's primary key as the uniqueId) without
	 *         creating multiple events. E.g. a twitter bot can use the tweetId
	 *         as the uniqueId.
	 */
	@CanBeNull
	String getUniqueId();
	
	/**
	 * @return true if the event was stored with a uniqueId which allows
	 *         updating the event later on.
	 */
	boolean hasUniqueId();
	
	/**
	 * @return an unmodifiable map of user-supplied extension data. Keys are
	 *         valid Identifier, values can be any strings. User data may not
	 *         exceed 500 KB.
	 */
	@NeverNull
	Map<String,String> getExtensionData();
	
}