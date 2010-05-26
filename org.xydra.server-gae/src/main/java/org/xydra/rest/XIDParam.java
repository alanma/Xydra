package org.xydra.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.X;
import org.xydra.core.model.XID;



/**
 * Helper class to let Jersey automatically parse XID parameters.
 * 
 * @author dscharrer
 * 
 */
public class XIDParam {
	
	private final XID xid;
	
	public XIDParam(String str) {
		
		try {
			this.xid = X.getIDProvider().fromString(str);
		} catch(IllegalArgumentException ufe) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(
			        "invalid XID parameter: " + str).build());
		}
		
	}
	
	public XID getId() {
		return this.xid;
	}
	
}
