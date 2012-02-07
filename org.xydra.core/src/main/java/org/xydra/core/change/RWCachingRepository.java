package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * A read- & write-caching repository.
 * 
 * @author xamde
 * 
 */
public class RWCachingRepository extends AbstractDelegatingWritableRepository {
	
	private static final Logger log = LoggerFactory.getLogger(RWCachingRepository.class);
	
	private ReadCachingWritableRepository2 readRepo;
	private ChangedRepository diffRepo;
	
	@Override
	public XAddress getAddress() {
		return this.diffRepo.getAddress();
	}
	
	@Override
	public XID getID() {
		return this.diffRepo.getID();
	}
	
	@Override
	public boolean hasModel(XID id) {
		return this.diffRepo.hasModel(id);
	}
	
	@Override
	public boolean isEmpty() {
		return this.diffRepo.isEmpty();
	}
	
	@Override
	public XWritableModel getModel(XID modelId) {
		return this.diffRepo.getModel(modelId);
	}
	
	@Override
	public boolean removeModel(XID modelId) {
		return this.diffRepo.removeModel(modelId);
	}
	
	public RWCachingRepository(XWritableRepository baseRepository, XydraPersistence persistence,
	        boolean prefetchModels) {
		super(baseRepository);
		this.readRepo = new ReadCachingWritableRepository2(persistence, prefetchModels);
		this.diffRepo = new ChangedRepository(this.readRepo);
	}
	
	@Override
	public XWritableModel createModel(XID modelId) {
		return this.diffRepo.createModel(modelId);
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.diffRepo.iterator();
	}
	
	public ChangedRepository getDiffWritableRepository() {
		return this.diffRepo;
	}
	
	public ReadCachingWritableRepository2 getReadCachingWritableRepository() {
		return this.readRepo;
	}
	
	/**
	 * Write changes in batch.
	 * 
	 * @param rwcRepo containing all changes to write. Changes are committed to
	 *            the base repository.
	 * @param actorId ..
	 * @return the command result. 200 = OK, 500-599 = error.
	 */
	public synchronized static int commit(RWCachingRepository rwcRepo, XID actorId) {
		XID repositoryId = rwcRepo.getID();
		ChangedRepository diffRepo = rwcRepo.getDiffWritableRepository();
		
		XydraPersistence persistence = XydraRuntime.getPersistence(repositoryId);
		WritableRepositoryOnPersistence baseRepo = new WritableRepositoryOnPersistence(persistence,
		        actorId);
		
		int result = 200;
		for(XID id : diffRepo.getRemoved()) {
			boolean partialResult = baseRepo.removeModel(id);
			if(!partialResult) {
				// TODO use GA?...
				log.warn("Could not remove model '" + id + "' while comitting");
			}
		}
		for(ChangedModel addedModel : diffRepo.getAdded()) {
			baseRepo.createModel(addedModel.getID());
			if(addedModel.hasChanges()) {
				XTransactionBuilder builder = new XTransactionBuilder(addedModel.getAddress());
				builder.applyChanges(addedModel);
				XTransaction txn = builder.build();
				
				// FIXME check txn that no object is created twice
				
				// XTransaction txn = addedModel.toTransaction();
				int partialResult = executeModelTransacton(repositoryId, addedModel.getID(), txn,
				        actorId);
				if(partialResult != 200) {
					log.warn("In model '" + addedModel.getID() + "' could not execute txn: "
					        + txnToString(txn));
					result = partialResult;
				}
			}
		}
		for(ChangedModel potentiallyChangedModel : diffRepo.getPotentiallyChanged()) {
			if(potentiallyChangedModel.hasChanges()) {
				XTransactionBuilder builder = new XTransactionBuilder(
				        potentiallyChangedModel.getAddress());
				builder.applyChanges(potentiallyChangedModel);
				XTransaction txn = builder.build();
				
				// XTransaction txn = potentiallyChangedModel.toTransaction();
				int partialResult = executeModelTransacton(repositoryId,
				        potentiallyChangedModel.getID(), txn, actorId);
				if(partialResult != 200) {
					log.warn("In model '" + potentiallyChangedModel.getID()
					        + "' could not execute txn: " + txnToString(txn));
					result = partialResult;
				}
			}
		}
		return result;
	}
	
	private static String txnToString(XTransaction txn) {
		StringBuffer buf = new StringBuffer();
		for(XAtomicCommand command : txn) {
			buf.append(command.toString()).append("\n");
		}
		return buf.toString();
	}
	
	/**
	 * @param repositoryId
	 * @param modelId
	 * @param txn may be null
	 * @param actorId
	 */
	private static int executeModelTransacton(XID repositoryId, XID modelId, XTransaction txn,
	        XID actorId) {
		if(txn != null) {
			long l = XydraRuntime.getPersistence(repositoryId).executeCommand(actorId, txn);
			if(l < 0) {
				log.warn("Could not execute non-empty txn " + l + " for " + txn + " = "
				        + txnToString(txn));
				return (int)(600 + l);
			}
		}
		return 200;
	}
	
	// @Deprecated
	// public Set<XID> getChangedModelIds() {
	// Set<XID> changedModelIds = new HashSet<XID>();
	// DiffWritableRepository wcRepo = this.getDiffWritableRepository();
	// for(XID id : wcRepo.getRemoved()) {
	// changedModelIds.add(id);
	// }
	// for(DiffWritableModel addedModel : wcRepo.getAdded()) {
	// changedModelIds.add(addedModel.getID());
	// }
	// for(DiffWritableModel potentiallyChangedModel :
	// wcRepo.getPotentiallyChanged()) {
	// if(potentiallyChangedModel.hasChanges()) {
	// changedModelIds.add(potentiallyChangedModel.getID());
	// }
	// }
	// return changedModelIds;
	// }
}
