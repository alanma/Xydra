package org.xydra.oo.testtypes;

import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XV;
import org.xydra.oo.runtime.shared.IMapper;

/**
 * A test type to simulate some type, here based on a Java Long
 *
 * @author xamde
 *
 */
public class MyLongBasedType {

	private final long v;

	public MyLongBasedType(final long v) {
		this.v = v;
	}

	public static final IMapper<MyLongBasedType, XLongValue> MAPPER = new IMapper<MyLongBasedType, XLongValue>() {

		@Override
		public MyLongBasedType toJava(final XLongValue x) {
			return new MyLongBasedType(x.contents());
		}

		@Override
		public XLongValue toXydra(final MyLongBasedType j) {
			return XV.toValue(j.v);
		}

		@Override
		public java.lang.String toJava_asSourceCode() {
			return "new MyLongBasedType(x.contents())";
		}

		@Override
		public java.lang.String toXydra_asSourceCode() {
			return "XV.toValue(j.v)";
		}

	};

	public long getInternalLong() {
		return this.v;
	}

}
