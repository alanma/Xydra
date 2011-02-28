package org.xydra.fbsdk4gwt.client;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;


/**
 * Topmenu links
 * 
 * @author ola
 */
public class TopMenuLinksPanel extends Composite {
	
	private DockPanel links = new DockPanel();
	private HorizontalPanel leftSide = new HorizontalPanel();
	
	Anchor sourceCodeLink = new Anchor("Source");
	
	public TopMenuLinksPanel() {
		
		this.links.getElement().setId("TopMenuLinks");
		
		this.sourceCodeLink
		        .setHref("http://code.google.com/p/gwtfb/source/browse/#svn/trunk/GwtFB/src/org.xydra.fbsdk4gwt/sdk");
		this.sourceCodeLink.setTarget("blank");
		
		this.leftSide.add(new Hyperlink("Home", "home"));
		this.leftSide.add(new Hyperlink("Documentation", "wave"));
		this.leftSide.add(this.sourceCodeLink);
		
		this.links.add(this.leftSide, DockPanel.WEST);
		
		initWidget(this.links);
	}
}
