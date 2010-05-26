package org.xydra.core.access.impl.gae;

import java.util.Iterator;

import org.xydra.core.access.XAccessDefinition;
import org.xydra.core.access.XAccessEvent;
import org.xydra.core.access.XAccessListener;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.model.XAddress;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlAccess;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.server.gae.GaeTestfixer;
import org.xydra.server.gae.GaeUtils;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;



public class GaeAccess {
	
	private static final String PREFIX_ARM = "access/";
	private static final String PROP_DEFS = "definitions";
	private static final String KIND_ARM = "arm";
	
	public static XAccessManager loadAccessManager(XAddress addr, XGroupDatabase groups) {
		
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		Key armKey = getAccessManagerKey(addr);
		
		Entity e = GaeUtils.getEntity(armKey);
		
		XAccessManager arm;
		
		if(e == null) {
			// empty ARM
			arm = new MemoryAccessManager(groups);
			
		} else {
			
			String defsStr = (String)e.getProperty(PROP_DEFS);
			
			MiniElement xml = new MiniXMLParserImpl().parseXml(defsStr);
			arm = XmlAccess.toAccessManager(xml, groups);
		}
		
		arm.addListener(new Persister(addr, arm));
		
		return arm;
		
	}
	
	private static Key getAccessManagerKey(XAddress addr) {
		return KeyFactory.createKey(KIND_ARM, PREFIX_ARM + addr.toString());
	}
	
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
	
	private static class Persister implements XAccessListener {
		
		private final XAddress addr;
		private final XAccessManager arm;
		
		public Persister(XAddress addr, XAccessManager arm) {
			this.addr = addr;
			this.arm = arm;
		}
		
		public void onAccessEvent(XAccessEvent event) {
			Iterator<XAccessDefinition> it = this.arm.getDefinitions();
			if(it.hasNext()) {
				saveAccessManager(this.addr, it);
			} else {
				deleteAccessManager(this.addr);
			}
		}
		
	}
	
}
