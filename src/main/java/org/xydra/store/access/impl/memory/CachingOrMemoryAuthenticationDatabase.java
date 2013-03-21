package org.xydra.store.access.impl.memory;

import java.util.HashMap;
import java.util.Map;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XAuthenticationDatabase;


/**
 * An in-memory implementation of {@link XAuthenticationDatabase}. An second,
 * optional {@link XAuthenticationDatabase} can act as a persistence layer.
 * 
 * @author voelkel
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class CachingOrMemoryAuthenticationDatabase implements XAuthenticationDatabase {
	
	private static class Actor {
		public int failedLoginAttempts = 0;
		
		public String passwordHash = null;
		
		public Actor(int i) {
			this.failedLoginAttempts = i;
		}
		
		public Actor(String s) {
			this.passwordHash = s;
		}
		public Actor(String s, int i) {
			this.passwordHash = s;
			this.failedLoginAttempts = i;
		}
	}
	private Map<XId,Actor> actors;
	
	private XAuthenticationDatabase secondLevel = null;
	
	/**
	 * @param secondLevel is not null, the second level is checked whenever
	 *            there is no data in this database. This instance then acts as
	 *            a cache. All write are also performed on second level (if not
	 *            null).
	 */
	public CachingOrMemoryAuthenticationDatabase(XAuthenticationDatabase secondLevel) {
		this.actors = new HashMap<XId,Actor>();
		this.secondLevel = secondLevel;
	}
	
	/**
	 * Clear this database and try to clear the underlying database as well.
	 */
	@Override
    public void clear() {
		clearCache();
		this.secondLevel.clear();
	}
	
	/**
	 * Clears this database, but not the underlying second level persistence (if
	 * there is any).
	 */
	public void clearCache() {
		this.actors.clear();
	}
	
	@Override
	@ReadOperation
	public int getFailedLoginAttempts(XId actorId) {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		Actor actor = this.actors.get(actorId);
		if(actor == null) {
			// try to get from 2nd level
			if(this.secondLevel != null) {
				int i = this.secondLevel.getFailedLoginAttempts(actorId);
				if(i != 0) {
					this.actors.put(actorId, new Actor(i));
				}
			}
			return 0;
		} else
			return actor.failedLoginAttempts;
	}
	
	private Actor getOrCreateActor(XId actorId) {
		Actor actor = this.actors.get(actorId);
		if(actor == null) {
			// try to get from 2nd level
			int i = 0;
			String s = null;
			if(this.secondLevel != null) {
				i = this.secondLevel.getFailedLoginAttempts(actorId);
				s = this.secondLevel.getPasswordHash(actorId);
				
			}
			actor = new Actor(s, i);
			this.actors.put(actorId, actor);
		}
		return actor;
	}
	
	@Override
	@ReadOperation
	public String getPasswordHash(XId actorId) {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		Actor actor = this.actors.get(actorId);
		if(actor == null) {
			if(this.secondLevel != null) {
				String s = this.secondLevel.getPasswordHash(actorId);
				if(s != null) {
					this.actors.put(actorId, new Actor(s));
				}
			}
			return null;
		} else
			return actor.passwordHash;
	}
	
	@Override
	@ModificationOperation
	public int incrementFailedLoginAttempts(XId actorId) {
		int failedLoginAttepts = getFailedLoginAttempts(actorId);
		failedLoginAttepts++;
		getOrCreateActor(actorId).failedLoginAttempts = failedLoginAttepts;
		if(this.secondLevel != null) {
			this.secondLevel.incrementFailedLoginAttempts(actorId);
		}
		return failedLoginAttepts;
	}
	
	@Override
	@ModificationOperation
	public void removePasswordHash(XId actorId) {
		XyAssert.xyAssert(actorId != null); assert actorId != null;
		Actor actor = this.actors.get(actorId);
		if(actor != null) {
			actor.passwordHash = null;
		}
		if(this.secondLevel != null) {
			this.secondLevel.removePasswordHash(actorId);
		}
	}
	
	@Override
	@ModificationOperation
	public void resetFailedLoginAttempts(XId actorId) {
		Actor actor = this.actors.get(actorId);
		if(actor != null) {
			actor.failedLoginAttempts = 0;
		}
		if(this.secondLevel != null) {
			this.secondLevel.resetFailedLoginAttempts(actorId);
		}
	}
	
	@Override
	@ModificationOperation
	public void setPasswordHash(XId actorId, String passwordHash) {
		getOrCreateActor(actorId).passwordHash = passwordHash;
		if(this.secondLevel != null) {
			this.secondLevel.setPasswordHash(actorId, passwordHash);
		}
	}
	
}
