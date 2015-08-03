package org.xydra.oo.generator.codespec;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.oo.generator.codespec.impl.AbstractConstructorOrMethodSpec;
import org.xydra.oo.generator.codespec.impl.AnnotationSpec;
import org.xydra.oo.generator.codespec.impl.ClassSpec;
import org.xydra.oo.generator.codespec.impl.ConstructorSpec;
import org.xydra.oo.generator.codespec.impl.FieldSpec;
import org.xydra.oo.generator.codespec.impl.MethodSpec;
import org.xydra.oo.generator.codespec.impl.PackageSpec;
import org.xydra.oo.generator.java.JavaCodeGenerator;

/**
 * Writes ...Spec data objects into Java source code via {@link CodeWriter}.
 *
 * @author xamde
 */
public class SpecWriter {

	private static final Logger log = LoggerFactory.getLogger(JavaCodeGenerator.class);

	static void writeClass(final Writer w, final ClassSpec classSpec) throws IOException {
		writeClass(w, classSpec.getPackageName(), classSpec);
	}

	public static void writeClass(final Writer w, final String packageName, final ClassSpec classSpec)
			throws IOException {
		// package org.xydra.oo.test.tasks;
		//
		w.write("package " + packageName + ";\n");
		w.write("\n");

		// import java.util.*;
		// import org.xydra.oo.*;
		final List<String> imports = new ArrayList<String>();
		imports.addAll(classSpec.getRequiredImports());
		Collections.sort(imports);
		for (final String imp : imports) {
			w.write("import " + imp + ";\n");
		}
		w.write("\n");

		// /**
		// * Generated on @@ by @@
		// */
		// public interface ITaskList {
		//
		CodeWriter.writeJavaDocComment(w, "",

		(classSpec.getComment() != null ? classSpec.getComment() + " " : "") +

		"Generated on " + new Date() + " by " + SpecWriter.class.getSimpleName()
				+ ", a part of xydra.org:oo");
		w.write("public " + classSpec.kind + " " + classSpec.getName());
		if (classSpec.superClass != null) {
			w.write(" extends " + classSpec.superClass.getName());
		}
		if (!classSpec.implementedInterfaces.isEmpty()) {
			w.write(" implements ");
			for (int i = 0; i < classSpec.implementedInterfaces.size(); i++) {
				final String s = classSpec.implementedInterfaces.get(i);
				w.write(s);
				if (i + 1 < classSpec.implementedInterfaces.size()) {
					w.write(", ");
				}
			}
		}
		w.write(" {\n");
		w.write("\n");

		Collections.sort(classSpec.members);
		writeMethodsAndFields(w, classSpec);

		// }
		w.write("}\n");
	}

	public static void writeClassMethod(final Writer w, final String indent, final MethodSpec method)
			throws IOException {
		writeMethodHead(w, indent, method);
		writeMethodBody(w, indent, method);
	}

	public static void writeInterfaceMethod(final Writer w, final String indent, final MethodSpec method)
			throws IOException {
		writeMethodHead(w, indent, method);
		w.write(";\n");
	}

	public static void writeMethodComment(final Writer w, final String indent,
			final AbstractConstructorOrMethodSpec methodOrConstructor) throws IOException {
		final StringBuilder comment = new StringBuilder();
		if (methodOrConstructor.getComment() != null) {
			comment.append(methodOrConstructor.getComment());
			comment.append("\n");
			comment.append("\n");
		}
		for (final FieldSpec p : methodOrConstructor.params) {
			comment.append("@param ");
			comment.append(p.getName());
			comment.append(" ");
			comment.append(p.getComment() == null ? "" : p.getComment());
			comment.append("\n");
		}
		if (methodOrConstructor instanceof MethodSpec) {
			final MethodSpec method = (MethodSpec) methodOrConstructor;
			if (!method.isVoid()) {
				comment.append("@return ");
				comment.append(method.returnType.getComment() == null ? "..." : method.returnType
						.getComment());
				comment.append("\n");
			}
		}
		CodeWriter.writeJavaDocComment(w, indent, comment.toString());
	}

	static void writeMethodHead(final Writer w, final String indent, final MethodSpec method) throws IOException {
		for (final AnnotationSpec<?> ann : method.annotations) {
			CodeWriter.writeAnnotation(w, indent, ann.annot.getSimpleName(), ann.getValues());
		}
		w.write(indent);
		w.write(method.getModifiers());
		if (method.getModifiers().length() > 0) {
			w.write(" ");
		}
		w.write(method.getReturnTypeString());
		w.write(" ");
		w.write(method.getName());
		writeMethodParams(w, method);
	}

	private static void writeMethodParams(final Writer w, final AbstractConstructorOrMethodSpec com)
			throws IOException {
		w.write("(");
		boolean first = true;
		for (final FieldSpec p : com.params) {
			if (!first) {
				w.write(", ");
			}
			w.write(p.getTypeString());
			w.write(" ");
			w.write(p.getName());
			first = false;
		}
		w.write(")");

	}

	public static void writeConstructor(final Writer w, final String indent, final ConstructorSpec method)
			throws IOException {
		w.write(indent);
		w.write("public ");
		w.write(method.getName());
		writeMethodParams(w, method);
		writeMethodBody(w, indent, method);
	}

	private static void writeMethodBody(final Writer w, final String indent, final AbstractConstructorOrMethodSpec com)
			throws IOException {
		if (com instanceof MethodSpec && ((MethodSpec) com).isAbstract()) {
			w.write(";\n");
		} else {
			w.write(" {\n");
			for (final String line : com.sourceLines) {
				w.write(indent + "    " + line + "\n");
			}
			w.write(indent + "}\n");
		}
	}

	public static void writeClass(final ClassSpec classSpec, final File outDir) throws IOException {
		if (classSpec.isBuiltIn()) {
			return;
		}

		final File javaFile = CodeWriter.toJavaSourceFile(outDir, classSpec.getPackageName(),
				classSpec.getName());
		final Writer w = CodeWriter.openWriter(javaFile);
		writeClass(w, classSpec);
		w.close();
	}

	public static void writePackage(final PackageSpec packageSpec, final File outDir) throws IOException {
		log.info("Writing package " + packageSpec.getFQPackageName() + " to "
				+ outDir.getAbsolutePath());

		if (!packageSpec.isBuiltIn()) {
			int i = 0;
			for (final ClassSpec classSpec : packageSpec.classes) {
				ClassSpec currentClass = classSpec;

				while (currentClass != null) {
					writeClass(currentClass, outDir);
					i++;

					currentClass = currentClass.superClass;
				}
			}
			log.info("Wrote " + i + " files");
		}
		for (final PackageSpec subPackage : packageSpec.subPackages) {
			writePackage(subPackage, outDir);
		}
	}

	/**
	 * @param w
	 * @param classSpec
	 * @throws IOException
	 */
	public static void writeMethodsAndFields(final Writer w, final ClassSpec classSpec) throws IOException {
		final String indent = "    ";
		for (final IMember t : classSpec.members) {
			if (t instanceof FieldSpec) {
				final FieldSpec fieldSpec = (FieldSpec) t;
				writeFieldComment(w, indent, fieldSpec);
				writeField(w, indent, fieldSpec);
			} else if (t instanceof ConstructorSpec) {
				final ConstructorSpec constructorSpec = (ConstructorSpec) t;
				writeMethodComment(w, indent, constructorSpec);
				writeConstructor(w, indent, constructorSpec);
			} else {
				assert t instanceof MethodSpec;
				final MethodSpec methodSpec = (MethodSpec) t;
				writeMethodComment(w, indent, methodSpec);
				if (classSpec.kind.equals("interface")) {
					writeInterfaceMethod(w, indent, methodSpec);
				} else {
					writeClassMethod(w, indent, methodSpec);
				}
			}
			w.write("\n");
		}

	}

	private static void writeField(final Writer w, final String indent, final FieldSpec fieldSpec) throws IOException {
		CodeWriter.writeField(w, indent, fieldSpec.getTypeString(), fieldSpec.getName());
	}

	private static void writeFieldComment(final Writer w, final String indent, final FieldSpec fieldSpec)
			throws IOException {
		if (fieldSpec.getComment() == null) {
			return;
		}
		CodeWriter.writeJavaDocComment(w, indent, fieldSpec.getComment());
	}

}
