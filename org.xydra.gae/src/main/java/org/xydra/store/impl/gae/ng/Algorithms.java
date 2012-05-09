package org.xydra.store.impl.gae.ng;

import java.util.List;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;


/**
 * Executed MOF-commands
 * 
 * @author xamde
 */
public class Algorithms {
	
	public static enum Effect {
		Add, Remove, NoEffect;
		
		public static boolean modelExists(Effect e) {
			if(e == Add) {
				return true;
			} else if(e == Remove) {
				return false;
			} else {
				throw new AssertionError("Effect is neither Add nor Remove");
			}
		}
	}
	
	/**
	 * @param change must be Status.SuccessExecuted
	 * @return ..
	 */
	public static Effect effectOnModelExists(@NeverNull final GaeChange change) {
		if(change.getStatus() == Status.SuccessExecuted) {
			List<XAtomicEvent> events = change.getAtomicEvents().getFirst();
			XAtomicEvent lastEvent = events.get(events.size() - 1);
			return eventIndicatesModelExists(lastEvent) ? Effect.Add : Effect.Remove;
		}
		return Effect.NoEffect;
	}
	
	/**
	 * @param e atomic or transaction event @NeverNull
	 * @return true if model must exist after this event
	 */
	public static boolean eventIndicatesModelExists(@NeverNull final XEvent e) {
		XyAssert.xyAssert(e != null);
		assert e != null;
		XEvent event = e;
		if(event.getChangeType() == ChangeType.TRANSACTION) {
			// check only last event
			XTransactionEvent txnEvent = (XTransactionEvent)event;
			XyAssert.xyAssert(txnEvent.size() >= 1);
			event = txnEvent.getEvent(txnEvent.size() - 1);
			XyAssert.xyAssert(event != null);
			assert event != null;
		}
		XyAssert.xyAssert(event.getChangeType() != ChangeType.TRANSACTION);
		if(event.getTarget().getAddressedType() == XType.XREPOSITORY) {
			if(event.getChangeType() == ChangeType.REMOVE) {
				return false;
			}
		}
		return true;
	}
	
	public static long increaseExponentiallyWithFactorAndMaximum(long l, int f, long max) {
		long result = l * 2;
		if(result > max) {
			result = max;
		}
		return result;
	}
	
}
