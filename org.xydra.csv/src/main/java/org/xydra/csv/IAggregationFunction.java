package org.xydra.csv;

public interface IAggregationFunction {

	static class AVERAGE implements IAggregationFunction {

		@Override
        public String aggregate(double a, double b) {
			return "" + (a + b) / 2;
		}

		@Override
        public String aggregate(long a, long b) {
			return "" + (a + b) / 2;
		}

		@Override
        public String aggregate(String a, String b) {
			return "##";
		}

	}

	static class SUM implements IAggregationFunction {

		@Override
        public String aggregate(double a, double b) {
			return "" + a + b;
		}

		@Override
        public String aggregate(long a, long b) {
			return "" + a + b;
		}

		@Override
        public String aggregate(String a, String b) {
			return "##";
		}

	}

	String aggregate(double a, double b);

	String aggregate(long a, long b);

	String aggregate(String a, String b);

}
