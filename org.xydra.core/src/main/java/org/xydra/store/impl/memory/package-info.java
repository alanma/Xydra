/**
 * The main implementation strategy for {@link org.xydra.store.XydraStore} is
 * this:
 * <ul>
 * <li> {@link org.xydra.store.impl.delegating.DelegatingSecureStore} implements
 * {@link org.xydra.store.XydraStore}</li>
 * 
 * <li> {@link org.xydra.store.impl.memory.DelegatingAllowAllStore} implements
 * {@link org.xydra.store.XydraStore} - but allows everything</li>
 * 
 * <li> {@link org.xydra.store.impl.delegating.DelegatingAsyncBatchPersistence}
 * implements {@link org.xydra.store.impl.delegating.XydraAsyncBatchPersistence} -
 * maps to synchronous methods</li>
 * 
 * <li> {@link org.xydra.store.impl.memory.MemoryBlockingPersistence} implements
 * {@link org.xydra.store.impl.delegating.XydraBlockingPersistence}</li>
 * 
 * <li>Uses internally for each model a
 * {@link org.xydra.store.impl.memory.MemoryModelPersistence}</li>
 */
package org.xydra.store.impl.memory;

