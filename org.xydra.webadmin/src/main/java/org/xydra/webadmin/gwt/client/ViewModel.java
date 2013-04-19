package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.EntityTree;


/**
 * Knows about each address if it is presented right now
 * 
 * @author andi
 */
public class ViewModel {
	
	private static final Logger log = LoggerFactory.getLogger(ViewModel.class);
	
	private EntityTree entityTree;
	
	public ViewModel() {
		this.entityTree = new EntityTree();
	}
	
	public void openLocation(XAddress entityAddress) {
		this.entityTree.add(entityAddress);
		
		// log.info(this.entityTree.toString());
	}
	
	public void closeLocation(XAddress entityAddress) {
		this.entityTree.remove(entityAddress);
	}
	
	public Set<XId> getOpenRepos() {
		return this.entityTree.getOpenRepos();
	}
	
}
