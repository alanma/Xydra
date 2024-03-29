package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.annotations.CanBeNull;
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
import org.xydra.base.value.XValueFactory;


/**
 * An implementation of {@link XValueFactory}
 *
 * @author xamde
 * @author kaidel
 * @author dscharrer
 *
 */
public class MemoryValueFactory implements XValueFactory {

    @Override
    public XAddressListValue createAddressListValue(final Collection<XAddress> addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressListValue(addresses);
    }

    @Override
    public XAddressListValue createAddressListValue(final XAddress[] addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressListValue(addresses);
    }

    @Override
    public XAddressSetValue createAddressSetValue(final Collection<XAddress> addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressSetValue(addresses);
    }

    @Override
    public XAddressSetValue createAddressSetValue(final XAddress[] addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressSetValue(addresses);
    }

    @Override
    public XAddressSortedSetValue createAddressSortedSetValue(final Collection<XAddress> addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressSortedSetValue(addresses);
    }

    @Override
    public XAddressSortedSetValue createAddressSortedSetValue(final XAddress[] addresses) {
        if(addresses == null) {
			return null;
		}
        return new MemoryAddressSortedSetValue(addresses);
    }

    @Override
    public XBooleanListValue createBooleanListValue(final boolean[] booleans) {
        if(booleans == null) {
			return null;
		}
        return new MemoryBooleanListValue(booleans);
    }

    @Override
    public XBooleanListValue createBooleanListValue(final Collection<Boolean> booleans) {
        if(booleans == null) {
			return null;
		}
        return new MemoryBooleanListValue(booleans);
    }

    @Override
    public XBooleanValue createBooleanValue(final boolean b) {
        return new MemoryBooleanValue(b);
    }

    @Override
    public XBinaryValue createBinaryValue(final byte[] bytes) {
        if(bytes == null) {
			return null;
		}
        return new MemoryBinaryValue(bytes);
    }

    @Override
    public XBinaryValue createBinaryValue(final Collection<Byte> bytes) {
        if(bytes == null) {
			return null;
		}
        return new MemoryBinaryValue(bytes);
    }

    @Override
    public XDoubleListValue createDoubleListValue(final Collection<Double> doubles) {
        if(doubles == null) {
			return null;
		}
        return new MemoryDoubleListValue(doubles);
    }

    @Override
    public XDoubleListValue createDoubleListValue(final double[] doubles) {
        if(doubles == null) {
			return null;
		}
        return new MemoryDoubleListValue(doubles);
    }

    @Override
    public XDoubleValue createDoubleValue(final double d) {
        return new MemoryDoubleValue(d);
    }

    @Override
    public XIdListValue createIdListValue(final Collection<XId> xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdListValue(xids);
    }

    @Override
    public XIdListValue createIdListValue(final XId[] xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdListValue(xids);
    }

    @Override
    public XIdSetValue createIdSetValue(final Collection<XId> xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdSetValue(xids);
    }

    @Override
    public XIdSetValue createIdSetValue(final XId[] xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdSetValue(xids);
    }

    @Override
    public XIdSortedSetValue createIdSortedSetValue(final Collection<XId> xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdSortedSetValue(xids);
    }

    @Override
    public XIdSortedSetValue createIdSortedSetValue(final XId[] xids) {
        if(xids == null) {
			return null;
		}
        return new MemoryIdSortedSetValue(xids);
    }

    @Override
    public XIntegerListValue createIntegerListValue(final Collection<Integer> ints) {
        if(ints == null) {
			return null;
		}
        return new MemoryIntegerListValue(ints);
    }

    @Override
    public XIntegerListValue createIntegerListValue(final int[] ints) {
        if(ints == null) {
			return null;
		}
        return new MemoryIntegerListValue(ints);
    }

    @Override
    public XIntegerValue createIntegerValue(final int i) {
        return new MemoryIntegerValue(i);
    }

    @Override
    public XLongListValue createLongListValue(final Collection<Long> longs) {
        if(longs == null) {
			return null;
		}
        return new MemoryLongListValue(longs);
    }

    @Override
    public XLongListValue createLongListValue(final long[] longs) {
        if(longs == null) {
			return null;
		}
        return new MemoryLongListValue(longs);
    }

    @Override
    public XLongValue createLongValue(final long l) {
        return new MemoryLongValue(l);
    }

    @Override
    public XStringListValue createStringListValue(final Collection<String> strings) {
        if(strings == null) {
			return null;
		}
        return new MemoryStringListValue(strings);
    }

    @Override
    public XStringListValue createStringListValue(final String[] strings) {
        if(strings == null) {
			return null;
		}
        return new MemoryStringListValue(strings);
    }

    @Override
    public XStringSetValue createStringSetValue(final Collection<String> strings) {
        if(strings == null) {
			return null;
		}
        return new MemoryStringSetValue(strings);
    }

    @Override
    public XStringSetValue createStringSetValue(final String[] strings) {
        if(strings == null) {
			return null;
		}
        return new MemoryStringSetValue(strings);
    }

    @Override
    public XStringValue createStringValue(@CanBeNull final String string) {
        if(string == null) {
			return null;
		}
        return new MemoryStringValue(string);
    }
}
