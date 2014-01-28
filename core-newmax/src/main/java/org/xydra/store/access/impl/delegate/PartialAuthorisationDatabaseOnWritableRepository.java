package org.xydra.store.access.impl.delegate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.WritableUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XBooleanValue;
import org.xydra.core.XX;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAccessListener;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;
import org.xydra.store.access.XAuthorisationEvent;


/**
 * Delegate all reads and writes to a {@link XWritableRepository}.
 * 
 * Data modelling:
 * 
 * <pre>
 * objectId | fieldId                        | value
 * ---------+--------------------------------+----------
 * actorId  | "enc(address)+"_."+enc(rightId) | boolean
 * </pre>
 * 
 * Rights can be READ, WRITE, ADMIN.
 * 
 * Non-existing field: right not defined.
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class PartialAuthorisationDatabaseOnWritableRepository implements XAccessListener {
	
	private static final Logger log = LoggerFactory
	        .getLogger(PartialAuthorisationDatabaseOnWritableRepository.class);
	
	private static boolean accessIdToBoolean(XId accessType) {
		XyAssert.xyAssert(accessType != null);
		assert accessType != null;
		if(XA.ACCESS_ALLOW.equals(accessType)) {
			return true;
		}
		if(XA.ACCESS_DENY.equals(accessType)) {
			return false;
		}
		throw new IllegalArgumentException("Cannot understand accessType '" + accessType
		        + "' as boolean");
	}
	
	/**
	 * Apply those events with access rights change semantics.
	 * 
	 * @param events The events to apply.
	 * @param fastAuthorisationDatabase The database to apply the events to.
	 */
	public static void applyEventsTo(List<XEvent> events,
	        XAuthorisationDatabaseWitListeners fastAuthorisationDatabase) {
		/* apply events */
		for(XEvent event : events) {
			applyEventTo(event, fastAuthorisationDatabase);
		}
	}
	
	/**
	 * Translate from RMOF to group actions.
	 * 
	 * @param event
	 * @param fastDatabase
	 */
	private static void applyEventTo(XEvent event, XAuthorisationDatabaseWitListeners fastDatabase) {
		if(event.getChangeType() == ChangeType.TRANSACTION) {
			XTransactionEvent txn = (XTransactionEvent)event;
			for(XEvent atomicEvent : txn) {
				applyEventTo(atomicEvent, fastDatabase);
			}
			return;
		}
		
		XyAssert.xyAssert(event instanceof XAtomicEvent);
		
		/*
		 * We care for object.field: {actorId}.{enc(address)+"_."+enc(rightId)}
		 * = boolean
		 */
		XAddress target = event.getTarget();
		switch(target.getAddressedType()) {
		case XMODEL:
			// if REMOVE {model}.{actorId}: remove all rights for this actor
			XyAssert.xyAssert(event instanceof XModelEvent);
			if(event.getChangeType() == ChangeType.REMOVE) {
				log.warn("A better implementation would not remove all rights of actor '"
				        + event.getChangedEntity().getObject() + "' but this one doesn't");
			}
			break;
		case XOBJECT: {
			// if REMOVE {model}.{actor}.{field}: remove access
			if(event.getChangeType() == ChangeType.REMOVE) {
				
				// try to decode fieldId
				XId fieldId = event.getChangedEntity().getField();
				try {
					Pair<XAddress,XId> pair = fromFieldId(fieldId);
					fastDatabase.resetAccess(event.getChangedEntity().getObject(), pair.getFirst(),
					        pair.getSecond());
				} catch(IllegalArgumentException e) {
					log.warn("Could not parse '" + fieldId + "' as encoded(XAddress/XId)");
				}
			}
		}
			break;
		case XFIELD: {
			XFieldEvent fieldEvent = (XFieldEvent)event;
			// {model}.{actorId}.{enc(address)+"_."+enc(rightId)} = XBoolean
			
			// try to parse fieldId
			XId fieldId = event.getChangedEntity().getField();
			try {
				Pair<XAddress,XId> pair = fromFieldId(fieldId);
				XBooleanValue newAllowed = (XBooleanValue)fieldEvent.getNewValue();
				fastDatabase.setAccess(event.getChangedEntity().getObject(), pair.getFirst(),
				        pair.getSecond(), newAllowed.contents());
			} catch(IllegalArgumentException e) {
				log.warn("Could not parse '" + fieldId + "' as encoded(XAddress/XId)");
			}
		}
			break;
		default:
			// ignore repository events
			break;
		}
	}
	
	/**
	 * Parse fieldId to {@link XAddress} resource, {@link XId} access.
	 * 
	 * @param fieldId
	 * 
	 * @return XAddress resource, XId access
	 * @throws IllegalArgumentException if parsing failed
	 */
	public static final Pair<XAddress,XId> fromFieldId(XId fieldId) {
		String[] parts = fieldId.toString().split("_\\.");
		if(parts.length != 2) {
			throw new IllegalArgumentException("Could not parse '" + fieldId
			        + "' as XAddress/XId pair.");
		}
		return new Pair<XAddress,XId>(NamingUtils.decodeXAddress(parts[0]),
		        NamingUtils.decodeXid(parts[1]));
	}
	
	// TODO make non-public
	public static final XId toFieldId(XAddress resource, XId access) {
		return XX.toId(NamingUtils.encode(resource) + NamingUtils.ENCODING_SEPARATOR
		        + NamingUtils.encode(access));
	}
	
	protected XWritableRepository authorisationRepository;
	
	private boolean listeningToEvents;
	
	private transient Map<XId,ModelAccessDatabaseOnWritableModel> modelAccessDbs = new HashMap<XId,ModelAccessDatabaseOnWritableModel>();
	
	/**
	 * @param authorisationRepository used to read and write authorisation data.
	 */
	public PartialAuthorisationDatabaseOnWritableRepository(
	        XWritableRepository authorisationRepository) {
		this.authorisationRepository = authorisationRepository;
	}
	
	/**
	 * Beware, this simply kills the complete repository, including all data.
	 */
	@ModificationOperation
	public void clear() {
		WritableUtils.deleteAllModels(this.authorisationRepository);
		/*
		 * TODO IMPROVE read all right model Ids from global model; delete all
		 * right models; delete global right model;
		 */
	}
	
	public XAccessRightValue getAccessDefinition(XId actor, XAddress resource, XId access)
	        throws IllegalArgumentException {
		return getRightsModel(resource).getAccessDefinition(actor, resource, access);
	}
	
	/**
	 * every second model in the repo is typically a rights model. So we iterate
	 * over all models and detect via name conventions which we use
	 * 
	 * @return all right modelIds, excluding the global one.
	 */
	private Set<XId> getAllRightModelIds() {
		Set<XId> rightModelIds = new HashSet<XId>();
		for(XId modelId : this.authorisationRepository) {
			if(NamingUtils.isRightsModelId(modelId)) {
				rightModelIds.add(modelId);
			}
		}
		return rightModelIds;
	}
	
	public Set<XAccessRightDefinition> getDefinitions() {
		Set<XAccessRightDefinition> defs = new HashSet<XAccessRightDefinition>();
		defs.addAll(getGlobalRights().getDefinitions());
		for(XId modelId : getAllRightModelIds()) {
			defs.addAll(this.getModelAccessDatabase(modelId).getDefinitions());
		}
		return defs;
	}
	
	/**
	 * @param actorId Get only definitions for this actor.
	 * @return all {@link XAccessRightDefinition} defined for actorId
	 */
	public Set<XAccessRightDefinition> getDefinitionsFor(XId actorId) {
		Set<XAccessRightDefinition> defs = new HashSet<XAccessRightDefinition>();
		defs.addAll(getGlobalRights().getDefinitionsFor(actorId));
		for(XId modelId : getAllRightModelIds()) {
			defs.addAll(this.getModelAccessDatabase(modelId).getDefinitionsFor(actorId));
		}
		return defs;
	}
	
	private ModelAccessDatabaseOnWritableModel getGlobalRights() {
		return this.getModelAccessDatabase(NamingUtils.ID_REPO_AUTHORISATION_MODEL);
		
	}
	
	/**
	 * Creates new models in underlying persistence if necessary
	 * 
	 * @param modelId
	 * @return
	 */
	private ModelAccessDatabaseOnWritableModel getModelAccessDatabase(XId modelId) {
		ModelAccessDatabaseOnWritableModel modelAccessDb = this.modelAccessDbs.get(modelId);
		if(modelAccessDb == null) {
			XWritableModel rightsModel = this.authorisationRepository.getModel(modelId);
			if(rightsModel == null) {
				rightsModel = this.authorisationRepository.createModel(modelId);
				XyAssert.xyAssert(rightsModel != null);
				assert rightsModel != null;
			}
			modelAccessDb = new ModelAccessDatabaseOnWritableModel(rightsModel);
			this.modelAccessDbs.put(modelId, modelAccessDb);
		}
		return modelAccessDb;
	}
	
	private ModelAccessDatabaseOnWritableModel getRightsModel(XAddress resource) {
		XId rightsModelId = null;
		if(resource.getAddressedType() == XType.XREPOSITORY) {
			rightsModelId = NamingUtils.ID_REPO_AUTHORISATION_MODEL;
		} else {
			rightsModelId = NamingUtils.getRightsModelId(resource.getModel());
		}
		return this.getModelAccessDatabase(rightsModelId);
	}
	
	/*
	 * Access rights per model are initialised on-demand for each model.
	 * 
	 * @param hookAuthorisationManager
	 */
	public void loadInto(HookAuthorisationManagerAndDb hookAuthorisationManagerAndDb) {
		// global rights
		loadInto(getGlobalRights());
		for(XId modelId : getAllRightModelIds()) {
			ModelAccessDatabaseOnWritableModel rightsModelDb = this.getModelAccessDatabase(modelId);
			loadInto(rightsModelDb);
		}
	}
	
	private void loadInto(ModelAccessDatabaseOnWritableModel rightsModel) {
		for(XAccessRightDefinition def : rightsModel.getDefinitions()) {
			this.setAccess(def.getActor(), def.getResource(), def.getAccess(), def.isAllowed());
		}
	}
	
	@Override
	public void onAccessEvent(XAuthorisationEvent event) {
		if(!this.listeningToEvents) {
			return;
		}
		switch(event.getChangeType()) {
		case ADD: {
			boolean allowed = PartialAuthorisationDatabaseOnWritableRepository
			        .accessIdToBoolean(event.getAccessType());
			this.setAccess(event.getActor(), event.getResource(), event.getAccessType(), allowed);
		}
			break;
		case REMOVE: {
			this.resetAccess(event.getActor(), event.getResource(), event.getAccessType());
		}
			break;
		case CHANGE: {
			boolean allowed = PartialAuthorisationDatabaseOnWritableRepository
			        .accessIdToBoolean(event.getAccessType());
			this.setAccess(event.getActor(), event.getResource(), event.getAccessType(), allowed);
			break;
		}
		case TRANSACTION:
			throw new AssertionError("I didnt expect transactions here");
		}
	}
	
	// TODO make non-public
	public void resetAccess(XId actor, XAddress resource, XId access) {
		getRightsModel(resource).resetAccess(actor, resource, access);
	}
	
	public void setAccess(XId actor, XAddress resource, XId access, boolean allowed) {
		getRightsModel(resource).setAccess(actor, resource, access, allowed);
	}
	
	public void setEventListening(boolean enabled) {
		this.listeningToEvents = enabled;
	}
	
}
