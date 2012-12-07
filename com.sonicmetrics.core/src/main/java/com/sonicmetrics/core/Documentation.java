package com.sonicmetrics.core;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.ISonicPotentialEvent;
import com.sonicmetrics.core.shared.impl.memory.SonicEvent;
import com.sonicmetrics.core.shared.query.ISonicFilter;
import com.sonicmetrics.core.shared.query.ISonicQuery;


/**
 * SonicMetrics allows to write and query events.
 * 
 * <h3>Writing events</h3> Every event has a category and action (what), a
 * subject (about whom?), a source (who reports?). It can also have a uniqueId
 * which allows updating an already written event. Additionally, an event can
 * carry a simple value or even arbitrary key-value pairs as extension data.
 * 
 * An event ready to be written is an {@link ISonicPotentialEvent}, once written
 * it becomes an {@link ISonicEvent}. An {@link ISonicEvent} has a definitive
 * time-stamp and a key, which is used for persistence.
 * 
 * The default implementation is {@link SonicEvent}.
 * 
 * <h3>Querying events</h3> Events can be queried with an {@link ISonicFilter},
 * which defines which category, action, subject or source is to be constrained.
 * 
 * For constraining time or setting a limit on the amount of events returned, an
 * {@link ISonicQuery} is used.
 * 
 * @author xamde
 */
public interface Documentation {
}
