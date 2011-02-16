package org.xydra.testgae.xmas;

import java.util.LinkedList;
import java.util.List;


public class HtmlUtils {
	
	public static String link(String url, String text) {
		return "<a href=\"" + url + "\">" + text + "</a>";
	}
	
	public static SubmitInput inputSubmit(String label) {
		return new SubmitInput(label);
	}
	
	public static class Input {
		
	}
	
	public static class SubmitInput extends Input {
		
		private String label;
		
		public SubmitInput(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return "<input type=\"submit\" value=\"" + this.label + "\"/>";
		}
		
	}
	
	public static class TextInput extends Input {
		
		public TextInput(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		
		private String name;
		private String value;
		
		@Override
		public String toString() {
			return this.name + ": <input type=\"text\" name=\"" + this.name + "\" value=\""
			        + this.value + "\"/>";
		}
		
	}
	
	public static Form form(METHOD method, String action) {
		return new Form(method, action);
	}
	
	public static enum METHOD {
		GET, POST
	}
	
	public static class Form {
		
		private String action;
		private METHOD method;
		private List<Input> inputs = new LinkedList<HtmlUtils.Input>();
		
		public Form(METHOD method, String action) {
			this.method = method;
			this.action = action;
		}
		
		public Form withInputText(String name, String value) {
			this.inputs.add(new TextInput(name, value));
			return this;
		}
		
		public Form withInputSubmit(String value) {
			this.inputs.add(new SubmitInput(value));
			return this;
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("<form action='" + this.action + "' method='" + this.method + "'>");
			for(Input input : this.inputs) {
				buf.append(input.toString() + "<br />\n");
			}
			buf.append("</form>");
			return buf.toString();
		}
		
	}
	
}
