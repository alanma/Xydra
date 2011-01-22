package org.xydra.base.rmof;

public interface XRevWritableField extends XWritableField {
	
	/**
	 * @param rev the new revision number
	 */
	void setRevisionNumber(long rev);
	
}
