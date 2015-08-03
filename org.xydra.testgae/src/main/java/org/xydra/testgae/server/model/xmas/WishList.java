package org.xydra.testgae.server.model.xmas;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.common.NanoClock;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.SharedHtmlUtils;

/**
 * One wish list can have many, many wishes. One wish list is persisted as one
 * model.
 *
 * @author xamde
 */
public class WishList implements Iterable<XId> {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WishList.class);

	private final XWritableModel model;

	public WishList(final XWritableModel model) {
		assert model != null;
		this.model = model;

	}

	@SuppressWarnings("unused")
	public XId addWish(final String title, final int price, final String url) {
		final XId id = BaseRuntime.getIDProvider().createUniqueId();
		final XWritableObject xo = this.model.createObject(id);
		new Wish(xo, title, price, url);
		return id;
	}

	public void removeWish(final XId widhId) {
		this.model.removeObject(widhId);
	}

	public void editWishName(final XId wishId, final String productName) {
		final XWritableObject xo = this.model.getObject(wishId);
		final XWritableField title = xo.getField(Wish.TITLE);
		title.setValue(XV.toValue(productName));
	}

	public void editWishPrice(final XId wishId, final int price) {
		final XWritableObject xo = this.model.getObject(wishId);
		final XWritableField priceField = xo.getField(Wish.PRICE);
		priceField.setValue(XV.toValue(price));
	}

	public void editWishUrl(final XId wishId, final String url) {
		final XWritableObject xo = this.model.getObject(wishId);
		final XWritableField urlField = xo.getField(Wish.URL);
		urlField.setValue(XV.toValue(url));
	}

	public void removeAllWishes(final Writer writer) throws IOException {
		final NanoClock s1 = new NanoClock().start();
		final Iterator<XId> it = this.model.iterator();
		final LinkedList<XId> list = new LinkedList<XId>();
		while (it.hasNext()) {
			final XId xid = it.next();
			list.add(xid);
		}
		for (final XId xid : list) {
			this.model.removeObject(xid);
		}

		s1.stop("removeAllWishes");
		writer.write(s1.getStats());
	}

	public Wish getWish(final XId wishId) {
		if (this.model.hasObject(wishId)) {
			return new Wish(this.model.getObject(wishId));
		} else {
			return null;
		}
	}

	/**
	 * @return wish list as HTML
	 */
	public String toHtml() {
		final NanoClock s1 = new NanoClock().start();
		final StringBuffer buf = new StringBuffer();
		buf.append("<b>Wishlist " + this.model.getId() + "</b>");
		buf.append("<ol>\n");
		for (final XId wishId : this.model) {
			final Wish wish = getWish(wishId);
			buf.append("  <li>");
			buf.append(wish.toHtml());
			buf.append("</li>\n");
		}
		buf.append("</ol>\n");
		s1.stop("read-wishes-and-properties");
		buf.append(SharedHtmlUtils.link("/xmas/" + this.model.getAddress().getRepository() + "/"
				+ this.model.getId() + "/clear", "Delete all wishes")
				+ "<br />\n");
		// add stats
		buf.append(s1.getStats() + "<br />\n");
		return buf.toString();
	}

	/**
	 * @param wishesCount
	 *            how many wished to create and add to this list
	 * @param writer
	 *            to which some performance data is written, if not null. May be
	 *            null.
	 * @return a list of all IDs that have just been created
	 * @throws IOException
	 *             if the writer has them
	 */
	public List<XId> addDemoData(final int wishesCount, final Writer writer) throws IOException {
		final NanoClock s1 = new NanoClock();
		s1.start();
		final List<XId> wishIds = new LinkedList<XId>();
		for (int w = 1; w <= wishesCount; w++) {
			writer.write("Creating wish " + w + " in list " + this.model.getId() + " at "
					+ System.currentTimeMillis() + "<br />\n");
			final String name = NameUtils.getProductName();
			final String nameInUrl = name.replace(" ", "+");
			final XId wishId = addWish("" + name, (int) Math.round(100 * Math.random()),
					"http://www.google.de/images?q=" + nameInUrl);
			wishIds.add(wishId);
		}
		s1.stop("created-" + wishesCount + "-wishes");
		if (writer != null) {
			writer.write(s1.getStats() + "<br />\n");
		}
		return wishIds;
	}

	@Override
	public Iterator<XId> iterator() {
		return this.model.iterator();
	}

}
