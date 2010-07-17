package org.xydra.restless;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A minimalistic servlet to help using servlets.
 * 
 * Usage:
 * 
 * 1) in web.xml add
 * 
 * <pre>
 * &lt;servlet&gt;
 *  &lt;servlet-name&gt;restless&lt;/servlet-name&gt;
 *  &lt;servlet-class&gt;org.xydra.restless.Restless&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *    &lt;param-name&gt;app&lt;/param-name&gt;
 *    &lt;param-value&gt;com.example.Application&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *  &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *  &lt;servlet-name&gt;restless&lt;/servlet-name&gt;
 *  &lt;url-pattern&gt;/mypath/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * 2) create a corresponding class com.example.Application
 * 
 * It should look like this:
 * 
 * <pre>
 * public class Application {
 * 	
 * 	static {
 * 		new Foo().restless();
 * 		new Bar().restless();
 * 		// ... add more
 * 	}
 * 	
 * }
 * </pre>
 * 
 * 3) In your domain classes (Foo, Bar) add the mapping:
 * 
 * <pre>
 * public void restless() {
 * 	Restless.addGet(
 * 
 * 	&quot;/mysubpath/&quot;, //or any path you like
 * 	        
 * 	        this, &quot;getBaz&quot;, // the method name to call
 * 	        
 * 	        new RestlessParameter(&quot;source&quot;, // the query param name
 * 	                
 * 	                null // the default value of the param
 * 	        )
 * 
 * 	// or further RestlessParameter
 * 	        
 * 	        );
 * }
 * </pre>
 * 
 * The method "getBaz" needs to have at least one parameter of type
 * {@link HttpServletResponse}. All other parameters are filled with the values
 * from the request, using the mapping you provide.
 * 
 * 
 * @author voelkel
 * 
 */
public class Restless extends HttpServlet {
	
	private static Logger log = LoggerFactory.getLogger(Restless.class);
	
	private static final long serialVersionUID = 1670927362109153417L;
	
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
	public static final String MIME_XHTML = "application/xhtml+xml";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String CHARSET_UTF8 = "utf-8";
	public static final String XHTML_NS = "xmlns=\"http://www.w3.org/1999/xhtml\"";
	
	/**
	 * All publicly exposed methods
	 */
	private static List<RestlessMethod> methods = new LinkedList<RestlessMethod>();
	
	/*
	 * Called from the servlet environment if
	 * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; is in web.xml set for
	 * the Restless servlet.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {
		log.info("Restless init...");
		ServletConfig servletConfig = this.getServletConfig();
		String app = servletConfig.getInitParameter("app");
		try {
			Class<?> clazz = Class.forName(app);
			// trigger it to make sure static blocks are run
			log.info("Configured with " + clazz.getName());
		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Class '" + app + "' not found");
		}
		log.info("Done Restless init.");
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		if(req.getRequestURI().contains("admin/restless")) {
			doIntrospection(req, res);
		} else {
			restlessService(req, res);
		}
	}
	
	/**
	 * @param raw
	 * @return the input string with XML escaping
	 */
	private static final String xmlEncode(String raw) {
		String safe = raw;
		safe = safe.replace("&", "&amp;");
		safe = safe.replace("<", "&lt;");
		safe = safe.replace(">", "&gt;");
		safe = safe.replace("'", "&apos;");
		safe = safe.replace("\"", "&quto;");
		return safe;
	}
	
	/**
	 * Print the current mapping from URL patterns to Java methods as a web
	 * page.
	 * 
	 * @param req
	 * @param res
	 */
	private void doIntrospection(HttpServletRequest req, HttpServletResponse res) {
		res.setContentType(MIME_XHTML);
		res.setCharacterEncoding(CHARSET_UTF8);
		try {
			res.getWriter().println(XML_DECLARATION);
			res.getWriter().println(XHTML_DOCTYPE);
			res.getWriter().println(
			        "<html " + XHTML_NS
			                + "><head><title>Restless Configuration</title></head><body>");
			res.getWriter().println("<h3>Restless configuration</h3>");
			res.getWriter().println("<p><ol>");
			for(RestlessMethod rm : Restless.methods) {
				res.getWriter().print("<li>");
				res.getWriter().print(
				        "Mapping " + rm.httpMethod + " " + xmlEncode(rm.pathTemplate.getRegex())
				                + " --&gt; ");
				res.getWriter().print(rm.instance.getClass().getName() + "#" + rm.methodName);
				res.getWriter().println("</li>");
			}
			res.getWriter().println("</ol></p>");
			res.getWriter().println("</body></html>");
			res.getWriter().flush();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		res.setStatus(200);
	}
	
	/**
	 * @param req
	 * @return the local path of the resource
	 */
	private static String getRequestPath(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		String path = req.getRequestURI();
		path = path.substring(servletPath.length(), path.length());
		return path;
	}
	
	/**
	 * Generic method to map incoming web requests to mapped
	 * {@link RestlessMethod}. Match path and HTTP method.
	 * 
	 * @param req
	 * @param res
	 */
	protected void restlessService(HttpServletRequest req, HttpServletResponse res) {
		// find class mapped to path
		String path = getRequestPath(req);
		boolean foundMethod = false;
		
		for(RestlessMethod restlessMethod : methods) {
			if(restlessMethod.pathTemplate.matches(path)) {
				// run method
				if(req.getMethod().equalsIgnoreCase(restlessMethod.httpMethod)) {
					try {
						restlessMethod.run(req, res);
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
					foundMethod = true;
				}
			}
		}
		if(!foundMethod) {
			try {
				res.sendError(404, "No handler matched your " + req.getMethod() + "-request path '"
				        + path + "'");
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/*
	 * Called from servlet environment.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest
	 * , javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse res) {
		restlessService(req, res);
	}
	
	/**
	 * Add a method to be called if an HTTP GET matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addGet(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "GET", object, methodName, parameter);
	}
	
	/**
	 * Add a method to be called if an HTTP PUT matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addPut(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "PUT", object, methodName, parameter);
	}
	
	/**
	 * Add a method to be called if an HTTP POST matches the given pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addPost(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "POST", object, methodName, parameter);
	}
	
	/**
	 * Add a method to be called if an HTTP DELETE matches the given
	 * pathPattern.
	 * 
	 * See
	 * {@link Restless#addGeneric(String, String, Object, String, RestlessParameter...)}
	 * for details.
	 */
	public static void addDelete(String pathPattern, Object object, String methodName,
	        RestlessParameter ... parameter) {
		addGeneric(pathPattern, "DELETE", object, methodName, parameter);
	}
	
	/**
	 * If the method must have a parameter of type {@link HttpServletResponse},
	 * Restless hands over the response object and ignores the method response.
	 * 
	 * @param pathPrefix starts always with /
	 * @param methodName
	 * @param restlessParameter
	 * 
	 * @param pathTemplate see {@link PathTemplate} for syntax
	 * @param httpMethod one of 'GET', 'PUT', 'POST', or 'DELETE'
	 * @param object Java instance to be called
	 * @param methodName to be called on the Java instance. This method may not
	 *            have several signatures.
	 * @param parameter in the order in which they are used in the Java method.
	 *            The Java method may additionally use
	 *            {@link HttpServletRequest} and {@link HttpServletResponse} at
	 *            any position in the Java method. {@link HttpServletResponse}
	 *            should be used to send a response.
	 */
	public static void addGeneric(String pathTemplate, String httpMethod, Object object,
	        String methodName, RestlessParameter ... parameter) {
		PathTemplate pt = new PathTemplate(pathTemplate);
		methods.add(new RestlessMethod(object, httpMethod, methodName, pt, parameter));
		assert methodByName(object, methodName) != null : "method '" + methodName + "' not found";
	}
	
	/**
	 * @param object
	 * @param methodName
	 * @return a java.lang.reflect.{@link Method} from a String
	 */
	private static Method methodByName(Object object, String methodName) {
		for(Method method : object.getClass().getMethods()) {
			if(method.getName().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
	
	/**
	 * @param object
	 * @param methodName
	 * @return true if the Java method with the given name has a parameter of
	 *         type {@link HttpServletResponse}
	 */
	@SuppressWarnings("unused")
	private static boolean hasHttpServletResponseParameter(Object object, String methodName) {
		// probe for consistency
		Method method = methodByName(object, methodName);
		if(method == null) {
			return false;
		}
		
		for(Class<?> paramType : method.getParameterTypes()) {
			if(paramType.equals(HttpServletResponse.class)) {
				// ok
				return true;
			}
		}
		return false;
	}
	
	/**
	 * A HTTP method (GET, PUT,.... ) and a {@link PathTemplate} mapped to an
	 * object and a method name.
	 * 
	 * @author voelkel
	 */
	private static class RestlessMethod {
		
		/**
		 * GET, PUT, POST, DELETE
		 */
		private String httpMethod;
		private String methodName;
		private RestlessParameter[] parameter;
		private Object instance;
		private PathTemplate pathTemplate;
		
		/**
		 * @param object instance to be called when web method is used
		 * @param httpMethod
		 * @param methodName instance method to be called. This method may not
		 *            have several signatures with the same name.
		 * 
		 *            The method signature can use {@link HttpServletRequest}
		 *            and {@link HttpServletResponse} in any position. It is set
		 *            with the corresponding instances from the servlet
		 *            environment.
		 * 
		 *            If no {@link HttpServletResponse} is used, a default
		 *            content type of text/plain is used an the method return
		 *            type is expected to be of type String. This facility is
		 *            designer to return status information at development time.
		 * @param pathTemplate
		 * @param parameter in order of variables in 'method'. See
		 *            {@link RestlessParameter}.
		 */
		public RestlessMethod(Object object, String httpMethod, String methodName,
		        PathTemplate pathTemplate, RestlessParameter[] parameter) {
			this.instance = object;
			this.httpMethod = httpMethod;
			this.methodName = methodName;
			this.pathTemplate = pathTemplate;
			this.parameter = parameter;
		}
		
		/**
		 * Executes the method on the mapped instance
		 * 
		 * @param req
		 * @param res
		 * @throws IOException
		 */
		public void run(HttpServletRequest req, HttpServletResponse res) throws IOException {
			Method method = methodByName(this.instance, this.methodName);
			if(method == null) {
				res.sendError(500, "Malconfigured server. Method '" + this.methodName
				        + "' not found in '" + this.instance.getClass().getName() + "'");
			} else {
				// try to call it
				List<Object> args = new ArrayList<Object>();
				// build up parameters
				
				// extract values from path
				String path = getRequestPath(req);
				List<String> variablesFromPath = this.pathTemplate.extractVariables(path);
				
				int i = 0;
				boolean hasHttpServletResponseParameter = false;
				for(Class<?> paramType : method.getParameterTypes()) {
					// HttpServletResponse
					if(paramType.equals(HttpServletResponse.class)) {
						args.add(res);
						hasHttpServletResponseParameter = true;
					} else if(paramType.equals(HttpServletRequest.class)) {
						args.add(req);
					} else {
						RestlessParameter param = this.parameter[i];
						// try to get from request
						String[] values = req.getParameterValues(param.name);
						if(values == null) {
							// look in path template
							int pos = this.pathTemplate.variableNames.indexOf(param.name);
							if(pos >= 0) {
								String value = variablesFromPath.get(pos);
								args.add(value);
							} else {
								// using default
								args.add(param.defaultValue);
							}
						} else {
							if(values.length > 1) {
								log.warn("Multiple values for parameter '" + param.name
								        + "', using default");
								args.add(param.defaultValue);
							} else {
								args.add(values[0]);
							}
						}
						i++;
					}
				}
				
				try {
					Object result = method.invoke(this.instance, args.toArray(new Object[0]));
					if(!hasHttpServletResponseParameter) {
						res.setContentType(MIME_TEXT_PLAIN + "; charset=" + CHARSET_UTF8);
						res.setStatus(200);
						// we need to send back something standard ourselves
						res.getWriter().print(
						        "Executed " + this.instance.getClass().getSimpleName() + "."
						                + this.methodName + "\n");
						if(result != null && result instanceof String) {
							res.getWriter().print("Result: " + result);
						}
						res.getWriter().flush();
					}
				} catch(RestlessException re) {
					res.setStatus(re.getStatusCode());
					res.setContentType(MIME_TEXT_PLAIN + "; charset=" + CHARSET_UTF8);
					res.getWriter().print(re.getMessage());
				} catch(IllegalArgumentException e) {
					res.sendError(500, e.toString());
					log.error("", e);
				} catch(IllegalAccessException e) {
					res.sendError(500, e.toString());
					log.error("", e);
				} catch(InvocationTargetException e) {
					res.sendError(500, e.toString());
					log.error("", e);
				}
				
			}
		}
	}
	
}
