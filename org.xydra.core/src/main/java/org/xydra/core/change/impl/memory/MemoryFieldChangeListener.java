package org.xydra.core.change.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.base.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * An implementation of {@link XFieldEventListener}
 *
 * @author xamde
 *
 */
@RequiresAppEngine(false)
public class MemoryFieldChangeListener implements XFieldEventListener {

	private static Logger log = LoggerFactory.getLogger(MemoryFieldChangeListener.class);

	@Override
    public void onChangeEvent(final XFieldEvent event) {
		log.info("Value of field " + event.getFieldId() + "with revision number "
		        + event.getOldFieldRevision() + " changed to " + event.getNewValue());
	}
}
