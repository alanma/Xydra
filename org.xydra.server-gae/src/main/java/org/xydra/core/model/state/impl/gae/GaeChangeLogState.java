package org.xydra.core.model.state.impl.gae;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


/**
 * An implementation of {@link XChangeLogState} that persists to the Google App
 * Engine {@link DatastoreService}.
 */
public class GaeChangeLogState implements XChangeLogState {
	
	private static final long serialVersionUID = 6673976783142023642L;
	
	private static final String PROP_FIRST_REVISION = "firstRevision";
	private static final String PROP_CURRENT_REVISION = "lastRevision";
	private static final String KIND_XEVENT = "XEVENT";
	private static final String PROP_EVENT = "event";
	
	private final XAddress baseAddr;
	
	private final Key key;
	private long firstRev;
	private long currentRev;
	
	public GaeChangeLogState(XAddress baseAddr, long currentRev) {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		this.baseAddr = baseAddr;
		
		this.key = GaeUtils.keyForLog(baseAddr);
		
		Entity e = GaeUtils.getEntity(this.key);
		
		if(e == null) {
			this.firstRev = this.currentRev = currentRev;
		} else {
			this.firstRev = (Long)e.getProperty(PROP_FIRST_REVISION);
			this.currentRev = (Long)e.getProperty(PROP_CURRENT_REVISION);
		}
		
	}
	
	private Key getKey(long rev) {
		return this.key.getChild(KIND_XEVENT, Long.toString(rev));
	}
	
	public void appendEvent(XEvent event, Object trans) {
		
		if(!getBaseAddress().equalsOrContains(event.getTarget())) {
			throw new IllegalArgumentException("cannot store event " + event + "in change log for "
			        + getBaseAddress());
		}
		
		Entity e = new Entity(getKey(event.getModelRevisionNumber()));
		
		saveEvent(e, event);
		
		GaeUtils.putEntity(e, trans);
		
		long newRev = event.getModelRevisionNumber() + 1;
		
		assert newRev > 0;
		
		if(newRev > this.currentRev) {
			this.currentRev = newRev;
		}
		
	}
	
	public void saveEvent(Entity e, XEvent event) {
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlEvent.toXml(event, out, this.baseAddr);
		
		/* using Text to store more than 500 characters */
		Text text = new Text(out.getXml());
		e.setUnindexedProperty(PROP_EVENT, text);
	}
	
	public XEvent loadEvent(Entity e, boolean inTrans, long modelRev) {
		/* using Text to store more than 500 characters */
		Text eventText = (Text)e.getProperty(PROP_EVENT);
		String eventStr = eventText.getValue();
		
		MiniXMLParser miniXMLParser = new MiniXMLParserImpl();
		MiniElement miniElement = miniXMLParser.parseXml(eventStr);
		return XmlEvent.toEvent(miniElement, this.baseAddr);
	}
	
	public void delete(Object trans) {
		
		boolean newTrans = (trans == null);
		Object t = newTrans ? GaeUtils.beginTransaction() : trans;
		
		for(long i = this.firstRev; i <= this.currentRev; ++i) {
			GaeUtils.deleteEntity(getKey(i), t);
		}
		
		GaeUtils.deleteEntity(this.key, t);
		
		if(newTrans) {
			GaeUtils.endTransaction(t);
		}
		
	}
	
	public long getCurrentRevisionNumber() {
		return this.currentRev;
	}
	
	public XEvent getEvent(long revisionNumber) {
		
		// IMPROVE cache events
		
		Entity e = GaeUtils.getEntity(getKey(revisionNumber));
		
		if(e == null) {
			return null;
		}
		
		return loadEvent(e, false, revisionNumber);
	}
	
	public long getFirstRevisionNumber() {
		return this.firstRev;
	}
	
	public XAddress getBaseAddress() {
		return this.baseAddr;
	}
	
	public void save(Object trans) {
		
		Entity e = new Entity(this.key);
		
		e.setUnindexedProperty(PROP_FIRST_REVISION, this.firstRev);
		e.setUnindexedProperty(PROP_CURRENT_REVISION, this.currentRev);
		
		GaeUtils.putEntity(e, trans);
		
	}
	
	public boolean truncateToRevision(long revisionNumber, Object trans) {
		
		if(revisionNumber < this.firstRev) {
			return false;
		}
		
		boolean newTrans = (trans == null);
		Object t = newTrans ? GaeUtils.beginTransaction() : trans;
		
		while(this.currentRev > revisionNumber) {
			this.currentRev--;
			GaeUtils.deleteEntity(getKey(this.currentRev), t);
		}
		
		if(newTrans) {
			GaeUtils.endTransaction(t);
		}
		
		return true;
	}
	
	public void setFirstRevisionNumber(long rev) {
		if(this.currentRev != this.firstRev) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.currentRev = this.firstRev = rev;
	}
}
