package org.xydra.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.change.impl.memory.MemoryRepositoryEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * A small repository holding exactly the one model passed to the constructor.
 */
@Deprecated
public class DummyRepository implements XRepository {
	
	private final List<XRepositoryEventListener> listeners = new ArrayList<XRepositoryEventListener>();
	private XModel model;
	private final XID repoId = XX.toId("dummyRepo");
	
	public DummyRepository(XModel model) {
		this.model = model;
	}
	
	public DummyRepository() {
	}
	
	public XModel createModel(XID actor, XID id) {
		if(this.model != null)
			throw new RuntimeException("Can only hold one model.");
		this.model = new MemoryModel(id);
		XRepositoryEvent event = MemoryRepositoryEvent.createAddEvent(actor, null, id);
		for(XRepositoryEventListener listener : this.listeners)
			listener.onChangeEvent(event);
		return this.model;
	}
	
	public long executeRepositoryCommand(XID actor, XRepositoryCommand event) {
		return XCommand.FAILED;
	}
	
	public XModel getModel(XID id) {
		if(!hasModel(id))
			return null;
		return this.model;
	}
	
	public boolean hasModel(XID id) {
		return this.model != null && this.model.getID().equals(id);
	}
	
	public boolean isEmpty() {
		return false;
	}
	
	public boolean removeModel(XID actor, XID modelID) {
		if(!hasModel(modelID))
			return false;
		XRepositoryEvent event = MemoryRepositoryEvent.createRemoveEvent(actor, getAddress(),
		        modelID, this.model.getRevisionNumber());
		this.model = null;
		for(XRepositoryEventListener listener : this.listeners)
			listener.onChangeEvent(event);
		return true;
	}
	
	public XID getID() {
		return this.repoId;
	}
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		return this.listeners.add(changeListener);
	}
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener) {
		return this.listeners.remove(changeListener);
	}
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		return this.model != null && this.model.addListenerForModelEvents(changeListener);
	}
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		return this.model != null && this.model.removeListenerForModelEvents(changeListener);
	}
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.model != null && this.model.addListenerForObjectEvents(changeListener);
	}
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		return this.model != null && this.model.removeListenerForObjectEvents(changeListener);
	}
	
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.model != null && this.model.addListenerForFieldEvents(changeListener);
	}
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		return this.model != null && this.model.removeListenerForFieldEvents(changeListener);
	}
	
	public Iterator<XID> iterator() {
		return new SingleValueIterator<XID>(this.model.getID());
	}
	
	public XAddress getAddress() {
		return XX.toAddress(getID(), null, null, null);
	}
	
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.model != null && this.model.addListenerForTransactionEvents(changeListener);
	}
	
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		return this.model != null && this.model.removeListenerForTransactionEvents(changeListener);
	}
	
	public long executeCommand(XID actor, XCommand command) {
		if(this.model != null)
			return this.model.executeCommand(actor, command);
		return XCommand.FAILED;
	}
	
}
