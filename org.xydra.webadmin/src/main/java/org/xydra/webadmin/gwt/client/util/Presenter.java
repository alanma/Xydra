package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

public abstract class Presenter {

	private static final Logger log = LoggerFactory.getLogger(Presenter.class);

	/**
	 * Creates a new entity in entityAddress
	 *
	 * @param entityAddress
	 * @param idString
	 *            the new id
	 */
	public static void processUserInput(final XAddress entityAddress, final String idString) {
		final XType addressedType = entityAddress.getAddressedType();
		if (addressedType.equals(XType.XREPOSITORY)) {
			if (entityAddress.getRepository().equals(Base.toId("noRepo"))) {
				/* add a new Repository */
				final XId repoId = Base.toId(idString);
				XyAdmin.getInstance().getModel().addRepoID(repoId);
			} else {
				/* add new Model */
				XyAdmin.getInstance().getModel()
						.addModel(entityAddress.getRepository(), Base.toId(idString));
				log.info("attempting to add model " + idString);
			}
		} else if (addressedType.equals(XType.XMODEL)) {
			log.info("attempting to add object " + idString);
			XyAdmin.getInstance().getModel().addObject(entityAddress, Base.toId(idString));
		} else if (addressedType.equals(XType.XOBJECT)) {
			final XAddress fieldAddress = Base.resolveField(entityAddress, Base.toId(idString));
			XyAdmin.getInstance().getModel().addField(fieldAddress, null);
			log.info("attempting to add field " + idString);
		}
	}

	public void remove(final XAddress address) {
		XyAdmin.getInstance().getModel().removeItem(address);
	}

	public void remove(final XAddress address, final Boolean removeFromRepo) {
		remove(address);
		if (removeFromRepo) {
			log.info("selected Delete model");
			XyAdmin.getInstance().getController().removeModel(address);
		}

	}

}
