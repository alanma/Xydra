package org.xydra.gae.datalogger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.csv.HtmlTool;
import org.xydra.csv.impl.memory.CsvTable;
import org.xydra.csv.impl.memory.Row;
import org.xydra.gae.AboutAppEngine;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.HtmlUtils;
import org.xydra.restless.utils.ServletUtils;
import org.xydra.restless.utils.SharedHtmlUtils;
import org.xydra.restless.utils.SharedHtmlUtils.METHOD;
import org.xydra.sharedutils.URLUtils;
import org.xydra.store.impl.gae.GaeTestfixer;


/**
 * Log and display arbitrary key-value data. See {@link DataLogger} for details.
 * 
 * @author xamde
 */
public class DataloggerResource {
	
	private static final Logger log = LoggerFactory.getLogger(DataloggerResource.class);
	
	public static void restless(Restless restless, String prefix) {
		restless.addMethod("/datalog", "GET", DataloggerResource.class, "index", true,
		
		new RestlessParameter("start", null), new RestlessParameter("end", null),
		        new RestlessParameter("filterKey", null),
		        new RestlessParameter("filterValue", null),
		        new RestlessParameter("format", "html"), new RestlessParameter("delete", "false"));
		
		/** You can log via both methods */
		restless.addMethod("/datalog/log", "GET", DataloggerResource.class, "record", true);
		restless.addMethod("/datalog/log", "POST", DataloggerResource.class, "record", true);
		
		/** Listen to all log messages that contain "DATA?" in it */
		LoggerFactory.addLogListener(new DatalogLogListener());
	}
	
	/**
	 * @param startStr
	 * @param endStr
	 * @param filterKey return only record where this key...
	 * @param filterValue ... matches this value
	 * @param format "html" or "csv"
	 * @param deleteStr
	 * @param req
	 * @param res
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void index(String startStr, String endStr, String filterKey, String filterValue,
	        String format, String deleteStr, HttpServletRequest req, HttpServletResponse res)
	        throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Writer w;
		long start = -1;
		long end = -1;
		if(startStr != null && endStr != null) {
			start = Long.parseLong(startStr);
			end = Long.parseLong(endStr);
		}
		boolean delete = deleteStr != null && deleteStr.equals("true");
		if(start == -1) {
			start = System.currentTimeMillis() - (60 * 60 * 1000);
			delete = false;
		}
		if(end == -1) {
			end = System.currentTimeMillis();
			delete = false;
		}
		
		if(format.equals("csv")) {
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String name = AboutAppEngine.getApplicationId() + "-" + sdf.format(now) + ".csv";
			log.info("Wrapping in file named " + name);
			// Send the correct response headers.
			res.setContentType("text/csv");
			res.addHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
			ServletOutputStream os = res.getOutputStream();
			w = new OutputStreamWriter(os, "utf-8");
		} else {
			ServletUtils.headers(res, "text/html");
			w = HtmlUtils.startHtmlPage(res, "Datalogger", new SharedHtmlUtils.HeadLinkStyle(
			        "/s/cpodebug.css"));
			w.write("<div class='xydebug'>");
			
			w.write("Log your data: " + SharedHtmlUtils.link("/admin/datalog/log")
			        + "?key1=val1&key2=val2&...<br/>");
			
			w.write("Browse logs "
			        + HtmlUtils.link("/admin/datalog", "from last hour")
			        + " (or enter any other time scale) "
			        + SharedHtmlUtils.form(METHOD.GET, "/admin/datalog/")
			                .withInputText("start", "" + start).withInputText("end", "" + end)
			                .withInputText("filterKey", "").withInputText("filterValue", "")
			                .withInputText("delete", "false").withInputSubmit("Go") + "<br/>");
			w.write(HtmlUtils.link("/admin/datalog?start=" + start + "&end=" + end + "&filterKey="
			        + filterKey + "&filterValue=" + filterValue + "&format=csv", "Download as CSV")
			        + "<br/>");
		}
		
		if(start > -1 && end > -1) {
			Pair<String,String> filter = null;
			if(filterKey != null && !filterKey.equals("")) {
				filter = new Pair<String,String>(URLUtils.decode(filterKey),
				        URLUtils.decode(filterValue));
			}
			
			if(format.equals("html")) {
				w.write("Query for logs [" + start + "," + end + "] with filter " + filter);
				if(delete) {
					w.write("Deleting...");
					DataLogger.deleteRecords(start, end, filter);
				}
				w.flush();
			}
			// for both formats
			if(!delete) {
				writeRecords(w, start, end, filter, format);
			}
		}
		
		if(format.equals("html")) {
			w.write("Done.");
			w.write("</div>");
			HtmlUtils.endHtmlPage(w);
		} else {
			w.flush();
			w.close();
		}
	}
	
	private static void writeRecords(Writer w, long start, long end, Pair<String,String> filter,
	        String format) throws IOException {
		@SuppressWarnings("unchecked")
		Iterator<DataRecord> it = DataLogger.getRecords(start, end, filter);
		CsvTable table = new CsvTable();
		while(it.hasNext()) {
			DataRecord dataRecord = it.next();
			String key = dataRecord.getKey();
			assert key != null;
			Row row = table.getOrCreateRow(key, true);
			row.setValue("created", dataRecord.getCreationDate(), true);
			for(Map.Entry<String,String> e : dataRecord.getMap().entrySet()) {
				row.setValue(e.getKey(), e.getValue(), true);
			}
		}
		if(format.equals("html")) {
			HtmlTool.writeToHtml(table, "i_addr", w);
		} else {
			table.writeTo(w);
		}
	}
	
	public static void record(HttpServletRequest req, HttpServletResponse res) throws IOException {
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		Map<String,String> map = new HashMap<String,String>();
		Enumeration<?> names = req.getParameterNames();
		while(names.hasMoreElements()) {
			String name = (String)names.nextElement();
			map.put(name, URLUtils.decode(req.getParameter(name)));
		}
		DataLogger.log(map);
		
		ServletUtils.headers(res, "text/html");
		Writer w = HtmlUtils.startHtmlPage(res, "Datalogger");
		
		w.write("Data logged. Go to " + SharedHtmlUtils.link("/admin/datalog/", "Datalog"));
		
		HtmlUtils.endHtmlPage(w);
	}
}
