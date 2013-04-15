package org.xydra.persistence;

import org.xydra.base.XAddress;


/**
 * Helper class for batch operations to group an {@link XAddress} and a boolean
 * flag.
 * 
 * @author voelkel
 */
public class GetWithAddressRequest {
	
	public GetWithAddressRequest(XAddress address, boolean includeTentative) {
		super();
		this.address = address;
		this.includeTentative = includeTentative;
	}
	
	/**
	 * Create a request that does not ask for the tentative version
	 * 
	 * @param address
	 */
	public GetWithAddressRequest(XAddress address) {
		this(address, false);
	}
	
	public final XAddress address;
	
	public final boolean includeTentative;
	
	@Override
	public String toString() {
		return this.address + " inclTentative?" + this.includeTentative;
	}
	
}
