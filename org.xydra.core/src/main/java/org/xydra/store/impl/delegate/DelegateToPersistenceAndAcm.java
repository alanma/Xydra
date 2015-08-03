package org.xydra.store.impl.delegate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.AccessException;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.AuthorisationException;
import org.xydra.store.ConnectionException;
import org.xydra.store.InternalStoreException;
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
 * @author xamde
 */
@RunsInGWT(false)
public class DelegateToPersistenceAndAcm implements XydraBlockingStore, XydraStoreAdmin {

    private static final Logger log = LoggerFactory.getLogger(DelegateToPersistenceAndAcm.class);

    private final XAccessControlManager acm;
    private final XydraPersistence persistence;
    private transient XId repoIdCached;

    /**
     * @param persistence used to persists data (who would have guessed that :-)
     * @param acm use the {@link AllowAllAccessControlManager} to allow every
     *            access.
     */
    public DelegateToPersistenceAndAcm(final XydraPersistence persistence, final XAccessControlManager acm) {
        this.persistence = persistence;
        if(acm == null) {
            throw new IllegalArgumentException("Access Control Manager may not be null");
        }
        this.acm = acm;
    }

    private XId getRepoId() {
        if(this.repoIdCached == null) {
            this.repoIdCached = this.persistence.getRepositoryId();
        }
        return this.repoIdCached;
    }

    /**
     * @param actorId never null.
     * @param passwordHash if null, acotrId is authorised.
     */
    private void authorise(final XId actorId, final String passwordHash) {
        if(!checkLogin(actorId, passwordHash)) {
            throw new AuthorisationException("Could not authorise '" + actorId + "'");
        }
    }

    @Override
    public boolean checkLogin(final XId actorId, final String passwordHash) throws IllegalArgumentException,
            QuotaException, TimeoutException, ConnectionException, RequestException,
            InternalStoreException {
        /* null password -> always authorised */
        if(passwordHash == null) {
            return true;
        }
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;

        final boolean authenticated = this.acm.isAuthenticated(actorId, passwordHash);

        if(this.acm.getAuthenticationDatabase() == null) {
            // we cannot log
            // return true;
            // FIXME SECURITY DO:
            return authenticated;
        }
        int failedLoginAttempts = this.acm.getAuthenticationDatabase().getFailedLoginAttempts(
                actorId);
        if(failedLoginAttempts > XydraStore.MAX_FAILED_LOGIN_ATTEMPTS) {
            // TODO IMPROVE block the account automatically
        }
        if(authenticated) {
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
                } catch(final InterruptedException e) {
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

    private void checkRepoId(final XAddress address) {
        if(!getRepoId().equals(address.getRepository())) {
            throw new IllegalArgumentException("wrong repository ID: was " + address
                    + " but expected " + getRepoId());
        }
    }

    @Override
    public void clear() {
        this.persistence.clear();
    }

    @Override
    public long executeCommand(final XId actorId, final String passwordHash, final XCommand command)
            throws AccessException {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        final XAddress address = command.getChangedEntity();
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
    public XEvent[] getEvents(final XId actorId, final String passwordHash, final GetEventsRequest getEventsRequest) {

        if(getEventsRequest == null) {
            throw new RequestException("getEventsRequest must not be null");
        }

        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        final XAddress address = getEventsRequest.address;
        final long beginRevision = getEventsRequest.beginRevision;
        final long endRevision = getEventsRequest.endRevision;
        checkRepoId(address);
        if(endRevision < beginRevision) {
            throw new RequestException("invalid revision range for getEvents: [" + beginRevision
                    + "," + endRevision + "]");
        }

        // FIXME SECURITY see fixme below
        if(!triviallyAllowed(passwordHash)
                && !this.acm.getAuthorisationManager().canKnowAboutModel(actorId,
                        getRepositoryAddress(), address.getModel())) {
            // silently drop all events (if there are any)
            return new XEvent[0];
        }

        // assert: authenticated & mayKnowAbout model
        final List<XEvent> events = this.persistence.getEvents(address, beginRevision, endRevision);
        /* check access rights for model, each object and each field */

        if(events == null) {
            return null;
        }

        /*
         * FIXME SECURITY why are the access rights not checked if passwordHash
         * == null? passwordHash == null only indicates that the actor is
         * already authenticated, not that he is authorized to view all
         * requested info
         */
        if(!triviallyAllowed(passwordHash)) {
            XyAssert.xyAssert(this.acm.getAuthorisationManager() != null);
            assert this.acm.getAuthorisationManager() != null;
            final Iterator<XEvent> it = events.iterator();
            while(it.hasNext()) {
                // TODO handle XTransactionEvents
                final XEvent event = it.next();
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
                    final XId objectId = event.getChangedEntity().getObject();
                    if(!this.acm.getAuthorisationManager().canKnowAboutObject(actorId,
                            Base.resolveModel(event.getChangedEntity()), objectId)) {
                        // filter event out
                        it.remove();
                        // IMPROVE remove in the middle of array lists is
                        // inefficient
                    }
                    break;
                }
                case XOBJECT:
                case XFIELD: {
                    final XId fieldId = event.getChangedEntity().getField();
                    // TODO is knowAboutObject enough to get the field event?
                    // ~~max
                    if(!this.acm.getAuthorisationManager().canKnowAboutField(actorId,
                            Base.resolveObject(event.getChangedEntity()), fieldId)) {
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
    public Set<XId> getModelIds(final XId actorId, final String passwordHash) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        final Set<XId> modelIds = new HashSet<XId>();
        synchronized(this.persistence) {
            for(final XId modelId : this.persistence.getManagedModelIds()) {
                final ModelRevision modelRev = this.persistence
                        .getModelRevision(new GetWithAddressRequest(Base.resolveModel(
                                getRepoId(), modelId), false));
                // TODO can see all models you can know about? Seems
                // plausible.
                // ~ max
                if(triviallyAllowed(passwordHash)
                        || this.acm.getAuthorisationManager().canKnowAboutModel(actorId,
                                getRepositoryAddress(), modelId)) {
                    if(modelRev.modelExists()) {
                        modelIds.add(modelId);
                    }
                } else {
                    if(log.isTraceEnabled()) {
						log.trace("actor '" + actorId + "' not allowed to see model " + modelId);
					}
                }
            }
        }
        return modelIds;
    }

    @Override
    public ModelRevision getModelRevision(final XId actorId, final String passwordHash,
            final GetWithAddressRequest getWithAddressRequest) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        final XAddress address = getWithAddressRequest.address;
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
    public XReadableModel getModelSnapshot(final XId actorId, final String passwordHash,
            final GetWithAddressRequest addressRequest) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        final XAddress address = addressRequest.address;
        if(address.getAddressedType() != XType.XMODEL) {
            throw new RequestException("must use a model address to get a model snapshot, was "
                    + address);
        }
        checkRepoId(address);
        if(triviallyAllowed(passwordHash)
                || this.acm.getAuthorisationManager().canRead(actorId, address)) {
            final XWritableModel modelSnapshot = this.persistence.getModelSnapshot(addressRequest);
            // filter out objects & fields which the actor may not see
            if(!triviallyAllowed(passwordHash)) {
                final List<XId> objectIdsToBeRemoved = new LinkedList<XId>();
                for(final XId objectId : modelSnapshot) {
                    final XAddress objectAddress = Base.resolveObject(address, objectId);
                    if(!this.acm.getAuthorisationManager().canRead(actorId, objectAddress)) {
                        objectIdsToBeRemoved.add(objectId);
                    } else {
                        // remove fields the actorId may not READ
                        final List<XId> fieldIdsToBeRemoved = new LinkedList<XId>();
                        final XWritableObject object = modelSnapshot.getObject(objectId);
                        for(final XId fieldId : object) {
                            if(!this.acm.getAuthorisationManager().canRead(actorId,
                                    Base.resolveField(objectAddress, fieldId))) {
                                fieldIdsToBeRemoved.add(fieldId);
                            }
                        }
                        for(final XId fieldId : fieldIdsToBeRemoved) {
                            object.removeField(fieldId);
                        }
                    }
                }
                for(final XId objectId : objectIdsToBeRemoved) {
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
    public XReadableObject getObjectSnapshot(final XId actorId, final String passwordHash,
            final GetWithAddressRequest addressRequest) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        final XAddress address = addressRequest.address;
        if(address.getAddressedType() != XType.XOBJECT) {
            throw new RequestException("must use an object address to get an object snapshot, was "
                    + address);
        }
        checkRepoId(address);
        if(triviallyAllowed(passwordHash)
                || this.acm.getAuthorisationManager().canRead(actorId, address)) {
            final XWritableObject objectSnapshot = this.persistence.getObjectSnapshot(addressRequest);
            if(passwordHash != null) {
                /* remove fields the actorId may not read */
                final List<XId> toBeRemoved = new LinkedList<XId>();
                for(final XId fieldId : objectSnapshot) {
                    if(!this.acm.getAuthorisationManager().canRead(actorId,
                            Base.resolveField(address, fieldId))) {
                        toBeRemoved.add(fieldId);
                    }
                }
                for(final XId fieldId : toBeRemoved) {
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
        return BaseRuntime.getIDProvider().fromComponents(getRepoId(), null, null, null);
    }

    @Override
    public XId getRepositoryId(final XId actorId, final String passwordHash) {
        XyAssert.xyAssert(actorId != null);
        assert actorId != null;
        authorise(actorId, passwordHash);
        return getRepoId();
    }

    @Override
    public XydraStoreAdmin getXydraStoreAdmin() {
        return this;
    }

    private boolean triviallyAllowed(final String passwordHash) {
        final boolean result = passwordHash == null || this.acm.getAuthorisationManager() == null;
        assert result || this.acm.getAuthorisationManager() != null : "If user is not trivially allowed, there must be an authorisationManager to check the non-trivial case";
        return result;
    }

    @Override
    public XId getRepositoryId() {
        return getRepoId();
    }

}
