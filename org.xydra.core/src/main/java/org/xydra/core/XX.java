package org.xydra.core;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;

/**
 * A utility class for using {@link XId} and {@link XAddress}.
 *
 * @author xamde
 * @author kaidel
 * @author dscharrer
 */
public class XX extends Base {

	/**
	 * Use {@link XCopyUtils#copyObject(XId, String, XReadableObject)} if the
	 * resulting object should not be backed by the XReadableObject.
	 *
	 * @param actor The session actor to use for the returned object.
	 * @param password The password corresponding to the given actor.
	 * @param objectSnapshot
	 * @return an object with the same initial state as the given object
	 *         snapshot. The returned object may be backed by the provided
	 *         XReadableObject instance, so it should no longer be modified
	 *         directly or the behavior of the model is undefined.
	 */
	public static XObject wrap(final XId actor, final String password,
			final XReadableObject objectSnapshot) {
		if (objectSnapshot instanceof XRevWritableObject) {
			return new MemoryObject(actor, password, (XRevWritableObject) objectSnapshot, null);
		} else {
			return XCopyUtils.copyObject(actor, password, objectSnapshot);
		}
	}

	/**
	 * Use {@link XCopyUtils#copyModel(XId, String, XReadableModel)} if the
	 * resulting model should not be backed by the XReadableModel.
	 *
	 * @param actor The session actor to use for the returned model.
	 * @param password The password corresponding to the given actor.
	 * @param modelSnapshot
	 * @return a model with the same initial state as the given model snapshot.
	 *         The returned model may be backed by the provided XReadableModel
	 *         instance, so it should no longer be modified directly or the
	 *         behavior of the model is undefined.
	 */
	public static XModel wrap(final XId actor, final String password,
			final XReadableModel modelSnapshot) {
		if (modelSnapshot instanceof XExistsRevWritableModel) {
			return new MemoryModel(actor, password, (XExistsRevWritableModel) modelSnapshot);
		} else {
			return XCopyUtils.copyModel(actor, password, modelSnapshot);
		}
	}

	public static boolean isValidXmlNameStartChar(final char c) {
		return

				c == ':' ||

				c >= 'A' && c <= 'Z' ||

				c == '_' ||

				c >= 'a' && c <= 'z' ||

				// a with accent
				c >= '\u00C0' && c <= '\u00D6' ||

				c >= '\u00D8' && c <= '\u00F6' ||

				c >= '\u00F8' && c <= '\u02FF' ||

				c >= '\u0370' && c <= '\u037D' ||

				c >= '\u037F' && c <= '\u1FFF' ||

				c >= '\u200C' && c <= '\u200D' ||

				c >= '\u2070' && c <= '\u218F' ||

				c >= '\u2C00' && c <= '\u2FEF' ||

				c >= '\u2001' && c <= '\uD7FF' ||

				c >= '\uF900' && c <= '\uFDCF' ||

				c >= '\uFDF0' && c <= '\uFFFD';

				/*
				 * Java can handle only 16 bit unicode
				 *
				 * (c >= '\u10000' && c <= '\uEFFFF') ||
				 */
	}

	public static boolean isValidXmlNameChar(final char c) {
		return isValidXmlNameStartChar(c) ||

				c == '-' ||

				c == '.' ||

				c >= '0' && c <= '9' ||

				// MIDDLE DOT
				c == '\u00B7' ||

				c >= '\u0300' && c <= '\u036F' ||

				c >= '\u203F' && c <= '\u2040';
	}

	public static String toUnicodeFourDigits(final char c) {
		return ("" + (10000 + c)).substring(1);
	}

	/**
	 * @param addr @NeverNull
	 * @param repositoryId @CanBeNull
	 * @param modelId @CanBeNull
	 * @param objectId @CanBeNull
	 * @param fieldId @CanBeNull
	 * @return
	 */
	public static boolean isSameAddress(final XAddress addr, final XId repositoryId, final XId modelId,
			final XId objectId, final XId fieldId) {

		/* faster by comparing high-variance parts first */
		if (fieldId == null) {
			if (addr.getField() != null) {
				return false;
			}
		} else if (!fieldId.equals(addr.getField())) {
			return false;
		}

		if (objectId == null) {
			if (addr.getObject() != null) {
				return false;
			}
		} else if (!objectId.equals(addr.getObject())) {
			return false;
		}

		if (modelId == null) {
			if (addr.getModel() != null) {
				return false;
			}
		} else if (!modelId.equals(addr.getModel())) {
			return false;
		}

		if (repositoryId == null) {
			if (addr.getRepository() != null) {
				return false;
			}
		} else if (!repositoryId.equals(addr.getRepository())) {
			return false;
		}

		return true;
	}

}
