package org.xydra.testgae.client;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;


@RunsInGWT(true)
@RunsInAppEngine(false)
public class TestGaeClient implements EntryPoint {
	
	@Override
	public void onModuleLoad() {
		RootPanel.getBodyElement().setInnerText("HelloWorld");
		
	}
	
}
