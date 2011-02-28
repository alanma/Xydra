package org.xydra.fbsdk4gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Display Top Menu
 * @author ola
 */
public class TopMenuPanel extends Composite {

	private HorizontalPanel outer = new HorizontalPanel ();
	
	public TopMenuPanel () {
	    AppImageBundle images = GWT.create( AppImageBundle.class);
	    
		this.outer.getElement().setId("TopMenu");
		this.outer.add ( new Image ( images.logo() ) );
        this.outer.add ( new HTML ( "<div style='margin-top: 2px; float: right;'><fb:login-button autologoutlink='true' perms='publish_stream,read_stream' /> </div>" ) );
        
        initWidget ( this.outer );
	}
	
	
}
