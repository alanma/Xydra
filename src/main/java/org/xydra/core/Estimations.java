package org.xydra.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Some estimated numbers for performance calculations and tests
 * 
 * @author xamde
 * 
 */
public class Estimations {
	
	public static final Logger log = LoggerFactory.getLogger(Estimations.class);
	
	public static class LongValue {
		public long min;
		public long max;
		public long typicalMin;
		public long typicalMax;
		public long typical;
		
		public LongValue(long min, long typicalMin, long typical, long typicalMax, long max) {
			this.min = min;
			this.typicalMin = typicalMin;
			this.typical = typical;
			this.typicalMax = typicalMax;
			this.max = max;
		}
		
		public LongValue plus(LongValue other) {
			return new LongValue(this.min + other.min, this.typicalMin + other.typicalMin,
			        this.typical + other.typical, this.typicalMax + other.typicalMax,
			        
			        this.max == Long.MAX_VALUE || other.max == Long.MAX_VALUE ? Long.MAX_VALUE
			                : this.max + other.max);
		}
		
		public LongValue times(LongValue other) {
			return new LongValue(this.min * other.min, this.typicalMin * other.typicalMin,
			        this.typical * other.typical, this.typicalMax * other.typicalMax,
			        
			        this.max == Long.MAX_VALUE || other.max == Long.MAX_VALUE ? Long.MAX_VALUE
			                : this.max * other.max);
		}
		
		@Override
		public String toString() {
			return "min: " + nice(this.min) + " typical: [" + nice(this.typicalMin) + " - "
			        + nice(this.typical) + " - " + nice(this.typicalMax) + "] max: "
			        + nice(this.max);
		}
		
		public LongValue times(long scalar) {
			return new LongValue(this.min * scalar, this.typicalMin * scalar,
			        this.typical * scalar, this.typicalMax * scalar,
			        
			        this.max == Long.MAX_VALUE || scalar == Long.MAX_VALUE ? Long.MAX_VALUE
			                : this.max * scalar);
		}
		
		public LongValue times(double scalar) {
			return new LongValue((long)(this.min * scalar), (long)(this.typicalMin * scalar),
			        (long)(this.typical * scalar), (long)(this.typicalMax * scalar),
			        (long)(this.max * scalar));
		}
		
		public String toByteSizes() {
			return "min: " + byteSize(this.min) + " typical: [" + byteSize(this.typicalMin) + " - "
			        + byteSize(this.typical) + " - " + byteSize(this.typicalMax) + "] max: "
			        + byteSize(this.max);
		}
		
		public static String byteSize(long l) {
			long s = l;
			if(s < 1024) {
				return l + "bytes";
			}
			s = s / 1024;
			if(s < 1024) {
				return s + "KB";
			}
			s = s / 1024;
			if(s < 1024) {
				return s + "MB";
			}
			s = s / 1024;
			if(s < 1024) {
				return s + "GB";
			}
			s = s / 1024;
			return s + "TB (!)";
		}
		
		public static String nice(long l) {
			long s = l;
			if(s < 1000) {
				return s + "";
			}
			s = s / 1000;
			if(s < 1000) {
				return s + "k";
			}
			s = s / 1000;
			if(s < 1000) {
				return s + " Mio.";
			}
			s = s / 1000;
			return s + " Milliarden!";
		}
	}
	
	public static final long ONE_MILLION = 1000000;
	
	public static final long FACEBOOK_MAU = 900 * ONE_MILLION;
	
	public static final long FACEBOOK_DAU = (2 / 3) * FACEBOOK_MAU;
	
	public static final long FACEBOOK_MAX_FRIENDS = 5000;
	
	/** Average number of Facebook contacts in 2011 */
	public static final long FACEBOOK_AVERAGE_FRIENDS = 260;
	
	public static LongValue objectsPerModel = new LongValue(0l, 1l, 200, 10000l, Long.MAX_VALUE);
	
	public static LongValue fieldsPerObject = new LongValue(0l, 2l, 20, 50l, Long.MAX_VALUE);
	
	public static LongValue numberOfUsers = new LongValue(1, 1000, 100000, 1000000, FACEBOOK_MAU);
	
	/** = same as users */
	public static LongValue numberOfModels = numberOfUsers;
	
	public static LongValue itemsPerUser = new LongValue(1, 2, 100, 1000, 1000000);
	
	public static LongValue collectionSharedWithUsers = new LongValue(1, 2, 5, 800, ONE_MILLION);
	
	/** other users that are subscribed to me */
	public static LongValue contactsPerUser_subscribers = new LongValue(0, 20, 260,
	        FACEBOOK_MAX_FRIENDS, ONE_MILLION);
	
	/** other users which i am subscribed to */
	public static LongValue contactsPerUser_publishers = new LongValue(0, 20,
	        FACEBOOK_AVERAGE_FRIENDS, 500, 5000);
	
	public static LongValue contactsPerUser = new LongValue(0, 1, 5, FACEBOOK_MAX_FRIENDS,
	        ONE_MILLION);
	
	/** people who are just allowed to read what some writer collective writes */
	public static LongValue sharedReadersPerItem = new LongValue(1, 2, 5, FACEBOOK_MAX_FRIENDS,
	        ONE_MILLION);
	
	public static final long localStorageSpaceInCharacters = 5 * 1024 * 1024 / 2;
	
	/**
	 * i.e. several authors working on the same article. Only this set can
	 * create write-conflicts
	 */
	public static LongValue sharedWritersPerItem = new LongValue(1, 1, 2, 600, FACEBOOK_MAX_FRIENDS);
	
	/** per minute */
	public static LongValue concurrentUsers = numberOfUsers.times(0.1d);
	
	public static LongValue devicesPerUser = new LongValue(0, 1, 2, 5, 100);
	
	/**
	 * a generated RFC4122, version 4 ID. Example:
	 * "92329D39-6F5C-4520-ABFC-AAB64544E172"
	 */
	public static LongValue lengthOfXid_inCharacters = new LongValue(1, 36, 36, 36, 100);
	
	public static LongValue lengthOfValue_inCharacters = new LongValue(1, 10, 50, 200,
	        Integer.MAX_VALUE);
	
	/** Two for UTF-8, two for overhead */
	public static long bytesPerCharacter = 4;
	
	public static LongValue sizeOfObject_inCharacters = fieldsPerObject
	        .times(lengthOfXid_inCharacters.plus(lengthOfValue_inCharacters));
	
	public static LongValue sizeOfModel_inCharacters = sizeOfObject_inCharacters
	        .times(objectsPerModel);
	
	public static long bytesPerRevisionNumber = 8;
	
	public static long bytesPerValueAddress = lengthOfXid_inCharacters.typical;
	
	public static class UserSession {
		int durationInMs = 30 * 60 * 1000;
		List<UserAction> ops = new ArrayList<UserAction>();
		
		public UserSession(UserAction ... ops) {
			for(int i = 0; i < ops.length; i++) {
				this.ops.add(ops[i]);
			}
		}
	}
	
	public static class UserAction {
		List<Operation> ops = new ArrayList<Operation>();
		
		public UserAction(Operation ... ops) {
			for(int i = 0; i < ops.length; i++) {
				this.ops.add(ops[i]);
			}
		}
	}
	
	/**
	 * Such as write datastore, send email, put memcache, ...
	 */
	public static class Operation {
		String name;
		
		public Operation(String name) {
			this.name = name;
		}
	}
	
	public static class Collection {
		
	}
	
	public static class Item {
		
	}
	
	/**
	 * @param timeslots
	 * @param operations
	 * @return the probability of at least two operations in the given timeslots
	 *         to occur at the same time. Each operation takes one time slot.
	 */
	public static double p_conflict(int timeslots, int operations) {
		if(operations < 2) {
			return 0;
		}
		
		int n = timeslots;
		int k = operations;
		double f = pi(numbersBetween(n - k, n));
		double e = Math.pow(n, k);
		double p = 1d - (f / e);
		log.trace(p + "= 1 - (" + f + "/" + e + ")");
		return p;
	}
	
	public static double pi(int ... factors) {
		double result = 1;
		for(int i = 0; i < factors.length; i++) {
			result *= factors[i];
		}
		return result;
	}
	
	/**
	 * @param first exclusive
	 * @param last inclusive
	 * @return an array that looks like this: [first+1, first+2, ... last]
	 */
	public static int[] numbersBetween(int first, int last) {
		int size = last - first;
		int[] result = new int[size];
		for(int i = 0; i < result.length; i++) {
			result[i] = first + i + 1;
		}
		log.trace("Between " + first + " and " + last + " = " + Arrays.toString(result));
		return result;
	}
	
	public static double p_appengineWrite(int writesPerMinute) {
		return p_conflict(60, writesPerMinute);
	}
	
	public static void main(String[] args) {
		System.out.println("Typical fields per user = " + objectsPerModel.times(fieldsPerObject));
		System.out.println("Typical object size in bytes = "
		        + sizeOfObject_inCharacters.times(bytesPerCharacter).toByteSizes());
		System.out.println("Typical model size in bytes = "
		        + sizeOfModel_inCharacters.times(bytesPerCharacter).toByteSizes());
		System.out.println("concurrent users: " + concurrentUsers);
		System.out.println("Typical memory usage for snapshots of concurrent users "
		        + concurrentUsers.times(sizeOfModel_inCharacters).toByteSizes());
		
		System.out.println("Model size if values are external "
		        + fieldsPerObject.times(lengthOfXid_inCharacters)
		                .times(bytesPerCharacter + bytesPerRevisionNumber + bytesPerValueAddress)
		                .toByteSizes());
		
		for(int i = 1; i < 30; i++) {
			System.out.println("i=" + i + " : " + p_appengineWrite(i));
		}
	}
	
}
