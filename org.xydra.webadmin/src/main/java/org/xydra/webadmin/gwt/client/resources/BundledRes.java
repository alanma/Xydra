package org.xydra.webadmin.gwt.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * 
 * @author alpha
 * 
 */
public interface BundledRes extends ClientBundle {
	/**
	 * The message file used for i18n
	 * 
	 */

	public interface Images extends ClientBundle {

		@Source("img/table_edit.png")
		ImageResource edit();

		@Source("img/delete.png")
		ImageResource delete();

		@Source("img/table_save.png")
		ImageResource save();

		@Source("img/cancel.png")
		ImageResource cancel();

		@Source("img/table_add.png")
		ImageResource add();

		@Source("img/text_columns.png")
		ImageResource list();

		@Source("img/spinner.gif")
		ImageResource spinner();

	}

	public static final BundledRes INSTANCE = GWT.create(BundledRes.class);

	public Images images();

}
