package org.xydra.server;

import java.util.HashMap;
import java.util.Map;


import org.xydra.core.XX;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.CompositeAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.core.model.session.impl.arm.AbstractArmProtectedRepository;


public class RepositoryManager {
	
	static final Object COOKIE_ACTOR = "actor";
	
	static private XRepository repository;
	
	static private XGroupDatabase groups;
	
	static private XAccessManager repoAccess;
	static private Map<XID,XAccessManager> modelAccess = new HashMap<XID,XAccessManager>();
	
	private static ArmLoader armLoader;
	
	public static interface ArmLoader {
		
		public XAccessManager loadArmForModel(XAddress modelAddr, XGroupDatabase groups);
		
	}
	
	public static synchronized XRepository getRepository() {
		return repository;
	}
	
	public static void setRepository(XRepository repo) {
		
		if(repository != null) {
			throw new IllegalStateException("the repository can only be set once");
		}
		
		repository = repo;
		
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
		return groups;
	}
	
	public static void setGroups(XGroupDatabase groupdb) {
		
		if(groups != null) {
			throw new IllegalStateException("the group database can only be set once");
		}
		
		groups = groupdb;
		
	}
	
	/**
	 * @return the ARM responsible for accesses to the repository but to models.
	 */
	public static synchronized XAccessManager getArmForRepository() {
		return repoAccess;
	}
	
	public static void setAccessManager(XAccessManager arm, ArmLoader loader) {
		
		if(repoAccess != null) {
			throw new IllegalStateException("the access manager can only be set once");
		}
		
		repoAccess = arm;
		armLoader = loader;
		
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
		
		if(armLoader == null) {
			return repoArm;
		}
		
		XAccessManager arm = modelAccess.get(modelAddr.getModel());
		
		if(arm == null) {
			
			arm = armLoader.loadArmForModel(modelAddr, getGroups());
			
			arm = new CompositeAccessManager(modelAddr, repoArm, arm);
			
		}
		
		return arm;
	}
	
	public static boolean isInitialized() {
		return repository != null && repoAccess != null && groups != null;
	}
	
}
