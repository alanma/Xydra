package org.xydra.store;

import org.xydra.base.change.XCommand;
import org.xydra.index.query.Pair;
import org.xydra.sharedutils.XyAssert;


/**
 * The result of executing a {@link XCommand} within a batch operation in the
 * {@link XydraStore} API.
 * 
 * Contains either a result or the exception.
 * 
 * @author xamde
 * 
 * @param <K> type of result
 */
public class BatchedResult<K> extends Pair<K,Throwable> {
	
	/**
	 * @param result of {@link BatchedResult}
	 */
	public BatchedResult(K result) {
		super(result, null);
	}
	
	public BatchedResult(Throwable t) {
		super(null, t);
		XyAssert.xyAssert(t != null); assert t != null;
	}
	
	/**
	 * @return the exception caused by executing the {@link XCommand}. If null,
	 *         there was no exception and {@link #getResult()} contains the
	 *         result.
	 */
	public Throwable getException() {
		return this.getSecond();
	}
	
	/**
	 * @return the result of executing a {@link XCommand}. If null, the
	 *         {@link #getException()} is non-null.
	 */
	public K getResult() {
		return this.getFirst();
	}
	
}
