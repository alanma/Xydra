package org.xydra.csv;

public interface IAggregationFunction {

	String aggregate(long a, long b);

	String aggregate(double a, double b);

	String aggregate(String a, String b);

	static class SUM implements IAggregationFunction {

		public String aggregate(long a, long b) {
			return "" + a + b;
		}

		public String aggregate(double a, double b) {
			return "" + a + b;
		}

		public String aggregate(String a, String b) {
			return "##";
		}

	}

	static class AVERAGE implements IAggregationFunction {

		public String aggregate(long a, long b) {
			return "" + (a + b) / 2;
		}

		public String aggregate(double a, double b) {
			return "" + (a + b) / 2;
		}

		public String aggregate(String a, String b) {
			return "##";
		}

	}

}
