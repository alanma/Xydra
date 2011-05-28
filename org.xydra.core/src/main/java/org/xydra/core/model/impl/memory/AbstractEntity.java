package org.xydra.core.model.impl.memory;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXID;


/**
 * Abstract super entity type for XModel, XObject and XField types containing
 * central implementations of methods, for example equals()
 * 
 * @author Kaidel
 * 
 */

public abstract class AbstractEntity implements IHasXAddress, IHasXID {
	
	protected abstract long getRevisionNumber();
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof AbstractEntity) {
			AbstractEntity entity = (AbstractEntity)object;
			return this.getAddress().equals(entity.getAddress())
			        && this.getRevisionNumber() == entity.getRevisionNumber();
		} else {
			return false;
		}
	}
	
	protected abstract AbstractEntity getFather();
	
	@Override
	public int hashCode() {
		int hashCode = this.getID().hashCode() + (int)this.getRevisionNumber();
		
		// TODO this causes objects which are "equal" to have different hash
		// codes, as equals does not check the parent revisions
		AbstractEntity father = this.getFather();
		if(father != null) {
			hashCode += father.hashCode();
		}
		
		return hashCode;
	}
}
