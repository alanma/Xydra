package org.xydra.core.util;

import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;


public class DumpUtils {
	
	public static String dump(String label, XReadableRepository repo) {
		assert repo != null;
		assert repo.getAddress().getAddressedType() == XType.XREPOSITORY;
		System.out.println(label + " * Repo " + repo.getID() + " ...");
		for(XID modelId : repo) {
			XReadableModel model = repo.getModel(modelId);
			dump(label, model);
		}
		return "";
	}
	
	public static String dump(String label, XReadableModel model) {
		assert model != null;
		assert model.getAddress().getAddressedType() == XType.XMODEL;
		System.out.println(label + " ** Model " + model.getAddress());
		for(XID objectId : model) {
			XReadableObject object = model.getObject(objectId);
			dump(label, object);
		}
		return "";
	}
	
	public static String dump(String label, XReadableObject object) {
		assert object != null;
		assert object.getAddress().getAddressedType() == XType.XOBJECT;
		System.out.println(label + " *** Object " + object.getAddress());
		for(XID fieldId : object) {
			XReadableField field = object.getField(fieldId);
			dump(label, field);
		}
		return "";
	}
	
	public static String dump(String label, XReadableField field) {
		assert field != null;
		assert field.getAddress().getAddressedType() == XType.XFIELD;
		System.out.println(label + " **** " + field.getAddress() + " = '" + field.getValue() + "'");
		return "";
	}
}
