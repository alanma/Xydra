package org.xydra.client.gwt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.core.XX;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;



/**
 * A class that listens to changes to a local repository and saves them to a CXM
 * /data service (ignoring any changes done by others to the remote repository).
 * 
 * Remote actions will be performed with a specified minimum delay between them.
 */
public class DataServiceWriter implements XRepositoryEventListener, XModelEventListener,
        XObjectEventListener, XFieldEventListener {
	
	private final XRepository repo;
	private final XDataService service;
	private final Set<XID> models = new HashSet<XID>();
	private final Set<XAddress> objects = new HashSet<XAddress>();
	private final Set<XAddress> fields = new HashSet<XAddress>();
	private int delay;
	private final Timer timer = new Timer() {
		@Override
		public void run() {
			sendData();
		}
	};
	boolean running = false;
	
	public DataServiceWriter(XRepository repo, XDataService service, int delay) {
		this.repo = repo;
		this.service = service;
		this.delay = delay;
		this.repo.addListenerForFieldEvents(this);
		this.repo.addListenerForObjectEvents(this);
		this.repo.addListenerForModelEvents(this);
		this.repo.addListenerForRepositoryEvents(this);
	}
	
	public void onChangeEvent(XRepositoryEvent event) {
		if(event.getChangeType() == ChangeType.REMOVE && this.models.contains(event.getModelID()))
			this.models.remove(event.getModelID());
		else {
			this.models.add(event.getModelID());
			newEvent();
		}
	}
	
	public void onChangeEvent(XModelEvent event) {
		if(this.models.contains(event.getModelID()))
			return;
		XAddress objectAddr = XX.resolveObject(event.getTarget(), event.getObjectID());
		if(event.getChangeType() == ChangeType.REMOVE && this.objects.contains(objectAddr))
			this.objects.remove(objectAddr);
		else {
			this.objects.add(objectAddr);
			newEvent();
		}
	}
	
	public void onChangeEvent(XObjectEvent event) {
		if(this.models.contains(event.getModelID()))
			return;
		if(this.objects.contains(event.getTarget()))
			return;
		XAddress fieldAddr = XX.resolveField(event.getTarget(), event.getFieldID());
		this.fields.add(fieldAddr);
		newEvent();
	}
	
	public void onChangeEvent(XFieldEvent event) {
		if(this.models.contains(event.getModelID()))
			return;
		XAddress objectAddr = event.getTarget().getParent();
		if(this.objects.contains(objectAddr))
			return;
		this.fields.add(event.getTarget());
		newEvent();
	}
	
	/**
	 * A new event is available, restart the timer if necessary.
	 */
	private void newEvent() {
		if(this.running)
			return;
		this.timer.scheduleRepeating(this.delay);
		this.running = true;
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				sendData();
			}
		});
	}
	
	/**
	 * Get one entry from a collection.
	 */
	<T> T pop(Collection<T> c) {
		Iterator<T> it = c.iterator();
		T t = it.next();
		it.remove();
		return t;
	}
	
	/**
	 * Send one changed entity to the remote repository or stop the timer if
	 * there are no more changed entities.
	 */
	protected void sendData() {
		
		if(!this.models.isEmpty()) {
			XID modelId = pop(this.models);
			if(this.repo.hasModel(modelId))
				this.service.setModel(this.repo.getModel(modelId), null);
			else
				this.service.deleteModel(modelId, null);
		}

		else if(!this.objects.isEmpty()) {
			XAddress objectAddr = pop(this.objects);
			XModel model = this.repo.getModel(objectAddr.getModel());
			if(model == null) {
				sendData();
				return;
			}
			if(model.hasObject(objectAddr.getObject()))
				this.service.setObject(objectAddr.getModel(), model.getObject(objectAddr
				        .getObject()), null);
			else
				this.service.deleteObject(objectAddr.getModel(), objectAddr.getObject(), null);
		}

		else if(!this.fields.isEmpty()) {
			XAddress fieldAddr = pop(this.fields);
			XModel model = this.repo.getModel(fieldAddr.getModel());
			if(model == null) {
				sendData();
				return;
			}
			XObject object = model.getObject(fieldAddr.getObject());
			if(object == null) {
				sendData();
				return;
			}
			if(object.hasField(fieldAddr.getField()))
				this.service.setField(fieldAddr.getModel(), fieldAddr.getObject(), object
				        .getField(fieldAddr.getField()), null);
			else
				this.service.deleteField(fieldAddr.getModel(), fieldAddr.getObject(), fieldAddr
				        .getField(), null);
		}

		else {
			this.timer.cancel();
			this.running = false;
		}
	}
	
}
