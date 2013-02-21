package org.xydra.oo.testgen.alltypes.shared;

import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import java.util.List;
import org.xydra.base.value.XDoubleValue;
import org.xydra.oo.Field;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.XAddress;
import org.xydra.base.IHasXID;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XStringValue;
import java.util.SortedSet;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XIDListValue;
import java.util.Set;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.XID;

/** 
 * Generated on Thu Feb 21 21:10:34 CET 2013 by  
 * PersistenceSpec2InterfacesGenerator, a part of Xydra.org  
 */
public interface IHasAllType extends IHasXID {

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("bestFriends")
    SortedSet<IPerson> bestFriends();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("friends")
    Set<IPerson> friends();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jBooleanarray")
    Boolean[] jBooleanarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jBoolean")
    Boolean getJBoolean();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jBoolean the value to set 
     */
    @Field("jBoolean")
    void setJBoolean(Boolean jBoolean);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jDoublearray")
    Double[] jDoublearray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jDouble")
    Double getJDouble();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jDouble the value to set 
     */
    @Field("jDouble")
    void setJDouble(Double jDouble);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jIntegerarray")
    Integer[] jIntegerarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jInteger")
    Integer getJInteger();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jInteger the value to set 
     */
    @Field("jInteger")
    void setJInteger(Integer jInteger);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jLongarray")
    Long[] jLongarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jLong")
    Long getJLong();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jLong the value to set 
     */
    @Field("jLong")
    void setJLong(Long jLong);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jString")
    String getJString();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jString the value to set 
     */
    @Field("jString")
    void setJString(String jString);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jaddressarray")
    XAddress[] jaddressarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jaddresset")
    Set<XAddress> jaddresset();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jaddresslist")
    List<XAddress> jaddresslist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jaddresssortedset")
    SortedSet<XAddress> jaddresssortedset();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jbinary")
    byte[] jbinary();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jbooleanarray")
    boolean[] jbooleanarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jbooleanlist")
    List<Boolean> jbooleanlist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jboolean")
    boolean getJboolean();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jboolean the value to set 
     */
    @Field("jboolean")
    void setJboolean(boolean jboolean);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jdoublearray")
    double[] jdoublearray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jdoublelist")
    List<Double> jdoublelist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jdouble")
    double getJdouble();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jdouble the value to set 
     */
    @Field("jdouble")
    void setJdouble(double jdouble);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jiddarray")
    XID[] jiddarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jintarray")
    int[] jintarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jintegerlist")
    List<Integer> jintegerlist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jint")
    int getJint();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jint the value to set 
     */
    @Field("jint")
    void setJint(int jint);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jlongarray")
    long[] jlongarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jlonglist")
    List<Long> jlonglist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("jlong")
    long getJlong();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param jlong the value to set 
     */
    @Field("jlong")
    void setJlong(long jlong);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jstringarray")
    String[] jstringarray();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jstringlist")
    List<String> jstringlist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jstringset")
    Set<String> jstringset();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jxidlist")
    List<XID> jxidlist();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jxidset")
    Set<XID> jxidset();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("jxidsortedset")
    SortedSet<XID> jxidsortedset();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("members")
    IPerson[] members();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return a writable collection proxy, never null 
     */
    @Field("nextBirthdays")
    List<IPerson> nextBirthdays();

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("partner")
    IPerson getPartner();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param partner the value to set 
     */
    @Field("partner")
    void setPartner(IPerson partner);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xaddresslist")
    XAddressListValue getXaddresslist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xaddresslist the value to set 
     */
    @Field("xaddresslist")
    void setXaddresslist(XAddressListValue xaddresslist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xaddress")
    XAddress getXaddress();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xaddress the value to set 
     */
    @Field("xaddress")
    void setXaddress(XAddress xaddress);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xaddressset")
    XAddressSetValue getXaddressset();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xaddressset the value to set 
     */
    @Field("xaddressset")
    void setXaddressset(XAddressSetValue xaddressset);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xaddresssortedset")
    XAddressSortedSetValue getXaddresssortedset();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xaddresssortedset the value to set 
     */
    @Field("xaddresssortedset")
    void setXaddresssortedset(XAddressSortedSetValue xaddresssortedset);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xbinary")
    XBinaryValue getXbinary();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xbinary the value to set 
     */
    @Field("xbinary")
    void setXbinary(XBinaryValue xbinary);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xbooleanlist")
    XBooleanListValue getXbooleanlist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xbooleanlist the value to set 
     */
    @Field("xbooleanlist")
    void setXbooleanlist(XBooleanListValue xbooleanlist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xboolean")
    XBooleanValue getXboolean();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xboolean the value to set 
     */
    @Field("xboolean")
    void setXboolean(XBooleanValue xboolean);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xdoublelist")
    XDoubleListValue getXdoublelist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xdoublelist the value to set 
     */
    @Field("xdoublelist")
    void setXdoublelist(XDoubleListValue xdoublelist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xdouble")
    XDoubleValue getXdouble();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xdouble the value to set 
     */
    @Field("xdouble")
    void setXdouble(XDoubleValue xdouble);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xidlist")
    XIDListValue getXidlist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xidlist the value to set 
     */
    @Field("xidlist")
    void setXidlist(XIDListValue xidlist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xid")
    XID getXid();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xid the value to set 
     */
    @Field("xid")
    void setXid(XID xid);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xidset")
    XIDSetValue getXidset();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xidset the value to set 
     */
    @Field("xidset")
    void setXidset(XIDSetValue xidset);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xidsortedset")
    XIDSortedSetValue getXidsortedset();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xidsortedset the value to set 
     */
    @Field("xidsortedset")
    void setXidsortedset(XIDSortedSetValue xidsortedset);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xintegerlist")
    XIntegerListValue getXintegerlist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xintegerlist the value to set 
     */
    @Field("xintegerlist")
    void setXintegerlist(XIntegerListValue xintegerlist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xinteger")
    XIntegerValue getXinteger();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xinteger the value to set 
     */
    @Field("xinteger")
    void setXinteger(XIntegerValue xinteger);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xlonglist")
    XLongListValue getXlonglist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xlonglist the value to set 
     */
    @Field("xlonglist")
    void setXlonglist(XLongListValue xlonglist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xlong")
    XLongValue getXlong();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xlong the value to set 
     */
    @Field("xlong")
    void setXlong(XLongValue xlong);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xstringlist")
    XStringListValue getXstringlist();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xstringlist the value to set 
     */
    @Field("xstringlist")
    void setXstringlist(XStringListValue xstringlist);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xstring")
    XStringValue getXstring();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xstring the value to set 
     */
    @Field("xstring")
    void setXstring(XStringValue xstring);

    /** 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @return the current value or null if not defined 
     */
    @Field("xstringset")
    XStringSetValue getXstringset();

    /** 
     * Set a value, silently overwriting existing values, if any. 
     * Generated from org.xydra.oo.testspecs.AllTypesSpec.HasAllType. 
     *  
     * @param xstringset the value to set 
     */
    @Field("xstringset")
    void setXstringset(XStringSetValue xstringset);

}
