package org.xydra.testgae.server.model.xmas;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.utils.SharedHtmlUtils;

/**
 * A single wish with a title, price and URL for more information.
 *
 * @author xamde
 */
public class Wish {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Wish.class);

	private final XWritableObject xo;

	public static final XId TITLE = XX.toId("title");
	public static final XId PRICE = XX.toId("price");
	public static final XId URL = XX.toId("url");

	/**
	 * Create a new wish
	 *
	 * @param xo
	 *            where new state data is persisted
	 * @param title
	 *            of the wish
	 * @param price
	 *            in euros
	 * @param url
	 *            product information
	 */
	public Wish(final XWritableObject xo, final String title, final int price, final String url) {
		this(xo);
		xo.createField(TITLE).setValue(XV.toValue(title));
		xo.createField(PRICE).setValue(BaseRuntime.getValueFactory().createIntegerValue(price));
		xo.createField(URL).setValue(XV.toValue(url));
		assert getTitle().equals(title);
		assert getPrice() == price;
		assert getUrl().equals(url);
	}

	/**
	 * Internal constructor for loading an already initialised
	 * {@link XWritableObject}.
	 *
	 * @param xo
	 *            where to read state information from
	 */
	public Wish(final XWritableObject xo) {
		this.xo = xo;
	}

	public String toHtml() {
		return "<b>"
				+ SharedHtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
						+ this.xo.getAddress().getModel() + "/" + this.xo.getId(), getTitle())
				+ "</b> "
				+

				"("
				+ getPrice()
				+ " EUR) "

				+ SharedHtmlUtils.link(getUrl(), getUrl())
				+ " "

				+ SharedHtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
						+ this.xo.getAddress().getModel() + "/" + this.xo.getId() + "/delete",
						"[Delete this wish]")

				// TODO implement edit functionalities
				+ SharedHtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
						+ this.xo.getAddress().getModel() + "/" + this.xo.getId()
						+ "/editName?name=test", "[Edit name]")
				+ SharedHtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
						+ this.xo.getAddress().getModel() + "/" + this.xo.getId()
						+ "/editPrice?price=1", "[Edit price]")
				+ SharedHtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
						+ this.xo.getAddress().getModel() + "/" + this.xo.getId()
						+ "/editUrl?url=www.test.test", "[Edit URL]")

				+ "<br />\n";
	}

	public String getTitle() {
		final XWritableField field = this.xo.getField(TITLE);
		if (field == null) {
			return "";
		}
		return ((XStringValue) field.getValue()).contents();
	}

	public String getUrl() {
		final XWritableField field = this.xo.getField(URL);
		if (field == null) {
			return "";
		}
		return ((XStringValue) field.getValue()).contents();
	}

	public int getPrice() {
		final XWritableField field = this.xo.getField(PRICE);
		if (field == null) {
			return -1;
		}
		return ((XIntegerValue) field.getValue()).contents();
	}

	public void delete() {
		final XId modelId = this.xo.getAddress().getModel();
		final XId repoId = this.xo.getAddress().getRepository();
		final XWritableModel model = Xmas.getRepository(repoId.toString()).createModel(modelId);
		final WishList wishList = new WishList(model);
		wishList.removeWish(this.xo.getId());
	}
}
