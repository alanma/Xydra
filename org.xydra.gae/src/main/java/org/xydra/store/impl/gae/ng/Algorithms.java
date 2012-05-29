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


/**
 * Executed MOF-commands
 * 
 * @author xamde
 */
public class Algorithms {
	
	public static enum Effect {
		Add, Remove, NoEffect;
		
		/**
		 * @param effect
		 * @return true if model has just been added, false if removed
		 * @throws IllegalArgumentException if effect is neither add nor remove
		 */
		public static boolean modelExists(Effect effect) {
			if(effect == Add) {
				return true;
			} else if(effect == Remove) {
				return false;
			} else {
				throw new IllegalArgumentException("Effect is neither Add nor Remove");
			}
		}
	}
	
	/**
	 * @param change must be Status.SuccessExecuted
	 * @return ..
	 */
	@Deprecated
	public static Effect effectOnModelExistsFlag(@NeverNull final GaeChange change) {
		if(change.getStatus().changedSomething()) {
			
			List<XAtomicEvent> atomics = change.getAtomicEvents().getFirst();
			XAtomicEvent last = atomics.get(atomics.size() - 1);
			return eventIndicatesModelExists(last) ? Effect.Add : Effect.Remove;
			
			// XEvent event = change.getEvent();
			// if(event instanceof XTransactionEvent) {
			// event = ((XTransactionEvent)event).getLastEvent();
			// }
			// return eventIndicatesModelExists(event) ? Effect.Add :
			// Effect.Remove;
		}
		return Effect.NoEffect;
	}
	
	/**
	 * @param change must have a status that changed something
	 * @return true if the change indicates that the model exists; false if
	 *         model does not exists.
	 * @throws IllegalArgumentException if change did not change anything or
	 *             even failed
	 */
	public static boolean changeIndicatesModelExists(@NeverNull final GaeChange change) {
		if(!change.getStatus().changedSomething()) {
			throw new IllegalArgumentException("change must have changed something but is "
			        + change);
		}
		
		List<XAtomicEvent> atomics = change.getAtomicEvents().getFirst();
		XAtomicEvent last = atomics.get(atomics.size() - 1);
		return eventIndicatesModelExists(last);
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
