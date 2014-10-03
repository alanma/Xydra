package org.xydra.restless.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

public class LocaleUtils {

	private static final Logger log = LoggerFactory.getLogger(LocaleUtils.class);

	public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * Helper for parsing HTTP header
	 */
	private static class AcceptLanguage {

		private List<LangDef> langDefs = new LinkedList<LangDef>();

		/**
		 * Silently ignores unparseable parts
		 * 
		 * @param acceptLanguageHeader
		 */
		public AcceptLanguage(String acceptLanguageHeader) {
			if (acceptLanguageHeader == null)
				return;

			String[] defs = acceptLanguageHeader.split(",");
			for (String def : defs) {
				try {
					LangDef langDef = new LangDef(def);
					this.langDefs.add(langDef);
				} catch (IllegalArgumentException e) {
					// ignore it
				}
			}
			Collections.sort(this.langDefs);
		}

	}

	/**
	 * Helper for parsing HTTP header
	 */
	public static class LangDef implements Comparable<LangDef> {

		private String lang;

		private double q;

		public LangDef(String def) {
			String[] pair = def.split(";");
			if (pair.length > 2) {
				throw new IllegalArgumentException("More than two parts in '" + def + "'");
			}
			if (pair.length == 1) {
				this.lang = def.trim();
				this.q = 1.0;
			} else if (pair.length == 2) {
				this.lang = pair[0].trim();
				try {
					this.q = Double.parseDouble(pair[1].trim());
					if (this.q < 0 || this.q > 1) {
						log.warn("Malformed q-value in " + HEADER_ACCEPT_LANGUAGE + " '" + pair[1]
								+ "'");
						this.q = 0.5;
					}
				} catch (NumberFormatException e) {
					// set to 0.5
					this.q = 0.5;
				}
			}
		}

		/**
		 * @return an unsanitised string from the HTTP header for a language
		 */
		public String getLang() {
			return this.lang;
		}

		/**
		 * @return 0..1, 1 = most desired
		 */
		public double getFactor() {
			return this.q;
		}

		@Override
		public int compareTo(LangDef o) {
			return (int) Math.signum(this.q - o.q);
		}
	}

	/**
	 * Silently ignores unparseable parts
	 * 
	 * @param acceptLanguageHeader
	 * @return a (possibly empty) list of LangDef values
	 */
	public static List<LangDef> parseAcceptLanguageHeader(String acceptLanguageHeader) {
		/*
		 * Accept-Language = "Accept-Language" ":" 1#( language-range [ ";" "q"
		 * * "=" qvalue ] )
		 * 
		 * language-range = ( ( 1*8ALPHA *( "-" 1*8ALPHA ) ) | "*" )
		 * 
		 * Each language-range MAY be given an associated quality value which
		 * represents an estimate of the user's preference for the languages
		 * specified by that range. The quality value defaults to "q=1". For
		 * example,
		 * 
		 * Accept-Language: da, en-gb;q=0.8, en;q=0.7
		 * 
		 * would mean:
		 * "I prefer Danish, but will accept British English and other types of English."
		 * 
		 * A language-range matches a language-tag if it exactly equals the tag,
		 * or if it exactly equals a prefix of the tag such that the first tag
		 * character following the prefix is "-". The special range "*", if
		 * present in the Accept-Language field, matches every tag not matched
		 * by any other range present in the Accept-Language field.
		 */

		AcceptLanguage acceptLanguage = new AcceptLanguage(acceptLanguageHeader);
		return acceptLanguage.langDefs;
	}

	/**
	 * @param req
	 * @return the locales contained in the browser's request. Returns an empty
	 *         array if none found. Sorted in order of preference.
	 */
	public static List<Locale> getLocalesFromBrowser(HttpServletRequest req) {
		ArrayList<Locale> locales = new ArrayList<Locale>();

		Enumeration<String> e = req.getHeaders(HEADER_ACCEPT_LANGUAGE);
		while (e.hasMoreElements()) {
			String acceptLanguageHeader = (String) e.nextElement();
			List<LangDef> langDefs = parseAcceptLanguageHeader(acceptLanguageHeader);
			for (LangDef l : langDefs) {
				Locale loc = new Locale(l.getLang());
				locales.add(loc);
			}
		}

		return locales;
	}

	/**
	 * @param locale
	 *            never null
	 * @param precision
	 *            3 for high precision down to 0 = no precision
	 * @return e.g. "en_us_slang"
	 */
	public static String toString(Locale locale, int precision) {
		StringBuffer buf = new StringBuffer();
		if (precision > 0) {
			buf.append(locale.getLanguage().toLowerCase());
			if (precision > 1) {
				if (locale.getCountry() != null && !locale.getCountry().equals("")) {
					buf.append("_");
					buf.append(locale.getCountry().toLowerCase());
				}
				if (precision > 2) {
					if (locale.getVariant() != null && !locale.getVariant().equals("")) {
						buf.append("_");
						buf.append(locale.getVariant().toLowerCase());
					}
				}
			}
		}
		return buf.toString();
	}

}
