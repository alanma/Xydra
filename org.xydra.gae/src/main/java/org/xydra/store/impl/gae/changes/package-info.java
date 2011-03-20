/**
 * Core implementation of {@link org.xydra.store.impl.delegate.XydraPersistence}
 * with GAE datastore using our own concurrent transaction and locking concepts
 * to avoid datastore contention.
 * 
 * See {@link org.xydra.store.impl.gae.changes.GaeChangesService}.
 */
package org.xydra.store.impl.gae.changes;

