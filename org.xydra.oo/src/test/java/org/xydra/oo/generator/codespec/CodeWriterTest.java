package org.xydra.oo.generator.codespec;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class CodeWriterTest {

	public static void main(final String[] args) throws IOException {
		final List<String> lines = CodeWriter
				.typeset(
						"On a long and rainy day the water was pouring from the sky like cola cans in a supermarket bombarded from a meteor",
						40);
		for (final String l : lines) {
			System.out.println("LINE " + l);
		}
		System.out.println("xxxx");
		final Writer w = new OutputStreamWriter(System.out);
		CodeWriter
				.writeJavaDocComment(
						w,
						"----",
						"On a long and rainy day the water was pouring from the sky like cola cans in a supermarket bombarded from a meteor");
		w.flush();
	}

}
