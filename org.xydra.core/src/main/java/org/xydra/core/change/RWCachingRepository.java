package org.xydra.core.change;

import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableRepository;


/**
 * A read- & write-caching repository.
 * 
 * @author xamde
 * 
 */
public class RWCachingRepository extends AbstractDelegatingWritableRepository {
	
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
	
	public boolean hasModel(XID id) {
		return this.diffRepo.hasModel(id);
	}
	
	public boolean isEmpty() {
		return this.diffRepo.isEmpty();
	}
	
	public XWritableModel getModel(XID modelId) {
		return this.diffRepo.getModel(modelId);
	}
	
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
}
