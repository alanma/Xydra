package org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.RepoBranchPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;


/**
 * Performs all the logic for {@link ModelBranchWidget}s. So it:
 * 
 * <ul>
 * <li>triggers the presentation of models in the {@link EditorPanel}
 * <li>triggers requests to add objects
 * <li>triggers the removal of the underlying model (locally and if demanded
 * from the persistence)
 * <li>has listeners for {@link ModelChangedEvent}s
 * </ul>
 * 
 * The listeners react, when
 * <ul>
 * <li>a model with the appropriate address is indexed (fetched from the server
 * and locally indexed),
 * <li>when the model is removed and,
 * <li>when the model is extended by an object (asserts the model is presented
 * then)
 * </ul>
 * 
 * @author Andi_Ka
 * 
 */
public class ModelBranchPresenter extends SelectionTreePresenter {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private XAddress modelAddress;
	private IModelBranchWidget widget;
	boolean expanded = false;
	
	private RepoBranchPresenter presenter;
	
	private HandlerRegistration registration;
	
	private HandlerRegistration repoChangeRegistration;
	
	public ModelBranchPresenter(RepoBranchPresenter repoPresenter, XAddress address) {
		this.presenter = repoPresenter;
		this.modelAddress = address;
		
	}
	
	protected void processChanges(XAddress modelAddress, final EntityStatus status, XId info) {
		switch(status) {
		case CHANGED:
			// doesn't happen...
			break;
		case DELETED:
			this.widget.delete();
			this.presenter.removeRegistration(this.registration);
			this.registration.removeHandler();
			break;
		case INDEXED:
			if(info.equals(XX.toId("removed"))) {
				this.widget.setStatusDeleted();
				this.repoChangeRegistration = EventHelper.addRepoChangeListener(
				        XX.toAddress(modelAddress.getRepository(), null, null, null),
				        new IRepoChangedEventHandler() {
					        
					        @Override
					        public void onRepoChange(RepoChangedEvent event) {
						        if(event.getStatus().equals(EntityStatus.EXTENDED)) {
							        if(!XyAdmin
							                .getInstance()
							                .getModel()
							                .getRepo(
							                        ModelBranchPresenter.this.modelAddress
							                                .getRepository())
							                .isNotExisting(
							                        ModelBranchPresenter.this.modelAddress
							                                .getModel())) {
								        ModelBranchPresenter.this.reset();
							        }
						        }
						        ModelBranchPresenter.this.repoChangeRegistration.removeHandler();
						        
					        }
				        });
				this.presenter.addRegistration(this.repoChangeRegistration);
			} else {
				long revisionNumber = XyAdmin.getInstance().getModel()
				        .getRepo(modelAddress.getRepository()).getModel(modelAddress.getModel())
				        .getRevisionNumber();
				this.widget.setRevisionNumber(revisionNumber);
			}
			break;
		case EXTENDED:
			if(!this.modelAddress.equals(XyAdmin.getInstance().getController()
			        .getCurrentlyOpenedModelAddress())) {
				this.presentModel();
			}
			break;
		default:
			break;
		
		}
		
	}
	
	protected void reset() {
		this.widget.setRevisionNumber(XyAdmin.getInstance().getModel()
		        .getRepo(this.modelAddress.getRepository()).getModel(this.modelAddress.getModel())
		        .getRevisionNumber());
		this.widget.removeStatusDeleted();
		
	}
	
	public void openAddElementDialog(String string) {
		super.openAddElementDialog(this.modelAddress, string);
		
	}
	
	private void checkStatus() {
		if(XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
		        .isNotExisting(this.modelAddress.getModel())) {
			this.widget.setStatusDeleted();
		}
		if(!XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
		        .isAddedModel(this.modelAddress.getModel())) {
			if(!XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
			        .getModel(this.modelAddress.getModel()).knowsAllObjects()) {
				
				this.widget.setRevisionUnknown();
			}
		}
	}
	
	public void presentModel() {
		XyAdmin.getInstance().getController().presentModel(this.modelAddress);
		
	}
	
	public IModelBranchWidget presentWidget() {
		this.widget = new ModelBranchWidget(this);
		
		ClickHandler anchorClickHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				ModelBranchPresenter.this.presentModel();
			}
		};
		
		this.widget.init(this, this.modelAddress, anchorClickHandler);
		this.checkStatus();
		
		this.registration = EventHelper.addModelChangedListener(this.modelAddress,
		        new IModelChangedEventHandler() {
			        
			        @Override
			        public void onModelChange(ModelChangedEvent event) {
				        ModelBranchPresenter.this.processChanges(event.getModelAddress(),
				                event.getStatus(), event.getMoreInfos());
			        }
		        });
		this.presenter.addRegistration(this.registration);
		return this.widget.asWidget();
	}
}
