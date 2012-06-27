package com.sonicmetrics.core.shared.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

import com.google.gwt.regexp.shared.RegExp;
import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.rest.ISonicREST_API;
import com.sonicmetrics.core.shared.util.JsonUtils;
import com.sonicmetrics.core.shared.util.ValidationUtils;


/**
 * Client-side event.
 * 
 * Note: WE have a dependency to the server-side code of GWT. This does not make
 * the server slower, but allows this class to be used seamlessly in client code
 * as well.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class SonicEvent implements ISonicEvent, Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(SonicEvent.class);
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Alphanumerics plus dash '-' and underscore '_'. May not begin with
	 * underscore. Required length: 2-100 characters.
	 */
	public static final String IDENTIFIER = "[0-9a-zA-Z][0-9a-zA-Z-_]{1,99}";
	
	/**
	 * Alphanumerics plus dash '-', underscore '_', at '@', and dot '.'. May not
	 * begin with underscore. Required length: 2-99 characters.
	 */
	public static final String USERIDENTIFIER = "[0-9a-zA-Z-@.][0-9a-zA-Z-_@.]{1,100}";
	
	public static final RegExp IDENTIFIER_PATTERN = ValidationUtils.compilePattern(IDENTIFIER);
	
	public static final RegExp USERIDENTIFIER_PATTERN = ValidationUtils
	        .compilePattern(USERIDENTIFIER);
	
	@NeverNull
	protected String source;
	
	@NeverNull
	protected String action;
	
	@NeverNull
	protected String category;
	
	@CanBeNull
	protected String label;
	
	protected long timestamp;
	
	@NeverNull
	protected String subject;
	
	@CanBeNull
	protected String value;
	
	@CanBeNull
	public String key;
	
	@CanBeNull
	protected String uniqueId;
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	
	// for GWT only
	public SonicEvent() {
	}
	
	private SonicEvent(long utcTimestamp) {
		this.timestamp = utcTimestamp;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public long getWhen() {
		return this.timestamp;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public String getKey() {
		return this.key;
	}
	
	/**
	 * Additional key-value-pairs
	 */
	private final Map<String,String> extensionDataMap = new HashMap<String,String>();
	
	/**
	 * @return category.action.label if all three are defined; category.action
	 *         if these two are defined; or just the category.
	 */
	public String getDotString() {
		StringBuilder b = new StringBuilder();
		b.append(this.getCategory());
		String action = this.getAction();
		if(action != null) {
			b.append(".").append(action);
		}
		String label = this.getLabel();
		if(label != null) {
			b.append(".").append(label);
		}
		return b.toString();
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getValue() {
		return this.value;
	}
	
	private static final long MS_PER_MINUTE = 1000 * 60;
	private static final long MS_PER_DAY = MS_PER_MINUTE * 60 * 24;
	
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
	
	@Override
	public String toString() {
		return "At " + this.timestamp + " (" + toDebugTime(this.timestamp) + ") '" + this.subject
		        + "' did '" + this.category + "." + this.action + "." + this.label
		        + (this.value == null ? "" : "=" + this.value) + "' source: '" + this.source + "'";
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
	
	public boolean hasLabel() {
		return getLabel() != null;
	}
	
	public boolean hasUniqueId() {
		return this.getUniqueId() != null;
	}
	
	public static Builder create(long utcTimestamp) {
		return new Builder(utcTimestamp);
	}
	
	public void setKey(String key) {
		XyAssert.validateCondition(this.key == null, "Key can be set only once");
		// key = key name
		this.key = key;
	}
	
	public static class Builder {
		
		private SonicEvent se;
		
		public Builder(long utcTimestamp) {
			this.se = new SonicEvent(utcTimestamp);
		}
		
		public Builder category(String category) throws IllegalArgumentException {
			XyAssert.validateNotNull(category, "category");
			this.se.category = category.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, category),
			        "Category must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this;
		}
		
		public Builder action(String action) throws IllegalArgumentException {
			XyAssert.validateNotNull(action, "action");
			this.se.action = action.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, action),
			        "Action must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this;
		}
		
		public Builder label(String label) throws IllegalArgumentException {
			XyAssert.validateNotNull(label, "label");
			this.se.label = label.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, label),
			        "Label must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this;
		}
		
		public Builder subject(String subject) throws IllegalArgumentException {
			XyAssert.validateNotNull(subject, "subject");
			this.se.subject = subject.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(USERIDENTIFIER_PATTERN, subject),
			        "Subject must be a valid UserIdentifier" + ", i.e. match the regex '"
			                + USERIDENTIFIER_PATTERN + "' (Java syntax). See doc.sonicmetrics.com");
			return this;
		}
		
		public Builder source(String source) throws IllegalArgumentException {
			XyAssert.validateNotNull(source, "source");
			this.se.source = source.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(USERIDENTIFIER_PATTERN, source),
			        "Source must be a valid UserIdentifier" + ", i.e. match the regex '"
			                + USERIDENTIFIER_PATTERN + "' (Java syntax). See doc.sonicmetrics.com");
			return this;
		}
		
		public Builder withParam(String key, String value) throws IllegalArgumentException {
			if(ValidationUtils.matches(IDENTIFIER_PATTERN, key)) {
				this.se.extensionDataMap.put(key, value);
			} else {
				log.debug("Ignored invalid key '" + key + "'" + ". Did not match the regex '"
				        + IDENTIFIER + "' (Java syntax). See doc.sonicmetrics.com");
			}
			
			return this;
		}
		
		public SonicEvent done() {
			XyAssert.validateNotNull(this.se.category, "category");
			XyAssert.validateNotNull(this.se.action, "action");
			XyAssert.validateNotNull(this.se.subject, "subject");
			XyAssert.validateNotNull(this.se.source, "source");
			return this.se;
		}
		
		public Builder withParams(Map<String,String> map) throws IllegalArgumentException {
			// TODO validate
			this.se.extensionDataMap.putAll(map);
			return this;
		}
		
		public Builder uniqueId(String uniqueId) {
			XyAssert.validateNotNull(uniqueId, "uniqueid");
			// intentionally not to lowercase
			this.se.uniqueId = uniqueId;
			return this;
		}
		
	}
	
	@Override
	public Map<String,String> getExtensionData() {
		// TODO this breaks validation
		return this.extensionDataMap;
	}
	
	public int hashCode() {
		return ((int)(this.timestamp % Integer.MAX_VALUE))
		        + (this.key == null ? 0 : this.key.hashCode());
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
	
}
