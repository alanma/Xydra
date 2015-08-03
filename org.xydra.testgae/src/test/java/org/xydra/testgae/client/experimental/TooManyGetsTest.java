package org.xydra.testgae.client.experimental;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.testgae.server.model.xmas.Xmas;

public class TooManyGetsTest {

	@SuppressWarnings("unused")
	@Test
	public void testTooManyGetsIssue() {
		final Writer w = new OutputStreamWriter(System.out);

		final XWritableRepository repo = Xmas.getRepository("repo1");

		System.out.println("-------- creating model");

		final XWritableModel model = repo.createModel(Base.toId("list1"));
		System.out.println("-------- creaintg object");

		final XWritableObject xo = model.createObject(Base.toId("wish1"));
		// Wish wish = new Wish(xo);

		// Xmas.addData("test-repo", 1, 1, w);

		// WishList wishList = WishlistResource
		// .load(Xmas.getRepository("test-repo"), XX.toId("list1"));
		// wishList.toHtml();
	}

}
