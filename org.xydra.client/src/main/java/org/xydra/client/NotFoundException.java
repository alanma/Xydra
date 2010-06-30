package org.xydra.client;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;


public class NotFoundException extends HttpException {
	
	private static final long serialVersionUID = 3076607855891669653L;
	
	private final XAddress address;
	
	public NotFoundException(String address) {
		super(404, address + " not found");
		XAddress addr = null;
		try {
			addr = X.getIDProvider().fromAddress(address);
		} catch(Exception e) {
			// do nothing
		}
		this.address = addr;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
}
