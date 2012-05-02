package org.xydra.core;

/**
 * Some estimated numbers for performance calculations and tests
 * 
 * @author xamde
 * 
 */
public class Estimations {
	
	public static class LongValue {
		long min, max, typicalMin, typicalMax;
		long typical;
		
		public LongValue(long min, long typicalMin, long typical, long typicalMax, long max) {
			this.min = min;
			this.typicalMin = typicalMin;
			this.typical = typical;
			this.typicalMax = typicalMax;
			this.max = max;
		}
		
		public LongValue plus(LongValue other) {
			return new LongValue(this.min + other.min, this.typicalMin + other.typicalMin,
			        this.typical + other.typical, this.typicalMax + other.typicalMax, this.max
			                + other.max);
		}
		
		public LongValue times(LongValue other) {
			return new LongValue(this.min * other.min, this.typicalMin * other.typicalMin,
			        this.typical * other.typical, this.typicalMax * other.typicalMax, this.max
			                * other.max);
		}
		
		@Override
		public String toString() {
			return "min: " + this.min + " typical: [" + this.typicalMin + " - " + this.typical
			        + " - " + this.typicalMax + "] max: " + this.max;
		}
		
		public LongValue times(long scalar) {
			return new LongValue(this.min * scalar, this.typicalMin * scalar,
			        this.typical * scalar, this.typicalMax * scalar, this.max * scalar);
		}
	}
	
	public static LongValue objectsPerModel = new LongValue(0l, 1l, 200, 1000l, Long.MAX_VALUE);
	
	public static LongValue fieldsPerObject = new LongValue(0l, 2l, 20, 50l, Long.MAX_VALUE);
	
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
	
	public static void main(String[] args) {
		System.out.println("Typical object size in bytes = "
		        + sizeOfObject_inCharacters.times(bytesPerCharacter));
		System.out.println("Typical model size in bytes = "
		        + sizeOfModel_inCharacters.times(bytesPerCharacter));
	}
	
}
