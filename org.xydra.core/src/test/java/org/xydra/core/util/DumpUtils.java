package org.xydra.core.util;

import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * @author xamde
 * 
 */
public class DumpUtils {
	
	private static final Logger log = LoggerFactory.getLogger(DumpUtils.class);
	
	/**
	 * @param label to identify the log output
	 * @param repo to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableRepository repo) {
		assert repo != null;
		assert repo.getAddress().getAddressedType() == XType.XREPOSITORY;
		log.info(label + " * Repo " + repo.getID() + " ...");
		for(XID modelId : repo) {
			XReadableModel model = repo.getModel(modelId);
			dump(label, model);
		}
		return "";
	}
	
	/**
	 * @param label to identify the log output
	 * @param model to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableModel model) {
		log.info(label + " ** Model " + toStringBuffer(model));
		return "";
	}
	
	/**
	 * @param model to be dumped to a String
	 * @return the model as a human-readable String
	 */
	public static StringBuffer toStringBuffer(XReadableModel model) {
		assert model != null;
		assert model.getAddress().getAddressedType() == XType.XMODEL;
		StringBuffer buf = new StringBuffer();
		buf.append(" ** Model " + model.getAddress() + " [" + model.getRevisionNumber() + "]");
		for(XID objectId : model) {
			XReadableObject object = model.getObject(objectId);
			buf.append(toStringBuffer(object));
		}
		return buf;
	}
	
	/**
	 * @param label to identify the log output
	 * @param object to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableObject object) {
		log.info(label + toStringBuffer(object));
		return "";
	}
	
	/**
	 * @param object to be dumped
	 * @return given object as human-readable string
	 */
	public static StringBuffer toStringBuffer(XReadableObject object) {
		assert object != null;
		assert object.getAddress().getAddressedType() == XType.XOBJECT;
		StringBuffer buf = new StringBuffer();
		buf.append("*** Object " + object.getAddress() + " [" + object.getRevisionNumber() + "]\n");
		for(XID fieldId : object) {
			XReadableField field = object.getField(fieldId);
			buf.append(toStringBuffer(field));
		}
		return buf;
	}
	
	/**
	 * @param label to identify the log output
	 * @param field to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableField field) {
		log.info(label + toStringBuffer(field));
		return "";
	}
	
	/**
	 * @param field to be dumped
	 * @return the field as a human-readable String
	 */
	public static StringBuffer toStringBuffer(XReadableField field) {
		assert field != null;
		assert field.getAddress().getAddressedType() == XType.XFIELD;
		StringBuffer buf = new StringBuffer();
		buf.append(" **** " + field.getAddress() + " = '" + field.getValue() + "' " + " ["
		        + field.getRevisionNumber() + "]\n");
		return buf;
	}
}
