package org.xydra.restless.example;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;


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
	 */
	public static void restless() {
		// add dynamic
		Restless.addGet("/foo", new ExampleResource(), "getName",

		new RestlessParameter("name", null),

		new RestlessParameter("age", "23")

		);
		
		// add static
		Restless.addGenericStatic("/bar", "GET", ExampleResource.class, "getName",

		false,

		new RestlessParameter("name", null), new RestlessParameter("age", "23")

		);
	}
	
	private String name;
	private String age;
	
	public void getName(HttpServletRequest req, String name, String age, HttpServletResponse res)
	        throws IOException {
		// process
		this.name = name;
		this.age = age;
		
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/html");
		
		/* usually use a template engine or XML generating library here */
		res
		        .getWriter()
		        .println(
		                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
		                        + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
		res.getWriter().println("<head><title>A valid restless response</title></head>");
		res.getWriter().println(
		        "<body><p>name = " + this.name + ", age = " + this.age + "</p></body>");
		res.getWriter().println("</html>");
	}
	
}
