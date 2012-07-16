package com.sonicmetrics.core.shared.impl.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

import com.google.gwt.regexp.shared.RegExp;
import com.sonicmetrics.core.shared.ISonicPotentialEvent;
import com.sonicmetrics.core.shared.rest.ISonicREST_API;
import com.sonicmetrics.core.shared.util.ValidationUtils;


/**
 * Potential client-side event.
 * 
 * @author xamde
 * 
 */
public class SonicPotentialEvent implements ISonicPotentialEvent {
	
	/**
	 * Subclasses must set t to a temp object and b to a concrete builder.
	 * 
	 * @author xamde
	 * 
	 * @param <T>
	 * @param <B>
	 */
	public static abstract class Builder<T extends SonicPotentialEvent, B extends Builder<T,B>> {
		protected T t;
		protected B b;
		
		public T build() {
			validate();
			return this.t;
		}
		
		/**
		 * Throws exceptions if invalid
		 */
		protected void validate() {
		}
		
		public B action(String action) throws IllegalArgumentException {
			XyAssert.validateNotNull(action, "action");
			this.t.action = action.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, action),
			        "Action must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this.b;
		}
		
		public B category(String category) throws IllegalArgumentException {
			XyAssert.validateNotNull(category, "category");
			this.t.category = category.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, category),
			        "Category must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this.b;
		}
		
		public B label(String label) throws IllegalArgumentException {
			XyAssert.validateNotNull(label, "label");
			this.t.label = label.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(IDENTIFIER_PATTERN, label),
			        "Label must be a valid Identifier" + ", i.e. match the regex '" + IDENTIFIER
			                + "' (Java syntax). See doc.sonicmetrics.com");
			return this.b;
		}
		
		public B labelIgnoreIfNull(String label) {
			if(label != null) {
				return label(label);
			}
			return this.b;
		}
		
		public B source(String source) throws IllegalArgumentException {
			XyAssert.validateNotNull(source, "source");
			this.t.source = source.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(USERIDENTIFIER_PATTERN, source),
			        "Source must be a valid UserIdentifier" + ", i.e. match the regex '"
			                + USERIDENTIFIER_PATTERN + "' (Java syntax). See doc.sonicmetrics.com");
			return this.b;
		}
		
		public B subject(String subject) throws IllegalArgumentException {
			XyAssert.validateNotNull(subject, "subject");
			this.t.subject = subject.toLowerCase();
			XyAssert.validateCondition(ValidationUtils.matches(USERIDENTIFIER_PATTERN, subject),
			        "Subject must be a valid UserIdentifier" + ", i.e. match the regex '"
			                + USERIDENTIFIER_PATTERN + "' (Java syntax). See doc.sonicmetrics.com");
			return this.b;
		}
		
		public B uniqueId(String uniqueId) {
			XyAssert.validateNotNull(uniqueId, "uniqueid");
			// intentionally not to lowercase
			this.t.uniqueId = uniqueId;
			return this.b;
		}
		
		public B uniqueIdIgnoredIfNull(String uniqueId) {
			if(uniqueId != null) {
				return uniqueId(uniqueId);
			}
			return this.b;
		}
		
		public B value(String value) {
			XyAssert.validateNotNull(value, "value");
			this.t.value = value;
			return this.b;
		}
		
		public B valueIgnoreIfNull(String value) {
			if(value != null) {
				this.t.value = value;
			}
			return this.b;
		}
		
		public B withParam(FilterProperty key, String value) {
			switch(key) {
			case Action:
				return action(value);
			case Category:
				return category(value);
			case Label:
				return label(value);
			case Source:
				return source(value);
			case Subject:
				return subject(value);
			case Value:
				return value(value);
			}
			throw new AssertionError();
		}
		
		/**
		 * If a reserved key such as 'category' is used, this is mapped to a
		 * call of category(s). Silently ignored if value is null.
		 * 
		 * @param key
		 * @param value
		 * @return this builder
		 * @throws IllegalArgumentException
		 */
		public B withParam(String key, String value) throws IllegalArgumentException {
			if(value == null) {
				return this.b;
			}
			if(ValidationUtils.matches(IDENTIFIER_PATTERN, key)) {
				// auto-fix mapping to built-in parameters
				if(key.equalsIgnoreCase(ISonicREST_API.CATEGORY)) {
					return category(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.ACTION)) {
					return action(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.LABEL)) {
					return label(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.VALUE)) {
					return value(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.SUBJECT)) {
					return subject(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.SOURCE)) {
					return source(value);
				} else if(key.equalsIgnoreCase(ISonicREST_API.UNIQUEID)) {
					return uniqueId(value);
				} else {
					this.t.extensionDataMap.put(key, value);
				}
			} else {
				log.debug("Ignored invalid key '" + key + "'" + ". Did not match the regex '"
				        + IDENTIFIER + "' (Java syntax). See doc.sonicmetrics.com");
			}
			return this.b;
		}
		
	}
	
	public static class SpeBuilder extends
	        SonicPotentialEvent.Builder<SonicPotentialEvent,SonicPotentialEvent.SpeBuilder> {
		
		public SpeBuilder() {
			this.t = new SonicPotentialEvent();
			this.b = this;
		}
		
		public void validate() {
			XyAssert.validateNotNull(this.t.category, "category");
			XyAssert.validateNotNull(this.t.action, "action");
			XyAssert.validateNotNull(this.t.subject, "subject");
			XyAssert.validateNotNull(this.t.source, "source");
		}
		
	}
	
	/**
	 * Alphanumerics plus dash '-' and underscore '_'. May not begin with
	 * underscore. Required length: 2-100 characters.
	 */
	public static final String IDENTIFIER = "[0-9a-zA-Z][0-9a-zA-Z-_]{1,99}";
	
	public static final RegExp IDENTIFIER_PATTERN = ValidationUtils.compilePattern(IDENTIFIER);
	
	private static final Logger log = LoggerFactory.getLogger(SonicPotentialEvent.class);
	
	/**
	 * Alphanumerics plus dash '-', underscore '_', at '@', and dot '.'. May not
	 * begin with underscore. Required length: 2-99 characters.
	 */
	public static final String USERIDENTIFIER = "[0-9a-zA-Z-@.][0-9a-zA-Z-_@.]{1,100}";
	
	public static final RegExp USERIDENTIFIER_PATTERN = ValidationUtils
	        .compilePattern(USERIDENTIFIER);
	
	@NeverNull
	protected String action;
	
	@NeverNull
	protected String category;
	
	/**
	 * Additional key-value-pairs
	 */
	protected final Map<String,String> extensionDataMap = new HashMap<String,String>();
	
	@CanBeNull
	protected String label;
	
	@NeverNull
	protected String source;
	
	@NeverNull
	protected String subject;
	
	@CanBeNull
	protected String uniqueId;
	
	@CanBeNull
	protected String value;
	
	public static SpeBuilder create() {
		return new SpeBuilder();
	}
	
	// for GWT only
	public SonicPotentialEvent() {
	}
	
	public String getAction() {
		return this.action;
	}
	
	public String getCategory() {
		return this.category;
	}
	
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
	
	@Override
	public Map<String,String> getExtensionData() {
		return Collections.unmodifiableMap(this.extensionDataMap);
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public String getUniqueId() {
		return this.uniqueId;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public boolean hasLabel() {
		return getLabel() != null;
	}
	
	public boolean hasUniqueId() {
		return this.getUniqueId() != null;
	}
	
	@Override
	public String toString() {
		return "subject:'" + this.subject + "':'" + this.category + "." + this.action + "."
		        + this.label + (this.value == null ? "" : "=" + this.value) + "' source:'"
		        + this.source + "'";
	}
	
}
