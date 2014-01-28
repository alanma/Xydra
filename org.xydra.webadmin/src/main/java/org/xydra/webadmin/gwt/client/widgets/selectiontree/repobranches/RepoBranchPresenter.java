package org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches.ModelBranchPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches.ModelBranchWidget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HandlerRegistration;


/**
 * Performs all the logic for {@link RepoBranchWidget}s.
 * 
 * So it
 * 
 * <ul>
 * <li>fetches Model IDs from the server (and deactivates the option afterwards)
 * <li>adds new Models
 * <li>builds {@link ModelBranchWidget}s
 * <li>registers listeners for {@link RepoChangedEvent}s which
 * </ul>
 * 
 * The listeners react, when
 * <ul>
 * <li>models from the persistence are indexed (IDs fetched from the server and
 * locally indexed) and
 * <li>when a new model is created
 * </ul>
 * 
 * @author Andi_Ka
 * 
 */
public class RepoBranchPresenter extends SelectionTreePresenter {
	
	private static final Logger log = LoggerFactory.getLogger(RepoBranchPresenter.class);
	
	private XAddress repoAddress;
	private HashMap<XId,ModelBranchPresenter> existingBranches;
	private IRepoBranchWidget widget;
	boolean expanded = false;
	private Set<HandlerRegistration> registrations = new HashSet<HandlerRegistration>();
	
	public RepoBranchPresenter(XAddress address) {
		this.repoAddress = address;
		
	}
	
	public RepoBranchWidget presentWidget() {
		log.info("presenting repoBranch for " + this.repoAddress.toString());
		this.widget = new RepoBranchWidget(this);
		this.widget.init();
		build();
		
		EventHelper.addRepoChangeListener(this.repoAddress, new IRepoChangedEventHandler() {
			
			public void onRepoChange(RepoChangedEvent event) {
				processRepoDataChanges(event.getStatus());
			}
			
		});
		log.info("new repoBranchPresenter - Handler build!");
		
		return this.widget.asWidget();
	}
	
	private void processRepoDataChanges(EntityStatus status) {
		if(status.equals(EntityStatus.INDEXED)) {
			this.collapse();
			this.expand();
		} else if(status.equals(EntityStatus.EXTENDED)) {
			if(!this.expanded) {
				this.expand();
			} else {
				Iterator<XId> iterator = XyAdmin.getInstance().getModel()
				        .getLocallyStoredModelIDs(this.repoAddress);
				while(iterator.hasNext()) {
					XId xId = (XId)iterator.next();
					if(!this.existingBranches.containsKey(xId)) {
						this.addModelBranch(xId);
					}
				}
			}
		}
		
	}
	
	private void build() {
		
		XId id = this.repoAddress.getRepository();
		this.widget.setAnchorText(id.toString());
		
		Iterator<XId> iterator = XyAdmin.getInstance().getModel()
		        .getLocallyStoredModelIDs(this.repoAddress);
		
		buildModelBranches(iterator);
		
	}
	
	private void buildModelBranches(Iterator<XId> iterator) {
		
		while(iterator.hasNext()) {
			XId modelId = iterator.next();
			if(!this.existingBranches.keySet().contains(modelId)) {
				
				addModelBranch(modelId);
			}
		}
	}
	
	private void addModelBranch(XId modelId) {
		XAddress modelAddress = XX.resolveModel(this.repoAddress, modelId);
		ModelBranchPresenter newBranch = new ModelBranchPresenter(this, modelAddress);
		this.widget.addBranch((ModelBranchWidget)newBranch.presentWidget());
		// XyAdmin.getInstance().getModel().getRepo(this.repoAddress.getRepository())
		// .getModel(modelId).getRevisionNumber();
		
		this.existingBranches.put(modelId, newBranch);
	}
	
	void handleExpand(IRepoBranchWidget repoBranchWidget) {
		if(this.expanded) {
			collapse();
		} else {
			expand();
		}
	}
	
	void expand() {
		this.existingBranches = new HashMap<XId,ModelBranchPresenter>();
		this.build();
		this.widget.setExpandButtonText("-");
		this.expanded = true;
		
		log.info("repoWidget for " + this.repoAddress.toString()
		        + " built, now firing viewBuilt-Event!");
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				EventHelper.fireViewBuiltEvent(RepoBranchPresenter.this.repoAddress);
			}
		});
		
	}
	
	void collapse() {
		this.widget.clearBranches();
		this.existingBranches = null;
		this.widget.setExpandButtonText("+");
		
		this.unregistrateAllHandlers();
		
		this.expanded = false;
	}
	
	public void fetchModels() {
		XyAdmin.getInstance().getController().fetchModelIds(this.repoAddress);
		this.collapse();
		this.widget.deActivateFetchChilds();
	}
	
	public void openAddElementDialog(String string) {
		super.openAddElementDialog(this.repoAddress, string);
		
	}
	
	public void addRegistration(HandlerRegistration handler) {
		this.registrations.add(handler);
	}
	
	public void unregistrateAllHandlers() {
		for(HandlerRegistration handler : this.registrations) {
			handler.removeHandler();
			
			handler = null;
		}
		this.registrations.clear();
		log.info("unregistrated all handlers!");
	}
	
	public void removeRegistration(HandlerRegistration handler) {
		this.registrations.remove(handler);
	}
	
	public boolean assertExpanded() {
		boolean alreadyExpanded = true;
		if(!this.expanded) {
			expand();
			alreadyExpanded = false;
		}
		
		return alreadyExpanded;
	}
	
}
