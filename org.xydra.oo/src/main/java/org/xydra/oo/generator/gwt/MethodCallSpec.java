package org.xydra.oo.generator.gwt;

import java.util.ArrayList;
import java.util.List;

public class MethodCallSpec {

	private String method;
	private List<String> args = new ArrayList<String>();
	private String packageName;
	private String className;

	public MethodCallSpec(Class<?> c, String method) {
		this(c.getPackage().getName(), c.getSimpleName(), method);
	}

	public MethodCallSpec(String packageName, String className, String method) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.method = method;
	}

	public MethodCallSpec addParam(String p) {
		this.args.add(p);
		// fluent
		return this;
	}

	public String toMethodCall() {
		String s = this.className + "." + this.method + "(";
		for (int i = 0; i < this.args.size(); i++) {
			s += this.args.get(i);
			if (i + 1 < this.args.size()) {
				s += ", ";
			}
		}
		s += ")";
		return s;
	}

	public String getRequiredImports() {
		return this.packageName + "." + this.className;
	}

}
