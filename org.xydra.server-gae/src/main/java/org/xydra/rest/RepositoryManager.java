package org.xydra.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.gae.GaeAccess;
import org.xydra.core.access.impl.gae.GaeGroups;
import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.session.impl.arm.AbstractArmProtectedRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.gae.GaeStateStore;



public class RepositoryManager {
	
	private static final Object COOKIE_ACTOR = "actor";
	
	static private XRepository repository;
	
	static private XGroupDatabase groups;
	
	static private XAccessManager repoAccess;
	static private Map<XID,XAccessManager> modelAccess = new HashMap<XID,XAccessManager>();
	
	public static synchronized XRepository getRepository() {
		
		if(repository == null) {
			
			// configure XSPI
			XSPI.setStateStore(new GaeStateStore());
			
			repository = X.createMemoryRepository();
			
		}
		
		return repository;
	}
	
	/**
	 * @return the Repository of this server.
	 */
	public static synchronized XProtectedRepository getRepository(XID actor) {
		
		return new AbstractArmProtectedRepository(getRepository(), getArmForRepository(), actor) {
			
			@Override
			protected XAccessManager getArmForModel(XID modelId) {
				XAddress modelAddr = XX.resolveModel(getAddress(), modelId);
				return RepositoryManager.getArmForModel(getArm(), modelAddr);
			}
		};
		
	}
	
	/**
	 * @return the group database for this server.
	 */
	public static synchronized XGroupDatabase getGroups() {
		
		if(groups == null) {
			
			groups = GaeGroups.loadGroups();
			
		}
		
		return groups;
	}
	
	/**
	 * @return the ARM responsible for accesses to the repository but to models.
	 */
	public static synchronized XAccessManager getArmForRepository() {
		
		if(repoAccess == null) {
			
			repoAccess = GaeAccess.loadAccessManager(getRepository().getAddress(), getGroups());
			
		}
		
		return repoAccess;
	}
	
	/**
	 * @return the ARM responsible for accesses to the specified model (and all
	 *         contained objects and fields) or the repository but not other
	 *         models.
	 */
	public static XAccessManager getArmForModel(XAddress modelAddr) {
		return getArmForModel(getArmForRepository(), modelAddr);
	}
	
	private static synchronized XAccessManager getArmForModel(XAccessManager repoArm,
	        XAddress modelAddr) {
		
		assert modelAddr.getRepository() == getRepository().getID();
		
		XAccessManager arm = modelAccess.get(modelAddr.getModel());
		
		if(arm == null) {
			
			arm = GaeAccess.loadAccessManager(modelAddr, getGroups());
			
			arm = new CompositeAccessManager(modelAddr, getArmForRepository(), arm);
			
		}
		
		return arm;
	}
	
	/**
	 * Get and authenticate the current user.
	 * 
	 * @param headers The request headers.
	 * @return The authenticated actor or null if no actor was specified.
	 * @throws WebApplicationException if an actor was specified but could not
	 *             be authenticated
	 */
	public static synchronized XID getActor(HttpHeaders headers) {
		
		try {
			
			Cookie cookie = headers.getCookies().get(COOKIE_ACTOR);
			
			if(cookie == null) {
				// anonymous
				return null;
			}
			
			XID actorId = X.getIDProvider().fromString(cookie.getValue());
			
			// TODO authenticate
			
			return actorId;
			
		} catch(IllegalArgumentException iae) {
			// authentication failed (actor was not a valid XID)
			// TODO find a better HTTP status type to differentiate between
			// failed login and denied access
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		}
		
	}
	
}
