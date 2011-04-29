package org.xydra.testgae.xmas.data;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.testgae.Stopwatch;
import org.xydra.testgae.xmas.NameUtils;


public class WishList {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishList.class);
	
	private XWritableModel model;
	
	public WishList(XWritableModel model) {
		assert model != null;
		this.model = model;
		
	}
	
	public XID addWish(String title, int price, String url) {
		XID id = X.getIDProvider().createUniqueId();
		XWritableObject xo = this.model.createObject(id);
		new Wish(xo, title, price, url);
		return id;
	}
	
	public void removeWish(XID widhId) {
		this.model.removeObject(widhId);
	}
	
	public void removeAllWishes(Writer writer) throws IOException {
		Stopwatch s1 = new Stopwatch().start();
		Iterator<XID> it = this.model.iterator();
		LinkedList<XID> list = new LinkedList<XID>();
		while(it.hasNext()) {
			XID xid = it.next();
			list.add(xid);
		}
		for(XID xid : list) {
			this.model.removeObject(xid);
		}
		s1.stop();
		writer.write(s1.getFormattedResult("delete wish", list.size()) + "<br />\n");
	}
	
	public Wish getWish(XID wishId) {
		if(this.model.hasObject(wishId)) {
			return new Wish(this.model.getObject(wishId));
		} else {
			return null;
		}
	}
	
	/**
	 * @param w for performance data
	 * @return HTML
	 */
	public String toHtml(Writer w) throws IOException {
		Stopwatch s1 = new Stopwatch().start();
		StringBuffer buf = new StringBuffer();
		buf.append("<b>Wishlist " + this.model.getID() + "</b>");
		buf.append("<ol>\n");
		int count = 0;
		for(XID wishId : this.model) {
			count++;
			Wish wish = getWish(wishId);
			buf.append("  <li>");
			buf.append(wish.toHtml());
			buf.append("</li>\n");
		}
		buf.append("</ol>\n");
		s1.stop();
		if(w != null) {
			w.write(s1.getFormattedResult("read wish+props", count) + "<br />\n");
		}
		buf.append(HtmlUtils.link("/xmas/" + this.model.getAddress().getRepository() + "/"
		        + this.model.getID() + "/clear", "Delete all wishes")
		        + "<br />\n");
		return buf.toString();
	}
	
	public List<XID> addDemoData(int wishesCount, Writer writer) throws IOException {
		Stopwatch s1 = new Stopwatch();
		s1.start();
		List<XID> wishIds = new LinkedList<XID>();
		for(int w = 1; w <= wishesCount; w++) {
			writer.write("Creating wish " + w + " in list " + this.model.getID() + " at "
			        + System.currentTimeMillis() + "<br />\n");
			String name = NameUtils.getProductName();
			String nameInUrl = name.replace(" ", "+");
			XID wishId = addWish("" + name, (int)Math.round(100 * Math.random()),
			        "http://www.google.de/images?q=" + nameInUrl);
			wishIds.add(wishId);
		}
		s1.stop();
		writer.write(s1.getFormattedResult("create wish", wishesCount) + "<br />\n");
		return wishIds;
	}
	
}
