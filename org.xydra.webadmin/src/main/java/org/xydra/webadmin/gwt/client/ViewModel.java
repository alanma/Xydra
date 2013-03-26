package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.EntityTree;


public class ViewModel {
	
	private static ViewModel instance;
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	private EntityTree entityTree;
	
	private ViewModel() {
		
		this.entityTree = new EntityTree();
	}
	
	public static ViewModel getInstance() {
		if(instance == null)
			instance = new ViewModel();
		return instance;
	}
	
	public void openLocation(XAddress entityAddress) {
		this.entityTree.add(entityAddress);
		
		log.info(entityTree.toString());
	}
	
	public void closeLocation(XAddress entityAddress) {
		this.entityTree.remove(entityAddress);
	}
	
	public Set<XId> getOpenRepos() {
		return this.entityTree.getOpenRepos();
	}
	
}
