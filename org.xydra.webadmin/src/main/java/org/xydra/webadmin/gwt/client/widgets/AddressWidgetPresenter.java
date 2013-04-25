package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent;
import org.xydra.webadmin.gwt.client.events.ViewBuiltEvent.IViewBuiltHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;

import com.google.gwt.event.shared.HandlerRegistration;


public class AddressWidgetPresenter extends Presenter {
	
	private static final Logger log = LoggerFactory.getLogger(AddressWidgetPresenter.class);
	
	public interface CompoundActionCallback {
		
		void presentModelAndContinue();
		
		void presentObjects();
		
		void executeCommand();
	}
	
	@SuppressWarnings("unused")
	private AddressWidget widget;
	private HandlerRegistration currentPresentingRegistration;
	protected HandlerRegistration currentChangeRegistration;
	
	public AddressWidgetPresenter(AddressWidget addressWidget) {
		this.widget = addressWidget;
		XyAdmin.getInstance().getController().registerAddressWidgetPresenter(this);
	}
	
	/**
	 * loads model from repository
	 * 
	 * @param desiredAddress
	 */
	void openAddress(final XAddress desiredAddress) {
		log.info("ordered to open address " + desiredAddress.toString());
		// TODO handle case where model only exists locally
		// TODO new rows don't have background color...
		
		XId repoID = desiredAddress.getRepository();
		XAddress repoAddress = XX.resolveRepository(repoID);
		
		CompoundActionCallback openModelCallback = null;
		if(desiredAddress.getModel() != null) {
			openModelCallback = new CompoundActionCallback() {
				
				@Override
				public void presentObjects() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void presentModelAndContinue() {
					CompoundActionCallback showObjectCallback = null;
					if(desiredAddress.getObject() != null) {
						showObjectCallback = new CompoundActionCallback() {
							
							@Override
							public void presentObjects() {
								AddressWidgetPresenter.showObjectAndField(desiredAddress);
							}
							
							@Override
							public void presentModelAndContinue() {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void executeCommand() {
								// TODO Auto-generated method stub
								
							}
						};
						
					}
					showModel(desiredAddress, showObjectCallback);
					
				}
				
				@Override
				public void executeCommand() {
					// TODO Auto-generated method stub
					
				}
			};
		}
		showRepository(repoAddress, openModelCallback);
	}
	
	private void showRepository(final XAddress repoAddress,
	        final CompoundActionCallback compoundActionCallback) {
		if(XyAdmin.getInstance().getModel().getRepo(repoAddress.getRepository()) != null) {
			boolean alreadyOpened = XyAdmin.getInstance().getController()
			        .getSelectionTreePresenter().showRepository(repoAddress);
			if(!alreadyOpened) {
				this.currentPresentingRegistration = EventHelper.addViewBuildListener(repoAddress,
				        new IViewBuiltHandler() {
					        
					        @Override
					        public void onViewBuilt(ViewBuiltEvent event) {
						        
						        AddressWidgetPresenter.this.currentPresentingRegistration
						                .removeHandler();
						        if(compoundActionCallback != null) {
							        log.info("proceeding execution of request after "
							                + repoAddress.toString() + " was presented!");
							        compoundActionCallback.presentModelAndContinue();
						        }
					        }
				        });
			} else {
				
				log.info("proceeding execution of request, " + repoAddress.toString()
				        + " already existed! Firing ViewBuiltEvent");
				EventHelper.fireViewBuiltEvent(repoAddress);
				if(compoundActionCallback != null) {
					compoundActionCallback.presentModelAndContinue();
				}
				
			}
			
		} else {
			@SuppressWarnings("unused")
			WarningDialog dialog = new WarningDialog("repository " + repoAddress.getRepository()
			        + " does not exist!");
		}
	}
	
	private void showModel(XAddress desiredAddress, final CompoundActionCallback showObjectCallback) {
		RepoDataModel repo = XyAdmin.getInstance().getModel()
		        .getRepo(desiredAddress.getRepository());
		if(repo.getModel(desiredAddress.getModel()) != null) {
			log.info("starting to present model " + desiredAddress.toString());
			boolean alreadyPresented = false;
			
			Controller controller = XyAdmin.getInstance().getController();
			XAddress resolvedModelAddress = XX.resolveModel(desiredAddress);
			SessionCachedModel model = repo.getModel(desiredAddress.getModel());
			if(model.knowsAllObjects()) {
				if(controller.getCurrentlyOpenedModelAddress().equals(
				        XX.resolveModel(desiredAddress))) {
					log.info("model was already open and therefore doesn't need to be presented again... - firing ViewBuildEvent!");
					alreadyPresented = true;
					EventHelper.fireViewBuiltEvent(resolvedModelAddress);
				} else {
					log.info("model was already locally loaded - just presenting the local storage contents!");
					controller.presentModel(resolvedModelAddress);
					alreadyPresented = true;
				}
			} else {
				controller.presentModel(resolvedModelAddress);
				controller.loadModelsObjects(resolvedModelAddress);
				alreadyPresented = false;
			}
			
			if(alreadyPresented) {
				if(showObjectCallback != null) {
					showObjectCallback.presentObjects();
				}
			} else {
				final XAddress modelAddress = XX.resolveModel(desiredAddress);
				this.currentPresentingRegistration = EventHelper.addViewBuildListener(modelAddress,
				        new IViewBuiltHandler() {
					        
					        @Override
					        public void onViewBuilt(ViewBuiltEvent event) {
						        
						        AddressWidgetPresenter.this.currentPresentingRegistration
						                .removeHandler();
						        if(showObjectCallback != null) {
							        log.info("proceeding execution of request after "
							                + modelAddress.toString() + " was presented!");
							        showObjectCallback.presentObjects();
						        }
						        
					        }
				        });
			}
		} else {
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog("model " + desiredAddress.toString()
			        + " does not exist!");
			Controller.showDefaultCursor();
		}
		
	}
	
	protected static void showObjectAndField(XAddress desiredAddress) {
		if(XyAdmin.getInstance().getModel().getRepo(desiredAddress.getRepository())
		        .getModel(desiredAddress.getModel()).getObject(desiredAddress.getObject()) != null) {
			XyAdmin.getInstance().getController().getEditorPanelPresenter()
			        .showObjectAndField(desiredAddress);
		} else {
			@SuppressWarnings("unused")
			WarningDialog dialog = new WarningDialog("object "
			        + desiredAddress.getObject().toString() + " does not exist!");
		}
	}
	
	@SuppressWarnings("static-access")
	public void addEntity(final XAddress desiredAddress) {
		log.info("requested to add address " + desiredAddress.toString());
		
		switch(desiredAddress.getAddressedType()) {
		case XREPOSITORY:
			XId repoId = desiredAddress.getRepository();
			this.processUserInput(XX.toAddress("/noRepo"), repoId.toString());
			break;
		case XMODEL:
			final XAddress repoAddress = XX.toAddress(desiredAddress.getRepository(), null, null,
			        null);
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(repoAddress, new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        AddressWidgetPresenter.this.currentChangeRegistration.removeHandler();
					        AddressWidgetPresenter.this.processUserInput(repoAddress,
					                desiredAddress.getModel().toString());
				        }
			        });
			this.openAddress(repoAddress);
			
			break;
		case XOBJECT:
			final XAddress modelAddress = desiredAddress.getParent();
			
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(modelAddress, new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        AddressWidgetPresenter.this.currentChangeRegistration.removeHandler();
					        AddressWidgetPresenter.this.processUserInput(modelAddress,
					                desiredAddress.getObject().toString());
				        }
			        });
			this.openAddress(modelAddress);
			break;
		case XFIELD:
			final XAddress objectAddress = desiredAddress.getParent();
			
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(XX.resolveModel(desiredAddress), new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        if(XyAdmin.getInstance().getModel()
					                .getRepo(objectAddress.getRepository())
					                .getModel(desiredAddress.getModel())
					                .getObject(desiredAddress.getObject()) != null) {
						        
						        AddressWidgetPresenter.this.currentChangeRegistration
						                .removeHandler();
						        AddressWidgetPresenter.this.processUserInput(objectAddress,
						                desiredAddress.getField().toString());
					        } else {
						        @SuppressWarnings("unused")
						        WarningDialog warning = new WarningDialog("object "
						                + objectAddress.getObject().toString()
						                + " does not exist in "
						                + objectAddress.getParent().toString());
					        }
				        }
			        });
			this.openAddress(XX.resolveModel(desiredAddress));
			
			break;
		default:
			break;
		
		}
	}
	
	public void unregisterAllListeners() {
		if(this.currentChangeRegistration != null)
			this.currentChangeRegistration.removeHandler();
		if(this.currentPresentingRegistration != null)
			this.currentPresentingRegistration.removeHandler();
	}
	
	public void removeEntity(final XAddress address) {
		log.info("requested to remove address " + address.toString());
		
		switch(address.getAddressedType()) {
		case XREPOSITORY:
			@SuppressWarnings("unused")
			WarningDialog warning = new WarningDialog("not implemented!");
			break;
		case XMODEL:
			final XAddress repoAddress = XX.toAddress(address.getRepository(), null, null, null);
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(repoAddress, new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        
					        AddressWidgetPresenter.this.currentChangeRegistration.removeHandler();
					        AddressWidgetPresenter.this.remove(address);
				        }
			        });
			this.openAddress(repoAddress);
			
			break;
		case XOBJECT:
			final XAddress modelAddress = address.getParent();
			
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(modelAddress, new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        AddressWidgetPresenter.this.currentChangeRegistration.removeHandler();
					        AddressWidgetPresenter.this.remove(address);
				        }
			        });
			this.openAddress(modelAddress);
			break;
		case XFIELD:
			final XAddress objectAddress = address.getParent();
			
			AddressWidgetPresenter.this.currentChangeRegistration = EventHelper
			        .addViewBuildListener(XX.resolveModel(address), new IViewBuiltHandler() {
				        
				        @Override
				        public void onViewBuilt(ViewBuiltEvent event) {
					        if(XyAdmin.getInstance().getModel()
					                .getRepo(objectAddress.getRepository())
					                .getModel(address.getModel()).getObject(address.getObject()) != null) {
						        
						        AddressWidgetPresenter.this.currentChangeRegistration
						                .removeHandler();
						        AddressWidgetPresenter.this.remove(address);
					        } else {
						        @SuppressWarnings("unused")
						        WarningDialog warning = new WarningDialog("object "
						                + objectAddress.getObject().toString()
						                + " does not exist in "
						                + objectAddress.getParent().toString());
					        }
				        }
			        });
			this.openAddress(XX.resolveObject(address));
			
			break;
		default:
			break;
		
		}
		
	}
}
