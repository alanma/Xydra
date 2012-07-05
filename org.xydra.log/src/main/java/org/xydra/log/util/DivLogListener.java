package org.xydra.log.util;

import org.xydra.log.ILogListener;
import org.xydra.log.Logger;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;


public class DivLogListener implements ILogListener {
	
	public DivLogListener(FlowPanel hostPanel) {
		this.hostPanel = hostPanel;
	}
	
	private FlowPanel hostPanel;
	
	@Override
	public void trace(Logger log, String msg) {
		log("TRACE", log, msg);
	}
	
	private void log(String level, Logger log, String msg, Throwable ... t) {
		if(level.equalsIgnoreCase("warn") || level.equalsIgnoreCase("error")) {
			append("<span style='color:red;'>" + level + "</span> ");
		}
		boolean dataAccess = msg.startsWith("|");
		if(dataAccess) {
			append("<span style='color:blue';>");
		}
		append("<b>");
		append(msg);
		append("</b>");
		if(dataAccess) {
			append("</span>");
		}
		append("<span style='font-size: smaller; color: #ccc;'>");
		append(" @");
		append(lastDotPart(log.toString()));
		append("<i>" + level + "</i> ");
		if(t != null && t.length > 0) {
			append(" " + t.getClass().getName());
		} else {
		}
		append("</span> ");
		append("<br/>\n");
	}
	
	private void append(String s) {
		this.hostPanel.add(new InlineHTML("" + s));
	}
	
	@Override
	public void trace(Logger log, String msg, Throwable t) {
		log("TRACE", log, msg, t);
	}
	
	@Override
	public void debug(Logger log, String msg) {
		log("DEBUG", log, msg);
	}
	
	@Override
	public void debug(Logger log, String msg, Throwable t) {
		log("DEBUG", log, msg, t);
	}
	
	@Override
	public void info(Logger log, String msg) {
		log("INFO", log, msg);
	}
	
	@Override
	public void info(Logger log, String msg, Throwable t) {
		log("INFO", log, msg, t);
	}
	
	@Override
	public void warn(Logger log, String msg) {
		log("WARN", log, msg);
	}
	
	@Override
	public void warn(Logger log, String msg, Throwable t) {
		log("WARN", log, msg, t);
	}
	
	@Override
	public void error(Logger log, String msg) {
		log("ERROR", log, msg);
	}
	
	@Override
	public void error(Logger log, String msg, Throwable t) {
		log("ERROR", log, msg, t);
	}
	
	/**
	 * @param s a canonical class-name
	 * @return the last part after a dot, which is, the short class-name
	 */
	public static String lastDotPart(String s) {
		int i = s.lastIndexOf(".");
		if(i > 0) {
			return s.substring(i + 1);
		} else {
			return s;
		}
	}
	
}
