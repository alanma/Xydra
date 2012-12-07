package org.xydra.store;

import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.sharedutils.XyAssert;


/**
 * This class contains the naming conventions used to add access rights control
 * and account management to Xydra.
 * 
 * @author xamde
 * 
 *         Constants: 'internal--rights-+{modelId}' ...
 * 
 *         'internal--index-'+{modelId} .... erbt rechts vom entsprechenden
 *         model cannot be read/written from outside --> let the impl handl this
 * 
 *         Model "phonebook" Daten
 * 
 *         Model "internal--rights-phonebook" Wer darf "phonebook" ändern? Wer
 *         darf "rechte-phonebook" lesen oder ändern?
 * 
 *         Model "internal--accounts" Acounts Gruppen Wer ist in welcher Gruppe?
 * 
 *         Model "internal--rights-internal--accounts" Wer darf acounts anlegen?
 *         Wer darf Gruppen ändern? ...
 * 
 *         Wer darf auf "internal-rights-accounts" zugreifen (lesen/schreiben)?
 * 
 * 
 *         === Algo für Anlegen eines Models Wer darf initial lesen/schreiben?
 *         Wer setzt diese Rechte?
 * 
 *         Policy Wer ein Model anlegt, dem gehört es allein
 * 
 */
/**
 * @author xamde
 * 
 */
public class NamingUtils {
	
	public static final String PREFIX_INTERNAL = "internal";
	
	public static final String NAMESPACE_SEPARATOR = "--";
	
	public static final String ENCODING_SEPARATOR = "_.";
	
	/**
	 * Default ID for the default model for accounts, created by store
	 * implementation: {@value}
	 */
	public static final XID ID_AUTHENTICATION_MODEL = XX.toId(PREFIX_INTERNAL + NAMESPACE_SEPARATOR
	        + "authentication");
	
	/**
	 * Default ID for the default model for groups, created by store
	 * implementation: {@value}
	 */
	public static final XID ID_GROUPS_MODEL = XX.toId(PREFIX_INTERNAL + NAMESPACE_SEPARATOR
	        + "groups");
	
	/**
	 * Default ID for the default model for the global authorisation model,
	 * created by store implementation: {@value}
	 */
	public static final XID ID_REPO_AUTHORISATION_MODEL = XX.toId(PREFIX_INTERNAL
	        + NAMESPACE_SEPARATOR + "repositoryRights");
	
	private static final String NULL_ENCODED = "_N";
	
	public static final String PREFIX_INDEX_ID = PREFIX_INTERNAL + NAMESPACE_SEPARATOR + "index-";
	
	public static final String PREFIX_RIGHTS_ID = PREFIX_INTERNAL + NAMESPACE_SEPARATOR + "rights-";
	
	/**
	 * @param encodedXAddress created via {@link #encode(XAddress)}
	 * @return the decoded XAddress or null if the encodedXAddress represents
	 *         null
	 * @throws IllegalArgumentException if decoding fails
	 */
	public static XAddress decodeXAddress(String encodedXAddress) {
		if(encodedXAddress.equals(NULL_ENCODED)) {
			return null;
		} else {
			String[] encParts = encodedXAddress.split("_\\.");
			if(encParts.length != 4) {
				throw new IllegalArgumentException("Encoded address consits not of four parts: "
				        + encodedXAddress);
			}
			return X.getIDProvider().fromComponents(decodeXid(encParts[0]), decodeXid(encParts[1]),
			        decodeXid(encParts[2]), decodeXid(encParts[3]));
		}
	}
	
	/**
	 * Decode back to an XID
	 * 
	 * @param encodedXid a string created via {@link #encode(XID)}
	 * @return an XID or null (if the string represented the null XID)
	 * @throws IllegalArgumentException if decoding fails
	 */
	public static XID decodeXid(String encodedXid) {
		String decoded = decodeXidString(encodedXid);
		if(decoded == null) {
			return null;
		} else {
			return XX.toId(decoded);
		}
	}
	
	/**
	 * @param encodedXid
	 * @return the decoded XID string
	 * @throws IllegalArgumentException if decoding fails
	 */
	private static String decodeXidString(String encodedXid) {
		if(encodedXid == null) {
			throw new IllegalArgumentException("string is null");
		}
		if(encodedXid.equals(NULL_ENCODED))
			return null;
		else
			return encodedXid.replace("_U", "_");
	}
	
	/**
	 * Encode address as XID string
	 * 
	 * @param address may be null
	 * @return a string that can be used as an XID, encoding the given address
	 */
	public static String encode(XAddress address) {
		if(address == null) {
			return "_N";
		} else {
			return encode(address.getRepository()) + ENCODING_SEPARATOR
			        + encode(address.getModel()) + ENCODING_SEPARATOR + encode(address.getObject())
			        + ENCODING_SEPARATOR + encode(address.getField());
		}
	}
	
	/**
	 * Encode XID to be used in a string, can handle nulls.
	 * 
	 * @param xid may be null
	 * @return a string to be used as an XID or part thereof
	 */
	public static String encode(XID xid) {
		if(xid == null) {
			return NULL_ENCODED;
		} else {
			return encodeNonNullString(xid.toString());
		}
	}
	
	/** now we can safely use "_." as a separator */
	private static String encodeNonNullString(String s) {
		XyAssert.xyAssert(s != null); assert s != null;
		return s.replace("_", "_U");
	}
	
	private static String encodeXid(String xid) {
		if(xid == null) {
			return NULL_ENCODED;
		} else {
			return encodeNonNullString(xid);
		}
	}
	
	/**
	 * @param indexModelId which has been created via
	 *            {@link #getIndexModelId(XID, String)}
	 * @return the base model on which this index model is based. Note: Several
	 *         indexes can be based on the same base model.
	 */
	public static XID getBaseModelIdForIndexModelId(XID indexModelId) {
		String modelIdStr = parseIndexModelId(indexModelId)[0];
		XID modelId = XX.toId(modelIdStr);
		return modelId;
	}
	
	/***
	 * An index model is a derived model that cannot be modified from outside
	 * and should be updated only be the server-side implementation that created
	 * it. Clients may have the rights to read these models.
	 * 
	 * Read-rights for an index model are inherited from the corresponding base
	 * model.
	 * 
	 * @param modelId XID of model
	 * @param indexName may not be null and not be the empty string.
	 * @return an XID in the internal name-space following the name-space
	 *         conventions for <em>index models</em>.
	 */
	public static XID getIndexModelId(XID modelId, String indexName) {
		if(indexName == null || indexName.length() == 0) {
			throw new IllegalArgumentException("Indexname may not be null and not the empty string");
		}
		return XX.toId(PREFIX_INDEX_ID + encode(modelId) + ENCODING_SEPARATOR
		        + encodeXid(indexName));
	}
	
	/**
	 * @param indexModelId which has been created via
	 *            {@link #getIndexModelId(XID, String)}
	 * @return the index name of this index. Different index models for a given
	 *         base model must have different index names.
	 */
	public static String getIndexNameForIndexModelId(XID indexModelId) {
		return parseIndexModelId(indexModelId)[1];
	}
	
	public static XID getRightsModelId(XID modelId) {
		return XX.toId(PREFIX_RIGHTS_ID + modelId.toString());
	}
	
	public static boolean isRightsModelId(XID modelId) {
		return modelId.toString().startsWith(PREFIX_RIGHTS_ID);
	}
	
	/**
	 * @param indexModelId
	 * @return { modelId as String, indexName as String }
	 */
	private static String[] parseIndexModelId(XID indexModelId) {
		String s = indexModelId.toString();
		if(s.length() <= PREFIX_INDEX_ID.length()) {
			throw new IllegalArgumentException(
			        "Given indexModelId is too short. It should start with '" + PREFIX_INDEX_ID
			                + "'");
		}
		s = s.substring(PREFIX_INDEX_ID.length());
		// now s = enc(modelId) + '_.' + enc(indexName)
		String[] parts = s.split("\\_\\.");
		return new String[] { decodeXidString(parts[0]), decodeXidString(parts[1]) };
	}
	
}
