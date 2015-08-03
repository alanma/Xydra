package org.xydra.store.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.id.UUID;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * A session is a set of {@link SessionModel SessionModels}. It always gives
 * back the same references.
 *
 * A {@link SessionModel} is a buffer for reads and writes on a not necessarily
 * existent model.
 *
 * @author xamde
 *
 */
public class ChangeSession {

	private static final Logger log = LoggerFactory.getLogger(ChangeSession.class);

	protected XId actorId;

	protected boolean closed = false;

	protected boolean readonly;

	protected Map<XId,SessionModel> sessionModels = new HashMap<XId,SessionModel>();

	protected ISessionPersistence sessionPersistence;

	/* for debugging */
	protected final String traceid = UUID.uuid(4);

	/**
	 * @param sessionPersistence
	 * @param readonly a flag marking an intent
	 * @param actorId
	 */
	protected ChangeSession(final ISessionPersistence sessionPersistence, final boolean readonly,
	        @NeverNull final XId actorId) {
		XyAssert.xyAssert(sessionPersistence != null);
		assert sessionPersistence != null;
		XyAssert.xyAssert(actorId != null);
		assert actorId != null;
		this.sessionPersistence = sessionPersistence;
		this.readonly = readonly;
		this.actorId = actorId;
	}

	public void assertIsOpen() {
		assert !this.closed : "Session should be open, but " + this + " is closed";
	}

	public void close() {
		assertIsOpen();
		assert !hasChanges() : "/!\\ closing session with uncommit changes" + dumpChanges("close");
		this.closed = true;
		log.info(this + " CLOSED");
	}

	public void closeAndIgnoreOpenChanges() {
		assertIsOpen();
		this.closed = true;
		log.info(this + " ABORTED (=CLOSED)");
	}

	/**
	 * Brute-force commits all opened models in this session.
	 *
	 * Should only be used in test. High-quality code should know which models
	 * need to be persisted.
	 *
	 * @return true if all commits worked fine
	 */
	public boolean commit() {
		assertIsOpen();
		for(final SessionModel sm : getSessionModels()) {
			final long l = sm.commitToSessionPersistence();
			if(l < 0 && l != XCommand.NOCHANGE) {
				log.warn("Error committing " + sm.getId() + " with " + l + " Changes:"
				        + sm.changesToString());
				return false;
			}
		}
		return true;
	}

	public String dumpChanges(final String s) {
		System.out.println("__/ Changes dump '" + s + "' for " + this);
		for(final SessionModel sm : this.sessionModels.values()) {
			if(sm.hasChanges()) {
				System.out.println(">>> Changes in Model '" + sm.getId() + "' trace = "
				        + sm.traceid + "\n" + sm.changesToString());
			}
		}
		System.out.println("__\\ End changes dump.");
		return " see console";
	}

	public XId getActorId() {
		return this.actorId;
	}

	public Collection<SessionModel> getSessionModels() {
		assertIsOpen();
		return this.sessionModels.values();
	}

	public ISessionPersistence getSessionPersistence() {
		assertIsOpen();
		return this.sessionPersistence;
	}

	public boolean hasChanges() {
		for(final SessionModel model : this.sessionModels.values()) {
			if(model.hasChanges()) {
				return true;
			}
		}
		return false;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public boolean isReadonly() {
		return this.readonly;
	}

	@Override
	public String toString() {
		return this.traceid + " hasChanges?" + hasChanges() + " openModels:"
		        + this.sessionModels.size() + " on " + this.sessionPersistence.getClass().getName();
	}

	/**
	 * @param sessionPersistence
	 * @param readonly
	 * @param actorId whom to assign the changes to; can be null if readonly
	 * @return a session
	 * @throws SessionException if model did not exist and could not be created
	 */
	public static ChangeSession createSession(final ISessionPersistence sessionPersistence,
	        final boolean readonly, final XId actorId) throws SessionException {
		final ChangeSession session = new ChangeSession(sessionPersistence, readonly, actorId);
		return session;
	}

	/**
	 * @param modelId any modelId. If it does not exist in the back-end, it is
	 *            created at commit-time.
	 * @param readonly may only be false for writable sessions. Read-only
	 *            sessions cannot have writable models.
	 * @return the model representing the given modelId in this session. Might
	 *         have something preloaded already.
	 */
	public SessionModel openModel(final XId modelId, final boolean readonly) {
		assert !(isReadonly() && !readonly) : "readonly sessions cannot have writable models. Session:"
		        + isReadonly() + " Model:" + readonly;
		SessionModel sessionModel = this.sessionModels.get(modelId);
		if(sessionModel == null) {
			final XAddress modelAddress = Base.resolveModel(this.sessionPersistence.getRepositoryId(),
			        modelId);
			sessionModel = new SessionModel(this, modelAddress, readonly);
			this.sessionModels.put(modelId, sessionModel);
			if(log.isDebugEnabled()) {
				log.debug("Session '" + this.traceid + "' opened session model '" + sessionModel + "'");
			}
		}
		return sessionModel;
	}

}
