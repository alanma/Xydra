package org.xydra.store.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.X;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AccessException;
import org.xydra.store.AuthorisationException;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.ModelRevision;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.TimeoutException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.impl.memory.AllowAllAccessControlManager;


/**
 * Implements a {@link XydraBlockingStore}.
 * 
 * Each method checks access rights via a {@link XAccessControlManager}
 * instance. If allowed, operation is performed by calling the
 * {@link XydraPersistence} instance.
 * 
 * PasswordHash can be set to null to force authorisation and allow access to
 * every resource with every operation (read,write,...).
 * 
 * The implementation assumes the actorId is never null.
 * 
 * TODO GWT doesn't have Thread
 * 
 * @author voelkel
 */
@RunsInGWT(false)
public class DelegateToPersistenceAndAcm implements XydraBlockingStore, XydraStoreAdmin {
	
	private static final Logger log = LoggerFactory.getLogger(DelegateToPersistenceAndAcm.class);
	
	private XAccessControlManager acm;
	private XydraPersistence persistence;
	private transient XID repoIdCached;
	
	/**
	 * @param persistence used to persists data (who would have guessed that :-)
	 * @param acm use the {@link AllowAllAccessControlManager} to allow every
	 *            access.
	 */
	public DelegateToPersistenceAndAcm(XydraPersistence persistence, XAccessControlManager acm) {
		this.persistence = persistence;
		if(acm == null) {
			throw new IllegalArgumentException("Access Control Manager may not be null");
		}
		this.acm = acm;
	}
	
	private XID getRepoId() {
		if(this.repoIdCached == null) {
			this.repoIdCached = this.persistence.getRepositoryId();
		}
		return this.repoIdCached;
	}
	
	/**
	 * @param actorId never null.
	 * @param passwordHash if null, acotrId is authorised.
	 */
	private void authorise(XID actorId, String passwordHash) {
		if(!checkLogin(actorId, passwordHash)) {
			throw new AuthorisationException("Could not authorise '" + actorId + "'");
		}
	}
	
	@Override
	public boolean checkLogin(XID actorId, String passwordHash) throws IllegalArgumentException,
	        QuotaException, TimeoutException, ConnectionException, RequestException,
	        InternalStoreException {
		/* null password -> always authorised */
		if(passwordHash == null) {
			return true;
		}
		assert actorId != null;
		if(this.acm.getAuthenticationDatabase() == null) {
			// we cannot log
			return true;
		}
		int failedLoginAttempts = this.acm.getAuthenticationDatabase().getFailedLoginAttempts(
		        actorId);
		if(failedLoginAttempts > XydraStore.MAX_FAILED_LOGIN_ATTEMPTS) {
			// TODO IMPROVE block the account automatically
		}
		if(this.acm.isAuthenticated(actorId, passwordHash)) {
			this.acm.getAuthenticationDatabase().resetFailedLoginAttempts(actorId);
			return true;
		} else {
			// always log failed attempts
			failedLoginAttempts = this.acm.getAuthenticationDatabase()
			        .incrementFailedLoginAttempts(actorId);
			// throw exception based on number of failed attempts
			if(failedLoginAttempts > XydraStore.MAX_FAILED_LOGIN_ATTEMPTS) {
				/* let user wait 10 seconds and inform administrator */
				try {
					Thread.sleep(1);
					// Thread.sleep(10 * 1000);
				} catch(InterruptedException e) {
					log.warn("could not sleep while throttling potential hacker", e);
				}
				// TODO IMPROVE inform admin better
				log.warn("SECURITY: Potential hacking attempt on account '" + actorId + "'");
				throw new QuotaException(XydraStore.MAX_FAILED_LOGIN_ATTEMPTS
				        + " failed login attempts.");
			}
			return false;
		}
	}
	
	private void checkRepoId(XAddress address) {
		if(!this.getRepoId().equals(address.getRepository())) {
			throw new IllegalArgumentException("wrong repository ID: was " + address
			        + " but expected " + this.getRepoId());
		}
	}
	
	@Override
	public void clear() {
		this.persistence.clear();
	}
	
	@Override
	public long executeCommand(XID actorId, String passwordHash, XCommand command)
	        throws AccessException {
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = command.getChangedEntity();
		checkRepoId(address);
		// check access rights
		assert command.getChangedEntity().getAddressedType() != XType.XREPOSITORY : "Nobody can add or remove a repository";
		// check access rights
		if(!triviallyAllowed(passwordHash)
		        && !this.acm.getAuthorisationManager().canExecute(actorId, command)) {
			throw new AccessException(actorId + " is not allowed to execute this command.");
		}
		return this.persistence.executeCommand(actorId, command);
	}
	
	@Override
	public XAccessControlManager getAccessControlManager() {
		return this.acm;
	}
	
	@Override
	public XEvent[] getEvents(XID actorId, String passwordHash, GetEventsRequest getEventsRequest) {
		
		if(getEventsRequest == null) {
			throw new RequestException("getEventsRequest must not be null");
		}
		
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = getEventsRequest.address;
		long beginRevision = getEventsRequest.beginRevision;
		long endRevision = getEventsRequest.endRevision;
		checkRepoId(address);
		if(endRevision < beginRevision) {
			throw new RequestException("invalid revision range for getEvents: [" + beginRevision
			        + "," + endRevision + "]");
		}
		
		// FIXME see fixme below
		if(!triviallyAllowed(passwordHash)
		        && !this.acm.getAuthorisationManager().canKnowAboutModel(actorId,
		                this.getRepositoryAddress(), address.getModel())) {
			// silently drop all events (if there are any)
			return new XEvent[0];
		}
		
		// assert: authenticated & mayKnowAbout model
		List<XEvent> events = this.persistence.getEvents(address, beginRevision, endRevision);
		/* check access rights for model, each object and each field */
		
		if(events == null) {
			return null;
		}
		
		/*
		 * FIXME why are the access rights not checked if passwordHash == null?
		 * passwordHash == null only indicates that the actor is already
		 * authenticated, not that he is authorized to view all requested info
		 */
		if(!triviallyAllowed(passwordHash)) {
			assert this.acm.getAuthorisationManager() != null;
			Iterator<XEvent> it = events.iterator();
			while(it.hasNext()) {
				// TODO handle XTransactionEvents
				XEvent event = it.next();
				switch(event.getChangedEntity().getAddressedType()) {
				case XREPOSITORY: {
					/*
					 * Model creation and remove events are part of the models'
					 * event log. On GAE this is needed for synchronization
					 * purposes (so are model remove events). Everywhere else
					 * this is useful to log who created/removed the model.
					 */
					break;
				}
				case XMODEL: {
					XID objectId = event.getChangedEntity().getObject();
					if(!this.acm.getAuthorisationManager().canKnowAboutObject(actorId,
					        XX.resolveModel(event.getChangedEntity()), objectId)) {
						// filter event out
						it.remove();
						// IMPROVE remove in the middle of array lists is
						// inefficient
					}
					break;
				}
				case XOBJECT:
				case XFIELD: {
					XID fieldId = event.getChangedEntity().getField();
					// TODO is knowAboutObject enough to get the field event?
					// ~~max
					if(!this.acm.getAuthorisationManager().canKnowAboutField(actorId,
					        XX.resolveObject(event.getChangedEntity()), fieldId)) {
						// filter event out
						it.remove();
					}
					break;
				}
				}
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
			for(XID modelId : this.persistence.getManagedModelIds()) {
				ModelRevision modelRev = this.persistence
				        .getModelRevision(new GetWithAddressRequest(XX.resolveModel(
				                this.getRepoId(), modelId), false));
				// TODO can see all models you can know about? Seems
				// plausible.
				// ~ max
				if(triviallyAllowed(passwordHash)
				        || this.acm.getAuthorisationManager().canKnowAboutModel(actorId,
				                getRepositoryAddress(), modelId)) {
					if(modelRev.modelExists()) {
						modelIds.add(modelId);
					}
				}
			}
		}
		return modelIds;
	}
	
	@Override
	public ModelRevision getModelRevision(XID actorId, String passwordHash,
	        GetWithAddressRequest getWithAddressRequest) {
		assert actorId != null;
		XAddress address = getWithAddressRequest.address;
		authorise(actorId, passwordHash);
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model revison, was "
			        + address);
		}
		checkRepoId(address);
		if(triviallyAllowed(passwordHash)
		        || this.acm.getAuthorisationManager().canRead(actorId, address)) {
			return this.persistence.getModelRevision(getWithAddressRequest);
		} else {
			return new ModelRevision(XCommand.FAILED, false);
		}
	}
	
	@Override
	public XReadableModel getModelSnapshot(XID actorId, String passwordHash,
	        GetWithAddressRequest addressRequest) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = addressRequest.address;
		if(address.getAddressedType() != XType.XMODEL) {
			throw new RequestException("must use a model address to get a model snapshot, was "
			        + address);
		}
		checkRepoId(address);
		if(triviallyAllowed(passwordHash)
		        || this.acm.getAuthorisationManager().canRead(actorId, address)) {
			XWritableModel modelSnapshot = this.persistence.getModelSnapshot(addressRequest);
			// filter out objects & fields which the actor may not see
			if(!triviallyAllowed(passwordHash)) {
				List<XID> objectIdsToBeRemoved = new LinkedList<XID>();
				for(XID objectId : modelSnapshot) {
					XAddress objectAddress = XX.resolveObject(address, objectId);
					if(!this.acm.getAuthorisationManager().canRead(actorId, objectAddress)) {
						objectIdsToBeRemoved.add(objectId);
					} else {
						// remove fields the actorId may not READ
						List<XID> fieldIdsToBeRemoved = new LinkedList<XID>();
						XWritableObject object = modelSnapshot.getObject(objectId);
						for(XID fieldId : object) {
							if(!this.acm.getAuthorisationManager().canRead(actorId,
							        XX.resolveField(objectAddress, fieldId))) {
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
	public XReadableObject getObjectSnapshot(XID actorId, String passwordHash,
	        GetWithAddressRequest addressRequest) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		XAddress address = addressRequest.address;
		if(address.getAddressedType() != XType.XOBJECT) {
			throw new RequestException("must use an object address to get an object snapshot, was "
			        + address);
		}
		checkRepoId(address);
		if(triviallyAllowed(passwordHash)
		        || this.acm.getAuthorisationManager().canRead(actorId, address)) {
			XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(addressRequest);
			if(passwordHash != null) {
				/* remove fields the actorId may not read */
				List<XID> toBeRemoved = new LinkedList<XID>();
				for(XID fieldId : objectSnapshot) {
					if(!this.acm.getAuthorisationManager().canRead(actorId,
					        XX.resolveField(address, fieldId))) {
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
	
	private XAddress getRepositoryAddress() {
		// TODO cache it
		return X.getIDProvider().fromComponents(this.getRepoId(), null, null, null);
	}
	
	@Override
	public XID getRepositoryId(XID actorId, String passwordHash) {
		assert actorId != null;
		authorise(actorId, passwordHash);
		return this.getRepoId();
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return this;
	}
	
	private boolean triviallyAllowed(String passwordHash) {
		boolean result = passwordHash == null || this.acm.getAuthorisationManager() == null;
		assert result || this.acm.getAuthorisationManager() != null : "If user is not trivially allowed, there must be an authorisationManager to check the non-trivial case";
		return result;
	}
	
	@Override
	public XID getRepositoryId() {
		return this.getRepoId();
	}
	
}
