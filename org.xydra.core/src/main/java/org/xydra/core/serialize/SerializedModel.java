package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.XRevWritableRepository;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XValue;
import org.xydra.core.AccessException;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.ISyncLogEntry;
import org.xydra.core.model.impl.memory.sync.MemorySyncLogEntry;
import org.xydra.core.model.impl.memory.sync.MemorySyncLogState;
import org.xydra.core.model.impl.memory.sync.XSyncLogState;
import org.xydra.index.query.Pair;
import org.xydra.sharedutils.XyAssert;

/**
 * Collection of methods to (de-)serialize variants of
 * {@link XReadableRepository}, {@link XReadableModel}, {@link XReadableObject}
 * and {@link XReadableField} to and from their XML/JSON representation.
 *
 * @author dscharrer
 *
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedModel {

	private static final String LOCALCHANGES_NAME = "localchanges";
	private static final String LOG_NAME = "log";
	private static final String NAME_COMMANDLIST = "commandlist";
	private static final String NAME_COMMANDS = "commands";
	private static final String NAME_EVENTLIST = "eventlist";
	private static final String NAME_EVENTS = "events";
	private static final String NAME_FIELDS = "fields";
	private static final String NAME_MODELS = "models";
	private static final String NAME_OBJECTS = "objects";
	// TODO SPACE use for more compact serialisation
	@SuppressWarnings("unused")
	private static final String NAME_SYNCLOGENTRY = "synclogentry";
	private static final String NAME_VALUE = "value";
	/** revision number returned by parser if no revision number was found */
	public static final long NO_REVISION = RevisionConstants.NO_REVISION;
	private static final String REVISION_ATTRIBUTE = "revision";
	private static final String STARTREVISION_ATTRIBUTE = "startRevision";
	private static final String SYNC_REVISION_ATTRIBUTE = "syncRevision";
	private static final String SYNCLOG_ELEMENT = "synclog";
	private static final String SYNCLOG_NAME = "synclog";
	private static final String XCHANGELOG_ELEMENT = "xlog";
	private static final String XFIELD_ELEMENT = "xfield";
	private static final String XLOCALCHANGES_ELEMENT = "xlocalchanges";
	private static final String XMODEL_ELEMENT = "xmodel";
	private static final String XOBJECT_ELEMENT = "xobject";
	private static final String XREPOSITORY_ELEMENT = "xrepository";

	private static long getBaseRevisionAttribute(final XydraElement element) {
		final Object revisionString = element.getAttribute(STARTREVISION_ATTRIBUTE);
		if (revisionString == null) {
			return NO_REVISION;
		}
		return SerializingUtils.toLong(revisionString);
	}

	private static long getRevisionAttribute(final XydraElement element) {
		final Object revisionString = element.getAttribute(REVISION_ATTRIBUTE);
		if (revisionString == null) {
			return NO_REVISION;
		}
		return SerializingUtils.toLong(revisionString);
	}

	public static long getSyncRevisionAttribute(final XydraElement element) {
		final Object revisionString = element.getAttribute(SYNC_REVISION_ATTRIBUTE);
		if (revisionString == null) {
			return NO_REVISION;
		}
		return SerializingUtils.toLong(revisionString);
	}

	public static boolean isModel(final XydraElement element) {
		return element == null || XMODEL_ELEMENT.equals(element.getType());
	}

	public static XChangeLogState loadChangeLogState(final XydraElement element, final XAddress baseAddr) {
		XydraElement logElement = element.getChild(SYNCLOG_NAME, SYNCLOG_ELEMENT);
		if (logElement != null) {
			final XSyncLogState log = new MemorySyncLogState(baseAddr);
			loadSyncLogState(logElement, log);
			return log;
		}
		logElement = element.getChild(LOG_NAME, XCHANGELOG_ELEMENT);
		if (logElement != null) {
			final XChangeLogState log = new MemoryChangeLogState(baseAddr);
			loadChangeLogState(logElement, log);
			return log;
		}
		return null;
	}

	/**
	 * Load the change log represented by the given XML/JSON element into an
	 * {@link XChangeLogState}.
	 *
	 * @param element
	 *
	 * @param state The change log state to load into.
	 */
	public static void loadChangeLogState(final XydraElement element, final XChangeLogState state) {
		SerializingUtils.checkElementType(element, XCHANGELOG_ELEMENT);

		long startRev = 0L;
		final Object revisionString = element.getAttribute(STARTREVISION_ATTRIBUTE);
		if (revisionString != null) {
			startRev = SerializingUtils.toLong(revisionString);
		}

		state.setBaseRevisionNumber(startRev);

		final Iterator<XydraElement> eventElementIt = element.getChildrenByName(NAME_EVENTS);
		while (eventElementIt.hasNext()) {
			final XydraElement e = eventElementIt.next();
			final XEvent event = SerializedEvent.toEvent(e, state.getBaseAddress());
			state.appendEvent(event);
		}
	}

	/**
	 * Load the local changes represented by the given XML/JSON element into a
	 * list of {@link XCommand}.
	 *
	 * @param element
	 *
	 * @param localChangesAsCommands The localChangesAsCommands list to load
	 *            into.
	 * @param context The {@link XId XIds} of the repository, model, object and
	 *            field to fill in if not specified in the XML/JSON. If the
	 *            given element represents a transaction, the context for the
	 *            contained commands will be given by the transaction.
	 */
	public static void loadLocalChangesAsCommands(final XydraElement element,
			final List<XCommand> localChangesAsCommands, final XAddress context) {
		SerializingUtils.checkElementType(element, XLOCALCHANGES_ELEMENT);

		final Iterator<XydraElement> commandElementIt = element.getChildrenByName(NAME_COMMANDS);
		while (commandElementIt.hasNext()) {
			final XydraElement e = commandElementIt.next();
			final XCommand command = SerializedCommand.toCommand(e, context);
			localChangesAsCommands.add(command);
		}
	}

	public static List<XCommand> loadLocalChangesAsCommands(final XydraElement element, final XAddress baseAddr) {
		final XydraElement localChangesElement = element.getChild(LOCALCHANGES_NAME,
				XLOCALCHANGES_ELEMENT);
		if (localChangesElement != null) {
			final List<XCommand> localChangesAsCommands = new ArrayList<XCommand>();

			loadLocalChangesAsCommands(localChangesElement, localChangesAsCommands, baseAddr);
			return localChangesAsCommands;
		}
		return null;
	}

	/**
	 * Load the change log represented by the given XML/JSON element into an
	 * {@link XChangeLogState}.
	 *
	 * @param element
	 *
	 * @param state The change log state to load into.
	 */
	public static void loadSyncLogState(final XydraElement element, final XSyncLogState state) {

		// FIXME is 'xmap' not 'synclog'
		SerializingUtils.checkElementType(element, SYNCLOG_ELEMENT);

		state.setBaseRevisionNumber(getBaseRevisionAttribute(element));

		state.setSyncRevisionNumber(getSyncRevisionAttribute(element));

		final XydraElement eventListElement = element.getChild(NAME_EVENTLIST, NAME_EVENTS);
		assert eventListElement != null;
		Iterator<XydraElement> iterator = eventListElement.getChildrenByName(NAME_EVENTS);
		final List<XEvent> eventList = new ArrayList<XEvent>();
		while (iterator.hasNext()) {
			final XydraElement e = iterator.next();
			final XEvent event = SerializedEvent.toEvent(e, state.getBaseAddress());
			eventList.add(event);
		}

		final XydraElement commandList = element.getChild(NAME_COMMANDLIST, NAME_COMMANDS);
		iterator = commandList.getChildrenByName(NAME_COMMANDS);
		int count = 0;
		while (iterator.hasNext()) {
			final XydraElement e = iterator.next();
			final XCommand command = SerializedCommand.toCommand(e, state.getBaseAddress());
			final MemorySyncLogEntry syncLogEntry = new MemorySyncLogEntry(command, eventList.get(count));

			state.appendSyncLogEntry(syncLogEntry);
			count++;
		}
	}

	/**
	 * Encode the given synclog {@link ISyncLog} as an XML/JSON element.
	 *
	 * @param syncLog the non-empty sync log
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param context The part of this event's target address that doesn't need
	 *            to be encoded in the element. Usually this is the model
	 *            address.
	 */
	public static void serialize(final ISyncLog syncLog, final XydraOut out, final XAddress context) {
		final List<XCommand> commandList = new ArrayList<XCommand>();
		Iterator<XCommand> commands;

		final long rev = syncLog.getBaseRevisionNumber();

		out.open(SYNCLOG_ELEMENT);
		out.attribute(STARTREVISION_ATTRIBUTE, rev);

		final long syncRev = syncLog.getSynchronizedRevision();
		out.attribute(SYNC_REVISION_ATTRIBUTE, syncRev);

		out.child(NAME_EVENTLIST);
		out.open(NAME_EVENTS);
		out.child(NAME_EVENTS);
		out.beginArray();
		final Iterator<ISyncLogEntry> syncLogEntryIterator = syncLog.getSyncLogEntriesSince(rev + 1);
		while (syncLogEntryIterator.hasNext()) {
			final ISyncLogEntry entry = syncLogEntryIterator.next();
			final XEvent event = entry.getEvent();

			/* handle event */
			SerializedEvent.serialize(event, out, context);

			/* handle command */
			final XCommand command = entry.getCommand();
			commandList.add(command);
		}
		out.endArray();
		out.close(NAME_EVENTS);

		commands = commandList.iterator();
		out.child(NAME_COMMANDLIST);
		out.open(NAME_COMMANDS);
		out.child(NAME_COMMANDS);
		out.beginArray();
		while (commands.hasNext()) {
			final XCommand command = commands.next();
			if (command != null) {
				SerializedCommand.serialize(command, out, context);
			}
		}
		out.endArray();
		out.close(NAME_COMMANDS);
		out.close(SYNCLOG_ELEMENT);
	}

	/**
	 * Encode the given {@link XChangeLog} as an XML/JSON element.
	 *
	 * @param log an {@link XChangeLog}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 */
	public static void serialize(final XChangeLog log, final XydraOut out) {
		// get values before outputting anything to prevent incomplete
		// elements on errors
		final long rev = log.getBaseRevisionNumber();
		final Iterator<XEvent> events = log.getEventsBetween(rev + 1, Long.MAX_VALUE);

		out.open(XCHANGELOG_ELEMENT);
		if (rev != 0) {
			out.attribute(STARTREVISION_ATTRIBUTE, rev);
		}

		out.child(NAME_EVENTS);
		out.beginArray();
		while (events.hasNext()) {
			SerializedEvent.serialize(events.next(), out, log.getBaseAddress());
		}
		out.endArray();
		out.close(XCHANGELOG_ELEMENT);
	}

	/**
	 * Encode the given {@link XChangeLogState} as an XML/JSON element.
	 *
	 * @param logState an {@link XChangeLogState}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 */
	public static void serialize(final XChangeLogState logState, final XydraOut out) {
		final long rev = logState.getBaseRevisionNumber();

		out.open(XCHANGELOG_ELEMENT);
		if (rev != 0) {
			out.attribute(STARTREVISION_ATTRIBUTE, rev);
		}

		out.child(NAME_EVENTS);
		out.beginArray();

		final XEvent last = logState.getLastEvent();
		if (last != null) {
			for (long i = rev + 1; i <= last.getRevisionNumber(); i++) {
				final XEvent event = logState.getEvent(i);
				// there can be gaps in rev numbers
				if (event != null) {
					SerializedEvent.serialize(event, out, logState.getBaseAddress());
				}
			}
		}

		out.endArray();
		out.close(XCHANGELOG_ELEMENT);
	}

	/**
	 * Encode the given {@link XReadableField} as an XML/JSON element, including
	 * revision numbers.
	 *
	 * @param xfield an {@link XReadableField}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableField xfield, final XydraOut out) {
		serialize(xfield, out, true);
	}

	/**
	 * Encode the given {@link XReadableField} as an XML/JSON element.
	 *
	 * @param xfield an {@link XReadableField}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved in the
	 *            element.
	 * @throws IllegalArgumentException if the field contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableField xfield, final XydraOut out, final boolean saveRevision) {
		serialize(xfield, out, saveRevision, true);
	}

	public static void serialize(final XReadableField xfield, final XydraOut out, final boolean saveRevision,
			final boolean saveId) {
		// get values before outputting anything to prevent incomplete
		// elements on errors
		final XValue xvalue = xfield.getValue();
		final long rev = xfield.getRevisionNumber();

		out.open(XFIELD_ELEMENT);
		if (saveId) {
			out.attribute(SerializingUtils.XID_ATTRIBUTE, xfield.getId());
		}
		if (saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}

		if (xvalue != null) {
			out.child(NAME_VALUE);
			SerializedValue.serialize(xvalue, out);
		}

		out.close(XFIELD_ELEMENT);
	}

	/**
	 * Encode the given {@link XReadableModel} as an XML/JSON element, including
	 * revision numbers and ignoring inaccessible entities.
	 *
	 * @param xmodel an {@link XReadableModel}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableModel xmodel, final XydraOut out) {
		serialize(xmodel, out, true, true, true);
	}

	/**
	 * Encode the given {@link XReadableModel} as an XML/JSON element.
	 *
	 * @param xmodel an {@link XReadableModel}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible objects and fields instead
	 *            of throwing an exception
	 * @param saveChangeLog if true, the change log is saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableModel xmodel, final XydraOut out, final boolean saveRevision,
			final boolean ignoreInaccessible, final boolean saveChangeLog) {
		serialize(xmodel, out, saveRevision, ignoreInaccessible, saveChangeLog, true);
	}

	/**
	 * @param xmodel
	 * @param out
	 * @param saveRevision
	 * @param ignoreInaccessible
	 * @param saveChangeLog
	 * @param saveId iff true, saves the model id
	 */
	public static void serialize(final XReadableModel xmodel, final XydraOut out, final boolean saveRevision,
			final boolean ignoreInaccessible, final boolean saveChangeLog, final boolean saveId) {
		if (!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}

		// get revision before outputting anything to prevent incomplete
		// elements on errors
		final long rev = xmodel.getRevisionNumber();

		out.open(XMODEL_ELEMENT);
		if (saveId) {
			out.attribute(SerializingUtils.XID_ATTRIBUTE, xmodel.getId());
		}
		if (saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}

		out.child(NAME_OBJECTS);
		out.beginMap(SerializingUtils.XID_ATTRIBUTE, XOBJECT_ELEMENT);
		for (final XId objectId : xmodel) {
			out.entry(objectId.toString());
			try {
				serialize(xmodel.getObject(objectId), out, saveRevision, ignoreInaccessible, false,
						false);
			} catch (final AccessException ae) {
				if (!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endMap();
		if (saveChangeLog && xmodel instanceof XSynchronizesChanges) {
			final XSynchronizesChanges synchronizesChanges = (XSynchronizesChanges) xmodel;

			final XChangeLog changeLog = synchronizesChanges.getChangeLog();
			assert changeLog instanceof ISyncLog;
			final ISyncLog syncLog = (ISyncLog) changeLog;

			// if(syncLog.getLastEvent() != null) {
			out.child(SYNCLOG_NAME);
			out.setChildType(SYNCLOG_ELEMENT);
			serialize(syncLog, out, synchronizesChanges.getAddress());
			// }

		}

		// if(saveChangeLog && xmodel instanceof XLoggedModel) {
		// XChangeLog log = ((XLoggedModel)xmodel).getChangeLog();
		// if(log != null) {
		// out.child(LOG_NAME);
		// out.setChildType(XCHANGELOG_ELEMENT);
		// serialize(log, out);
		// XyAssert.xyAssert(log.getCurrentRevisionNumber() ==
		// xmodel.getRevisionNumber(),
		// "log.rev=%s,modelRev=%s,", log.getCurrentRevisionNumber(),
		// xmodel.getRevisionNumber());
		// }
		// }

		out.close(XMODEL_ELEMENT);
	}

	/**
	 * Encode the given {@link XReadableObject} as an XML/JSON element,
	 * including revision numbers and ignoring inaccessible entities.
	 *
	 * @param xobject an {@link XReadableObject}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableObject xobject, final XydraOut out) {
		serialize(xobject, out, true, true, true);
	}

	/**
	 * Encode the given {@link XReadableObject} as an XML/JSON element.
	 *
	 * @param xobject an {@link XObject}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible fields instead of throwing
	 *            an exception
	 * @param saveChangeLog if true, any object change log is saved
	 * @throws IllegalArgumentException if the object contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableObject xobject, final XydraOut out, final boolean saveRevision,
			final boolean ignoreInaccessible, final boolean saveChangeLog) {
		serialize(xobject, out, saveRevision, ignoreInaccessible, saveChangeLog, true);
	}

	public static void serialize(final XReadableObject xobject, final XydraOut out, final boolean saveRevision,
			final boolean ignoreInaccessible, final boolean saveChangeLog, final boolean saveId) {

		if (!saveRevision && saveChangeLog) {
			throw new IllegalArgumentException("cannot save change log without saving revisions");
		}

		// get revision before outputting anything to prevent incomplete
		// elements on errors
		final long rev = xobject.getRevisionNumber();

		out.open(XOBJECT_ELEMENT);
		if (saveId) {
			out.attribute(SerializingUtils.XID_ATTRIBUTE, xobject.getId());
		}
		if (saveRevision) {
			out.attribute(REVISION_ATTRIBUTE, rev);
		}

		out.child(NAME_FIELDS);
		out.beginMap(SerializingUtils.XID_ATTRIBUTE, XFIELD_ELEMENT);
		for (final XId fieldId : xobject) {
			out.entry(fieldId.toString());
			try {
				serialize(xobject.getField(fieldId), out, saveRevision, false);
			} catch (final AccessException ae) {
				if (!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endMap();

		if (saveChangeLog && xobject instanceof XSynchronizesChanges) {
			final XChangeLog log = ((XSynchronizesChanges) xobject).getChangeLog();
			if (log != null && log.getBaseAddress().equals(xobject.getAddress())) {
				final ISyncLog syncLog = (ISyncLog) log;
				out.child(SYNCLOG_NAME);
				out.setChildType(SYNCLOG_ELEMENT);
				serialize(syncLog, out, xobject.getAddress());
				XyAssert.xyAssert(log.getCurrentRevisionNumber() == xobject.getRevisionNumber(),
						"log=%s,object=%s", log.getCurrentRevisionNumber(),
						xobject.getRevisionNumber());
			}
		}

		out.close(XOBJECT_ELEMENT);
	}

	/**
	 * Encode the given {@link XReadableRepository} as an XML/JSON element,
	 * including revision numbers and ignoring inaccessible entities.
	 *
	 * @param xrepository an {@link XReadableRepository}
	 * @param out the {@link XydraOut} that a partial XML/JSON document is
	 *            written to.
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableRepository xrepository, final XydraOut out) {
		serialize(xrepository, out, true, true, true);
	}

	/**
	 * Encode the given {@link XReadableRepository} as an XML/JSON element.
	 *
	 * @param xrepository an {@link XReadableRepository}
	 * @param out the {@link XydraOut} that a partial XML document is written
	 *            to.
	 * @param saveRevision true if revision numbers should be saved to the
	 *            element.
	 * @param ignoreInaccessible ignore inaccessible models, objects and fields
	 *            instead of throwing an exception
	 * @param saveChangeLog if true, any model change logs are saved
	 * @throws IllegalArgumentException if the model contains an unsupported
	 *             XValue type. See {@link SerializedValue} for details.
	 */
	public static void serialize(final XReadableRepository xrepository, final XydraOut out,
			final boolean saveRevision, final boolean ignoreInaccessible, final boolean saveChangeLog) {
		out.open(XREPOSITORY_ELEMENT);
		out.attribute(SerializingUtils.XID_ATTRIBUTE, xrepository.getId());

		out.child(NAME_MODELS);
		out.beginMap(SerializingUtils.XID_ATTRIBUTE, XMODEL_ELEMENT);
		for (final XId modelId : xrepository) {
			out.entry(modelId.toString());
			try {
				serialize(xrepository.getModel(modelId), out, saveRevision, ignoreInaccessible,
						saveChangeLog, false);
			} catch (final AccessException ae) {
				if (!ignoreInaccessible) {
					throw ae;
				}
			}
		}
		out.endMap();

		out.close(XREPOSITORY_ELEMENT);
	}

	/**
	 * Get the {@link XField} represented by the given XML/JSON element.
	 *
	 * @param actorId
	 * @param element
	 *
	 * @return an {@link XField}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XField element.
	 */
	public static XField toField(final XId actorId, final XydraElement element) {
		return new MemoryField(actorId, toFieldState(element, null));
	}

	/**
	 * Load the field represented by the given XML/JSON element into an
	 * {@link XRevWritableField}.
	 *
	 * @param element
	 *
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleField}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableField}
	 */
	public static XRevWritableField toFieldState(final XydraElement element, final XRevWritableObject parent) {
		return toFieldState(element, parent, null);
	}

	/**
	 * @param element
	 * @param parent
	 * @param context
	 * @return ...
	 */
	public static XRevWritableField toFieldState(final XydraElement element, final XRevWritableObject parent,
			final XAddress context) {
		SerializingUtils.checkElementType(element, XFIELD_ELEMENT);

		XId xid;
		if (context != null && context.getField() != null) {
			xid = context.getField();
		} else {
			xid = SerializingUtils.getRequiredXidAttribute(element);
		}

		final long revision = getRevisionAttribute(element);

		XValue xvalue = null;
		final XydraElement valueElement = element.getElement(NAME_VALUE);
		if (valueElement != null) {
			xvalue = SerializedValue.toValue(valueElement);
		}

		XRevWritableField fieldState;
		if (parent == null) {
			final XAddress fieldAddr = Base.toAddress(XId.DEFAULT, XId.DEFAULT, XId.DEFAULT, xid);
			fieldState = new SimpleField(fieldAddr);
		} else {
			fieldState = parent.createField(xid);
		}
		fieldState.setRevisionNumber(revision);
		fieldState.setValue(xvalue);

		return fieldState;
	}

	/**
	 * Get the {@link XModel} represented by the given XML/JSON element.
	 * Including the change log, if available.
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param element
	 *
	 * @return an {@link XModel}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XModel element.
	 */
	public static XModel toModel(final XId actorId, final String passwordHash, final XydraElement element) {
		final XExistsRevWritableModel state = toModelState(element, null, null);
		final XChangeLogState log = loadChangeLogState(element, state.getAddress());

		if (log == null) {
			return new MemoryModel(actorId, passwordHash, state);
		} else {

			assert state.getRevisionNumber() == log.getCurrentRevisionNumber();
			return new MemoryModel(actorId, passwordHash, state, (XSyncLogState) log);
		}
	}

	public static XExistsRevWritableModel toModelState(final XydraElement element, final XAddress context) {
		return toModelState(element, null, context);
	}

	/**
	 * Load the model represented by the given XML/JSON element into an
	 * {@link XRevWritableModel}.
	 *
	 * @param element
	 *
	 * @param parent @CanBeNull If parent is null, the field is loaded into a
	 *            {@link SimpleModel}, otherwise it is loaded into a child state
	 *            of parent.
	 * @return the created {@link XRevWritableModel}
	 */
	public static XExistsRevWritableModel toModelState(final XydraElement element,
			final XExistsRevWritableRepository parent) {
		return toModelState(element, parent, null);
	}

	/**
	 * @param element
	 * @param parent @CanBeNull
	 * @param context @CanBeNull
	 * @return @NeverNull
	 */
	private static XExistsRevWritableModel toModelState(final XydraElement element,
			final XExistsRevWritableRepository parent, final XAddress context) {

		SerializingUtils.checkElementType(element, XMODEL_ELEMENT);

		XId xid;
		if (context != null && context.getModel() != null) {
			xid = context.getModel();
		} else {
			xid = SerializingUtils.getRequiredXidAttribute(element);
		}

		final long revision = getRevisionAttribute(element);

		XExistsRevWritableModel modelState;
		XAddress modelAddr;
		if (parent == null) {
			if (context != null) {
				modelAddr = Base.toAddress(context.getRepository(), xid, null, null);
			} else {
				modelAddr = Base.toAddress(XId.DEFAULT, xid, null, null);
			}
			modelState = new SimpleModel(modelAddr);
		} else {
			modelState = parent.createModel(xid);
			modelAddr = modelState.getAddress();
		}
		if (revision != NO_REVISION) {
			modelState.setRevisionNumber(revision);
		} else {
			modelState.setRevisionNumber(revision);
		}

		final XydraElement objects = element.getChild(NAME_OBJECTS);

		final Iterator<Pair<String, XydraElement>> objectElementIt = objects.getEntriesByType(
				SerializingUtils.XID_ATTRIBUTE, XOBJECT_ELEMENT);
		while (objectElementIt.hasNext()) {
			final Pair<String, XydraElement> objectElement = objectElementIt.next();
			final XId objectId = Base.toId(objectElement.getFirst());
			final XAddress objectAddr = Base.resolveObject(modelAddr, objectId);
			final XRevWritableObject objectState = toObjectState(objectElement.getSecond(), modelState,
					objectAddr);
			XyAssert.xyAssert(modelState.getObject(objectState.getId()) == objectState);
		}

		return modelState;
	}

	/**
	 * Get the {@link XObject} represented by the given XML/JSON element.
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param element
	 *
	 * @return an {@link XObject}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XObject element.
	 */
	public static XObject toObject(final XId actorId, final String passwordHash, final XydraElement element) {
		final XRevWritableObject state = toObjectState(element, null, null);
		final XChangeLogState log = loadChangeLogState(element, state.getAddress());
		return new MemoryObject(actorId, passwordHash, state, log);
	}

	public static XRevWritableObject toObjectState(final XydraElement element, final XAddress context) {
		return toObjectState(element, null, context);
	}

	/**
	 * Load the object represented by the given XML/JSON element into an
	 * {@link XRevWritableObject}.
	 *
	 * @param element
	 *
	 * @param parent If parent is null, the field is loaded into a
	 *            {@link SimpleObject}, otherwise it is loaded into a child
	 *            state of parent.
	 * @return the created {@link XRevWritableObject}
	 */
	public static XRevWritableObject toObjectState(final XydraElement element, final XRevWritableModel parent) {
		return toObjectState(element, parent, null);
	}

	private static XRevWritableObject toObjectState(final XydraElement element, final XRevWritableModel parent,
			final XAddress context) {

		SerializingUtils.checkElementType(element, XOBJECT_ELEMENT);

		XId xid;
		if (context != null && context.getObject() != null) {
			xid = context.getObject();
		} else {
			xid = SerializingUtils.getRequiredXidAttribute(element);
		}

		final long revision = getRevisionAttribute(element);

		XRevWritableObject objectState;
		XAddress objectAddr;
		if (parent == null) {
			if (context != null) {
				objectAddr = Base.toAddress(context.getRepository(), context.getModel(), xid, null);
			} else {
				objectAddr = Base.toAddress(XId.DEFAULT, XId.DEFAULT, xid, null);
			}
			objectState = new SimpleObject(objectAddr);
		} else {
			objectState = parent.createObject(xid);
			objectAddr = objectState.getAddress();
		}

		objectState.setRevisionNumber(revision);

		final XydraElement fields = element.getChild(NAME_FIELDS);

		final Iterator<Pair<String, XydraElement>> fieldElementIt = fields.getEntriesByType(
				SerializingUtils.XID_ATTRIBUTE, XFIELD_ELEMENT);
		while (fieldElementIt.hasNext()) {
			final Pair<String, XydraElement> fieldElement = fieldElementIt.next();
			final XId fieldId = Base.toId(fieldElement.getFirst());
			final XAddress fieldAddr = Base.resolveField(objectAddr, fieldId);
			final XRevWritableField fieldState = toFieldState(fieldElement.getSecond(), objectState,
					fieldAddr);
			XyAssert.xyAssert(objectState.getField(fieldState.getId()) == fieldState);
		}

		return objectState;
	}

	/**
	 * Get the {@link XRepository} represented by the given XML/JSON element.
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param element
	 *
	 * @return an {@link XRepository}
	 * @throws IllegalArgumentException if the given element is not a valid
	 *             XRepository element.
	 */
	public static XRepository toRepository(final XId actorId, final String passwordHash, final XydraElement element) {
		return new MemoryRepository(actorId, passwordHash, toRepositoryState(element));
	}

	/**
	 * Load the repository represented by the given XML/JSON element into an
	 * {@link XRevWritableRepository}.
	 *
	 * @param element
	 *
	 * @return the created {@link XRevWritableRepository}
	 */
	public static XRevWritableRepository toRepositoryState(final XydraElement element) {
		SerializingUtils.checkElementType(element, XREPOSITORY_ELEMENT);

		final XId xid = SerializingUtils.getRequiredXidAttribute(element);

		final XAddress repoAddr = Base.toAddress(xid, null, null, null);
		final XExistsRevWritableRepository repositoryState = new SimpleRepository(repoAddr);

		final XydraElement models = element.getChild(NAME_MODELS);

		final Iterator<Pair<String, XydraElement>> modelElementIt = models.getEntriesByType(
				SerializingUtils.XID_ATTRIBUTE, XMODEL_ELEMENT);
		while (modelElementIt.hasNext()) {
			final Pair<String, XydraElement> modelElement = modelElementIt.next();
			final XId modelId = Base.toId(modelElement.getFirst());
			final XAddress modelAddr = Base.resolveModel(repoAddr, modelId);

			final XExistsRevWritableModel modelState = toModelState(modelElement.getSecond(),
					repositoryState, modelAddr);

			XyAssert.xyAssert(repositoryState.getModel(modelState.getId()) == modelState);
		}

		return repositoryState;
	}

}
