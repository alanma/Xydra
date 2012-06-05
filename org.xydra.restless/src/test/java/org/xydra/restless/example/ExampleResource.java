package org.xydra.restless.example;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.NanoClock;


/**
 * Has a parameterless constructor to be called by Restless if added in a static
 * way.
 * 
 * @author voelkel
 * 
 */
public class ExampleResource {
	
	/**
	 * Naming this method 'restless()' is just a convention
	 * 
	 * @param restless @NeverNull
	 */
	public static void restless(Restless restless) {
		ExampleResource exampleResource = new ExampleResource();
		/** Optional: Initialization and configuration of exampleResource */
		// add dynamic
		restless.addMethod("/foo", "GET", exampleResource, "getName", false,
		
		new RestlessParameter("name", null),
		
		new RestlessParameter("age", "23")
		
		);
		
		// add static, slightly faster startup times for large apps
		restless.addMethod("/bar", "GET", ExampleResource.class, "getName", false,
		
		new RestlessParameter("name", null),
		
		new RestlessParameter("age", "23")
		
		);
		
		// add static, slightly faster startup times for large apps
		restless.addMethod("/silly", "GET", ExampleResource.class, "silly", false);
	}
	
	private String name;
	private String age;
	
	/**
	 * This method is exposed by linking 'getName' in
	 * {@link #restless(Restless)}. It can have String parameters, in order of
	 * appearance of the {@link RestlessParameter}.
	 * 
	 * Additionally the parameter types {@link HttpServletResponse},
	 * {@link HttpServletRequest}, {@link IRestlessContext} and
	 * {@link NanoClock} are available.
	 * 
	 * @param req
	 * @param name
	 * @param age
	 * @param res
	 * @param requestClock
	 * @throws IOException
	 */
	public void getName(HttpServletRequest req, String name, String age, HttpServletResponse res,
	        NanoClock requestClock) throws IOException {
		// process
		this.name = name;
		this.age = age;
		
		res.setCharacterEncoding(Restless.CONTENT_TYPE_CHARSET_UTF8);
		res.setContentType("text/html");
		
		requestClock.stopAndStart("headers");
		
		/* usually use a template engine or XML generating library here */
		Writer w = res.getWriter();
		w.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
		        + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
		w.write("<head><title>A valid restless response</title></head>");
		w.write("<body><p>name = " + this.name + ", age = " + this.age + "</p></body>");
		w.write("</html>");
		
		requestClock.stopAndStart("content");
		
	}
	
	public String silly(IRestlessContext restlessContext) {
		Enumeration<?> names = restlessContext.getRestless().getInitParameterNames();
		List<String> list = new LinkedList<String>();
		while(names.hasMoreElements()) {
			String name = (String)names.nextElement();
			list.add(name);
		}
		return "As an example, these are the init parameter names: " + list;
	}
	
}
