package org.xydra.webadmin.gwt.client.util;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.core.XX;

public class UtilTest {

	@Test
	public void testEntityTree() {
		EntityTree entityTree = new EntityTree();

		XAddress[] addresses = new XAddress[] {

		XX.toAddress("/repo1"), XX.toAddress("/repo2"), XX.toAddress("/repo1/phonebook"),
				XX.toAddress("/repo1/phonebook"), XX.toAddress("/repo2/phonebook"),
				XX.toAddress("/repo1/phonebook2"), XX.toAddress("/repo1/phonebook/john"),
				XX.toAddress("/repo1/phonebook/john/deer"),
				XX.toAddress("/repo1/phonebook/claudia"), XX.toAddress("/repo1/wishlist/present"),
				XX.toAddress("/repo2/wishlist/present"), XX.toAddress("/repo3/wishlist/present"),
				XX.toAddress("/repo1/phonebook/sam/iAm")

		};
		for (XAddress xAddress : addresses) {
			entityTree.add(xAddress);

		}
		System.out.println(entityTree.toString());

		System.out.println("--------------------");

		entityTree.remove(XX.toAddress("/repo3/wishlist/present"));
		entityTree.remove(XX.toAddress("/repo2/wishlist/"));
		entityTree.remove(XX.toAddress("/repo1"));

		System.out.println(entityTree.toString());
	}
}
