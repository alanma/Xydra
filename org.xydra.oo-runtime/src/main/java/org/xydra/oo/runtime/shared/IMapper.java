package org.xydra.oo.runtime.shared;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.value.XValue;

/**
 * Maps from a java type + component type to a Xydra type -- and back.
 *
 * Used in {@link SharedTypeMapping}.
 *
 * @param <J>
 *            Java type
 * @param <X>
 *            Xydra type
 */
@RunsInGWT(true)
public interface IMapper<J, X extends XValue> {

	/**
	 * @param x
	 *            xydra value @NeverNull
	 * @return @NeverNull
	 */
	J toJava(@NeverNull X x);

	/**
	 * @param j
	 *            java object @NeverNull
	 * @return @NeverNull
	 */
	X toXydra(@NeverNull J j);

	/**
	 * Make sure to use all class with their canonical names to avoid required
	 * imports.
	 *
	 * @return an expression in Java source code that transforms an 'x'
	 *         (instance of X extends XValue) to J (the desired Java object)
	 */
	String toJava_asSourceCode();

	/**
	 * Make sure to use all class with their canonical names to avoid required
	 * imports.
	 *
	 * @return an expression in Java source code that transforms a 'j' (instance
	 *         of J) to X (extends XValue, the desired Xydra value instance)
	 */
	String toXydra_asSourceCode();

}
