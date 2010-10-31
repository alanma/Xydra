/**
 * 
 */
package org.xydra.core.model.state.impl.gae;

import org.xydra.core.model.state.XStateTransaction;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Transaction;


public class GaeStateTransaction implements XStateTransaction {
	
	Transaction gaeTransaction;
	
	private GaeStateTransaction(Transaction trans) {
		this.gaeTransaction = trans;
	}
	
	@Override
	public int hashCode() {
		return this.gaeTransaction.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof GaeStateTransaction
		        && ((GaeStateTransaction)other).gaeTransaction.equals(this.gaeTransaction);
	}
	
	public static Transaction asTransaction(XStateTransaction trans) {
		
		if(trans == null) {
			return null;
		}
		
		if(!(trans instanceof GaeStateTransaction)) {
			throw new IllegalArgumentException("unexpected transaction object of type "
			        + trans.getClass());
		}
		
		return ((GaeStateTransaction)trans).gaeTransaction;
	}
	
	public static Transaction getOrBeginTransaction(XStateTransaction trans) {
		if(trans == null) {
			return GaeUtils.beginTransaction();
		} else {
			Transaction t = asTransaction(trans);
			assert t != null;
			return t;
		}
	}
	
	public static XStateTransaction beginTransaction() {
		return new GaeStateTransaction(GaeUtils.beginTransaction());
	}
}
