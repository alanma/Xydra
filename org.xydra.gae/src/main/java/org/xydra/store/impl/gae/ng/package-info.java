/**
 * Concept for executing commands:
 * 
 * 
 * Concurrent transactions are sequentialised by using hierarchical locks. Once
 * a change knows that all locks with a lower revision number are not
 * conflicting, it can go and proceed, even if other changes in parallel change
 * other parts of the model.
 * 
 * The next step is to compute if a command can be executed, i.e. if it tries to
 * remove only things that actually exist. Here commands differ whether they are
 * forced or safe commands. Safe commands further complicated things as they
 * refer to previous revision numbers. A major change are transactions, since
 * they contain a list of commands. While working on the transaction, an
 * intermediate state must be computed to check if the next commands are legal
 * or not. Then the resulting changes - revision numbers, snapshots - must be
 * reflected back in the main state.
 * 
 * To speed things up, an always-up-to-date tentative object state is maintained
 * and stored in data store. For transactions, this state may not be changed
 * until it is sure that the transaction will succeed as a whole.
 * 
 * So the basic flow is:
 * <ol>
 * <li>Grab the next free revision number starting from 0 or a cached higher
 * value.</li>
 * <li>Wait until a active, conflicting locks below own revision number are
 * completed.</li>
 * <li>Compute if command is legal; compute resulting events.</li>
 * <li>Write events in change and set status to committed.</li>
 * </ol>
 * 
 * Open research questions:
 * 
 * How to deal with timed-out changes - let them fail, because the caller can no
 * longer be notified of the result OR roll them forward because failed changes
 * clutter up the changelog unnecessarily and will mostly be tried anyway. If a
 * command timed out, it might simply take too long, so all retries or
 * roll-fowards will also fail.
 */
package org.xydra.store.impl.gae.ng;

