package org.xydra.store.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XAtomicCommand;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AccessException;
import org.xydra.store.AuthorisationException;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.MAXDone;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.TimeoutException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.access.impl.delegate.AccountModelWrapperOnPersistence;
import org.xydra.store.impl.memory.AllowAllAuthorisationArm;


/**
 * Implements a {@link XydraBlockingStore}.
 * 
 * Each method checks access rights via a {@link XAuthorisationArm} instance. If
 * allowed, operation is performed by calling the {@link XydraPersistence}
 * instance.
 * 
 * PasswordHash can be set to null to force authorisation and allow access to
 * every resource with every operation (read,write,...).
 * 
 * The implementation assumes the actorId is never null.
 * 
 * @author voelkel
 */
@MAXDone
public class DelegateToPersistenceAndArm implements XydraBlockingStore, XydraStoreAdmin {
	
	private static final Logger log = LoggerFactory.getLogger(DelegateToPersistenceAndArm.class);
	
	private XydraPersistence persistence;
	private XAuthorisationArm arm;
	private transient XID repoId;
	private transient AccountModelWrapperOnPersistence accountDb;
	
	/**
	 * @param persistence used to persists data (who would have guessed that :-)
	 * @param arm use the {@link AllowAllAuthorisationArm} to allow every
	 *            access.
	 */
	public DelegateToPersistenceAndArm(XydraPersistence persistence, XAuthorisationArm arm) {
		this.persistence = persistence;
		this.arm = arm;
		// speed up
		this.repoId = this.persistence.getRepositoryId();
	}
	
	private void checkRepoId(XAddress address) {
		if(!this.repoId.equals(address.getRepository())) {
			throw new IllegalArgumentException("wrong repository ID: was " + address
			        + " but expected " + this.repoId);
		}
	}
	
	@Override
	public long executeCommand(XID actorId, String passwordHash, XCommand command)
	        throws AccessException {
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = command.getChangedEntity();
		checkRepoId(address);
		// check access rights (for model, object and field)
		assert command.getChangedEntity().getAddressedType() != XType.XREPOSITORY : "Nobody can add or remove a repository";
		XID modelId = command.getChangedEntity().getModel();
		assert modelId != null;
		XModelArm modelArm = this.arm.getModelArm(modelId);
		if(passwordHash != null) {
			if(command.getChangeType() == ChangeType.TRANSACTION) {
				/* check access for every command in the transaction */
				XTransaction txn = (XTransaction)command;
				for(XAtomicCommand atomicCommand : txn) {
					if(!mayExecuteAtomicCommand(actorId, modelArm, atomicCommand)) {
						throw new AccessException(actorId
						        + " is not allowed to execute this transaction.");
					}
				}
			} else {
				if(!mayExecuteAtomicCommand(actorId, modelArm, (XAtomicCommand)command)) {
					throw new AccessException(actorId + " is not allowed to execute this command.");
				}
			}
		}
		return this.persistence.executeCommand(actorId, command);
	}
	
	/**
	 * @param actorId
	 * @param modelArm
	 * @param atomicCommand
	 * @return true if the actorId has the rights defined in modelArm to execute
	 *         the atomicCommand
	 */
	private static boolean mayExecuteAtomicCommand(XID actorId, XModelArm modelArm,
	        XAtomicCommand atomicCommand) {
		switch(atomicCommand.getChangeType()) {
		case ADD:
		case REMOVE: {
			return mayAddRemove(actorId, modelArm, atomicCommand.getChangedEntity());
		}
		case CHANGE: {
			assert atomicCommand.getChangedEntity().getAddressedType() == XType.XFIELD;
			return mayChange(actorId, modelArm, atomicCommand.getChangedEntity());
		}
		case TRANSACTION: {
			throw new AssertionError("Atomic commands cannot be of type transaction");
		}
		}
		throw new AssertionError("All cases in switch-case return already.");
	}
	
	/**
	 * @param actorId
	 * @param modelArm
	 * @param changedEntity
	 * @return if actorId has READ access to repository, model, object, and
	 *         field and WRITE access to the field.
	 */
	private static boolean mayChange(XID actorId, XModelArm modelArm, XAddress address) {
		assert address.getAddressedType() == XType.XFIELD;
		return modelArm.hasModelReadAccess(actorId)
		        && modelArm.hasObjectReadAccess(actorId, address.getObject())
		        && modelArm.hasFieldWriteAccess(actorId, address.getObject(), address.getField());
	}
	
	/**
	 * @param actorId
	 * @param modelArm
	 * @param changedEntity
	 * @return true if actorId may READ all parents of addressed entity and
	 *         WRITE addresses entity
	 */
	private static boolean mayAddRemove(XID actorId, XModelArm modelArm, XAddress address) {
		switch(address.getAddressedType()) {
		case XREPOSITORY:
			throw new AssertionError("Nobody can add or remove repositories.");
		case XMODEL:
			// write implies read
			return modelArm.hasModelWriteAccess(actorId);
		case XOBJECT:
			return modelArm.hasModelReadAccess(actorId)
			        && modelArm.hasObjectWriteAccess(actorId, address.getObject());
		case XFIELD:
			return modelArm.hasModelReadAccess(actorId)
			        && modelArm.hasObjectReadAccess(actorId, address.getObject())
			        && modelArm.hasFieldWriteAccess(actorId, address.getObject(), address
			        		.getField());
		}
		throw new AssertionError("Switch-case returned already");
	}
	
	@Override
	public XEvent[] getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = getEventsRequest.address;
		long beginRevision = getEventsRequest.beginRevision;
		long endRevision = getEventsRequest.endRevision;
		checkRepoId(address);
		if(endRevision < beginRevision) {
			throw new IllegalArgumentException("invalid revision range for getEvents: ["
			        + beginRevision + "," + endRevision + "]");
		}
		XModelArm modelArm = this.arm.getModelArm(address.getModel());
		if(passwordHash != null && !modelArm.hasModelReadAccess(actorId)) {
			return new XEvent[0];
		}
		// assert: actorId has at least model READ access
		List<XEvent> events = this.persistence.getEvents(address, beginRevision, endRevision);
		/* check access rights for model, each object and each field */
		if(passwordHash != null) {
			Iterator<XEvent> it = events.iterator();
			while(it.hasNext()) {
				// TODO handle XTransactionEvents
				XEvent event = it.next();
				switch(event.getChangedEntity().getAddressedType()) {
				case XREPOSITORY: {
					/*
					 * TODO is the model creation event part of the models'
					 * event log? -- Yes. On GAE this is needed for
					 * synchronization purposes (so are model remove events).
					 * Everywhere else this is useful to log who created/removed
					 * the model. ~Daniel
					 */
					throw new AssertionError(
					        "This class should only return model events, not repository events");
				}
				case XMODEL: {
					// no need to filter it out
					// TODO while (with write access to the repo) the existence
					// of models
				}
					break;
				case XOBJECT: {
					XID objectId = event.getChangedEntity().getObject();
					if(!modelArm.hasObjectReadAccess(actorId, objectId)) {
						// filter event out
						it.remove();
						// IMPROVE remove in the middle of array lists is
						// inefficient
					}
				}
					break;
				case XFIELD: {
					XID objectId = event.getChangedEntity().getObject();
					XID fieldId = event.getChangedEntity().getField();
					if(!modelArm.hasFieldReadAccess(actorId, objectId, fieldId)) {
						// filter event out
						it.remove();
					}
				}
				}
				break;
			}
		}
		return events.toArray(new XEvent[events.size()]);
	}
	
	@Override
	public Set<XID> getModelIds(XID actorId, String passwordHash) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		Set<XID> modelIds = new HashSet<XID>();
		synchronized(this.persistence) {
			for(XID modelId : this.persistence.getModelIds()) {
				XModelArm modelArm = this.arm.getModelArm(modelId);
				if(passwordHash == null || modelArm.hasModelReadAccess(actorId)) {
					modelIds.add(modelId);
				}
			}
		}
		return modelIds;
	}
	
	/**
	 * @param actorId never null.
	 * @param passwordHash if null, acotrId is authorised.
	 */
	private void authorise(XID actorId, String passwordHash) {
		/* null password -> always authorised */
		if(passwordHash == null) {
			return;
		}
		assert actorId != null;
		int failedLoginAttempts = this.arm.getFailedLoginAttempts(actorId);
		if(failedLoginAttempts > AuthorisationArm.MAX_FAILED_LOGIN_ATTEMPTS) {
			// TODO IMPROVE block the account automatically
		}
		if(this.arm.isAuthorised(actorId, passwordHash)) {
			this.arm.resetFailedLoginAttempts(actorId);
			return;
		} else {
			// always log failed attempts
			failedLoginAttempts = this.arm.incrementFailedLoginAttempts(actorId);
			// throw exeception based on number of failed attempts
			if(failedLoginAttempts > AuthorisationArm.MAX_FAILED_LOGIN_ATTEMPTS) {
				/* let user wait 10 seconds and inform administrator */
				try {
					Thread.sleep(10 * 1000);
				} catch(InterruptedException e) {
					log.warn("could not sleep while throttling potential hacker", e);
				}
				// TODO IMPROVE inform admin better
				log.warn("SECURITY: Potential hacking attempt on account '" + actorId + "'");
				throw new QuotaException(AuthorisationArm.MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts.");
			}
			throw new AuthorisationException("Could not authorise '" + actorId + "'");
		}
	}
	
	@Override
	public long getModelRevision(XID actorId, String passwordHash, XAddress address) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model revison, was "
			        + address);
		}
		checkRepoId(address);
		XModelArm modelArm = this.arm.getModelArm(address.getModel());
		if(passwordHash == null || modelArm.hasModelReadAccess(actorId)) {
			return this.persistence.getModelRevision(address);
		} else {
			return XydraStore.MODEL_DOES_NOT_EXIST;
		}
	}
	
	@Override
	public XReadableModel getModelSnapshot(XID actorId, String passwordHash, XAddress address) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model snapshot, was "
			        + address);
		}
		checkRepoId(address);
		XModelArm modelArm = this.arm.getModelArm(address.getModel());
		if(passwordHash == null || modelArm.hasModelReadAccess(actorId)) {
			XHalfWritableModel modelSnapshot = this.persistence.getModelSnapshot(address);
			// filter out objects & fields which the actor may not see
			if(passwordHash != null) {
				List<XID> objectIdsToBeRemoved = new LinkedList<XID>();
				for(XID objectId : modelSnapshot) {
					if(!modelArm.hasObjectReadAccess(actorId, objectId)) {
						objectIdsToBeRemoved.add(objectId);
					} else {
						// remove fields the actorId may not see
						List<XID> fieldIdsToBeRemoved = new LinkedList<XID>();
						XHalfWritableObject object = modelSnapshot.getObject(objectId);
						for(XID fieldId : object) {
							if(!modelArm.hasFieldReadAccess(actorId, objectId, fieldId)) {
								fieldIdsToBeRemoved.add(fieldId);
							}
						}
						for(XID fieldId : fieldIdsToBeRemoved) {
							object.removeField(fieldId);
						}
					}
				}
				for(XID objectId : objectIdsToBeRemoved) {
					modelSnapshot.removeObject(objectId);
				}
			}
			return modelSnapshot;
		} else {
			log.warn("Hiding model '" + address.getModel() + "' from '" + actorId
			        + "' (authorised, but not allowed to read)");
			return null;
		}
	}
	
	@Override
	public XReadableObject getObjectSnapshot(XID actorId, String passwordHash, XAddress address) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("must use an object address to get an object snapshot, was "
			        + address);
		}
		checkRepoId(address);
		XModelArm modelArm = this.arm.getModelArm(address.getModel());
		XID objectId = address.getObject();
		if(passwordHash == null || modelArm.hasObjectReadAccess(actorId, objectId)) {
			XHalfWritableObject objectSnapshot = this.persistence.getObjectSnapshot(address);
			if(passwordHash != null) {
				/* remove fields the actorId may not read */
				List<XID> toBeRemoved = new LinkedList<XID>();
				for(XID fieldId : objectSnapshot) {
					if(!modelArm.hasFieldReadAccess(actorId, objectId, fieldId)) {
						toBeRemoved.add(fieldId);
					}
				}
				for(XID fieldId : toBeRemoved) {
					objectSnapshot.removeField(fieldId);
				}
			}
			return objectSnapshot;
		} else {
			return null;
		}
	}
	
	@Override
	public XID getRepositoryId(XID actorId, String passwordHash) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		return this.repoId;
	}
	
	public void clear() {
		this.persistence.clear();
	}
	
	public void setXydraAdminPasswordHash(String passwordHash) {
		this.arm.setXydraAdminPasswordHash(passwordHash);
	}
	
	public String getXydraAdminPasswordHash() {
		return this.arm.getXydraAdminPasswordHash();
	}
	
	@Override
	public boolean checkLogin(XID actorId, String passwordHash) throws IllegalArgumentException,
	        QuotaException, TimeoutException, ConnectionException, RequestException,
	        InternalStoreException {
		assert actorId != null;
		return this.arm.isAuthorised(actorId, passwordHash);
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this;
	}
	
	@Override
	public XAccountDatabase getAccountDatabase() {
		// FIXME !!!
		// if(this.accountDb == null) {
		this.accountDb = new AccountModelWrapperOnPersistence(this.persistence,
		        AuthorisationArm.INTERNAL_XYDRA_ADMIN_ID);
		// }
		return this.accountDb;
	}
	
}
