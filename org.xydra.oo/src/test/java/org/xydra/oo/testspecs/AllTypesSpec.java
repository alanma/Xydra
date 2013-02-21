package org.xydra.oo.testspecs;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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


public class AllTypesSpec {
    
    abstract class Person {
        String name;
        int age;
    }
    
    abstract class HasAllType {
        
        // simple types
        
        XAddress xaddress;
        
        XAddressListValue xaddresslist;
        List<XAddress> jaddresslist;
        XAddress[] jaddressarray;
        
        XAddressSetValue xaddressset;
        Set<XAddress> jaddresset;
        XAddressSortedSetValue xaddresssortedset;
        SortedSet<XAddress> jaddresssortedset;
        
        XBooleanListValue xbooleanlist;
        List<Boolean> jbooleanlist;
        Boolean[] jBooleanarray;
        boolean[] jbooleanarray;
        
        XBooleanValue xboolean;
        Boolean jBoolean;
        boolean jboolean;
        
        XBinaryValue xbinary;
        byte[] jbinary;
        
        XDoubleListValue xdoublelist;
        List<Double> jdoublelist;
        Double[] jDoublearray;
        double[] jdoublearray;
        
        XDoubleValue xdouble;
        Double jDouble;
        double jdouble;
        
        XID xid;
        
        XIDListValue xidlist;
        List<XID> jxidlist;
        XID[] jiddarray;
        
        XIDSetValue xidset;
        Set<XID> jxidset;
        
        XIDSortedSetValue xidsortedset;
        SortedSet<XID> jxidsortedset;
        
        XIntegerListValue xintegerlist;
        List<Integer> jintegerlist;
        Integer[] jIntegerarray;
        int[] jintarray;
        
        XIntegerValue xinteger;
        Integer jInteger;
        int jint;
        
        XLongListValue xlonglist;
        List<Long> jlonglist;
        Long[] jLongarray;
        long[] jlongarray;
        
        XLongValue xlong;
        Long jLong;
        long jlong;
        
        XStringListValue xstringlist;
        List<String> jstringlist;
        String[] jstringarray;
        
        XStringSetValue xstringset;
        Set<String> jstringset;
        
        XStringValue xstring;
        String jString;
        
        // derrived types
        
        // TODO add new invented types
        
        Person partner;
        Set<Person> friends;
        List<Person> nextBirthdays;
        SortedSet<Person> bestFriends;
        Person[] members;
        
    }
    
}
