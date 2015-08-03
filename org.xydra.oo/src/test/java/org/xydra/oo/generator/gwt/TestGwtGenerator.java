package org.xydra.oo.generator.gwt;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xydra.base.value.ValueType;
import org.xydra.oo.generator.codespec.SpecWriter;
import org.xydra.oo.generator.codespec.impl.ClassSpec;
import org.xydra.oo.generator.codespec.impl.PackageSpec;
import org.xydra.oo.runtime.java.JavaTypeMapping;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.MyLongBasedType;

/**
 * This is usually done automatically as part of gwt:compile
 */
public class TestGwtGenerator {

	public static void main(final String[] args) throws IOException, ClassNotFoundException {
		final TestGwtGenerator t = new TestGwtGenerator();
		t.generateGwtClasses();
	}

	@Test
	public void generateGwtClasses() throws IOException, ClassNotFoundException {

		// setup

		// optional: adding extended types
		JavaTypeMapping.addSingleTypeMapping(MyLongBasedType.class, ValueType.Long,
				MyLongBasedType.MAPPER);

		// required setup
		final PackageSpec ps = new PackageSpec(IHasAllType.class.getPackage().getName(), false);

		final ClassSpec c = GwtCodeGenerator.constructClassSpec(ps,
				"org.xydra.oo.testgen.alltypes.client", IHasAllType.class.getCanonicalName(),
				"GwtHasAllType");
		c.dump();

		final File f = new File("./src/test/java");
		f.mkdirs();
		SpecWriter.writePackage(ps, f);
	}

}
