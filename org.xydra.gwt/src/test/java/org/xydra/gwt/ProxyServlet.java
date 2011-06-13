package org.xydra.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A simple proxy servlet that forwards all requests to a different server.
 * 
 * @author dscharrer
 * 
 */
public class ProxyServlet extends HttpServlet {
	
	private final URI remoteService;
	
	public ProxyServlet() {
		try {
			this.remoteService = new URI("http://localhost:8080/xydra/");
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final long serialVersionUID = -5107058075707773775L;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		
		HttpURLConnection con;
		
		int code;
		
		try {
			
			String path = req.getPathInfo();
			String query = req.getQueryString();
			if(query != null)
				path += "?" + query;
			
			System.out.println(req.getMethod() + " " + path);
			
			while(path.length() > 0 && path.charAt(0) == '/')
				path = path.substring(1);
			
			URL url = this.remoteService.resolve(path).toURL();
			
			System.out.println("connecting to " + url);
			
			con = (HttpURLConnection)url.openConnection();
			
			String method = req.getMethod();
			con.setRequestMethod(method);
			
			Enumeration<String> hn = req.getHeaderNames();
			while(hn.hasMoreElements()) {
				String name = hn.nextElement();
				String value = req.getHeader(name);
				if(name.equalsIgnoreCase("Host"))
					continue;
				if(name.equalsIgnoreCase("Connection"))
					continue;
				if(name.equalsIgnoreCase("Keep-Alive"))
					continue;
				System.out.println("req: " + name + ": " + value);
				con.setRequestProperty(name, value);
			}
			
			if(!req.getMethod().equalsIgnoreCase("GET") && req.getContentLength() != 0) {
				con.setDoOutput(true);
				System.out.println("request body:");
				copy(req.getInputStream(), con.getOutputStream());
				System.out.println();
			}
			
			code = con.getResponseCode();
			
		} catch(IOException e) {
			System.out.println("cannot connect to server");
			resp.setStatus(503);
			try {
				resp.getWriter().write("cannot connect to xydra server: " + e.getMessage());
			} catch(IOException e1) {
				throw new RuntimeException(e1);
			}
			return;
		}
		System.out.println("response code: " + code);
		resp.setStatus(code);
		
		Map<String,List<String>> headers = con.getHeaderFields();
		for(Map.Entry<String,List<String>> header : headers.entrySet()) {
			String name = header.getKey();
			if(name == null)
				continue;
			for(String value : header.getValue()) {
				System.out.println("resp: " + name + ": " + value);
				resp.addHeader(name, value);
			}
		}
		
		if(con.getContentLength() != 0)
			try {
				copy(con.getInputStream(), resp.getOutputStream());
				con.disconnect();
			} catch(IOException e) {
				InputStream err = con.getErrorStream();
				if(err != null)
					try {
						copy(err, resp.getOutputStream());
					} catch(IOException e1) {
						throw new RuntimeException(e1);
					}
			}
		else {
			con.setDoInput(false);
			con.disconnect();
		}
		
	}
	
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	}
	
}
