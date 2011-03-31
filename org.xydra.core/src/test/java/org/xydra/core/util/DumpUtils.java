package org.xydra.core.util;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;


public class DumpUtils {
	
	public static void dump(XReadableRepository repo) {
		System.out.println("*****************");
		System.out.println("* Repo " + repo.getID());
		for(XID modelId : repo) {
			XReadableModel model = repo.getModel(modelId);
			dump(model);
		}
	}
	
	public static void dump(XReadableModel model) {
		System.out.println("* Model " + model.getAddress());
		for(XID objectId : model) {
			XReadableObject object = model.getObject(objectId);
			dump(object);
		}
	}
	
	public static void dump(XReadableObject object) {
		System.out.println("* Object " + object.getAddress());
		for(XID fieldId : object) {
			XReadableField field = object.getField(fieldId);
			dump(field);
		}
	}
	
	public static void dump(XReadableField field) {
		System.out.println("* " + field.getAddress() + " = " + field.getValue());
	}
}
