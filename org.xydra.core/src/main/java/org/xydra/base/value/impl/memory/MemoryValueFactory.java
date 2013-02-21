package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.annotations.CanBeNull;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
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
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryValueFactory implements XValueFactory {
    
    @Override
    public XAddressListValue createAddressListValue(Collection<XAddress> addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressListValue(addresses);
    }
    
    @Override
    public XAddressListValue createAddressListValue(XAddress[] addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressListValue(addresses);
    }
    
    @Override
    public XAddressSetValue createAddressSetValue(Collection<XAddress> addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressSetValue(addresses);
    }
    
    @Override
    public XAddressSetValue createAddressSetValue(XAddress[] addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressSetValue(addresses);
    }
    
    @Override
    public XAddressSortedSetValue createAddressSortedSetValue(Collection<XAddress> addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressSortedSetValue(addresses);
    }
    
    @Override
    public XAddressSortedSetValue createAddressSortedSetValue(XAddress[] addresses) {
        if(addresses == null)
            return null;
        return new MemoryAddressSortedSetValue(addresses);
    }
    
    @Override
    public XBooleanListValue createBooleanListValue(boolean[] booleans) {
        if(booleans == null)
            return null;
        return new MemoryBooleanListValue(booleans);
    }
    
    @Override
    public XBooleanListValue createBooleanListValue(Collection<Boolean> booleans) {
        if(booleans == null)
            return null;
        return new MemoryBooleanListValue(booleans);
    }
    
    @Override
    public XBooleanValue createBooleanValue(boolean b) {
        return new MemoryBooleanValue(b);
    }
    
    @Override
    public XBinaryValue createBinaryValue(byte[] bytes) {
        if(bytes == null)
            return null;
        return new MemoryBinaryValue(bytes);
    }
    
    @Override
    public XBinaryValue createBinaryValue(Collection<Byte> bytes) {
        if(bytes == null)
            return null;
        return new MemoryBinaryValue(bytes);
    }
    
    @Override
    public XDoubleListValue createDoubleListValue(Collection<Double> doubles) {
        if(doubles == null)
            return null;
        return new MemoryDoubleListValue(doubles);
    }
    
    @Override
    public XDoubleListValue createDoubleListValue(double[] doubles) {
        if(doubles == null)
            return null;
        return new MemoryDoubleListValue(doubles);
    }
    
    @Override
    public XDoubleValue createDoubleValue(double d) {
        return new MemoryDoubleValue(d);
    }
    
    @Override
    public XIDListValue createIDListValue(Collection<XID> xids) {
        if(xids == null)
            return null;
        return new MemoryIDListValue(xids);
    }
    
    @Override
    public XIDListValue createIDListValue(XID[] xids) {
        if(xids == null)
            return null;
        return new MemoryIDListValue(xids);
    }
    
    @Override
    public XIDSetValue createIDSetValue(Collection<XID> xids) {
        if(xids == null)
            return null;
        return new MemoryIDSetValue(xids);
    }
    
    @Override
    public XIDSetValue createIDSetValue(XID[] xids) {
        if(xids == null)
            return null;
        return new MemoryIDSetValue(xids);
    }
    
    @Override
    public XIDSortedSetValue createIDSortedSetValue(Collection<XID> xids) {
        if(xids == null)
            return null;
        return new MemoryIDSortedSetValue(xids);
    }
    
    @Override
    public XIDSortedSetValue createIDSortedSetValue(XID[] xids) {
        if(xids == null)
            return null;
        return new MemoryIDSortedSetValue(xids);
    }
    
    @Override
    public XIntegerListValue createIntegerListValue(Collection<Integer> ints) {
        if(ints == null)
            return null;
        return new MemoryIntegerListValue(ints);
    }
    
    @Override
    public XIntegerListValue createIntegerListValue(int[] ints) {
        if(ints == null)
            return null;
        return new MemoryIntegerListValue(ints);
    }
    
    @Override
    public XIntegerValue createIntegerValue(int i) {
        return new MemoryIntegerValue(i);
    }
    
    @Override
    public XLongListValue createLongListValue(Collection<Long> longs) {
        if(longs == null)
            return null;
        return new MemoryLongListValue(longs);
    }
    
    @Override
    public XLongListValue createLongListValue(long[] longs) {
        if(longs == null)
            return null;
        return new MemoryLongListValue(longs);
    }
    
    @Override
    public XLongValue createLongValue(long l) {
        return new MemoryLongValue(l);
    }
    
    @Override
    public XStringListValue createStringListValue(Collection<String> strings) {
        if(strings == null)
            return null;
        return new MemoryStringListValue(strings);
    }
    
    @Override
    public XStringListValue createStringListValue(String[] strings) {
        if(strings == null)
            return null;
        return new MemoryStringListValue(strings);
    }
    
    @Override
    public XStringSetValue createStringSetValue(Collection<String> strings) {
        if(strings == null)
            return null;
        return new MemoryStringSetValue(strings);
    }
    
    @Override
    public XStringSetValue createStringSetValue(String[] strings) {
        if(strings == null)
            return null;
        return new MemoryStringSetValue(strings);
    }
    
    @Override
    public XStringValue createStringValue(@CanBeNull String string) {
        if(string == null)
            return null;
        return new MemoryStringValue(string);
    }
}
