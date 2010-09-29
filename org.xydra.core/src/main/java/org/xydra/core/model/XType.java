package org.xydra.core.model;

/**
 * an enumeration used by {@link XAddress XAddresses} to tell what kind of Xydra
 * entity they refer to.
 * 
 */
public enum XType {
	
	XREPOSITORY, XMODEL, XOBJECT, XFIELD;
	
	/**
	 * @return the XType of entities containing entities of this XType or null
	 *         for XREPOSITORY
	 */
	public XType getParentType() {
		switch(this) {
		case XMODEL:
			return XREPOSITORY;
		case XOBJECT:
			return XMODEL;
		case XFIELD:
			return XOBJECT;
		default:
			return null;
		}
	}
	
	/**
	 * @return the XType of entities contained in entities of this XType or null
	 *         for XFIELD
	 */
	public XType getChildType() {
		switch(this) {
		case XREPOSITORY:
			return XMODEL;
		case XMODEL:
			return XOBJECT;
		case XOBJECT:
			return XFIELD;
		default:
			return null;
		}
	}
	
}
