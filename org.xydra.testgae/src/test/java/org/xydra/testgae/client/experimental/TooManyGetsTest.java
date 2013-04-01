package org.xydra.testgae.client.experimental;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.Test;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.core.XX;
import org.xydra.testgae.server.model.xmas.Xmas;


public class TooManyGetsTest {
	
	@SuppressWarnings("unused")
	@Test
	public void testTooManyGetsIssue() {
		Writer w = new OutputStreamWriter(System.out);
		
		XWritableRepository repo = Xmas.getRepository("repo1");
		
		System.out.println("-------- creating model");
		
		XWritableModel model = repo.createModel(XX.toId("list1"));
		System.out.println("-------- creaintg object");
		
		XWritableObject xo = model.createObject(XX.toId("wish1"));
		// Wish wish = new Wish(xo);
		
		// Xmas.addData("test-repo", 1, 1, w);
		
		// WishList wishList = WishlistResource
		// .load(Xmas.getRepository("test-repo"), XX.toId("list1"));
		// wishList.toHtml();
	}
	
}
