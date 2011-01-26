package org.xydra.store.access.impl.delegate;

import java.util.List;

import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.impl.delegate.WritableModelOnPersistence;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.store.NamingUtils;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XGroupDatabase;
import org.xydra.store.access.impl.memory.CachingOrMemoryAuthenticationDatabase;
import org.xydra.store.access.impl.memory.DelegatingAccessControlManager;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;
import org.xydra.store.impl.delegate.XydraPersistence;


/**
 * A minimal out of date overview on the implementation strategy of this class
 * is this:
 * 
 * <pre>
 * AccessControlManagerOnPersistence
 * -> CachingOrMemoryAuthenticationDatabase
 *    -> AuthenticationDatabaseOnHalfWritableModel
 *       -> HalfWritableModelOnPersistence
 *          -> XydraPersistence
 * -> SyncingHookAuthorisationManagerAndDb
 *    -> MemoryAuthorisationManager (as container for groupDB)
 *       -> SyncingHookGroupDatabase
 *          -> MemoryGroupDatabase
 *          -> PartialGroupDatabaseOnHalfWritableModel
 *             -> HalfWritableModelOnPersistence
 *                -> XydraPersistence
 *          -> HalfWritableModelOnPersistence
 *             -> XydraPersistence
 *          [x] load rights
 *          [X] user changes -> persistence
 *          [x] persistence changed -> in memory
 *    -> MemoryAuthorisationManager (as authorisationDB)
 *    -> PartialAuthorisationDatabaseOnHalfWritableRepository
 *       -> HalfWritableRepositoryOnPersistence
 *    [x] load rights
 *    [x] user changes -> persistence
 *    [x] persistence changed -> in memory
 * </pre>
 * 
 * @author xamde
 * 
 */
public class AccessControlManagerOnPersistence extends DelegatingAccessControlManager {
	
	@SuppressWarnings("unused")
	private class SyncingHookAuthorisationManagerAndDb extends HookAuthorisationManagerAndDb {
		
		private WritableRepositoryOnPersistence authorisationRepositoryOnPersistence;
		
		private PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb;
		
		public SyncingHookAuthorisationManagerAndDb(
		        MemoryAuthorisationManager fastAuthorisationManager,
		        WritableRepositoryOnPersistence authorisationRepositoryOnPersistence,
		        PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb) {
			super(fastAuthorisationManager);
			this.authorisationRepositoryOnPersistence = authorisationRepositoryOnPersistence;
			this.partialAuthorisationDb = partialAuthorisationDb;
		}
		
		@Override
		protected void beforeRead() {
			// FIXME check for events in persistence - unfinished impl!
			// List<XEvent> events =
			// this.authorisationRepositoryOnPersistence.getNewEvents();
			// if(events != null) {
			// /* don't let the user perform reads/write while we sync */
			// synchronized(this) {
			// /*
			// * stop listening to XGroupEvetns to avoid persisting events
			// * that are already persisted
			// */
			// this.partialAuthorisationDb.setEventListening(false);
			// /* apply */
			// PartialAuthorisationDatabaseOnWritableRepository.applyEventsTo(events,
			// this
			// .getBaseAuthorisationManager().getAuthorisationDatabase());
			// /* Restart event listening */
			// this.partialAuthorisationDb.setEventListening(true);
			// }
			// }
		}
		
	}
	
	/**
	 * Before each read in the fast in-memory index, we check in the underlying
	 * persistence if the data is still fresh and update as necessary.
	 * 
	 * @author xamde
	 */
	private class SyncPersistenceToGroupDatabase implements IHookListener {
		
		private MemoryGroupDatabase baseDatabase;
		private WritableModelOnPersistence groupModelOnPersistence;
		private PartialGroupDatabaseOnWritableModel partialGroupDb;
		
		public SyncPersistenceToGroupDatabase(MemoryGroupDatabase fastDatabase,
		        WritableModelOnPersistence groupModelOnPersistence,
		        PartialGroupDatabaseOnWritableModel partialGroupDb) {
			this.baseDatabase = fastDatabase;
			this.groupModelOnPersistence = groupModelOnPersistence;
			this.partialGroupDb = partialGroupDb;
		}
		
		@Override
		public void beforeRead() {
			// check for events in persistence
			List<XEvent> events = this.groupModelOnPersistence.getNewEvents();
			if(events != null) {
				/* don't let the user perform reads/write while we sync */
				synchronized(this) {
					/*
					 * stop listening to XGroupEvetns to avoid persisting events
					 * that are already persisted
					 */
					this.partialGroupDb.setEventListening(false);
					/* apply */
					PartialGroupDatabaseOnWritableModel.applyEventsTo(events, this.baseDatabase);
					/* Restart event listening */
					this.partialGroupDb.setEventListening(true);
				}
			}
		}
		
		@Override
		public void beforeWrite() {
		}
		
	}
	
	public AccessControlManagerOnPersistence(XydraPersistence persistence, XID executingActorId) {
		super();
		
		WritableRepositoryOnPersistence repoOnPersistence = new WritableRepositoryOnPersistence(
		        persistence, executingActorId);
		
		/* ------------ GROUPS -------------- */
		/* fast in-memory, initially empty */
		MemoryGroupDatabase memoryGroupDatabase = new MemoryGroupDatabase();
		
		// wrap persistence into groupModel-like API
		repoOnPersistence.createModel(NamingUtils.ID_GROUPS_MODEL);
		WritableModelOnPersistence groupModelOnPersistence = new WritableModelOnPersistence(
		        persistence, executingActorId, NamingUtils.ID_GROUPS_MODEL);
		PartialGroupDatabaseOnWritableModel partialGroupDb = new PartialGroupDatabaseOnWritableModel(
		        groupModelOnPersistence);
		/*
		 * Add beforeRead-hook to in-memory DB for sync from persistence to
		 * in-memory DB
		 */
		SyncPersistenceToGroupDatabase syncPersistenceToGroupDb = new SyncPersistenceToGroupDatabase(
		        memoryGroupDatabase, groupModelOnPersistence, partialGroupDb);
		memoryGroupDatabase.addHookListener(syncPersistenceToGroupDb);
		
		/* Load groups from persistence into memory */
		partialGroupDb.loadInto(memoryGroupDatabase);
		groupModelOnPersistence.ignoreAllEventsUntilNow();
		
		/* Listen to changes in groupDb from user and write to persistence */
		memoryGroupDatabase.addListener(partialGroupDb);
		
		/* ------------ AUTHORISATION -------------- */
		MemoryAuthorisationManager memoryAuthorisationManager = new MemoryAuthorisationManager(
		        memoryGroupDatabase);
		// TODO this should not be necessary
		memoryAuthorisationManager.grantGroupAllAccessToRepository(XGroupDatabase.ADMINISTRATOR_GROUP_ID,
		        persistence.getRepositoryId());
		
		// wrap persistence into groupModel-like API
		PartialAuthorisationDatabaseOnWritableRepository partialAuthorisationDb = new PartialAuthorisationDatabaseOnWritableRepository(
		        repoOnPersistence);
		/*
		 * Add beforeRead-hook to in-memory DB for sync from persistence to
		 * in-memory DB
		 */
		HookAuthorisationManagerAndDb hookAuthorisationManagerAndDb = new SyncingHookAuthorisationManagerAndDb(
		        memoryAuthorisationManager, repoOnPersistence, partialAuthorisationDb);
		/* Load access right definitions from persistence into memory */
		partialAuthorisationDb.loadInto(hookAuthorisationManagerAndDb);
		// FIXME repoOnPersistence.ignoreAllEventsUntilNow();
		
		/*
		 * Listen to changes in authorisationDb from user and write to
		 * persistence
		 */
		hookAuthorisationManagerAndDb.addListener(partialAuthorisationDb);
		
		super.authorisationManager = hookAuthorisationManagerAndDb;
		
		/* ------------ AUTHENTICATION -------------- */
		/* wrap persistence to RMOF-like API */
		repoOnPersistence.createModel(NamingUtils.ID_AUTHENTICATION_MODEL);
		XWritableModel authenticationModelOnPersistence = new WritableModelOnPersistence(
		        persistence, executingActorId, NamingUtils.ID_AUTHENTICATION_MODEL);
		/* wrap RMOF in authentication database API */
		XAuthenticationDatabase authenticationDbOnPersistence = new AuthenticationDatabaseOnWritableModel(
		        authenticationModelOnPersistence);
		/* wrap with an in-memory cache */
		super.authenticationDb = new CachingOrMemoryAuthenticationDatabase(
		        authenticationDbOnPersistence);
	}
	
}
