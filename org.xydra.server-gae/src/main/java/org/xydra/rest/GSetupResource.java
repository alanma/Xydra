package org.xydra.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.xydra.core.X;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;
import org.xydra.core.test.DemoModelUtil;



@Path("gsetup")
public class GSetupResource {
	
	@GET
	// create and persist phonebook-demo model
	@Produces("application/xml")
	public String init() {
		
		// configure
		XSPI.setStateStore(new GaeStateStore());
		
		XRepository repository = X.createMemoryRepository();
		
		// this persists the phonebook also in underlying GAE
		DemoModelUtil.addPhonebookModel(repository);
		
		return "done";
	}
	
}
