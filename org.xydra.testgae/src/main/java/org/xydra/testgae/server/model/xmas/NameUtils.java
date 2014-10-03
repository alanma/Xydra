package org.xydra.testgae.server.model.xmas;

/**
 * Generates funny and often unique names.
 * 
 * @author xamde
 */
public class NameUtils {

	private static final String[] ADJ = { "orange", "black", "super", "hydro", "antifog",
			"explosive", "flying", "invisible", "mighty", "power", "transforming", "shocking",
			"true", "magnetic" };

	private static final String[] NOUN1 = { "pirate", "pocket", "police", "dragon", "ghost",
			"stereo", "3D" };

	private static final String[] NOUN2 = { "warrior", "skull", "sword", "castle", "dictator",
			"server", "router", "package", "ninja" };

	public static String getProductName() {
		int a1 = (int) (Math.round((ADJ.length - 1) * Math.random()));
		int a2 = a1;
		while (a2 == a1) {
			a2 = (int) (Math.round((ADJ.length - 1) * Math.random()));
		}
		int n1 = (int) (Math.round((NOUN1.length - 1) * Math.random()));
		int n2 = (int) (Math.round((NOUN2.length - 1) * Math.random()));
		return

		ADJ[a1].substring(0, 1).toUpperCase() + ADJ[a1].substring(1) + "-" + ADJ[a2] + " "
				+ NOUN1[n1] + " " + NOUN2[n2];
	}

	private static final String[] FIRSTNAME = { "jim", "john", "dirk", "claudia", "tim", "tom" };
	private static final String[] LASTNAME = { "doe", "miller", "smith", "hagemann", "stern" };

	public static String getPersonName() {
		int firstname = (int) (Math.round((FIRSTNAME.length - 1) * Math.random()));
		int lastname = (int) (Math.round((LASTNAME.length - 1) * Math.random()));
		return FIRSTNAME[firstname].substring(0, 1).toUpperCase()
				+ FIRSTNAME[firstname].substring(1) + " "
				+ LASTNAME[lastname].substring(0, 1).toUpperCase()
				+ LASTNAME[lastname].substring(1);
	}

	public static void main(String[] args) {
		System.out.println(getProductName());
		System.out.println(getProductName());
		System.out.println(getProductName());
		System.out.println(getProductName());
		System.out.println(getProductName());
		System.out.println(getProductName());

		System.out.println(getPersonName());
		System.out.println(getPersonName());
		System.out.println(getPersonName());
		System.out.println(getPersonName());
		System.out.println(getPersonName());
		System.out.println(getPersonName());
	}
}
