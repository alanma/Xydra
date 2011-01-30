/**
 * Classes for representing and modifying a set of changes to
 * {@link org.xydra.core.model.XModel}s, {@link org.xydra.core.model.XObject}s
 * and {@link org.xydra.core.model.XField}s
 * 
 * These are used internally by some {@link org.xydra.core.model.XModel} and
 * {@link org.xydra.store.impl.delegate.XydraPersistence} implementations to
 * evaluate transactions.
 * 
 * They can also be used to construct transactions together with
 * {@link org.xydra.core.change.XTransactionBuilder}. Their result is undefined
 * if the underlying entities are changed directly, so the user needs to ensure
 * proper locking.
 */
package org.xydra.core.model.delta;

