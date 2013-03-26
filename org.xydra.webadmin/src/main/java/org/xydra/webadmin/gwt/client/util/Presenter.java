package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.ViewModel;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.RepoBranchWidget;


public abstract class Presenter {
    
    private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
    
    /**
     * Creates a new entity in entityAddress
     * 
     * @param entityAddress
     * @param idString the new id
     */
    // TODO move out to some util class
    public static void processInput(XAddress entityAddress, String idString) {
        XAddress newAddress = entityAddress;
        XType addressedType = entityAddress.getAddressedType();
        if(addressedType.equals(XType.XREPOSITORY)) {
            if(entityAddress.getRepository().equals(XX.toId("noRepo"))) {
                /* add a new Repository */
                XId repoId = XX.toId(idString);
                newAddress = XX.toAddress(repoId, null, null, null);
                DataModel.getInstance().addRepoID(repoId);
            } else {
                /* add new Model */
                DataModel.getInstance().addModel(entityAddress.getRepository(), XX.toId(idString));
                XId modelId = XX.toId(idString);
                newAddress = XX.toAddress(entityAddress.getRepository(), modelId, null, null);
                log.info("model " + idString + " added!");
            }
        } else if(addressedType.equals(XType.XMODEL)) {
            DataModel.getInstance().addObject(entityAddress, XX.toId(idString));
        } else if(addressedType.equals(XType.XOBJECT)) {
            XAddress fieldAddress = XX.resolveField(entityAddress, XX.toId(idString));
            DataModel.getInstance().addField(fieldAddress, null);
        }
        ViewModel.getInstance().openLocation(newAddress);
        Controller.getInstance().present();
    }
    
}
