package org.xydra.core.model.state.impl.gae;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.state.XChangeLogState;
import org.xydra.core.model.state.XStateTransaction;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlEvent;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.server.impl.newgae.GaeTestfixer;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;


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
	private long lastRev;
	
	public GaeChangeLogState(XAddress baseAddr, long currentRev) {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		this.baseAddr = baseAddr;
		
		this.key = GaeUtils.keyForLog(baseAddr);
		
		Entity e = GaeUtils.getEntity(this.key);
		
		if(e == null) {
			this.firstRev = currentRev;
			this.lastRev = currentRev - 1;
		} else {
			this.firstRev = (Long)e.getProperty(PROP_FIRST_REVISION);
			this.lastRev = (Long)e.getProperty(PROP_CURRENT_REVISION);
		}
		
	}
	
	private Key getKey(long rev) {
		return this.key.getChild(KIND_XEVENT, Long.toString(rev));
	}
	
	public void appendEvent(XEvent event, XStateTransaction trans) {
		
		if(!getBaseAddress().equalsOrContains(event.getChangedEntity())) {
			throw new IllegalArgumentException("cannot store event " + event + "in change log for "
			        + getBaseAddress());
		}
		
		Entity e = new Entity(getKey(event.getRevisionNumber()));
		
		saveEvent(e, event);
		
		GaeUtils.putEntity(e, GaeStateTransaction.asTransaction(trans));
		
		long newRev = event.getRevisionNumber();
		
		assert newRev >= 0;
		
		if(newRev > this.lastRev) {
			this.lastRev = newRev;
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
	
	public void delete(XStateTransaction trans) {
		
		Transaction t = GaeStateTransaction.getOrBeginTransaction(trans);
		
		for(long i = this.firstRev; i <= this.lastRev; ++i) {
			GaeUtils.deleteEntity(getKey(i), t);
		}
		
		GaeUtils.deleteEntity(this.key, t);
		
		if(trans == null) {
			GaeUtils.endTransaction(t);
		}
		
	}
	
	public long getCurrentRevisionNumber() {
		return this.lastRev;
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
	
	public void save(XStateTransaction trans) {
		
		Entity e = new Entity(this.key);
		
		e.setUnindexedProperty(PROP_FIRST_REVISION, this.firstRev);
		e.setUnindexedProperty(PROP_CURRENT_REVISION, this.lastRev);
		
		GaeUtils.putEntity(e, GaeStateTransaction.asTransaction(trans));
		
	}
	
	public boolean truncateToRevision(long revisionNumber, XStateTransaction trans) {
		
		if(revisionNumber < this.firstRev) {
			return false;
		}
		
		Transaction t = GaeStateTransaction.getOrBeginTransaction(trans);
		
		while(this.lastRev > revisionNumber) {
			GaeUtils.deleteEntity(getKey(this.lastRev), t);
			this.lastRev--;
		}
		
		if(trans == null) {
			GaeUtils.endTransaction(t);
		}
		
		return true;
	}
	
	public void setFirstRevisionNumber(long rev) {
		if(this.lastRev != this.firstRev) {
			throw new IllegalStateException(
			        "cannot set start revision number of non-empty change log");
		}
		this.lastRev = this.firstRev = rev;
	}
}
