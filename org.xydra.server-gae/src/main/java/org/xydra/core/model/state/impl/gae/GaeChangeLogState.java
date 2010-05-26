package org.xydra.core.model.state.impl.gae;

import org.xydra.core.XX;
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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;



public class GaeChangeLogState implements XChangeLogState {
	
	private static final long serialVersionUID = 6673976783142023642L;
	
	private static final String PROP_FIRST_REVISION = "firstRevision";
	private static final String PROP_CURRENT_REVISION = "lastRevision";
	private static final String KIND_CHANGELOG = "changelog";
	private static final String KIND_XEVENT = "xevent";
	private static final String PREFIX_CHANGELOG = "logs/";
	private static final String PROP_EVENT = "event";
	
	private final XAddress modelAddr;
	
	private final String prefix;
	private final Key key;
	private long firstRev;
	private long currentRev;
	
	public GaeChangeLogState(XAddress address, long currentRev) {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		this.modelAddr = address;
		
		String name = PREFIX_CHANGELOG + address.toString();
		this.prefix = name + "/";
		
		this.key = KeyFactory.createKey(KIND_CHANGELOG, name);
		
		Key key = KeyFactory.createKey(KIND_CHANGELOG, name);
		Entity e = GaeUtils.getEntity(key);
		
		if(e == null) {
			this.firstRev = this.currentRev = currentRev;
		} else {
			this.firstRev = (Long)e.getProperty(PROP_FIRST_REVISION);
			this.currentRev = (Long)e.getProperty(PROP_CURRENT_REVISION);
		}
		
	}
	
	private Key getKey(long rev) {
		return KeyFactory.createKey(KIND_XEVENT, this.prefix + rev);
	}
	
	public void appendEvent(XEvent event) {
		
		if(!XX.equalsOrContains(getModelAddress(), event.getTarget())) {
			throw new IllegalArgumentException("cannot store event " + event + "in change log for "
			        + getModelAddress());
		}
		
		Entity e = new Entity(getKey(event.getModelRevisionNumber()));
		
		saveEvent(e, event);
		
		GaeUtils.putEntity(e);
		
		long newRev = event.getModelRevisionNumber() + 1;
		
		assert newRev > 0;
		
		if(newRev > this.currentRev) {
			this.currentRev = newRev;
		}
		
	}
	
	public void saveEvent(Entity e, XEvent event) {
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlEvent.toXml(event, out, this.modelAddr);
		
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
		return XmlEvent.toEvent(miniElement, this.modelAddr);
	}
	
	public void delete() {
		
		for(long i = this.firstRev; i <= this.currentRev; ++i) {
			GaeUtils.deleteEntity(getKey(i));
		}
		
		GaeUtils.deleteEntity(this.key);
		
	}
	
	public long getCurrentRevisionNumber() {
		return this.currentRev;
	}
	
	public XEvent getEvent(long revisionNumber) {
		
		// IMPROVE cache events
		
		// TODO is it ok to save each event as it's own entity?
		
		Entity e = GaeUtils.getEntity(getKey(revisionNumber));
		
		if(e == null) {
			return null;
		}
		
		return loadEvent(e, false, revisionNumber);
	}
	
	public long getFirstRevisionNumber() {
		return this.firstRev;
	}
	
	public XAddress getModelAddress() {
		return this.modelAddr;
	}
	
	public void save() {
		
		Entity e = new Entity(this.key);
		
		e.setUnindexedProperty(PROP_FIRST_REVISION, this.firstRev);
		e.setUnindexedProperty(PROP_CURRENT_REVISION, this.currentRev);
		
		GaeUtils.putEntity(e);
		
	}
	
	public boolean truncateToRevision(long revisionNumber) {
		throw new AssertionError("the server change log cannot be truncated");
	}
	
}
