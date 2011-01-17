package org.xydra.store.impl.delegate;

/**
 * Note: The package-info comment doesn't render nicely in Eclipse. Therefore
 * this helper interface is used.
 * 
 * <table>
 * <tr>
 * <th>Batch operations</th>
 * <th>Concurrency</th>
 * <th>Authorisation</th>
 * <th>Access control</th>
 * <th>Interface</th>
 * <th>Implementation</th>
 * </tr>
 * 
 * <tr>
 * <td>Yes</td>
 * <td>Asynchronous</td>
 * <td>Yes, requires password</td>
 * <td>Yes</td>
 * <td> {@link org.xydra.store.XydraStore XydraStore}</td>
 * <td> {@link org.xydra.store.impl.DelegateToSingleOperationStore
 * DelegateToSingleOperationStore}</td>
 * </tr>
 * 
 * <tr>
 * <td>No</td>
 * <td>Asynchronous</td>
 * <td>Yes, allows null password</td>
 * <td>Yes</td>
 * <td> {@link org.xydra.store.impl.delegate.XydraSingleOperationStore
 * XydraSingleOperationStore}</td>
 * <td> {@link org.xydra.store.impl.delegate.DelegateToBlockingStore
 * DelegateToBlockingStore}</td>
 * </tr>
 * 
 * <tr>
 * <td>No</td>
 * <td>Blocking (synchronous)</td>
 * <td>Yes, allows null password</td>
 * <td>Yes</td>
 * <td> {@link org.xydra.store.impl.delegate.XydraBlockingStore
 * XydraBlockingStore}</td>
 * <td> {@link org.xydra.store.impl.delegate.DelegateToBlockingStore
 * DelegateToBlockingStore}</td>
 * </tr>
 * 
 * <tr>
 * <td>No</td>
 * <td>Blocking (synchronous)</td>
 * <td>No</td>
 * <td>No</td>
 * <td> {@link org.xydra.store.impl.delegate.XydraPersistence
 * XydraBlockingPersistence}</td>
 * <td> {@link org.xydra.store.impl.delegate.DelegateToBlockingStore
 * DelegateToBlockingStore}</td>
 * </tr>
 * 
 * </table>
 * 
 * @author xamde
 * 
 */
public interface Documentation {
	
}
