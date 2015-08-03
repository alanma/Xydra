package org.xydra.store.util;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XEvent;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.XydraPersistence;


public class StoreUtils {

    private static final Logger log = LoggerFactory.getLogger(StoreUtils.class);

    public static void dumpChangeLog(final XydraPersistence pers, final XAddress modelAddress) {
        final List<XEvent> events = pers.getEvents(modelAddress, 0, Long.MAX_VALUE);
        for(final XEvent event : events) {
            log.info("Event " + event.getRevisionNumber() + ": " + event);
        }
    }

}
