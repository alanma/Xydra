package org.xydra.core.change.impl.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;


/**
 * An implementation of {@link XFieldEventListener}
 * 
 * @author voelkel
 * 
 */
@RunsInJava
public class MemoryFieldChangeListener implements XFieldEventListener {
	
	private static Logger log = LoggerFactory.getLogger(MemoryFieldChangeListener.class);
	
	public void onChangeEvent(XFieldEvent event) {
		log.info("Value of field " + event.getFieldID() + "with revision number "
		        + event.getFieldRevisionNumber() + " changed to " + event.getNewValue());
	}
}
