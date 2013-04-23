package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;


public abstract class Presenter {
	
	private static final Logger log = LoggerFactory.getLogger(Presenter.class);
	
	/**
	 * Creates a new entity in entityAddress
	 * 
	 * @param entityAddress
	 * @param idString the new id
	 */
	public static void processUserInput(XAddress entityAddress, String idString) {
		XAddress newAddress = entityAddress;
		XType addressedType = entityAddress.getAddressedType();
		if(addressedType.equals(XType.XREPOSITORY)) {
			if(entityAddress.getRepository().equals(XX.toId("noRepo"))) {
				/* add a new Repository */
				XId repoId = XX.toId(idString);
				newAddress = XX.toAddress(repoId, null, null, null);
				XyAdmin.getInstance().getModel().addRepoID(repoId);
			} else {
				/* add new Model */
				XyAdmin.getInstance().getModel()
				        .addModel(entityAddress.getRepository(), XX.toId(idString));
				XId modelId = XX.toId(idString);
				newAddress = XX.toAddress(entityAddress.getRepository(), modelId, null, null);
				log.info("attempting to add model " + idString);
			}
		} else if(addressedType.equals(XType.XMODEL)) {
			log.info("attempting to add object " + idString);
			XyAdmin.getInstance().getModel().addObject(entityAddress, XX.toId(idString));
		} else if(addressedType.equals(XType.XOBJECT)) {
			XAddress fieldAddress = XX.resolveField(entityAddress, XX.toId(idString));
			XyAdmin.getInstance().getModel().addField(fieldAddress, null);
			log.info("attempting to add field " + idString);
		}
		XyAdmin.getInstance().getViewModel().openLocation(newAddress);
	}
	
	public void remove(XAddress address) {
		XyAdmin.getInstance().getModel().removeItem(address);
	}
	
	public void remove(XAddress address, Boolean removeFromRepo) {
		remove(address);
		if(removeFromRepo) {
			log.info("selected Delete model");
			XyAdmin.getInstance().getController().removeModel(address);
		}
		
	}
	
}