package org.xydra.core.change;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XField;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;


/**
 * TODO Document
 * 
 * TODO create special XObject- and XField-types that pass all changes that are
 * done on them to the TransactionModel and return these when a user tries to
 * get them by using the TransactionModel.
 * 
 * TODO check if this implementation is thread-safe "enough"
 * 
 * @author Kaidel
 * 
 */
// suppressing warning while this is in flux ~~max
@SuppressWarnings("unused")
public class TransactionModel implements XModel {
	private static final long serialVersionUID = -5636313889791653240L;
	
	private XModel baseModel;
	
	private Map<XID,XObject> changedObjects;
	private Map<XAddress,XField> changedFields;
	
	private Map<XID,XObject> removedObjects;
	private Map<XAddress,XField> removedFields;
	
	private LinkedList<XCommand> commands;
	
	public XChangeLog getChangeLog() {
		return this.baseModel.getChangeLog();
	}
	
	public XAddress getAddress() {
		return this.baseModel.getAddress();
	}
	
	public XID getID() {
		return this.baseModel.getID();
	}
	
	public long executeCommand(XCommand command) {
		return this.baseModel.executeCommand(command);
	}
	
	public long executeCommand(XCommand command, XLocalChangeCallback callback) {
		return this.baseModel.executeCommand(command, callback);
	}
	
	public long getRevisionNumber() {
		return this.baseModel.getRevisionNumber();
	}
	
	public boolean hasObject(XID objectId) {
		return this.baseModel.hasObject(objectId);
	}
	
	public XObject createObject(XID id) {
		return this.baseModel.createObject(id);
	}
	
	public boolean isEmpty() {
		return this.baseModel.isEmpty();
	}
	
	public long executeModelCommand(XModelCommand command) {
		return this.baseModel.executeModelCommand(command);
	}
	
	public XObject getObject(XID objectId) {
		return this.baseModel.getObject(objectId);
	}
	
	public boolean removeObject(XID objectId) {
		return this.baseModel.removeObject(objectId);
	}
	
	/*
	 * Special methods needed for the helperclasses InTransactionObject and
	 * InTransactionField
	 */

	protected XField getField(XAddress address) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected long getObjectRevisionNumber(XID id) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	protected long getFieldRevisionNumber(XAddress fieldAddress) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// Unsupported Methods
	@Override
	public Iterator<XID> iterator() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int countUnappliedLocalChanges() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XLocalChange[] getLocalChanges() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public XID getSessionActor() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long getSynchronizedRevision() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void rollback(long revision) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setSessionActor(XID actorId, String passwordHash) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean synchronize(XEvent[] remoteChanges) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public XRevWritableModel createSnapshot() {
		throw new UnsupportedOperationException();
	}
}
