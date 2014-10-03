package org.xydra.restless;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal class to represent a URL path that gets mapped to a set of variables
 * via regular expressions. Syntax inspired from JRS-311.
 * 
 * Examples for valid patterns
 * <ul>
 * <li>/my/path/is/this</li>
 * <li>/users/{userid}</li>
 * <li>/users/{userid}/view</li>
 * <li>/users/{userid}/view/{command}</li>
 * </ul>
 * 
 * Examples for invalid patterns
 * <dl>
 * <dt>my/path/is/this</dt>
 * <dd>Path must start with '/'</dd>
 * <dt>/users/{userid}here</dt>
 * <dd>Variable part must be between slashes '/'</dd>
 * <dt>/users/{user/here</dt>
 * <dd>Closing curly brace missing</dd>
 * </dl>
 * 
 * @author voelkel
 * 
 */
@ThreadSafe
public class PathTemplate {

	/** Matches any character */
	static final String ANY_REGEX = ".*";

	/** Matches '/' */
	static final String PATH_REGEX = "/";

	/** Matches a group of characters up to the next '/' */
	static final String VAR_REGEX = "([^/]+)";

	/** Matches '/' or empty string */
	static final String END_REGEX = "/?";

	/*
	 * patterns are thread-safe. Matchers are not thread-safe, but
	 * p.matcher(...) always creates new matchers, so there's no problem here.
	 */
	private transient Pattern p;

	/*
	 * since this variable is only read in this class, access to it is also
	 * thread-safe. (at the moment - if a method which writes to this variable
	 * is added, access to this variable needs to be made thread-safe)
	 */
	@NeverNull
	private final String regex;

	/**
	 * Changing the contents of this list is not allowed!
	 * 
	 * Concerning thread-safety: Since we're disallowing write access to this
	 * list, all threads only read from it, which means that no specific
	 * synchronization needs to take place.
	 */
	private List<String> variableNames = new ArrayList<String>();

	/**
	 * Create a new URL pattern with variable parts.
	 * 
	 * @param pathExpression
	 *            must start with '/'; may not contain '//' @NeverNull
	 */
	public PathTemplate(@NeverNull String pathExpression) {
		if (!pathExpression.startsWith("/")) {
			throw new IllegalArgumentException("Path must start with '/'. Path ='" + pathExpression
					+ "'");
		}
		if (pathExpression.contains("//")) {
			throw new IllegalArgumentException("Path may not contain '//'. Path ='"
					+ pathExpression + "'");
		}

		// turn into regex with groups
		String[] segments = pathExpression.substring(1).split("/");
		/* '/way/{id}/step' => '/way/[^/]+/step' */
		StringBuffer regexBuf = new StringBuffer();
		for (String segment : segments) {
			regexBuf.append(PATH_REGEX);
			if (segment.startsWith("{")) {
				if (!segment.endsWith("}")) {
					throw new IllegalArgumentException(
							"Segment in path must look like '{name}'. Closing brace missing in '"
									+ segment + "'.");
				}
				String variableName = segment.substring(1, segment.length() - 1);

				/*
				 * since this the constructor, we don't need to synchronize here
				 */
				this.variableNames.add(variableName);
				regexBuf.append(VAR_REGEX);
			} else {
				// normal as-is segment
				regexBuf.append(segment);
			}
		}
		regexBuf.append(END_REGEX);
		this.regex = regexBuf.toString();

		this.p = Pattern.compile(this.regex);
	}

	/**
	 * @param path
	 *            a full path containing values for parameters as declared in
	 *            constructor
	 * @return values of variables in order @NeverNull
	 */
	public List<String> extractVariables(@NeverNull String path) {
		Matcher m = this.p.matcher(path);

		if (m.groupCount() != this.variableNames.size()) {
			throw new IllegalArgumentException("Path contains a different number of variables ("
					+ m.groupCount() + ") than the template (" + this.variableNames.size() + ")");
		}

		if (!m.matches()) {
			return Collections.emptyList();
		}

		List<String> values = new ArrayList<String>();
		for (int i = 1; i <= m.groupCount(); i++) {
			String value = m.group(i);
			values.add(value);
		}
		return values;
	}

	/**
	 * @return the regular expression to match URLs to this
	 */
	public String getRegex() {
		return this.regex;
	}

	/**
	 * Returns the list of variable names.
	 * 
	 * Changing the content of this list is not allowed. If you're changing the
	 * content, no guarantees can be made to the behavior of the complete
	 * application (for example, it might lead to synchronization problems).
	 * 
	 * @return the names of variables in the order in which they are used to
	 *         call the Java method
	 */
	protected List<String> getVariableNames() {
		return this.variableNames;
	}

	/**
	 * @param path
	 *            starting with slash '/' @NeverNull
	 * @return true if this PathTemplate matches a given path
	 */
	public boolean matches(@NeverNull String path) {

		return this.p.matcher(path).matches();
	}

	@Override
	public String toString() {
		return "'" + this.regex + "', variables: " + this.variableNames;
	}
}
