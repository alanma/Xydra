package org.xydra.core.model.state.impl.gae;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;
import org.xydra.core.model.state.XFieldState;
import org.xydra.core.value.XValue;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlValue;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.server.impl.gae.OldGaeUtils;
import org.xydra.store.impl.gae.GaeUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


/**
 * An implementation of {@link XFieldState} that persists to the Google App
 * Engine {@link DatastoreService}.
 */
public class GaeFieldState extends AbstractGaeState implements XFieldState {
	
	private static final long serialVersionUID = 8492473097214011504L;
	
	private XValue value;
	
	public GaeFieldState(XAddress fieldAddr) {
		super(fieldAddr);
		if(fieldAddr.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("must be a field address, was: " + fieldAddr);
		}
	}
	
	/** Load value, parentAddress */
	@Override
	public void loadFromEntity(Entity e) {
		super.loadFromEntity(e);
		// value
		Text prop = (Text)e.getProperty(GaeSchema.PROP_VALUE);
		if(prop != null) {
			String valueAsXml = prop.getValue();
			if(valueAsXml != null) {
				MiniXMLParser miniXMLParser = new MiniXMLParserImpl();
				MiniElement miniElement = miniXMLParser.parseXml(valueAsXml);
				this.value = XmlValue.toValue(miniElement);
			}
		}
	}
	
	public XValue getValue() {
		loadIfNecessary();
		return this.value;
	}
	
	@Override
	protected void storeInEntity(Entity e) {
		super.storeInEntity(e);
		// value
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		if(this.value != null) {
			XmlValue.toXml(this.value, xo);
			String valueAsXml = xo.getXml();
			e.setUnindexedProperty(GaeSchema.PROP_VALUE, new Text(valueAsXml));
		}
	}
	
	public void setValue(XValue value) {
		loadIfNecessary();
		this.value = value;
	}
	
	public static XFieldState load(XAddress fieldStateAddress) {
		Key key = OldGaeUtils.keyForEntity(fieldStateAddress);
		Entity entity = GaeUtils.getEntity(key);
		if(entity == null) {
			return null;
		}
		GaeFieldState fieldState = new GaeFieldState(fieldStateAddress);
		fieldState.loadFromEntity(entity);
		return fieldState;
	}
	
	public XID getID() {
		return getAddress().getField();
	}
	
}
