package com.sonicmetrics.core.shared.impl.memory;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.index.query.Pair;

import com.sonicmetrics.core.shared.ISonicDB;
import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.query.ISonicQuery;
import com.sonicmetrics.core.shared.query.ISonicQueryResult;
import com.sonicmetrics.core.shared.query.SonicMetadataResult;
import com.sonicmetrics.core.shared.query.TimeConstraint;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public class SonicMemoryDB implements ISonicDB {
	
	public static final String LAST_UNICODE_CHAR = "\uFFFF";
	
	private SortedMap<String,ISonicEvent> db = new TreeMap<String,ISonicEvent>();
	
	@Override
	public void receiveEvent(ISonicEvent sonicEvent) {
		String key = sonicEvent.getKey();
		if(key == null) {
			key = toKey(sonicEvent.getWhen()) + "-" + UUID.uuid(8);
		}
		// FIXME set key in event
		this.db.put(key, sonicEvent);
	}
	
	/**
	 * @param timeConstraint
	 * @param now
	 * @return a pair of keys or a lookup [inclusive, exclusive)
	 */
	static Pair<String,String> toKeyConstraint(@NeverNull TimeConstraint timeConstraint, long now) {
		// end
		long endTime = timeConstraint.end;
		if(endTime == 0l) {
			endTime = now;
		}
		// start
		String lastKey = timeConstraint.lastKey;
		if(lastKey == null) {
			assert timeConstraint.start > 0;
			long startInclusive = timeConstraint.start;
			lastKey = startInclusive + "";
		}
		
		return new Pair<String,String>(lastKey, toKey(endTime) + LAST_UNICODE_CHAR);
	}
	
	private static String toKey(long now) {
		return now + "";
	}
	
	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	@Override
	public void setKey(ISonicEvent sonicEvent) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public SonicMetadataResult search(String keyword) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ISonicQueryResult query(ISonicQuery sonicQuery) {
		long now = getCurrentTime();
		
		Pair<String,String> keyConstraint = toKeyConstraint(sonicQuery.getTimeConstraint(), now);
		
		Iterator<ISonicEvent> it = this.db
		        .subMap(keyConstraint.getFirst(), keyConstraint.getSecond()).values().iterator();
		
		return new SonicQueryResult(it);
	}
	
	@Override
	public void delete(ISonicQuery sonicQuery) {
		// TODO Auto-generated method stub
		
	}
	
}
