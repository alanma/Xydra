package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraRuntime;


/**
 * A read- & write-caching repository.
 * 
 * @author xamde
 * 
 */
public class RWCachingRepository extends AbstractDelegatingWritableRepository {
	
	private static final Logger log = LoggerFactory.getLogger(RWCachingRepository.class);
	
	private ReadCachingWritableRepository readRepo;
	private DiffWritableRepository diffRepo;
	
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
	
	public RWCachingRepository(XWritableRepository baseRepository, boolean prefetchModels) {
		super(baseRepository);
		this.readRepo = new ReadCachingWritableRepository(baseRepository, prefetchModels);
		this.diffRepo = new DiffWritableRepository(this.readRepo);
	}
	
	@Override
	public XWritableModel createModel(XID modelId) {
		return this.diffRepo.createModel(modelId);
	}
	
	@Override
	public Iterator<XID> iterator() {
		return this.diffRepo.iterator();
	}
	
	public DiffWritableRepository getDiffWritableRepository() {
		return this.diffRepo;
	}
	
	public ReadCachingWritableRepository getReadCachingWritableRepository() {
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
	public static int commit(RWCachingRepository rwcRepo, XID actorId) {
		XID repositoryId = rwcRepo.getID();
		DiffWritableRepository wcRepo = rwcRepo.getDiffWritableRepository();
		ReadCachingWritableRepository rcRepo = rwcRepo.getReadCachingWritableRepository();
		XWritableRepository baseRepo = rcRepo.getBaseRepository();
		
		int result = 200;
		for(XID id : wcRepo.getRemoved()) {
			boolean partialResult = baseRepo.removeModel(id);
			if(!partialResult) {
				log.warn("Could not remove model '" + id + "' while comitting");
			}
		}
		for(DiffWritableModel addedModel : wcRepo.getAdded()) {
			baseRepo.createModel(addedModel.getID());
			XTransaction txn = addedModel.toTransaction();
			int partialResult = executeTransacton(repositoryId, txn, actorId);
			if(partialResult != 200) {
				log.warn("In model '" + addedModel.getID() + "' could not execute txn: "
				        + txnToString(txn));
				result = partialResult;
			}
		}
		for(DiffWritableModel potentiallyChangedModel : wcRepo.getPotentiallyChanged()) {
			XTransaction txn = potentiallyChangedModel.toTransaction();
			int partialResult = executeTransacton(repositoryId, txn, actorId);
			if(partialResult != 200) {
				log.warn("In model '" + potentiallyChangedModel.getID()
				        + "' could not execute txn: " + txnToString(txn));
				result = partialResult;
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
	 * @param txn may be null
	 * @param actorId
	 */
	private static int executeTransacton(XID repositoryId, XTransaction txn, XID actorId) {
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
}
