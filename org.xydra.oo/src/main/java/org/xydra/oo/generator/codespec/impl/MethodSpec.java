package org.xydra.oo.generator.codespec.impl;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.runtime.java.JavaReflectionUtils;
import org.xydra.oo.runtime.java.JavaTypeSpecUtils;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.TypeSpec;

public class MethodSpec extends AbstractConstructorOrMethodSpec implements IMember {

	public IType returnType;

	/**
	 * @param method
	 *            use to take name, returnType and returnType.componentType
	 * @param generatedFrom
	 */
	public MethodSpec(Method method, String generatedFrom) {
		super(method.getName(), generatedFrom);
		if (method.getReturnType().getSimpleName().equals("void")) {
			this.returnType = null;
		} else {
			this.returnType = JavaTypeSpecUtils.createTypeSpec(method.getReturnType(),
					JavaReflectionUtils.getComponentType(method), generatedFrom);
		}
	}

	MethodSpec(String name, Class<?> type, Class<?> componentType, String generatedFrom) {
		super(name, generatedFrom);
		this.returnType = JavaTypeSpecUtils.createTypeSpec(type, componentType, generatedFrom);
	}

	MethodSpec(String name, String typePackageName, String typeName, String generatedFrom) {
		super(name, generatedFrom);
		this.returnType = new TypeSpec(typePackageName, typeName, generatedFrom);
	}

	/**
	 * @param name
	 * @param returnType
	 * @param generatedFrom
	 */
	MethodSpec(String name, IType returnType, String generatedFrom) {
		super(name, generatedFrom);
		this.returnType = returnType;
	}

	public static MethodSpec createVoid(String name, String generatedFrom) {
		MethodSpec methodSpec = new MethodSpec(name, null, generatedFrom);
		return methodSpec;
	}

	public boolean isVoid() {
		return this.returnType == null;
	}

	public FieldSpec addParam(String name, java.lang.reflect.Type t, String generatedFrom) {
		FieldSpec fs = new FieldSpec(name, t, generatedFrom);
		this.params.add(fs);
		return fs;
	}

	public FieldSpec addParam(String name, IType typeSpec, String generatedFrom) {
		FieldSpec fs = new FieldSpec(name, typeSpec, generatedFrom);
		this.params.add(fs);
		return fs;
	}

	@Override
	public Set<String> getRequiredImports() {
		Set<String> req = new HashSet<String>();
		req.addAll(super.getRequiredImports());
		if (this.returnType != null)
			req.addAll(this.returnType.getRequiredImports());
		return req;
	}

	@Override
	public String toString() {
		String s = "";
		s += "// " + this.getComment() + "\n";
		s += "METHOD " + this.getName() + "\n";
		s += "  returnType:[" + this.getReturnTypeString() + "]" + "\n";
		for (FieldSpec p : this.params) {
			s += "  PARAM " + p.toString() + "\n";
		}
		for (String l : this.sourceLines) {
			s += "  CODE " + l + "\n";
		}
		return s;
	}

	public IType getReturnType() {
		return this.returnType;
	}

	public String getReturnTypeString() {
		return this.returnType == null ? "void" : this.returnType.getTypeString();
	}

}
