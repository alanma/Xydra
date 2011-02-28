package org.xydra.fbsdk4gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FrontpageViewController extends Composite {
	
	private VerticalPanel outer = new VerticalPanel ();
	
	public FrontpageViewController () {

		this.outer.getElement().setId ( "FrontpageViewController" );
		this.outer.setSpacing(10);
		this.outer.add ( new HTML ( "This demo uses Facebook Connect. Please click to login " ) );
		this.outer.add ( new HTML ( "<fb:login-button autologoutlink='true' perms='publish_stream,read_stream' /> " ) );
		this.outer.add ( new HTML ( "<hr/><fb:comments xid='gwtfb' />" ) );
		initWidget ( this.outer );
	}

}
