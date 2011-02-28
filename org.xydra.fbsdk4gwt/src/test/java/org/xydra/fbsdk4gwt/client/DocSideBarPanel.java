package org.xydra.fbsdk4gwt.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DocSideBarPanel extends Composite {

    private VerticalPanel outer = new VerticalPanel ();
    
    
    public DocSideBarPanel () {
        this.outer.getElement().setId( "SideBarPanel");
        this.outer.add ( new HTML ( "This is a public wave to let you ask question and read docs. As long as wave lives we'll keep it here " ) );
        initWidget(this.outer);
    }
}
