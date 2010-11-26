package org.xydra.store.impl.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XID;
import org.xydra.store.base.SimpleRepository;


public class MemoryNoAccessRightsNoBatchNoAsyncStore implements
        XydraNoAccessRightsNoBatchNoAsyncStore {
	
	private SimpleRepository simpleRepository;
	
	private List<XEvent> events = new ArrayList<XEvent>();
	
	public MemoryNoAccessRightsNoBatchNoAsyncStore(XID repositoryId) {
		Set<XID> modelIds = new HashSet<XID>();
		this.simpleRepository = new SimpleRepository(X.getIDProvider().fromComponents(repositoryId,
		        null, null, null), 0, modelIds);
	}
	
	@Override
	public long executeCommand(XID actorId, XCommand command) {
		return executeCommand(actorId, command, false);
	}
	
	public long executeCommand(XID actorId, XCommand command, boolean implied) {
		long oldFieldRevision = XEvent.RevisionOfEntityNotSet;
		long oldObjectRevision = XEvent.RevisionOfEntityNotSet;
		long oldModelRevision = XEvent.RevisionOfEntityNotSet;
		
		switch(command.getChangedEntity().getAddressedType()) {
		case XFIELD: {
			XBaseModel oldModel = this.getModelSnapshot(command.getChangedEntity().getParent()
			        .getParent());
			oldModelRevision = oldModel.getRevisionNumber();
			XBaseObject oldObject = oldModel.getObject(command.getChangedEntity().getObject());
			oldObjectRevision = oldObject.getRevisionNumber();
			XBaseField oldField = oldObject.getField(command.getChangedEntity().getField());
			oldFieldRevision = oldField.getRevisionNumber();
		}
			break;
		case XOBJECT: {
			XBaseModel oldModel = this.getModelSnapshot(command.getChangedEntity().getParent()
			        .getParent());
			oldModelRevision = oldModel.getRevisionNumber();
			XBaseObject oldObject = oldModel.getObject(command.getChangedEntity().getObject());
			oldObjectRevision = oldObject.getRevisionNumber();
		}
			break;
		case XMODEL: {
			XBaseModel oldModel = this.getModelSnapshot(command.getChangedEntity().getParent()
			        .getParent());
			oldModelRevision = oldModel.getRevisionNumber();
		}
			break;
		case XREPOSITORY: {
			// do nothing
		}
			break;
		}
		
		long revisionNumber = this.events.size();
		SimpleEvent event = new SimpleEvent(actorId, command.getChangeType(), command
		        .getChangedEntity(), oldFieldRevision, oldModelRevision, oldObjectRevision,
		        revisionNumber, command.getTarget(), command instanceof XTransaction, implied);
		this.events.add(event);
		
		// FIXME do the actual requested change, possibly causing many more
		// changes
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public XEvent[] getEvents(XAddress address, long beginRevision, long endRevision) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<XID> getModelIds() {
		return this.simpleRepository.getModelIds();
	}
	
	@Override
	public long getModelRevision(XAddress xAddress) {
		assert xAddress.getRepository().equals(this.getRepositoryId());
		return this.simpleRepository.getModel(xAddress.getModel()).getRevisionNumber();
	}
	
	@Override
	public XBaseModel getModelSnapshot(XAddress address) {
		assert address.getRepository().equals(this.getRepositoryId());
		return this.simpleRepository.getModel(address.getModel());
	}
	
	@Override
	public XBaseObject getObjectSnapshot(XAddress address) {
		XBaseModel baseModel = getModelSnapshot(XX.resolveModel(address, getRepositoryId()));
		return baseModel.getObject(address.getObject());
	}
	
	public XBaseField getFieldSnapshot(XAddress address) {
		XBaseObject baseObject = getObjectSnapshot(X.getIDProvider().fromComponents(
		        address.getRepository(), address.getModel(), address.getObject(), null));
		return baseObject.getField(address.getField());
	}
	
	@Override
	public XID getRepositoryId() {
		return this.simpleRepository.getID();
	}
	
}
