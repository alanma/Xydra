package org.xydra.gaemyadmin;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.utils.CookieUtils;

/**
 * Defend against XSS attacks.
 *
 * @author xamde
 */
public class AdminAuthUtils {

	public static final Logger log = LoggerFactory.getLogger(AdminAuthUtils.class);

	static final String COOKIE_NAME_CONFIRM = "confirmCookie";

	/**
	 * Set auth cookie for 120 seconds.
	 *
	 * @param context
	 * @param passwordPropertyNameInWebXml
	 */
	public static void setTempAuthCookie(final IRestlessContext context,
			final String passwordPropertyNameInWebXml) {
		final String password = context.getRestless().getInitParameter(passwordPropertyNameInWebXml);
		if (password == null) {
			throw new RuntimeException(
					"Password not set in web.xml as init-param of Restless. So admin actions cannot be confirmed securely. Please set '"
							+ passwordPropertyNameInWebXml
							+ "' as a restless init param in web.xml");
		}
		CookieUtils
				.setCookie(context.getResponse(), COOKIE_NAME_CONFIRM, password, null, null, 120);
	}

	/**
	 * @param context
	 * @param passwordPropertyNameInWebXml
	 * @param confirmParam
	 * @throws IllegalStateException
	 *             if not authorised
	 */
	public static void checkIfAuthorised(final IRestlessContext context,
			final String passwordPropertyNameInWebXml, final String confirmParam) throws IllegalStateException {
		final String password = context.getRestless().getInitParameter(passwordPropertyNameInWebXml);
		if (password == null) {
			throw new RuntimeException(
					"Password not set in web.xml as init-param of Restless. So admin actions cannot be confirmed securely. Please set '"
							+ passwordPropertyNameInWebXml
							+ "' as a restless init param in web.xml");
		}

		final String gotCookie = CookieUtils.getCookie(context.getRequest(), COOKIE_NAME_CONFIRM);
		log.info("Found confirm param='" + confirmParam + "' and cookie ='" + gotCookie + "'");

		if (confirmParam == null) {
			throw new IllegalStateException("No confirm param found");
		}
		if (gotCookie == null) {
			throw new IllegalStateException("No confirm cookie found (name='" + COOKIE_NAME_CONFIRM
					+ "')");
		}
		if (!confirmParam.equals(gotCookie)) {
			throw new IllegalStateException("Cookie does not match confirmParam");
		}
	}

}
