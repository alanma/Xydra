package org.xydra.base.id;

import java.util.Iterator;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.URIFormatException;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;
import org.xydra.core.XX;
import org.xydra.index.IEntrySet;
import org.xydra.index.impl.IntegerRangeIndex;
import org.xydra.index.impl.MapSetIndex;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * An implementation of {@link XIdProvider}
 *
 * @author xamde
 * @author kaidel
 */

@RunsInGWT(true)
@RequiresAppEngine(false)
public abstract class BaseStringIDProvider implements XIdProvider {

	private static final Logger log = LoggerFactory.getLogger(BaseStringIDProvider.class);

	/**
	 * Notable excludes: [-.0-9]
	 *
	 * Some notable includes: [:_öä]
	 */
	public static final String nameStartChar = // .
	"A-Z" // .
			+ "_" // .
			+ "a-z" // .
			+ "\\xC0-\\xD6" // .
			+ "\\xD8-\\xF6" // .
			+ "\\u00F8-\\u02FF" // .
			+ "\\u0370-\\u037D" // .
			+ "\\u037F-\\u1FFF"// .
			+ "\\u200C-\\u200D"// .
			+ "\\u2070-\\u218F"// .
			+ "\\u2C00-\\u2FEF"// .
			+ "\\u3001-\\uD7FF"// .
			+ "\\uF900-\\uFDCF" // .
			+ "\\uFDF0-\\uFFFD";

	public static IntegerRangeIndex RANGEINDEX_nameStartChar;

	public static IntegerRangeIndex RANGEINDEX_nameChar;

	static {
		RANGEINDEX_nameStartChar = new IntegerRangeIndex();
		RANGEINDEX_nameStartChar.index('A', 'Z');// .
		RANGEINDEX_nameStartChar.index('_', '_');// .
		RANGEINDEX_nameStartChar.index('a', 'z');// .
		RANGEINDEX_nameStartChar.index(hex("C0"), hex("D6")); // .
		RANGEINDEX_nameStartChar.index(hex("D8"), hex("F6")); // .
		RANGEINDEX_nameStartChar.index(hex("00F8"), hex("02FF")); // .
		RANGEINDEX_nameStartChar.index(hex("0370"), hex("037D")); // .
		RANGEINDEX_nameStartChar.index(hex("037F"), hex("1FFF")); // .
		RANGEINDEX_nameStartChar.index(hex("200C"), hex("200D")); // .
		RANGEINDEX_nameStartChar.index(hex("2070"), hex("218F")); // .
		RANGEINDEX_nameStartChar.index(hex("2C00"), hex("2FEF")); // .
		RANGEINDEX_nameStartChar.index(hex("3001"), hex("D7FF")); // .
		RANGEINDEX_nameStartChar.index(hex("F900"), hex("FDCF")); // .
		RANGEINDEX_nameStartChar.index(hex("FDF0"), hex("FFFD")); // .
		RANGEINDEX_nameChar = new IntegerRangeIndex();
		RANGEINDEX_nameChar.addAll(RANGEINDEX_nameStartChar);
		RANGEINDEX_nameChar.index('-', '-');
		RANGEINDEX_nameChar.index('.', '.');
		RANGEINDEX_nameChar.index('0', '9');
		RANGEINDEX_nameChar.index(hex("B7"), hex("B7"));
		RANGEINDEX_nameChar.index(hex("0300"), hex("036F"));
		RANGEINDEX_nameChar.index(hex("203F"), hex("2040"));
	}

	private static int hex(final String s) {
		return Integer.parseInt(s, 16);
	}

	public static void main(final String[] args) {
		System.out.println(hex("C0"));
		System.out.println(hex("00F8"));
	}

	/*
	 * the XML spec also allows 5-digit unicode characters (\u10000-\uEFFFF) but
	 * Java can't handle them as char datatype is 16-bit only.
	 */

	/**
	 * Notable excludes: ' ;<=>?@[\]^{|}~§°´%!"#$%&'()*+,-./ '
	 *
	 * Notable includes: [-:_äö0-9]
	 *
	 * ':' is used in XML namespaces
	 *
	 * Random XIds are UUIDs which match [-0-9a-z]
	 *
	 * This leaves '_' as the only reasonable escape character
	 * */
	public static final String nameChar = // .
	nameStartChar + // .
			"\\-" // .
			+ "\\."// .
			+ "0-9"// .
			+ "\\xB7"// . the "middle dot"
			+ "\\u0300-\u036F"// .
			+ "\\u203F-\\u2040";

	public static final String nameRegex = "[" + nameStartChar + "][" + nameChar + "]*";

	@Override
	public XId createUniqueId() {
		/* leading 'a' ensures legal XML name */
		return createInstance("a" + UUID.uuid());
	}

	/**
	 * Without any checks.
	 *
	 * @param string
	 * @return
	 */
	protected abstract XId createInstance(String string);

	@Override
	public XAddress fromAddress(final String address) {

		if (address == null) {
			throw new IllegalArgumentException("address may not be null");
		}
		final String[] components = address.split("/");
		// Note: this strips any trailing slashes

		if (components.length > 5) {
			throw new URIFormatException("The address \"" + address
					+ "\" contains too many components.");
		}

		if (components.length < 2 || components[0].length() > 0) {
			throw new URIFormatException("The address \"" + address
					+ "\" does not start with a slash ('/').");
		}

		XId repository = null;
		if (components.length >= 2 && !components[1].equals("-")) {
			repository = fromString(components[1]);
		}

		XId model = null;
		if (components.length >= 3 && !components[2].equals("-")) {
			model = fromString(components[2]);
		}

		XId object = null;
		if (components.length >= 4 && !components[3].equals("-")) {
			object = fromString(components[3]);
		}

		XId field = null;
		if (components.length >= 5 && !components[4].equals("-")) {
			field = fromString(components[4]);
		}

		return fromComponents(repository, model, object, field);
	}

	// FIXME experimental
	private transient MapSetIndex<Integer, XAddress> addressIndex = MapSetIndex
			.createWithFastWeakEntrySets();

	@Override
	public XAddress fromComponents(final XId repositoryId, final XId modelId, final XId objectId, final XId fieldId) {

		final int hash = (repositoryId == null ? 0 : repositoryId.hashCode())
				+ (modelId == null ? 0 : modelId.hashCode())
				+ (objectId == null ? 0 : objectId.hashCode())
				+ (fieldId == null ? 0 : fieldId.hashCode());

		final IEntrySet<XAddress> addressSet = this.addressIndex.lookup(hash);
		if (addressSet != null) {
			final Iterator<XAddress> it = addressSet.iterator();
			while (it.hasNext()) {
				final XAddress addr = it.next();

				if (XX.isSameAddress(addr, repositoryId, modelId, objectId, fieldId)) {
					return addr;
				}
			}
		}

		final XAddress addr = new MemoryAddress(repositoryId, modelId, objectId, fieldId);

		this.addressIndex.index(hash, addr);

		return addr;
	}

	/**
	 * @param s @NeverNull
	 * @return true if valid XId string
	 */
	public static boolean isValidId(final String s) {
		if (s == null) {
			throw new IllegalArgumentException("s is null");
		}
		if (s.length() > XIdProvider.MAX_LENGTH) {
			log.trace("Too long");
			return false;
		}
		if (s.length() == 0) {
			log.trace("Too short");
			return false;
		}
		final int firstCodePoint = s.codePointAt(0);
		final int i = Character.charCount(firstCodePoint);

		if (!RANGEINDEX_nameStartChar.isInInterval(firstCodePoint)) {
			return false;
		}

		return IntegerRangeIndex.isAllCharactersInIntervals(RANGEINDEX_nameChar, s.substring(i));
	}

	/**
	 * Alternative, slower implementation
	 *
	 * @param s
	 * @return true if valid XId string
	 */
	public static boolean isValidId_versionB(final String s) {
		// slower ?
		return MemoryStringIdRegexGwtEmul.matchesXydraId(s);
	}

	@Override
	public XId fromString(final String uriString) {
		if (uriString == null) {
			throw new IllegalArgumentException("'" + uriString + "' is null - cannot create XId");
		}
		if (uriString.length() > XIdProvider.MAX_LENGTH) {
			throw new IllegalArgumentException("'" + uriString + "' is too long (over "
					+ XIdProvider.MAX_LENGTH + ") - cannot create XId");
		}
		if (!isValidId(uriString)) {
			throw new IllegalArgumentException("'" + uriString
					+ "' is not a valid XML name or contains ':', cannot create XId");
		}
		assert !uriString.contains(" ") : "uriString='" + uriString + "'";
		return createInstance(uriString);
	}

}
