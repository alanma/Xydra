package org.xydra.client;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;


/**
 * An exception that indicates that the requested resource could not be found on
 * the server.
 * 
 * @author dscharrer
 * 
 */
public class NotFoundException extends RequestException {
	
	private static final long serialVersionUID = 3076607855891669653L;
	
	private final XAddress address;
	
	public NotFoundException(String address) {
		super(address + " not found");
		XAddress addr = null;
		try {
			addr = XX.toAddress(address);
		} catch(Exception e) {
			// do nothing
		}
		this.address = addr;
	}
	
	public XAddress getAddress() {
		return this.address;
	}
	
}
