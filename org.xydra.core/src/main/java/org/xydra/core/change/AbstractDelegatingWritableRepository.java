package org.xydra.core.change;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableRepository;


/**
 * A abstract helper class for the commonalities between
 * {@link XWritableRepository} implementations that have a delegation strategy
 * to an internal state.
 * 
 * 
 * @author xamde
 * 
 */
public abstract class AbstractDelegatingWritableRepository implements XWritableRepository {
	
	public AbstractDelegatingWritableRepository(XWritableRepository baseRepository) {
		this.baseRepository = baseRepository;
	}
	
	protected XWritableRepository baseRepository;
	
	@Override
	public XAddress getAddress() {
		return XX.toAddress(getID(), null, null, null);
	}
	
	@Override
	public XID getID() {
		return this.baseRepository.getID();
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
	public XWritableRepository getBaseRepository() {
		return this.baseRepository;
	}
	
}