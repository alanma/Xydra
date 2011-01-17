/**
 * Simple implementations of the read-only
 * {@link org.xydra.core.model.XBaseRepository},
 * {@link org.xydra.core.model.XBaseModel},
 * {@link org.xydra.core.model.XBaseObject} and
 * {@link org.xydra.core.model.XBaseField} which can be used synchronously on
 * the server-side.
 * 
 * Base{Repository,Model,Object,Field} implements
 * XBase{Repository,Model,Object,Field}.
 * 
 * Simple{Repository,Model,Object,Field} extends
 * Base{Repository,Model,Object,Field}, implements
 * Writable{Repository,Model,Object,Field}.
 * 
 * They facilitate the implementation of the access rights manager and im-memory
 * implementations.
 */
package org.xydra.store.base;

