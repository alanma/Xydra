package org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.tableWidgets.EntityWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class ModelBranchWidget extends Composite implements IModelBranchWidget {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ModelBranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelBranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField(provided = true)
	EntityWidget entityWidget;
	private ModelBranchPresenter presenter;
	
	public ModelBranchWidget(XAddress address) {
		
		this.presenter = new ModelBranchPresenter(address, this);
		
	}
	
	public void init(XAddress address, ClickHandler anchorClickHandler) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.entityWidget = new EntityWidget(this.presenter, address, anchorClickHandler);
		
		this.entityWidget.setDeleteModelDialog();
	}
	
	@Override
	public void setStatusDeleted() {
		this.entityWidget.setStatusDeleted();
	}
	
	@Override
	public void setRevisionUnknown() {
		this.entityWidget.setRevisionUnknown();
		
	}
	
	@Override
	public void delete() {
		this.removeFromParent();
		this.presenter = null;
	}
	
	@Override
	public void setRevisionNumber(long revisionNumber) {
		this.entityWidget.setRevisionNumber(revisionNumber);
		
	}
}
