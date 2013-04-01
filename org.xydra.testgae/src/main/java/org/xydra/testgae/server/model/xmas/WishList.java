package org.xydra.testgae.server.model.xmas;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.core.X;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.NanoClock;


/**
 * One wish list can have many, many wishes. One wish list is persisted as one
 * model.
 * 
 * @author xamde
 */
public class WishList implements Iterable<XId> {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishList.class);
	
	private XWritableModel model;
	
	public WishList(XWritableModel model) {
		assert model != null;
		this.model = model;
		
	}
	
	@SuppressWarnings("unused")
	public XId addWish(String title, int price, String url) {
		XId id = X.getIDProvider().createUniqueId();
		XWritableObject xo = this.model.createObject(id);
		new Wish(xo, title, price, url);
		return id;
	}
	
	public void removeWish(XId widhId) {
		this.model.removeObject(widhId);
	}
	
	public void editWishName(XId wishId, String productName) {
		XWritableObject xo = this.model.getObject(wishId);
		XWritableField title = xo.getField(Wish.TITLE);
		title.setValue(XV.toValue(productName));
	}
	
	public void editWishPrice(XId wishId, int price) {
		XWritableObject xo = this.model.getObject(wishId);
		XWritableField priceField = xo.getField(Wish.PRICE);
		priceField.setValue(XV.toValue(price));
	}
	
	public void editWishUrl(XId wishId, String url) {
		XWritableObject xo = this.model.getObject(wishId);
		XWritableField urlField = xo.getField(Wish.URL);
		urlField.setValue(XV.toValue(url));
	}
	
	public void removeAllWishes(Writer writer) throws IOException {
		NanoClock s1 = new NanoClock().start();
		Iterator<XId> it = this.model.iterator();
		LinkedList<XId> list = new LinkedList<XId>();
		while(it.hasNext()) {
			XId xid = it.next();
			list.add(xid);
		}
		for(XId xid : list) {
			this.model.removeObject(xid);
		}
		
		s1.stop("removeAllWishes");
		writer.write(s1.getStats());
	}
	
	public Wish getWish(XId wishId) {
		if(this.model.hasObject(wishId)) {
			return new Wish(this.model.getObject(wishId));
		} else {
			return null;
		}
	}
	
	/**
	 * @return wish list as HTML
	 */
	public String toHtml() {
		NanoClock s1 = new NanoClock().start();
		StringBuffer buf = new StringBuffer();
		buf.append("<b>Wishlist " + this.model.getId() + "</b>");
		buf.append("<ol>\n");
		for(XId wishId : this.model) {
			Wish wish = getWish(wishId);
			buf.append("  <li>");
			buf.append(wish.toHtml());
			buf.append("</li>\n");
		}
		buf.append("</ol>\n");
		s1.stop("read-wishes-and-properties");
		buf.append(HtmlUtils.link("/xmas/" + this.model.getAddress().getRepository() + "/"
		        + this.model.getId() + "/clear", "Delete all wishes")
		        + "<br />\n");
		// add stats
		buf.append(s1.getStats() + "<br />\n");
		return buf.toString();
	}
	
	/**
	 * @param wishesCount how many wished to create and add to this list
	 * @param writer to which some performance data is written, if not null. May
	 *            be null.
	 * @return a list of all IDs that have just been created
	 * @throws IOException if the writer has them
	 */
	public List<XId> addDemoData(int wishesCount, Writer writer) throws IOException {
		NanoClock s1 = new NanoClock();
		s1.start();
		List<XId> wishIds = new LinkedList<XId>();
		for(int w = 1; w <= wishesCount; w++) {
			writer.write("Creating wish " + w + " in list " + this.model.getId() + " at "
			        + System.currentTimeMillis() + "<br />\n");
			String name = NameUtils.getProductName();
			String nameInUrl = name.replace(" ", "+");
			XId wishId = addWish("" + name, (int)Math.round(100 * Math.random()),
			        "http://www.google.de/images?q=" + nameInUrl);
			wishIds.add(wishId);
		}
		s1.stop("created-" + wishesCount + "-wishes");
		if(writer != null) {
			writer.write(s1.getStats() + "<br />\n");
		}
		return wishIds;
	}
	
	@Override
	public Iterator<XId> iterator() {
		return this.model.iterator();
	}
	
}
