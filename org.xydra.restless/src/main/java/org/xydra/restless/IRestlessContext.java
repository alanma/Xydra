package org.xydra.restless;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public interface IRestlessContext {
	
	Restless getRestless();
	
	HttpServletRequest getRequest();
	
	HttpServletResponse getResponse();
	
}
