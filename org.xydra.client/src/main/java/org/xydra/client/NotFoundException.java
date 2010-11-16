package org.xydra.client;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.store.RequestException;


/**
 * An exception that indicates that the requested resource could not be found on
 * the server.
 * 
 * HTTP implementations should map any HTTP 404 response codes to this
 * exception.
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
	
	/**
	 * @return the address of the entity that could not be found or null if not
	 *         available. The availability of this information depends on the
	 *         server and service implementation and should only be used for
	 *         debugging purposes.
	 */
	public XAddress getAddress() {
		return this.address;
	}
	
}
