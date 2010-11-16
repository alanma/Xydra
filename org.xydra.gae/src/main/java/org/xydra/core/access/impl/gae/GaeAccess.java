package org.xydra.core.access.impl.gae;

import java.util.Iterator;

import org.xydra.core.access.XAccessDefinition;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabaseWithListeners;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlAccess;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.server.impl.newgae.GaeTestfixer;
import org.xydra.server.impl.newgae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


/**
 * Utility that can persist and load an {@link XAccessManager} in the GAE
 * datastore.
 * 
 * The XAccessManager is represented by a single entity that contains an
 * XML-encoded list of {@link XAccessDefinition}s. The whole ARM iw serialized
 * and saved to the entity on every change.
 * 
 * IMPROVE create a real GAE XAccessManager implementation to lower startup
 * costs and not save the whole ARM on every change.
 * 
 * 
 * @author dscharrer
 * 
 */
public class GaeAccess {
	
	private static final String PREFIX_ARM = "access/";
	private static final String PROP_DEFS = "definitions";
	private static final String KIND_ARM = "arm";
	
	/**
	 * Load the whole access manager from the GAE datastore into memory. Changes
	 * to the returned {@link XAccessManager} are persisted.
	 */
	public static XAccessManager loadAccessManager(XAddress addr, XGroupDatabaseWithListeners groups) {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		// Load entity containing the access definitions for ARM.
		Key armKey = getAccessManagerKey(addr);
		Entity e = GaeUtils.getEntity(armKey);
		XAccessManager arm;
		
		if(e == null) {
			// There was no ARM for the given access in the state store, so
			// just return an empty one.
			// The entity will be created by the Persister when needed.
			arm = new MemoryAccessManager(groups);
		} else {
			// Deserialize the access definitions contained in the entity.
			String defsStr = (String)e.getProperty(PROP_DEFS);
			MiniElement xml = new MiniXMLParserImpl().parseXml(defsStr);
			arm = XmlAccess.toAccessManager(xml, groups);
		}
		
		// Listen to changes made so they can be persisted.
		arm.addListener(new Persister(addr, arm));
		
		return arm;
	}
	
	private static Key getAccessManagerKey(XAddress addr) {
		return KeyFactory.createKey(KIND_ARM, PREFIX_ARM + addr.toString());
	}
	
	/**
	 * Store the given list of access definitions in the ARM entity for the
	 * given XAddress.
	 */
	private static void saveAccessManager(XAddress addr, Iterator<XAccessDefinition> defs) {
		Key armKey = getAccessManagerKey(addr);
		Entity e = new Entity(armKey);
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlAccess.toXml(defs, out);
		
		e.setUnindexedProperty(PROP_DEFS, out.getXml());
		GaeUtils.putEntity(e);
	}
	
	private static void deleteAccessManager(XAddress addr) {
		GaeUtils.deleteEntity(getAccessManagerKey(addr));
	}
	
	/**
	 * Listen to {@link XAccessEvent}s and persist in data store immediately.
	 * 
	 * @author dscharrer
	 */
	private static class Persister implements XAccessListener {
		
		private final XAddress addr;
		private final XAccessManager arm;
		
		public Persister(XAddress addr, XAccessManager arm) {
			this.addr = addr;
			this.arm = arm;
		}
		
		public void onAccessEvent(XAccessEvent event) {
			
			// FIXME handle concurrency
			
			Iterator<XAccessDefinition> it = this.arm.getDefinitions();
			if(it.hasNext()) {
				saveAccessManager(this.addr, it);
			} else {
				deleteAccessManager(this.addr);
			}
		}
		
	}
	
}
