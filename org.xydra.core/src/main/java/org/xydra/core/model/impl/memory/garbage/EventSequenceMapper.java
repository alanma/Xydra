package org.xydra.core.model.impl.memory.garbage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.index.query.Pair;


/**
 * Determines which {@link XLocalChanges} have been applied remotely and which
 * have not been applied aka failed. In addition, the serverEvents that have
 * originated remotely and are yet unknown locally are determined.
 *
 * Warning: This code has not been used in production and is of prototypical
 * nature on a work-in-progress towards a new synchronizer. It needs tests and
 * work when dealing with Transactions.
 *
 * @author alpha
 *
 */
public class EventSequenceMapper {

	/**
	 * FIXME Transactions in serverEvents are not yet considered as a whole only
	 * their contained events are mapped.
	 *
	 * @param serverEvents - an array of serverEvents returned from
	 *            synchronization
	 * @param localChanges - the localChanges that happened on the client since
	 *            last synchronization
	 * @return the {@link Result} - the mapping result, including mapped events,
	 *         non-mapped localChanges and non-mapped serverEvents
	 */
	public static Result map(final XEvent[] serverEvents, final XLocalChanges localChanges) {
		final List<XEvent> unpackedServerEvents = unpackImpliedTxEvents(serverEvents);
		/*
		 * FIXME Transactions in serverEvents are not yet considered as a whole
		 * only their contained events are mapped.
		 */
		return mapServerEventsToLocalChanges(unpackedServerEvents, localChanges);
	}

	/**
	 * Find a longest sub-sequence of events from the specified
	 * {@link XLocalChanges} in the sequence of the specified {@link XEvent}
	 * serverEvents.
	 *
	 * Find a longest sub-sequence of events from the specified
	 * {@link XLocalChanges} in the sequence of the specified {@link XEvent}
	 * serverEvents. As many events from localChanges as possible should appear.
	 * Additional events can appear between any two events from localChanges.
	 * E.g. if the serverEvents is ABCDEFGHIJKLM and localChanges is CEGXL the
	 * longest sequence is CEGL. The problem is know as the Longest common
	 * subsequence problem.
	 *
	 * The implementation is using an algorithm similar to the one described
	 * here: http://rosettacode.org/wiki/Longest_common_subsequence#
	 * Dynamic_Programming_2
	 *
	 * @param unpackedServerEvents - the serverEvents in which transaction that
	 *            only contain implied events and one non-implied event (those
	 *            tx resulting from a remove {@link XCommand})are replaced with
	 *            the events they contain
	 * @param localChanges - the localchanges that happened on the client since
	 *            last synchronization
	 * @return the {@link Result} - the mapping result, including mapped events,
	 *         non-mapped localChanges and non-mapped serverEvents
	 */
	private static Result mapServerEventsToLocalChanges(final List<XEvent> unpackedServerEvents,
	        final XLocalChanges localChanges) {

		final List<LocalChange> nonMappedLocalEvents = new ArrayList<LocalChange>();
		final List<XEvent> nonMappedServerEvents = new ArrayList<XEvent>();
		final List<Pair<XEvent,LocalChange>> mapped = new ArrayList<Pair<XEvent,LocalChange>>();

		final int[][] matches = calcMatches(unpackedServerEvents, localChanges);

		final int numServerEvents = unpackedServerEvents.size();
		final int numLocalChanges = localChanges.getList().size();
		/*
		 * Backtrack through the matches matrix to find a longest sequence,
		 * there can be multiple equally long sequences, but the algorithm
		 * deterministically finds only one
		 */
		for(int x = numServerEvents, y = numLocalChanges; x > 0 || y > 0;) {
			if(x > 0 && matches[x][y] == matches[x - 1][y]) {
				final XEvent serverEvent = unpackedServerEvents.get(x - 1);
				nonMappedServerEvents.add(serverEvent);
				x--;
			} else if(y > 0 && matches[x][y] == matches[x][y - 1]) {
				nonMappedLocalEvents.add(localChanges.getList().get(y - 1));
				y--;
			} else if(x > 0 && y > 0) {
				final XEvent serverEvent = unpackedServerEvents.get(x - 1);
				final LocalChange localChange = localChanges.getList().get(y - 1);
				assert isEqual(serverEvent, localChange);
				final Pair<XEvent,LocalChange> pair = new Pair<XEvent,LocalChange>(serverEvent,
				        localChange);
				mapped.add(pair);
				x--;
				y--;
			}
		}

		// The backtrack result needs to be reversed into the right order
		Collections.reverse(nonMappedLocalEvents);
		Collections.reverse(nonMappedServerEvents);
		Collections.reverse(mapped);

		assert numServerEvents == nonMappedServerEvents.size() + mapped.size();
		assert numLocalChanges == nonMappedLocalEvents.size() + mapped.size();

		return new Result(nonMappedServerEvents, nonMappedLocalEvents, mapped);
	}

	/**
	 * Calculates the matrix of matching pairs. The implementation is using an
	 * algorithm similar to the one described here:
	 * http://rosettacode.org/wiki/Longest_common_subsequence#
	 * Dynamic_Programming_2
	 *
	 * @param unpackedServerEvents - the serverEvents in which transactions that
	 *            only contain implied events and one non-implied event (those
	 *            tx resulting from a remove {@link XCommand}) are replaced with
	 *            the events they contain
	 *
	 * @param localChanges - the localChanges that happened on the client since
	 *            last synchronization
	 * @return an matrix which contains counts of matching pairs
	 */
	private static int[][] calcMatches(final List<XEvent> unpackedServerEvents, final XLocalChanges localChanges) {

		final int numServerEvents = unpackedServerEvents.size();
		final int numLocalChanges = localChanges.getList().size();
		final int[][] matches = new int[numServerEvents + 1][numLocalChanges + 1];
		for(int x = 0; x < numServerEvents; x++) {
			final XEvent serverEvent = unpackedServerEvents.get(x);
			for(int y = 0; y < numLocalChanges; y++) {
				final LocalChange localChange = localChanges.getList().get(y);
				if(isEqual(serverEvent, localChange)) {
					matches[x + 1][y + 1] = matches[x][y] + 1;
				} else {
					matches[x + 1][y + 1] = Math.max(matches[x + 1][y], matches[x][y + 1]);
				}
			}
		}
		return matches;
	}

	/**
	 * <p>
	 * Compares a local command-event Pair with a remote server event.
	 *
	 * They are considered equal if:
	 * <p>
	 * 1) localCommand is an atomicCommand, resulting in an atomicEvent and on
	 * the server is also an atomicEvent or, iff localCommand has ChangeType
	 * remove, a transaction event with implied remove events and a final
	 * non-implied remove event.
	 *
	 * Target address and changedEntityAddress must match. ChangeType must
	 * match.
	 *
	 * Additionally, the command mode of the local command is checked:
	 *
	 * mode == forced or mode == state-bound: isEqual=true;
	 *
	 * mode == revision-bound: check if oldXXXrevision matches the command
	 * condition.
	 * </p>
	 * <p>
	 * 2) localCommand is an atomicCommand, resulting in an transactionEvent and
	 * on the server is an atomicEvent or transactionEvent.
	 *
	 * If mode == forced OR mode == state-bound, match is true even if server
	 * event contains additional or less atomicEvents.
	 *
	 * If mode == revision-bound is true if revision-condition matches.
	 *
	 * </p>
	 * <p>
	 * 3) localCommand is transaction, resulting in an transactionEvent and on
	 * the server is an atomicEvent or transactionEvent.
	 *
	 * Match if all localAtomicCommands within the localTransaction match to one
	 * or several events (1:n can happen for REMOVE commands only) from the
	 * serverTransactionEvent or map to the single serverAtomicEvent if there
	 * was just one.
	 * </p>
	 * <p>
	 *
	 * 4) In other cases: No match.
	 * </p>
	 *
	 * <h3>Mapping commands to events:</h3>
	 *
	 * <ul>
	 * <li>ADD-command => ADD-event</li>
	 * <li>REMOVE-command => REMOVE-event + ( implied REMOVE-events of children)
	 * </li>
	 * <li>CHANGE-command => CHANGE-event</li>
	 * </ul>
	 *
	 * @param serverEvent
	 *
	 * @param localEvent
	 *
	 * @return
	 */
	private static boolean isEqual(final XEvent serverEvent, final LocalChange localChange) {

		final XCommand lCmd = localChange.getCommand();
		final XEvent lEvent = localChange.getEvent();
		// 4 cases
		if(lCmd instanceof XAtomicCommand) {
			if(lEvent instanceof XAtomicEvent) {
				assert lCmd instanceof XAtomicCommand;
				assert lEvent instanceof XAtomicEvent;

				if(serverEvent instanceof XAtomicEvent) {
					assert serverEvent instanceof XAtomicEvent;
					if(isLocalEventFromLocalCommandEqualToServerEvent((XAtomicCommand)lCmd,
					        (XAtomicEvent)lEvent, (XAtomicEvent)serverEvent)) {
						return true;
					}
				} else {
					assert serverEvent instanceof XTransactionEvent;

					final XTransactionEvent serverTxEvent = (XTransactionEvent)serverEvent;
					if(isTxEventResultingFromRemove(serverTxEvent)) {
						final XAtomicEvent lastServerEvent = serverTxEvent.getLastEvent();
						if(isLocalEventFromLocalCommandEqualToServerEvent((XAtomicCommand)lCmd,
						        (XAtomicEvent)lEvent, lastServerEvent)) {
							return true;
						}
					}
				}
			} else {
				assert lCmd instanceof XAtomicCommand;
				assert lEvent instanceof XTransactionEvent;

				final XTransactionEvent lTxEvent = (XTransactionEvent)lEvent;
				assert isTxEventResultingFromRemove(lTxEvent);
				final XAtomicEvent lastLocalEvent = lTxEvent.getLastEvent();

				if(serverEvent instanceof XAtomicEvent) {
					assert serverEvent instanceof XAtomicEvent;
					if(isLocalEventFromLocalCommandEqualToServerEvent((XAtomicCommand)lCmd,
					        lastLocalEvent, (XAtomicEvent)serverEvent)) {
						return true;
					}

				} else {
					assert serverEvent instanceof XTransactionEvent;

					final XTransactionEvent rTxEvent = (XTransactionEvent)serverEvent;
					assert isTxEventResultingFromRemove(rTxEvent);
					final XAtomicEvent lastRemoteEvent = rTxEvent.getLastEvent();
					if(isLocalEventFromLocalCommandEqualToServerEvent((XAtomicCommand)lCmd,
					        lastLocalEvent, lastRemoteEvent)) {
						return true;
					}
				}
			}
		} else {
			assert lCmd instanceof XTransaction;
			// could be 3, 4
		}

		// 4
		return false;
	}

	/**
	 * Tests the equality of a local event and a remote event. If the local
	 * {@link XCommand} that caused the local event is a revision bound SAFE
	 * command the revisions are also compared.
	 *
	 * @param lCmd - the local {@link XCommand}
	 * @param lEvent - the local {@link XEvent}
	 * @param rEvent - the remote {@link XEvent}
	 * @return
	 */
	private static boolean isLocalEventFromLocalCommandEqualToServerEvent(final XAtomicCommand lCmd,
	        final XAtomicEvent lEvent, final XAtomicEvent rEvent) {
		final long lCmdRev = lCmd.getRevisionNumber();
		if(rEvent.getChangeType().equals(lEvent.getChangeType())
		        && rEvent.getChangedEntity().equals(lEvent.getChangedEntity())
		        && rEvent.getTarget().equals(lEvent.getTarget())) {

			if(lCmdRev == XCommand.FORCED || lCmdRev == XCommand.SAFE_STATE_BOUND) {
				return true;
			} else {
				if(lCmd instanceof XFieldCommand) {
					assert rEvent instanceof XFieldEvent;
					if(rEvent.getOldFieldRevision() == lCmdRev) {
						return true;
					}
				} else if(lCmd instanceof XObjectCommand) {
					assert rEvent instanceof XObjectEvent;
					if(rEvent.getOldObjectRevision() == lCmdRev) {
						return true;
					}
				} else if(lCmd instanceof XModelCommand) {
					assert rEvent instanceof XModelEvent;
					if(rEvent.getOldModelRevision() == lCmdRev) {
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Extract the contained events within transactions that only contain
	 * implied events and one non-implied event (those tx resulting from a
	 * remove {@link XCommand}). Keeps AtmocEvents within the serverEvent
	 * untouched.
	 *
	 * @param serverEvents - an array of remote events with can contain
	 *            transactions
	 * @return a list of {@link XEvent} with implied tx replaced with their
	 *         contained events
	 */
	private static List<XEvent> unpackImpliedTxEvents(final XEvent[] serverEvents) {
		final List<XEvent> unpackedServerEvents = new ArrayList<XEvent>();
		for(final XEvent event : serverEvents) {
			assert event != null;
			if(event instanceof XAtomicEvent) {
				unpackedServerEvents.add(event);
			} else if(event instanceof XTransactionEvent) {
				final XTransactionEvent txEvent = (XTransactionEvent)event;
				unpackedServerEvents.addAll(unpackTxEvents(txEvent));
			}
		}

		return unpackedServerEvents;
	}

	/**
	 * Returns a list of XEvents where transactions that only contain implied
	 * events and one non-implied event (those tx resulting from a remove
	 * {@link XCommand}) are unpacked.
	 *
	 * @param txEvent - the {@link XTransactionEvent} that may be unpacked
	 * @return a list of {@link XEvent} with implied tx replaced with their
	 *         contained events
	 */
	private static List<XEvent> unpackTxEvents(final XTransactionEvent txEvent) {
		final List<XEvent> unpackedTxEvents = new ArrayList<XEvent>();
		if(isTxEventResultingFromRemove(txEvent)) {
			final List<XEvent> containedEvents = getContainedEventsFromTx(txEvent);
			unpackedTxEvents.addAll(containedEvents);
		} else {
			unpackedTxEvents.add(txEvent);
		}
		return unpackedTxEvents;
	}

	/**
	 * Tests whether a {@link XTransactionEvent} is a transaction that only
	 * contain implied events and one non-implied event (those tx resulting from
	 * a remove {@link XCommand}) are unpacked.
	 *
	 * @param txEvent
	 * @return
	 */
	private static boolean isTxEventResultingFromRemove(final XTransactionEvent txEvent) {
		for(int i = 0; i < txEvent.size() - 1; i++) {
			if(!txEvent.getEvent(i).isImplied()) {
				return false;
			}
		}

		final XAtomicEvent lastEvent = txEvent.getLastEvent();

		if(!lastEvent.getChangeType().equals(ChangeType.REMOVE) || lastEvent.isImplied()) {
			return false;
		}

		return true;
	}

	/**
	 * Utility method to get the {@link XAtomicEvent}s within a
	 * {@link XTransactionEvent}
	 *
	 * @param txEvent
	 * @return
	 */
	private static List<XEvent> getContainedEventsFromTx(final XTransactionEvent txEvent) {
		final List<XEvent> containedEvents = new ArrayList<XEvent>();
		for(int i = 0; i < txEvent.size(); i++) {
			containedEvents.add(txEvent.getEvent(i));
		}
		return containedEvents;
	}

	public static class Result {

		public Result(final List<XEvent> nonMappedServerEvents, final List<LocalChange> nonMappedLocalEvents,
		        final List<Pair<XEvent,LocalChange>> mapped) {
			this.nonMappedServerEvents = nonMappedServerEvents;
			this.nonMappedLocalEvents = nonMappedLocalEvents;
			this.mapped = mapped;
		}

		/**
		 * List of true remote events = not seen yet on client
		 */
		public List<XEvent> nonMappedServerEvents;

		/**
		 * List of local events that were not mapped = not executed on server
		 */
		public List<LocalChange> nonMappedLocalEvents;

		/**
		 * Mapping between local changes and remove events = events originated
		 * locally and successfully executed on server
		 */
		public List<Pair<XEvent,LocalChange>> mapped;

	}

}
