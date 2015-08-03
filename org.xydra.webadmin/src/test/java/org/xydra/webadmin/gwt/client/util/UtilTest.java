package org.xydra.webadmin.gwt.client.util;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.core.XX;

public class UtilTest {

	@Test
	public void testEntityTree() {
		final EntityTree entityTree = new EntityTree();

		final XAddress[] addresses = new XAddress[] {

		Base.toAddress("/repo1"), Base.toAddress("/repo2"), Base.toAddress("/repo1/phonebook"),
				Base.toAddress("/repo1/phonebook"), Base.toAddress("/repo2/phonebook"),
				Base.toAddress("/repo1/phonebook2"), Base.toAddress("/repo1/phonebook/john"),
				Base.toAddress("/repo1/phonebook/john/deer"),
				Base.toAddress("/repo1/phonebook/claudia"), Base.toAddress("/repo1/wishlist/present"),
				Base.toAddress("/repo2/wishlist/present"), Base.toAddress("/repo3/wishlist/present"),
				Base.toAddress("/repo1/phonebook/sam/iAm")

		};
		for (final XAddress xAddress : addresses) {
			entityTree.add(xAddress);

		}
		System.out.println(entityTree.toString());

		System.out.println("--------------------");

		entityTree.remove(Base.toAddress("/repo3/wishlist/present"));
		entityTree.remove(Base.toAddress("/repo2/wishlist/"));
		entityTree.remove(Base.toAddress("/repo1"));

		System.out.println(entityTree.toString());
	}
}
