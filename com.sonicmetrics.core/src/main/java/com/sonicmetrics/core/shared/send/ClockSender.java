package com.sonicmetrics.core.shared.send;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent.SeBuilder;
import com.sonicmetrics.core.shared.query.ISonicListener;

public class ClockSender {

	private static final long ONE_MINUTE = 60000;

	/**
	 * round up to the next minute and schedule 15 events
	 * 
	 * @param startTime
	 *            use current system time here to schedule events
	 * @param listener
	 *            to which to send the events
	 */
	public static void scheduleMinuteEvents(long startTime, ISonicListener listener) {
		long minute = (startTime / ONE_MINUTE) + 1;
		for (int i = 0; i < 15; i++) {
			long time = (minute + i) * ONE_MINUTE;

			SeBuilder b = SonicEvent.create(time);
			b.category("time");
			b.action("minute");
			b.uniqueId("" + time);
			b.subject("time");
			b.source("ClockResource");
			ISonicEvent sonicEvent = b.build();
			listener.receiveEvent(sonicEvent);
		}
	}

}
