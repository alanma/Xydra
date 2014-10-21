package org.xydra.oo.runtime.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;

@RunsInGWT(true)
public class SharedTypeSystem {

	private static final Set<String> javaPrimitiveTypes = new HashSet<String>(Arrays.asList(

	"boolean", "byte", "char", "double", "float", "int", "long", "short"

	));

	public static boolean isJavaPrimitiveType(String simpleName) {
		return javaPrimitiveTypes.contains(simpleName);
	}

	public static SharedTypeMapping getMapping(TypeSpec type) {
		SharedTypeMapping mapping = SharedTypeMapping.getMapping(type);
		if (mapping == null) {
			// retry as mapped collection type
			if (isCollection(type.getBaseType())) {
				mapping = SharedTypeMapping.getMapping(new TypeSpec(type.getBaseType(),
						TypeConstants.BaseType_XId, "SharedTypeSystem"));
			} else {
				// assume its a single mapped type
				mapping = SharedTypeMapping.getMapping(TypeConstants.Id);
			}
		}
		assert mapping != null;
		return mapping;
	}

	public static boolean isCollection(IBaseType baseType) {
		return baseType.getPackageName().equals("java.util")
				&& (baseType.getSimpleName().equals("Set")
						|| baseType.getSimpleName().equals("List") || baseType.getSimpleName()
						.equals("SortedSet"));
	}

}
