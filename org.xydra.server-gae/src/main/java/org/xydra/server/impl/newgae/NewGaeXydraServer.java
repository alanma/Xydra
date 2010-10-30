package org.xydra.server.impl.newgae;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.change.XCommand;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.log.gae.GaeLoggerFactorySPI;
import org.xydra.server.IXydraServer;
import org.xydra.server.impl.InfrastructureServiceFactory;
import org.xydra.server.impl.gae.GaeInfrastructureProvider;
import org.xydra.server.impl.gae.GaeTestfixer;


/**
 * An {@link IXydraServer} backend that persists changes in the Google Appengine
 * datastore.
 * 
 * @author dscharrer
 * 
 */
public class NewGaeXydraServer implements IXydraServer {
	
	private static final Logger log = LoggerFactory.getLogger(NewGaeXydraServer.class);
	
	private final XAddress repoAddr = XX.toAddress(XX.toId("repo"), null, null, null);
	
	XGroupDatabase groups;
	XAccessManager arm;
	
	public NewGaeXydraServer() {
		
		// To enable local JUnit testing with multiple threads
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Register AppEngine infrastructure services
		InfrastructureServiceFactory.setProvider(new GaeInfrastructureProvider());
		
		// FIXME this is too late, logging will already be initialized
		GaeLoggerFactorySPI.init();
		
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
		// IMPROVE cache GaeSnapshotService instances?
		return new GaeSnapshotService(getChangesService(modelId));
	}
	
	public XChangeLog getChangeLog(XID modelId) {
		return getChangesService(modelId);
	}
	
	public XBaseModel getModelSnapshot(XID modelId) {
		return getSnapshotService(modelId).getSnapshot();
	}
	
	@Override
	public XAccessManager getAccessManager() {
		return this.arm;
	}
	
	@Override
	public XGroupDatabase getGroups() {
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
