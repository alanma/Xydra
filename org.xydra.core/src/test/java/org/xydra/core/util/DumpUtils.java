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
		assert model != null;
		assert model.getAddress().getAddressedType() == XType.XMODEL;
		log.info(label + " ** Model " + model.getAddress() + " [" + model.getRevisionNumber() + "]");
		for(XID objectId : model) {
			XReadableObject object = model.getObject(objectId);
			dump(label, object);
		}
		return "";
	}
	
	/**
	 * @param label to identify the log output
	 * @param object to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableObject object) {
		assert object != null;
		assert object.getAddress().getAddressedType() == XType.XOBJECT;
		log.info(label + " *** Object " + object.getAddress() + " [" + object.getRevisionNumber()
		        + "]");
		for(XID fieldId : object) {
			XReadableField field = object.getField(fieldId);
			dump(label, field);
		}
		return "";
	}
	
	/**
	 * @param label to identify the log output
	 * @param field to be dumped
	 * @return an empty string, so that this method can be used in <code>
	 * assert condition : DumpUtils.dump("mylabel", myentity);
	 * </code>
	 */
	public static String dump(String label, XReadableField field) {
		assert field != null;
		assert field.getAddress().getAddressedType() == XType.XFIELD;
		log.info(label + " **** " + field.getAddress() + " = '" + field.getValue() + "' " + " ["
		        + field.getRevisionNumber() + "]");
		return "";
	}
}
