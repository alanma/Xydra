package org.xydra.base.rmof.impl;

import org.xydra.base.IHasXAddress;
import org.xydra.base.rmof.XRevisionReadable;


/**
 * Marker interface to denote entities that can be used in a sync process
 *
 * @author xamde
 */
public interface ISyncableState extends XRevisionReadable, IHasXAddress {

}
