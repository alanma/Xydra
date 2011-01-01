package org.xydra.core.model;

import org.xydra.core.change.XEvent;


public interface IHasChangeLog {
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this entity.
	 */
	XChangeLog getChangeLog();
	
}
