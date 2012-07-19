package com.sonicmetrics.core.shared.impl.memory;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.rest.ISonicREST_API;
import com.sonicmetrics.core.shared.util.JsonUtils;


/**
 * Client-side event.
 * 
 * Note: WE have a dependency to the server-side code of GWT. This does not make
 * the server slower, but allows this class to be used seamlessly in client code
 * as well.
 * 
 * Note: Hashcode and equals are defined in a way that two events are only equal
 * if both have the same KEY as well. That means an event that has not been
 * logged (and hence has no key set) is never equals to an event that has been
 * logged (and hence has a key defined).
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class SonicEvent extends SonicPotentialEvent implements ISonicEvent, Serializable {
	
	public static class SeBuilder extends
	        SonicPotentialEvent.Builder<SonicEvent,SonicEvent.SeBuilder> {
		
		public SeBuilder(long utcTimestamp) {
			this.t = new SonicEvent(utcTimestamp);
			this.b = this;
		}
		
		public void validate() {
			XyAssert.validateNotNull(this.t.category, "category");
			XyAssert.validateNotNull(this.t.action, "action");
			XyAssert.validateNotNull(this.t.subject, "subject");
			XyAssert.validateNotNull(this.t.source, "source");
		}
		
		/**
		 * Adds all data given in map to the extension data.
		 * 
		 * @param map
		 * @return this
		 * @throws IllegalArgumentException
		 */
		public SeBuilder withParams(@NeverNull Map<String,String> map)
		        throws IllegalArgumentException {
			for(Entry<String,String> e : map.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();
				
				boolean recognized = false;
				for(FilterProperty ip : FilterProperty.values()) {
					if(ip.name().toLowerCase().equals(key.toLowerCase())) {
						recognized = true;
						withParam(ip, value);
						break;
					}
				}
				if(!recognized) {
					withParam(key, value);
				}
			}
			return this;
		}
		
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(SonicEvent.class);
	
	private static final long MS_PER_MINUTE = 1000 * 60;
	
	private static final long MS_PER_DAY = MS_PER_MINUTE * 60 * 24;
	
	private static final long serialVersionUID = 1L;
	
	private static boolean bothNullOrEqual(Object a, Object b) {
		if(a == null) {
			return b == null;
		} else {
			if(b == null)
				return false;
			else
				return a.equals(b);
		}
	}
	
	public static SeBuilder create(long utcTimestamp) {
		return new SeBuilder(utcTimestamp);
	}
	
	public static SeBuilder createFrom(ISonicEvent se) {
		SeBuilder b = create(se.getWhen());
		b.category(se.getCategory());
		b.action(se.getAction());
		b.source(se.getSource());
		b.subject(se.getSubject());
		b.labelIgnoreIfNull(se.getLabel());
		b.valueIgnoreIfNull(se.getValue());
		if(se.getUniqueId() != null) {
			b.uniqueId(se.getUniqueId());
		}
		b.withParams(se.getExtensionData());
		return b;
	}
	
	/**
	 * @param utcTimestamp
	 * @return a quickly calculated WRONG (ignores timezones, optional
	 *         days/seconds) human-readable date
	 */
	public static String toDebugTime(long utcTimestamp) {
		long days = utcTimestamp / MS_PER_DAY;
		long millis = utcTimestamp % MS_PER_DAY;
		long minutes = millis / MS_PER_MINUTE;
		long remainingMillis = millis % MS_PER_MINUTE;
		return days + "d|" + minutes + "m|" + remainingMillis + "ms";
	}
	
	@CanBeNull
	public String key;
	
	protected long timestamp;
	
	// for GWT only
	public SonicEvent() {
	}
	
	private SonicEvent(long utcTimestamp) {
		this.timestamp = utcTimestamp;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof ISonicEvent))
			return false;
		
		ISonicEvent o = (ISonicEvent)other;
		
		if(this.getWhen() != o.getWhen())
			return false;
		
		if(!bothNullOrEqual(this.getKey(), o.getKey())) {
			return false;
		}
		if(!bothNullOrEqual(this.getUniqueId(), o.getUniqueId())) {
			return false;
		}
		if(!bothNullOrEqual(this.getSubject(), o.getSubject())) {
			return false;
		}
		if(!bothNullOrEqual(this.getCategory(), o.getCategory())) {
			return false;
		}
		if(!bothNullOrEqual(this.getAction(), o.getAction())) {
			return false;
		}
		if(!bothNullOrEqual(this.getLabel(), o.getLabel())) {
			return false;
		}
		if(!bothNullOrEqual(this.getSource(), o.getSource())) {
			return false;
		}
		return true;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public long getWhen() {
		return this.timestamp;
	}
	
	public int hashCode() {
		return ((int)(this.timestamp % Integer.MAX_VALUE))
		        + (this.key == null ? 0 : this.key.hashCode());
	}
	
	public void setKey(String key) {
		XyAssert.validateCondition(this.key == null, "Key can be set only once");
		// key = key name
		this.key = key;
	}
	
	public StringBuilder toJsonObject() {
		StringBuilder b = new StringBuilder();
		b.append("{ ");
		
		/* line ----------------- */
		// when: 1339851818,
		JsonUtils.appendKeyValue(b, ISonicREST_API.WHEN, getWhen());
		b.append(", ");
		
		// subject: “team@sonicmetrics.com”,
		JsonUtils.appendKeyValue(b, ISonicREST_API.SUBJECT, getSubject());
		b.append(", ");
		
		// category: “event”,
		JsonUtils.appendKeyValue(b, ISonicREST_API.CATEGORY, getCategory());
		b.append(", ");
		
		// action: “hackathon”,
		JsonUtils.appendKeyValue(b, ISonicREST_API.ACTION, getAction());
		
		// OPTIONAL: label: “start”,
		if(hasLabel()) {
			b.append(", ");
			b.append(JsonUtils.string(ISonicREST_API.LABEL)).append(":")
			        .append(JsonUtils.string(getLabel()));
			
		}
		b.append(", ");
		
		/* line ----------------- */
		// source: “frontend-1.0”,
		JsonUtils.appendKeyValue(b, ISonicREST_API.SOURCE, getSource());
		
		// key: “1339851818-ACEF-0604”
		if(getKey() != null) {
			b.append(", ");
			JsonUtils.appendKeyValue(b, ISonicREST_API.KEY, getKey());
		}
		
		// OPTIONAL uniqueId
		if(hasUniqueId()) {
			b.append(", ");
			JsonUtils.appendKeyValue(b, ISonicREST_API.UNIQUEID, getUniqueId());
		}
		
		/** MORE lines ----------------- */
		for(Entry<String,String> entry : this.extensionDataMap.entrySet()) {
			b.append(", ");
			JsonUtils.appendKeyValue(b, entry.getKey(), entry.getValue());
		}
		
		// NO COMMA HERE !!!!
		b.append(" ");
		
		b.append("}");
		return b;
	}
	
	@Override
	public String toString() {
		return this.timestamp + "utc=(" + toDebugTime(this.timestamp) + ") " + super.toString();
	}
	
	public Map<String,String> getModifyableExtensionData() {
		return this.extensionDataMap;
	}
	
}
