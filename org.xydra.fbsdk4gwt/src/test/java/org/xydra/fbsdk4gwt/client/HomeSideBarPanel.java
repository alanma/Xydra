package org.xydra.fbsdk4gwt.client;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Displays links on the left siden on the HomeView page
 * @author ola
 */
public class HomeSideBarPanel extends Composite {
    
    VerticalPanel linkPanel = new VerticalPanel ();
    
    public HomeSideBarPanel () {
        this.linkPanel.getElement().setId("SideBarPanel");
        this.linkPanel.add ( new HTML ( "<h3>Methods</h3>" ) );
        this.linkPanel.add( new Hyperlink ( "Stream Publish", "example/stream.publish" ) );
        this.linkPanel.add ( new Hyperlink ( "Friends", "example/friends" ) );
        initWidget ( this.linkPanel );
    }

}
