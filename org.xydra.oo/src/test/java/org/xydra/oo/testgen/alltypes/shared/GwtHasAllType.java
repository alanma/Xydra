package org.xydra.oo.testgen.alltypes.shared;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XIdSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValueJavaUtils;
import org.xydra.oo.runtime.client.GwtXydraMapped;
import org.xydra.oo.runtime.shared.BaseTypeSpec;
import org.xydra.oo.runtime.shared.CollectionProxy.IComponentTransformer;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.runtime.shared.SetProxy;
import org.xydra.oo.runtime.shared.SharedTypeMapping;
import org.xydra.oo.runtime.shared.SortedSetProxy;
import org.xydra.oo.runtime.shared.TypeSpec;
import org.xydra.oo.testgen.alltypes.client.GwtFactory;

/**
 * Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of
 * xydra.org:oo
 */
public class GwtHasAllType extends GwtXydraMapped implements
		org.xydra.oo.testgen.alltypes.shared.IHasAllType {

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public SortedSet<IPerson> bestFriends() {
		final IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson> t = new IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>() {
			@Override
			public org.xydra.oo.testgen.alltypes.shared.IPerson toJavaComponent(final org.xydra.base.XId x) {
				return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), x);
			}

			@Override
			public org.xydra.base.XId toXydraComponent(
					final org.xydra.oo.testgen.alltypes.shared.IPerson javaType) {
				return javaType.getId();
			}

			@Override
			public org.xydra.base.value.XIdListValue createCollection() {
				return XV.toIdListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SortedSetProxy<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>(
				this.oop.getXObject(), Base.toId("bestFriends"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Set<IPerson> friends() {
		final IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.Set<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson> t = new IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.Set<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>() {
			@Override
			public org.xydra.oo.testgen.alltypes.shared.IPerson toJavaComponent(final org.xydra.base.XId x) {
				return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), x);
			}

			@Override
			public org.xydra.base.XId toXydraComponent(
					final org.xydra.oo.testgen.alltypes.shared.IPerson javaType) {
				return javaType.getId();
			}

			@Override
			public org.xydra.base.value.XIdListValue createCollection() {
				return XV.toIdListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SetProxy<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.Set<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>(
				this.oop.getXObject(), Base.toId("friends"), t);
	}

	/**
	 * Auto-convert enum to XStringValue [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Colors getColor() {
		final String s = XValueJavaUtils.getString(this.oop.getXObject(), Base.toId("color"));
		if (s == null) {
			return null;
		}
		return Colors.valueOf(s);
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Boolean getJBoolean() {
		final XBooleanValue x = (XBooleanValue) this.oop.getValue("jBoolean");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Boolean"), null, "gwt"));
		return (Boolean) mapping.toJava(x);
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public boolean[] getJBooleanarray() {
		return XValueJavaUtils.getBooleanArray(this.oop.getXObject(), Base.toId("jBooleanarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Double getJDouble() {
		final XDoubleValue x = (XDoubleValue) this.oop.getValue("jDouble");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Double"), null, "gwt"));
		return (Double) mapping.toJava(x);
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public double[] getJDoublearray() {
		return XValueJavaUtils.getDoubleArray(this.oop.getXObject(), Base.toId("jDoublearray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Integer getJInteger() {
		final XIntegerValue x = (XIntegerValue) this.oop.getValue("jInteger");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Integer"), null, "gwt"));
		return (Integer) mapping.toJava(x);
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public int[] getJIntegerarray() {
		return XValueJavaUtils.getIntegerArray(this.oop.getXObject(), Base.toId("jIntegerarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Long getJLong() {
		final XLongValue x = (XLongValue) this.oop.getValue("jLong");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Long"), null, "gwt"));
		return (Long) mapping.toJava(x);
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public long[] getJLongarray() {
		return XValueJavaUtils.getLongArray(this.oop.getXObject(), Base.toId("jLongarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public String getJString() {
		final XStringValue x = (XStringValue) this.oop.getValue("jString");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "String"), null, "gwt"));
		return (String) mapping.toJava(x);
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XAddress[] getJaddressarray() {
		return XValueJavaUtils.getAddressArray(this.oop.getXObject(), Base.toId("jaddressarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public byte[] getJbinary() {
		final XBinaryValue x = (XBinaryValue) this.oop.getValue("jbinary");
		if (x == null) {
			// byte[]
			return null;
		}
		return x.getValue();
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public boolean getJboolean() {
		final XBooleanValue x = (XBooleanValue) this.oop.getValue("jboolean");
		if (x == null) {
			// Java primitive type
			return false;
		}
		return x.contents();
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public boolean[] getJbooleanarray() {
		return XValueJavaUtils.getBooleanArray(this.oop.getXObject(), Base.toId("jbooleanarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public double getJdouble() {
		final XDoubleValue x = (XDoubleValue) this.oop.getValue("jdouble");
		if (x == null) {
			// Java primitive type
			return 0d;
		}
		return x.contents();
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public double[] getJdoublearray() {
		return XValueJavaUtils.getDoubleArray(this.oop.getXObject(), Base.toId("jdoublearray"));
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XId[] getJiddarray() {
		return XValueJavaUtils.getIdArray(this.oop.getXObject(), Base.toId("jiddarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public int getJint() {
		final XIntegerValue x = (XIntegerValue) this.oop.getValue("jint");
		if (x == null) {
			// Java primitive type
			return 0;
		}
		return x.contents();
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public int[] getJintarray() {
		return XValueJavaUtils.getIntegerArray(this.oop.getXObject(), Base.toId("jintarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public long getJlong() {
		final XLongValue x = (XLongValue) this.oop.getValue("jlong");
		if (x == null) {
			// Java primitive type
			return 0l;
		}
		return x.contents();
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public long[] getJlongarray() {
		return XValueJavaUtils.getLongArray(this.oop.getXObject(), Base.toId("jlongarray"));
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public String[] getJstringarray() {
		return XValueJavaUtils.getStringArray(this.oop.getXObject(), Base.toId("jstringarray"));
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public MyLongBasedType getMyLongBasedType() {
		final XLongValue x = (XLongValue) this.oop.getValue("myLongBasedType");
		if (x == null) {
			return null;
		}
		// Extended types with a mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"org.xydra.oo.testgen.alltypes.shared", "MyLongBasedType"), null, "gwt"));
		return (MyLongBasedType) mapping.toJava(x);
	}

	/**
	 * Proxy type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public IPerson getPartner() {
		final XId id = XValueJavaUtils.getId(this.oop.getXObject(), Base.toId("partner"));
		if (id == null) {
			return null;
		}
		return GwtFactory.wrapPerson(this.oop.getXModel(), id);
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XAddress getXaddress() {
		final XAddress x = (XAddress) this.oop.getValue("xaddress");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XAddressListValue getXaddresslist() {
		final XAddressListValue x = (XAddressListValue) this.oop.getValue("xaddresslist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XAddressSetValue getXaddressset() {
		final XAddressSetValue x = (XAddressSetValue) this.oop.getValue("xaddressset");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XAddressSortedSetValue getXaddresssortedset() {
		final XAddressSortedSetValue x = (XAddressSortedSetValue) this.oop.getValue("xaddresssortedset");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XBinaryValue getXbinary() {
		final XBinaryValue x = (XBinaryValue) this.oop.getValue("xbinary");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XBooleanValue getXboolean() {
		final XBooleanValue x = (XBooleanValue) this.oop.getValue("xboolean");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XBooleanListValue getXbooleanlist() {
		final XBooleanListValue x = (XBooleanListValue) this.oop.getValue("xbooleanlist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XDoubleValue getXdouble() {
		final XDoubleValue x = (XDoubleValue) this.oop.getValue("xdouble");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XDoubleListValue getXdoublelist() {
		final XDoubleListValue x = (XDoubleListValue) this.oop.getValue("xdoublelist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XId getXid() {
		final XId x = (XId) this.oop.getValue("xid");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XIdListValue getXidlist() {
		final XIdListValue x = (XIdListValue) this.oop.getValue("xidlist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XIdSetValue getXidset() {
		final XIdSetValue x = (XIdSetValue) this.oop.getValue("xidset");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XIdSortedSetValue getXidsortedset() {
		final XIdSortedSetValue x = (XIdSortedSetValue) this.oop.getValue("xidsortedset");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XIntegerValue getXinteger() {
		final XIntegerValue x = (XIntegerValue) this.oop.getValue("xinteger");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XIntegerListValue getXintegerlist() {
		final XIntegerListValue x = (XIntegerListValue) this.oop.getValue("xintegerlist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XLongValue getXlong() {
		final XLongValue x = (XLongValue) this.oop.getValue("xlong");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XLongListValue getXlonglist() {
		final XLongListValue x = (XLongListValue) this.oop.getValue("xlonglist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XStringValue getXstring() {
		final XStringValue x = (XStringValue) this.oop.getValue("xstring");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XStringListValue getXstringlist() {
		final XStringListValue x = (XStringListValue) this.oop.getValue("xstringlist");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * Mapped Xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public XStringSetValue getXstringset() {
		final XStringSetValue x = (XStringSetValue) this.oop.getValue("xstringset");
		if (x == null) {
			return null;
		}
		// Xydra value type
		return x;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Set<XAddress> jaddresset() {
		final IComponentTransformer<org.xydra.base.value.XAddressSetValue, org.xydra.base.XAddress, java.util.Set<org.xydra.base.XAddress>, org.xydra.base.XAddress> t = new IComponentTransformer<org.xydra.base.value.XAddressSetValue, org.xydra.base.XAddress, java.util.Set<org.xydra.base.XAddress>, org.xydra.base.XAddress>() {
			@Override
			public org.xydra.base.XAddress toJavaComponent(final org.xydra.base.XAddress x) {
				return x;
			}

			@Override
			public org.xydra.base.XAddress toXydraComponent(final org.xydra.base.XAddress javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XAddressSetValue createCollection() {
				return org.xydra.base.value.XV.toAddressSetValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SetProxy<org.xydra.base.value.XAddressSetValue, org.xydra.base.XAddress, java.util.Set<org.xydra.base.XAddress>, org.xydra.base.XAddress>(
				this.oop.getXObject(), Base.toId("jaddresset"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<XAddress> jaddresslist() {
		final IComponentTransformer<org.xydra.base.value.XAddressListValue, org.xydra.base.XAddress, java.util.List<org.xydra.base.XAddress>, org.xydra.base.XAddress> t = new IComponentTransformer<org.xydra.base.value.XAddressListValue, org.xydra.base.XAddress, java.util.List<org.xydra.base.XAddress>, org.xydra.base.XAddress>() {
			@Override
			public org.xydra.base.XAddress toJavaComponent(final org.xydra.base.XAddress x) {
				return x;
			}

			@Override
			public org.xydra.base.XAddress toXydraComponent(final org.xydra.base.XAddress javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XAddressListValue createCollection() {
				return org.xydra.base.value.XV.toAddressListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XAddressListValue, org.xydra.base.XAddress, java.util.List<org.xydra.base.XAddress>, org.xydra.base.XAddress>(
				this.oop.getXObject(), Base.toId("jaddresslist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public SortedSet<XAddress> jaddresssortedset() {
		final IComponentTransformer<org.xydra.base.value.XAddressSortedSetValue, org.xydra.base.XAddress, java.util.SortedSet<org.xydra.base.XAddress>, org.xydra.base.XAddress> t = new IComponentTransformer<org.xydra.base.value.XAddressSortedSetValue, org.xydra.base.XAddress, java.util.SortedSet<org.xydra.base.XAddress>, org.xydra.base.XAddress>() {
			@Override
			public org.xydra.base.XAddress toJavaComponent(final org.xydra.base.XAddress x) {
				return x;
			}

			@Override
			public org.xydra.base.XAddress toXydraComponent(final org.xydra.base.XAddress javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XAddressSortedSetValue createCollection() {
				return org.xydra.base.value.XV
						.toAddressSortedSetValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SortedSetProxy<org.xydra.base.value.XAddressSortedSetValue, org.xydra.base.XAddress, java.util.SortedSet<org.xydra.base.XAddress>, org.xydra.base.XAddress>(
				this.oop.getXObject(), Base.toId("jaddresssortedset"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<Boolean> jbooleanlist() {
		final IComponentTransformer<org.xydra.base.value.XBooleanListValue, java.lang.Boolean, java.util.List<java.lang.Boolean>, java.lang.Boolean> t = new IComponentTransformer<org.xydra.base.value.XBooleanListValue, java.lang.Boolean, java.util.List<java.lang.Boolean>, java.lang.Boolean>() {
			@Override
			public java.lang.Boolean toJavaComponent(final java.lang.Boolean x) {
				return x;
			}

			@Override
			public java.lang.Boolean toXydraComponent(final java.lang.Boolean javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XBooleanListValue createCollection() {
				return org.xydra.base.value.XV.toBooleanListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XBooleanListValue, java.lang.Boolean, java.util.List<java.lang.Boolean>, java.lang.Boolean>(
				this.oop.getXObject(), Base.toId("jbooleanlist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<Double> jdoublelist() {
		final IComponentTransformer<org.xydra.base.value.XDoubleListValue, java.lang.Double, java.util.List<java.lang.Double>, java.lang.Double> t = new IComponentTransformer<org.xydra.base.value.XDoubleListValue, java.lang.Double, java.util.List<java.lang.Double>, java.lang.Double>() {
			@Override
			public java.lang.Double toJavaComponent(final java.lang.Double x) {
				return x;
			}

			@Override
			public java.lang.Double toXydraComponent(final java.lang.Double javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XDoubleListValue createCollection() {
				return org.xydra.base.value.XV.toDoubleListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XDoubleListValue, java.lang.Double, java.util.List<java.lang.Double>, java.lang.Double>(
				this.oop.getXObject(), Base.toId("jdoublelist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<Integer> jintegerlist() {
		final IComponentTransformer<org.xydra.base.value.XIntegerListValue, java.lang.Integer, java.util.List<java.lang.Integer>, java.lang.Integer> t = new IComponentTransformer<org.xydra.base.value.XIntegerListValue, java.lang.Integer, java.util.List<java.lang.Integer>, java.lang.Integer>() {
			@Override
			public java.lang.Integer toJavaComponent(final java.lang.Integer x) {
				return x;
			}

			@Override
			public java.lang.Integer toXydraComponent(final java.lang.Integer javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XIntegerListValue createCollection() {
				return org.xydra.base.value.XV.toIntegerListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XIntegerListValue, java.lang.Integer, java.util.List<java.lang.Integer>, java.lang.Integer>(
				this.oop.getXObject(), Base.toId("jintegerlist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<Long> jlonglist() {
		final IComponentTransformer<org.xydra.base.value.XLongListValue, java.lang.Long, java.util.List<java.lang.Long>, java.lang.Long> t = new IComponentTransformer<org.xydra.base.value.XLongListValue, java.lang.Long, java.util.List<java.lang.Long>, java.lang.Long>() {
			@Override
			public java.lang.Long toJavaComponent(final java.lang.Long x) {
				return x;
			}

			@Override
			public java.lang.Long toXydraComponent(final java.lang.Long javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XLongListValue createCollection() {
				return org.xydra.base.value.XV.toLongListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XLongListValue, java.lang.Long, java.util.List<java.lang.Long>, java.lang.Long>(
				this.oop.getXObject(), Base.toId("jlonglist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<String> jstringlist() {
		final IComponentTransformer<org.xydra.base.value.XStringListValue, java.lang.String, java.util.List<java.lang.String>, java.lang.String> t = new IComponentTransformer<org.xydra.base.value.XStringListValue, java.lang.String, java.util.List<java.lang.String>, java.lang.String>() {
			@Override
			public java.lang.String toJavaComponent(final java.lang.String x) {
				return x;
			}

			@Override
			public java.lang.String toXydraComponent(final java.lang.String javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XStringListValue createCollection() {
				return org.xydra.base.value.XV.toStringListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XStringListValue, java.lang.String, java.util.List<java.lang.String>, java.lang.String>(
				this.oop.getXObject(), Base.toId("jstringlist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Set<String> jstringset() {
		final IComponentTransformer<org.xydra.base.value.XStringSetValue, java.lang.String, java.util.Set<java.lang.String>, java.lang.String> t = new IComponentTransformer<org.xydra.base.value.XStringSetValue, java.lang.String, java.util.Set<java.lang.String>, java.lang.String>() {
			@Override
			public java.lang.String toJavaComponent(final java.lang.String x) {
				return x;
			}

			@Override
			public java.lang.String toXydraComponent(final java.lang.String javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XStringSetValue createCollection() {
				return org.xydra.base.value.XV.toStringSetValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SetProxy<org.xydra.base.value.XStringSetValue, java.lang.String, java.util.Set<java.lang.String>, java.lang.String>(
				this.oop.getXObject(), Base.toId("jstringset"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<XId> jxidlist() {
		final IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.base.XId>, org.xydra.base.XId> t = new IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.base.XId>, org.xydra.base.XId>() {
			@Override
			public org.xydra.base.XId toJavaComponent(final org.xydra.base.XId x) {
				return x;
			}

			@Override
			public org.xydra.base.XId toXydraComponent(final org.xydra.base.XId javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XIdListValue createCollection() {
				return org.xydra.base.value.XV.toIdListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.base.XId>, org.xydra.base.XId>(
				this.oop.getXObject(), Base.toId("jxidlist"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public Set<XId> jxidset() {
		final IComponentTransformer<org.xydra.base.value.XIdSetValue, org.xydra.base.XId, java.util.Set<org.xydra.base.XId>, org.xydra.base.XId> t = new IComponentTransformer<org.xydra.base.value.XIdSetValue, org.xydra.base.XId, java.util.Set<org.xydra.base.XId>, org.xydra.base.XId>() {
			@Override
			public org.xydra.base.XId toJavaComponent(final org.xydra.base.XId x) {
				return x;
			}

			@Override
			public org.xydra.base.XId toXydraComponent(final org.xydra.base.XId javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XIdSetValue createCollection() {
				return org.xydra.base.value.XV.toIdSetValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SetProxy<org.xydra.base.value.XIdSetValue, org.xydra.base.XId, java.util.Set<org.xydra.base.XId>, org.xydra.base.XId>(
				this.oop.getXObject(), Base.toId("jxidset"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public SortedSet<XId> jxidsortedset() {
		final IComponentTransformer<org.xydra.base.value.XIdSortedSetValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.base.XId>, org.xydra.base.XId> t = new IComponentTransformer<org.xydra.base.value.XIdSortedSetValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.base.XId>, org.xydra.base.XId>() {
			@Override
			public org.xydra.base.XId toJavaComponent(final org.xydra.base.XId x) {
				return x;
			}

			@Override
			public org.xydra.base.XId toXydraComponent(final org.xydra.base.XId javaType) {
				return javaType;
			}

			@Override
			public org.xydra.base.value.XIdSortedSetValue createCollection() {
				return org.xydra.base.value.XV.toIdSortedSetValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new SortedSetProxy<org.xydra.base.value.XIdSortedSetValue, org.xydra.base.XId, java.util.SortedSet<org.xydra.base.XId>, org.xydra.base.XId>(
				this.oop.getXObject(), Base.toId("jxidsortedset"), t);
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @return ...
	 */
	@Override
	public List<IPerson> nextBirthdays() {
		final IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson> t = new IComponentTransformer<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>() {
			@Override
			public org.xydra.oo.testgen.alltypes.shared.IPerson toJavaComponent(final org.xydra.base.XId x) {
				return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), x);
			}

			@Override
			public org.xydra.base.XId toXydraComponent(
					final org.xydra.oo.testgen.alltypes.shared.IPerson javaType) {
				return javaType.getId();
			}

			@Override
			public org.xydra.base.value.XIdListValue createCollection() {
				return XV.toIdListValue(java.util.Collections.EMPTY_LIST);
			}

		};

		return new ListProxy<org.xydra.base.value.XIdListValue, org.xydra.base.XId, java.util.List<org.xydra.oo.testgen.alltypes.shared.IPerson>, org.xydra.oo.testgen.alltypes.shared.IPerson>(
				this.oop.getXObject(), Base.toId("nextBirthdays"), t);
	}

	/**
	 * Enum types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param color
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setColor(final Colors color) {
		XValueJavaUtils.setString(this.oop.getXObject(), Base.toId("color"), color.name());
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jBooleanarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJBooleanarray(final boolean[] jBooleanarray) {
		XValueJavaUtils.setBooleanArray(this.oop.getXObject(), Base.toId("jBooleanarray"),
				jBooleanarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jBoolean
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJBoolean(final Boolean jBoolean) {
		// non-xydra type 'Boolean' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Boolean"), null, "gwt"));
		final XBooleanValue x = (XBooleanValue) mapping.toXydra(jBoolean);
		this.oop.setValue("jBoolean", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jDoublearray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJDoublearray(final double[] jDoublearray) {
		XValueJavaUtils
				.setDoubleArray(this.oop.getXObject(), Base.toId("jDoublearray"), jDoublearray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jDouble
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJDouble(final Double jDouble) {
		// non-xydra type 'Double' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Double"), null, "gwt"));
		final XDoubleValue x = (XDoubleValue) mapping.toXydra(jDouble);
		this.oop.setValue("jDouble", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jIntegerarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJIntegerarray(final int[] jIntegerarray) {
		XValueJavaUtils.setIntegerArray(this.oop.getXObject(), Base.toId("jIntegerarray"),
				jIntegerarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jInteger
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJInteger(final Integer jInteger) {
		// non-xydra type 'Integer' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Integer"), null, "gwt"));
		final XIntegerValue x = (XIntegerValue) mapping.toXydra(jInteger);
		this.oop.setValue("jInteger", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jLongarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJLongarray(final long[] jLongarray) {
		XValueJavaUtils.setLongArray(this.oop.getXObject(), Base.toId("jLongarray"), jLongarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jLong
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJLong(final Long jLong) {
		// non-xydra type 'Long' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "Long"), null, "gwt"));
		final XLongValue x = (XLongValue) mapping.toXydra(jLong);
		this.oop.setValue("jLong", x);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jString
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJString(final String jString) {
		// non-xydra type 'String' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"java.lang", "String"), null, "gwt"));
		final XStringValue x = (XStringValue) mapping.toXydra(jString);
		this.oop.setValue("jString", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jaddressarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJaddressarray(final XAddress[] jaddressarray) {
		XValueJavaUtils.setAddressArray(this.oop.getXObject(), Base.toId("jaddressarray"),
				jaddressarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jbinary
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJbinary(final byte[] jbinary) {
		// non-xydra type 'byte[]' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"org.xydra.oo.runtime.shared", "byte[]"), null, "gwt"));
		final XBinaryValue x = (XBinaryValue) mapping.toXydra(jbinary);
		this.oop.setValue("jbinary", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jbooleanarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJbooleanarray(final boolean[] jbooleanarray) {
		XValueJavaUtils.setBooleanArray(this.oop.getXObject(), Base.toId("jbooleanarray"),
				jbooleanarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jboolean
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJboolean(final boolean jboolean) {
		// non-xydra type 'boolean' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				null, "boolean"), null, "gwt"));
		final XBooleanValue x = (XBooleanValue) mapping.toXydra(jboolean);
		this.oop.setValue("jboolean", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jdoublearray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJdoublearray(final double[] jdoublearray) {
		XValueJavaUtils
				.setDoubleArray(this.oop.getXObject(), Base.toId("jdoublearray"), jdoublearray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jdouble
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJdouble(final double jdouble) {
		// non-xydra type 'double' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				null, "double"), null, "gwt"));
		final XDoubleValue x = (XDoubleValue) mapping.toXydra(jdouble);
		this.oop.setValue("jdouble", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jiddarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJiddarray(final XId[] jiddarray) {
		XValueJavaUtils.setIdArray(this.oop.getXObject(), Base.toId("jiddarray"), jiddarray);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jintarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJintarray(final int[] jintarray) {
		XValueJavaUtils.setIntegerArray(this.oop.getXObject(), Base.toId("jintarray"), jintarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jint
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJint(final int jint) {
		// non-xydra type 'int' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				null, "int"), null, "gwt"));
		final XIntegerValue x = (XIntegerValue) mapping.toXydra(jint);
		this.oop.setValue("jint", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jlongarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJlongarray(final long[] jlongarray) {
		XValueJavaUtils.setLongArray(this.oop.getXObject(), Base.toId("jlongarray"), jlongarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jlong
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJlong(final long jlong) {
		// non-xydra type 'long' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				null, "long"), null, "gwt"));
		final XLongValue x = (XLongValue) mapping.toXydra(jlong);
		this.oop.setValue("jlong", x);
		return this;
	}

	/**
	 * Java types corresponding to Xydra types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param jstringarray
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setJstringarray(final String[] jstringarray) {
		XValueJavaUtils
				.setStringArray(this.oop.getXObject(), Base.toId("jstringarray"), jstringarray);
		return this;
	}

	/**
	 * [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param myLongBasedType
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setMyLongBasedType(final MyLongBasedType myLongBasedType) {
		// non-xydra type 'MyLongBasedType' with mapping
		final SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
				"org.xydra.oo.testgen.alltypes.shared", "MyLongBasedType"), null, "gwt"));
		final XLongValue x = (XLongValue) mapping.toXydra(myLongBasedType);
		this.oop.setValue("myLongBasedType", x);
		return this;
	}

	/**
	 * Proxy types [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param partner
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setPartner(final IPerson partner) {
		XValueJavaUtils.setId(this.oop.getXObject(), Base.toId("partner"), partner.getId());
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xaddresslist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXaddresslist(final XAddressListValue xaddresslist) {
		this.oop.setValue("xaddresslist", xaddresslist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xaddressset
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXaddressset(final XAddressSetValue xaddressset) {
		this.oop.setValue("xaddressset", xaddressset);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xaddresssortedset
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXaddresssortedset(final XAddressSortedSetValue xaddresssortedset) {
		this.oop.setValue("xaddresssortedset", xaddresssortedset);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xaddress
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXaddress(final XAddress xaddress) {
		this.oop.setValue("xaddress", xaddress);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xbinary
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXbinary(final XBinaryValue xbinary) {
		this.oop.setValue("xbinary", xbinary);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xbooleanlist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXbooleanlist(final XBooleanListValue xbooleanlist) {
		this.oop.setValue("xbooleanlist", xbooleanlist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xboolean
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXboolean(final XBooleanValue xboolean) {
		this.oop.setValue("xboolean", xboolean);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xdoublelist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXdoublelist(final XDoubleListValue xdoublelist) {
		this.oop.setValue("xdoublelist", xdoublelist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xdouble
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXdouble(final XDoubleValue xdouble) {
		this.oop.setValue("xdouble", xdouble);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xidlist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXidlist(final XIdListValue xidlist) {
		this.oop.setValue("xidlist", xidlist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xidset
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXidset(final XIdSetValue xidset) {
		this.oop.setValue("xidset", xidset);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xidsortedset
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXidsortedset(final XIdSortedSetValue xidsortedset) {
		this.oop.setValue("xidsortedset", xidsortedset);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xid
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXid(final XId xid) {
		this.oop.setValue("xid", xid);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xintegerlist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXintegerlist(final XIntegerListValue xintegerlist) {
		this.oop.setValue("xintegerlist", xintegerlist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xinteger
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXinteger(final XIntegerValue xinteger) {
		this.oop.setValue("xinteger", xinteger);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xlonglist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXlonglist(final XLongListValue xlonglist) {
		this.oop.setValue("xlonglist", xlonglist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xlong
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXlong(final XLongValue xlong) {
		this.oop.setValue("xlong", xlong);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xstringlist
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXstringlist(final XStringListValue xstringlist) {
		this.oop.setValue("xstringlist", xstringlist);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xstringset
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXstringset(final XStringSetValue xstringset) {
		this.oop.setValue("xstringset", xstringset);
		return this;
	}

	/**
	 * Trivial xydra type [generated from:
	 * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 *
	 * @param xstring
	 *            [generated from:
	 *            'org.xydra.oo.testgen.alltypes.shared.IHasAllType']
	 * @return ...
	 */
	@Override
	public IHasAllType setXstring(final XStringValue xstring) {
		this.oop.setValue("xstring", xstring);
		return this;
	}

}
