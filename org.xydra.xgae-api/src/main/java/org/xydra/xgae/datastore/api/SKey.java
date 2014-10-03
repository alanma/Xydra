package org.xydra.xgae.datastore.api;

public interface SKey extends SWrapper, SValue {

	/**
	 * @return the 'kind' part, encoding type information
	 */
	String getKind();

	String getName();

}
