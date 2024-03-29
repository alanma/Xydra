package org.xydra.store.impl.gae;

import java.util.Map.Entry;

import org.xydra.annotations.RunsInGWT;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeModelRevision;
import org.xydra.store.impl.gae.changes.KeyStructure;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.store.impl.utils.IDebugFormatter;
import org.xydra.xgae.datastore.api.SEntity;
import org.xydra.xgae.datastore.api.SKey;

/**
 * @author xamde
 */
@RunsInGWT(false)
public class GaeDebugFormatter implements IDebugFormatter {

	@Override
	public String format(final Object value) {
		if (value instanceof SKey) {
			return KeyStructure.toString((SKey) value);
		} else if (value instanceof SEntity) {
			final SEntity e = (SEntity) value;
			if (e.equals(Memcache.NULL_ENTITY)) {
				return "NullEntity";
			}
			final StringBuffer buf = new StringBuffer();
			buf.append("key:" + e.getKey() + " ");
			for (final Entry<String, Object> a : e.getAttributes().entrySet()) {
				final String aKey = a.getKey();
				final Object aValue = a.getValue();
				buf.append("<br />"
						+ aKey
						+ ": "
						+ DebugFormatter.formatString(aValue == null ? "null" : aValue.toString(),
								300, true) + "; ");
			}
			return "Entity={" + buf.toString() + " }";
		} else if (value instanceof GaeChange) {
			final GaeChange c = (GaeChange) value;
			return "GaeChange {" + DebugFormatter.formatString(c.toString(), 1000, false) + "}";
		} else if (value instanceof GaeModelRevision) {
			final GaeModelRevision g = (GaeModelRevision) value;
			return "GaeModelRevision {" + DebugFormatter.formatString(g.toString(), 140, false)
					+ "}";
		}

		return null;
	}

}
