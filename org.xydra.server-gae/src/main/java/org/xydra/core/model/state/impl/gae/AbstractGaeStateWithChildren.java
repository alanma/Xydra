package org.xydra.core.model.state.impl.gae;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.server.gae.GaeSchema;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;


/**
 * Make sure to call super.loadFromEntity and super.storeInEntity in
 * sub-classes.
 * 
 * @author voelkel
 */
public abstract class AbstractGaeStateWithChildren extends AbstractGaeState {
	
	protected Set<XID> children;
	
	public AbstractGaeStateWithChildren(XAddress address) {
		super(address);
		this.children = new HashSet<XID>();
	}
	
	/** Load revisionNumber, parent, children */
	@Override
	public void loadFromEntity(Entity e) {
		super.loadFromEntity(e);
		// children
		@SuppressWarnings("unchecked")
		List<String> children = (List<String>)e.getProperty(GaeSchema.PROP_CHILD_IDS);
		if(children != null) {
			for(String s : children) {
				this.children.add(X.getIDProvider().fromString(s));
			}
		}
	}
	
	@Override
	/** Save revisionNumber, parent, children */
	protected void storeInEntity(Entity e) {
		super.storeInEntity(e);
		// children
		List<String> children = new LinkedList<String>();
		for(XID id : this.children) {
			children.add(id.toString());
		}
		e.setUnindexedProperty(GaeSchema.PROP_CHILD_IDS, children);
	}
	
	@Override
	public Object beginTransaction() {
		return GaeUtils.beginTransaction();
	}
	
	@Override
	public void endTransaction(Object trans) {
		GaeUtils.endTransaction(trans);
	}
	
}
