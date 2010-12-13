package org.xydra.server.impl.newgae;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.access.XGroupDatabaseWithListeners;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.server.IXydraServer;
import org.xydra.server.impl.InfrastructureServiceFactory;
import org.xydra.server.impl.newgae.changes.GaeChangesService;
import org.xydra.server.impl.newgae.snapshot.GaeSnapshotService;


/**
 * An {@link IXydraServer} backend that persists changes in the Google Appengine
 * datastore.
 * 
 * FIXME MAX LOOK AT THIS schaust ob da irgendwas komisch aussieht bzw. nicht
 * gut dokumentiert ist
 * 
 * @author dscharrer
 */
public class NewGaeXydraServer implements IXydraServer {
	
	private static final Logger log = LoggerFactory.getLogger(NewGaeXydraServer.class);
	
	private final XAddress repoAddr = XX.toAddress(XX.toId("repo"), null, null, null);
	
	XGroupDatabaseWithListeners groups;
	XAccessManagerWithListeners arm;
	
	public NewGaeXydraServer() {
		
		// To enable local JUnit testing with multiple threads
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Register AppEngine infrastructure services
		InfrastructureServiceFactory.setProvider(new GaeInfrastructureProvider());
		
		// TODO remove once it's stable
		log.warn("Using the new, incomplete GAE IXydraServer backend");
		
		// TODO create better GAE group DB and ARM implementations
		this.groups = GaeGroups.loadGroups();
		this.arm = GaeAccess.loadAccessManager(this.repoAddr, this.groups);
	}
	
	public long executeCommand(XCommand command, XID actorId) {
		
		if(!this.repoAddr.equalsOrContains(command.getTarget())) {
			return XCommand.FAILED;
		}
		
		XID modelId = command.getChangedEntity().getModel();
		
		// TODO wrap GAE exceptions in InternalStoreExceptions
		
		return getChangesService(modelId).executeCommand(command, actorId);
	}
	
	private XAddress getModelAddress(XID modelId) {
		return XX.resolveModel(this.repoAddr, modelId);
	}
	
	private GaeChangesService getChangesService(XID modelId) {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		// IMPROVE cache GaeChangesService instances?
		return new GaeChangesService(getModelAddress(modelId));
	}
	
	private GaeSnapshotService getSnapshotService(XID modelId) {
		
		XChangeLog log = getChangeLog(modelId);
		
		if(log == null) {
			return null;
		}
		
		// IMPROVE cache GaeSnapshotService instances?
		return new GaeSnapshotService(log);
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		
		GaeChangesService cs = getChangesService(modelId);
		
		// TODO is this really necessary?
		if(!cs.hasLog()) {
			return null;
		}
		
		return cs;
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		
		GaeSnapshotService s = getSnapshotService(modelId);
		
		if(s == null) {
			return null;
		}
		
		return s.getSnapshot();
	}
	
	@Override
	public XAccessManagerWithListeners getAccessManager() {
		return this.arm;
	}
	
	@Override
	public XGroupDatabaseWithListeners getGroups() {
		return this.groups;
	}
	
	@Override
	public XAddress getRepositoryAddress() {
		return this.repoAddr;
	}
	
	@Override
	public Iterator<XID> iterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
