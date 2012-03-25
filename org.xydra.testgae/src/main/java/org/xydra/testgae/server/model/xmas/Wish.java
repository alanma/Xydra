package org.xydra.testgae.server.model.xmas;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.HtmlUtils;


/**
 * A single wish with a title, price and URL for more information.
 * 
 * @author xamde
 */
public class Wish {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Wish.class);
	
	private XWritableObject xo;
	
	public static final XID TITLE = XX.toId("title");
	public static final XID PRICE = XX.toId("price");
	public static final XID URL = XX.toId("url");
	
	/**
	 * Create a new wish
	 * 
	 * @param xo where new state data is persisted
	 * @param title of the wish
	 * @param price in euros
	 * @param url product information
	 */
	public Wish(XWritableObject xo, String title, int price, String url) {
		this(xo);
		xo.createField(TITLE).setValue(XV.toValue(title));
		xo.createField(PRICE).setValue(X.getValueFactory().createIntegerValue(price));
		xo.createField(URL).setValue(XV.toValue(url));
		assert getTitle().equals(title);
		assert getPrice() == price;
		assert getUrl().equals(url);
	}
	
	/**
	 * Internal constructor for loading an already initialised
	 * {@link XWritableObject}.
	 * 
	 * @param xo where to read state information from
	 */
	public Wish(XWritableObject xo) {
		this.xo = xo;
	}
	
	public String toHtml() {
		return "<b>"
		        + HtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
		                + this.xo.getAddress().getModel() + "/" + this.xo.getId(), getTitle())
		        + "</b> "
		        +

		        "("
		        + getPrice()
		        + " EUR) "
		        
		        + HtmlUtils.link(getUrl(), getUrl())
		        + " "
		        
		        + HtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
		                + this.xo.getAddress().getModel() + "/" + this.xo.getId() + "/delete",
		                "[Delete this wish]")
		        
		        // TODO implement edit functionalities
		        + HtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
		                + this.xo.getAddress().getModel() + "/" + this.xo.getId()
		                + "/editName?name=test", "[Edit name]")
		        + HtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
		                + this.xo.getAddress().getModel() + "/" + this.xo.getId()
		                + "/editPrice?price=1", "[Edit price]")
		        + HtmlUtils.link("/xmas/" + this.xo.getAddress().getRepository() + "/"
		                + this.xo.getAddress().getModel() + "/" + this.xo.getId()
		                + "/editUrl?url=www.test.test", "[Edit URL]")

		        + "<br />\n";
	}
	
	public String getTitle() {
		XWritableField field = this.xo.getField(TITLE);
		if(field == null)
			return "";
		return ((XStringValue)field.getValue()).contents();
	}
	
	public String getUrl() {
		XWritableField field = this.xo.getField(URL);
		if(field == null)
			return "";
		return ((XStringValue)field.getValue()).contents();
	}
	
	public int getPrice() {
		XWritableField field = this.xo.getField(PRICE);
		if(field == null)
			return -1;
		return ((XIntegerValue)field.getValue()).contents();
	}
	
	public void delete() {
		XID modelId = this.xo.getAddress().getModel();
		XID repoId = this.xo.getAddress().getRepository();
		XWritableModel model = Xmas.getRepository(repoId.toString()).createModel(modelId);
		WishList wishList = new WishList(model);
		wishList.removeWish(this.xo.getId());
	}
}
