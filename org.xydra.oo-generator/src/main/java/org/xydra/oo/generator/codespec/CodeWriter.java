package org.xydra.oo.generator.codespec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * Knows nothing about ...Specs. Knows just how to write certain things as Java
 * source code to text files.
 *
 * @author xamde
 */
public class CodeWriter {

	private static final Logger log = LoggerFactory.getLogger(CodeWriter.class);

	public static void writeJavaDocComment(final Writer w, final String indent, final String value)
			throws IOException {
		final List<String> lines = new ArrayList<String>();
		final BufferedReader br = new BufferedReader(new StringReader(value));
		String line = br.readLine();
		while (line != null) {
			line = firstLetterCapital(line);
			lines.add(line);
			line = br.readLine();
		}

		for (int i = 0; i < lines.size(); i++) {
			line = lines.get(i);
			if (line.length() > 70) {
				// split into more lines
				final List<String> shorterLines = typeset(line, 70);
				lines.remove(i);
				lines.addAll(i, shorterLines);
			}
		}

		if (lines.size() == 1) {
			w.write(indent + "/** " + lines.get(0) + " */\n");
		} else {
			w.write(indent + "/** \n");
			for (final String l : lines) {
				w.write(indent + " * " + l + " \n");
			}
			w.write(indent + " */\n");
		}
	}

	private static String firstLetterCapital(final String s) {
		if (s == null) {
			return null;
		}
		if (s.length() == 0) {
			return "";
		}
		if (s.length() == 1) {
			return s.toUpperCase();
		}
		assert s.length() >= 2;
		return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
	}

	public static List<String> typeset(final String longline, final int maxLen) {
		assert !longline.contains("\n");

		final List<String> shortLines = new ArrayList<String>();
		StringBuilder current = new StringBuilder();
		int lineLen = 0;
		final String[] words = longline.split(" ");
		for (final String word : words) {
			if (lineLen + word.length() < maxLen) {
				current.append(word + " ");
				lineLen += word.length();
			} else {
				shortLines.add(current.toString());
				current = new StringBuilder();
				current.append(word + " ");
				lineLen = 0;
			}
		}
		shortLines.add(current.toString().trim());
		return shortLines;
	}

	private static final int MB1 = 1024 * 1024;

	static void writeAnnotation(final Writer w, final String indent, final String annotationName, final Object[] values)
			throws IOException {
		w.write(indent + "@");
		w.write(annotationName);
		if (values != null && values.length > 0) {
			w.write("(");
			for (int i = 0; i < values.length; i++) {
				final Object o = values[i];
				if (o instanceof String) {
					w.write("\"" + values[i].toString() + "\"");
				} else {
					throw new RuntimeException("Cannot handle annotation value of type "
							+ o.getClass().getCanonicalName() + " yet");
				}

			}
			w.write(")");
		}
		w.write("\n");
	}

	public static File toJavaSourceFile(final File outDir, final String basePackage, final String className) {
		assert basePackage != null;
		final File sourceDir = new File(outDir, basePackage.replace(".", "/"));
		final File javaFile = new File(sourceDir, className + ".java");
		return javaFile;
	}

	public static Writer openWriter(final File f) throws IOException {
		f.getParentFile().mkdirs();
		try {
			final FileOutputStream fos = new FileOutputStream(f);
			final OutputStreamWriter w = new OutputStreamWriter(fos, "utf-8");
			final BufferedWriter bw = new BufferedWriter(w, MB1);
			return bw;
		} catch (final FileNotFoundException e) {
			log.warn("Problem writing " + f.getAbsolutePath(), e);
			throw e;
		}
	}

	public static void writeField(final Writer w, final String indent, final String type, final String name)
			throws IOException {
		w.write(indent);
		w.write(type);
		w.write(" ");
		w.write(name);
		w.write(";\n");
	}

}
