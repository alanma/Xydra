package org.xydra.oo.testgen.alltypes.shared;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.xydra.base.IHasXId;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
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
import org.xydra.oo.Field;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;
import org.xydra.oo.testtypes.Colors;
import org.xydra.oo.testtypes.MyLongBasedType;

/** Generated on Tue Oct 21 22:14:27 CEST 2014 by SpecWriter, a part of xydra.org:oo */
public interface IHasAllType extends IHasXId {

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("bestFriends")
    SortedSet<IPerson> bestFriends();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("friends")
    Set<IPerson> friends();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("color")
    Colors getColor();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jBoolean")
    Boolean getJBoolean();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jBooleanarray")
    boolean[] getJBooleanarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jDouble")
    Double getJDouble();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jDoublearray")
    double[] getJDoublearray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jInteger")
    Integer getJInteger();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jIntegerarray")
    int[] getJIntegerarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jLong")
    Long getJLong();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jLongarray")
    long[] getJLongarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jString")
    String getJString();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jaddressarray")
    XAddress[] getJaddressarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jbinary")
    byte[] getJbinary();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jboolean")
    boolean getJboolean();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jbooleanarray")
    boolean[] getJbooleanarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jdouble")
    double getJdouble();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jdoublearray")
    double[] getJdoublearray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jiddarray")
    XId[] getJiddarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jint")
    int getJint();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jintarray")
    int[] getJintarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jlong")
    long getJlong();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jlongarray")
    long[] getJlongarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("jstringarray")
    String[] getJstringarray();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("myLongBasedType")
    MyLongBasedType getMyLongBasedType();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("partner")
    IPerson getPartner();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xaddress")
    XAddress getXaddress();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xaddresslist")
    XAddressListValue getXaddresslist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xaddressset")
    XAddressSetValue getXaddressset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xaddresssortedset")
    XAddressSortedSetValue getXaddresssortedset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xbinary")
    XBinaryValue getXbinary();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xboolean")
    XBooleanValue getXboolean();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xbooleanlist")
    XBooleanListValue getXbooleanlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xdouble")
    XDoubleValue getXdouble();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xdoublelist")
    XDoubleListValue getXdoublelist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xid")
    XId getXid();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xidlist")
    XIdListValue getXidlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xidset")
    XIdSetValue getXidset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xidsortedset")
    XIdSortedSetValue getXidsortedset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xinteger")
    XIntegerValue getXinteger();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xintegerlist")
    XIntegerListValue getXintegerlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xlong")
    XLongValue getXlong();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xlonglist")
    XLongListValue getXlonglist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xstring")
    XStringValue getXstring();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xstringlist")
    XStringListValue getXstringlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return the current value or null if not defined
     */
    @Field("xstringset")
    XStringSetValue getXstringset();

    /**
     * For GWT-internal use only [generated from: 'toClassSpec 1']
     *
     * @param model  [generated from: 'toClassSpec 2']
     * @param id  [generated from: 'toClassSpec 3']
     */
    void init(XWritableModel model, XId id);

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jaddresset")
    Set<XAddress> jaddresset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jaddresslist")
    List<XAddress> jaddresslist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jaddresssortedset")
    SortedSet<XAddress> jaddresssortedset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jbooleanlist")
    List<Boolean> jbooleanlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jdoublelist")
    List<Double> jdoublelist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jintegerlist")
    List<Integer> jintegerlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jlonglist")
    List<Long> jlonglist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jstringlist")
    List<String> jstringlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jstringset")
    Set<String> jstringset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jxidlist")
    List<XId> jxidlist();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jxidset")
    Set<XId> jxidset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("jxidsortedset")
    SortedSet<XId> jxidsortedset();

    /**
     *  [generated from: 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @return a writable collection proxy, never null
     */
    @Field("nextBirthdays")
    List<IPerson> nextBirthdays();

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param color the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("color")
    IHasAllType setColor(Colors color);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jBooleanarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jBooleanarray")
    IHasAllType setJBooleanarray(boolean[] jBooleanarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jBoolean the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jBoolean")
    IHasAllType setJBoolean(Boolean jBoolean);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jDoublearray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jDoublearray")
    IHasAllType setJDoublearray(double[] jDoublearray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jDouble the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jDouble")
    IHasAllType setJDouble(Double jDouble);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jIntegerarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jIntegerarray")
    IHasAllType setJIntegerarray(int[] jIntegerarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jInteger the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jInteger")
    IHasAllType setJInteger(Integer jInteger);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jLongarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jLongarray")
    IHasAllType setJLongarray(long[] jLongarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jLong the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jLong")
    IHasAllType setJLong(Long jLong);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jString the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jString")
    IHasAllType setJString(String jString);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jaddressarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jaddressarray")
    IHasAllType setJaddressarray(XAddress[] jaddressarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jbinary the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jbinary")
    IHasAllType setJbinary(byte[] jbinary);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jbooleanarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jbooleanarray")
    IHasAllType setJbooleanarray(boolean[] jbooleanarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jboolean the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jboolean")
    IHasAllType setJboolean(boolean jboolean);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jdoublearray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jdoublearray")
    IHasAllType setJdoublearray(double[] jdoublearray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jdouble the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jdouble")
    IHasAllType setJdouble(double jdouble);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jiddarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jiddarray")
    IHasAllType setJiddarray(XId[] jiddarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jintarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jintarray")
    IHasAllType setJintarray(int[] jintarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jint the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jint")
    IHasAllType setJint(int jint);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jlongarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jlongarray")
    IHasAllType setJlongarray(long[] jlongarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jlong the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jlong")
    IHasAllType setJlong(long jlong);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param jstringarray the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("jstringarray")
    IHasAllType setJstringarray(String[] jstringarray);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param myLongBasedType the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("myLongBasedType")
    IHasAllType setMyLongBasedType(MyLongBasedType myLongBasedType);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param partner the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("partner")
    IHasAllType setPartner(IPerson partner);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xaddresslist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xaddresslist")
    IHasAllType setXaddresslist(XAddressListValue xaddresslist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xaddressset the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xaddressset")
    IHasAllType setXaddressset(XAddressSetValue xaddressset);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xaddresssortedset the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xaddresssortedset")
    IHasAllType setXaddresssortedset(XAddressSortedSetValue xaddresssortedset);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xaddress the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xaddress")
    IHasAllType setXaddress(XAddress xaddress);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xbinary the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xbinary")
    IHasAllType setXbinary(XBinaryValue xbinary);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xbooleanlist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xbooleanlist")
    IHasAllType setXbooleanlist(XBooleanListValue xbooleanlist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xboolean the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xboolean")
    IHasAllType setXboolean(XBooleanValue xboolean);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xdoublelist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xdoublelist")
    IHasAllType setXdoublelist(XDoubleListValue xdoublelist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xdouble the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xdouble")
    IHasAllType setXdouble(XDoubleValue xdouble);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xidlist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xidlist")
    IHasAllType setXidlist(XIdListValue xidlist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xidset the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xidset")
    IHasAllType setXidset(XIdSetValue xidset);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xidsortedset the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xidsortedset")
    IHasAllType setXidsortedset(XIdSortedSetValue xidsortedset);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xid the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xid")
    IHasAllType setXid(XId xid);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xintegerlist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xintegerlist")
    IHasAllType setXintegerlist(XIntegerListValue xintegerlist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xinteger the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xinteger")
    IHasAllType setXinteger(XIntegerValue xinteger);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xlonglist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xlonglist")
    IHasAllType setXlonglist(XLongListValue xlonglist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xlong the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xlong")
    IHasAllType setXlong(XLongValue xlong);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xstringlist the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xstringlist")
    IHasAllType setXstringlist(XStringListValue xstringlist);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xstringset the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xstringset")
    IHasAllType setXstringset(XStringSetValue xstringset);

    /**
     * Set a value, silently overwriting existing values, if any. [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     *
     * @param xstring the value to set [generated from:
     * 'org.xydra.oo.testspecs.AllTypesSpec.HasAllType']
     * @return ...
     */
    @Field("xstring")
    IHasAllType setXstring(XStringValue xstring);

}
