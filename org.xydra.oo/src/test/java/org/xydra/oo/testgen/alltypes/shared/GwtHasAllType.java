package org.xydra.oo.testgen.alltypes.shared;

import java.lang.Boolean;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
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
import org.xydra.oo.runtime.shared.CollectionProxy;
import org.xydra.oo.runtime.shared.CollectionProxy.ITransformer;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.runtime.shared.SetProxy;
import org.xydra.oo.runtime.shared.SharedTypeMapping;
import org.xydra.oo.runtime.shared.SortedSetProxy;
import org.xydra.oo.runtime.shared.TypeSpec;
import org.xydra.oo.testgen.alltypes.client.GwtFactory;
import org.xydra.oo.testgen.alltypes.shared.Colors;
import org.xydra.oo.testgen.alltypes.shared.IPerson;
import org.xydra.oo.testgen.alltypes.shared.MyLongBasedType;

/** Generated on Thu Mar 07 18:40:49 CET 2013 by SpecWriter, a part of xydra.org:oo */
public class GwtHasAllType extends GwtXydraMapped {

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    SortedSet<IPerson> bestFriends() {
        ITransformer<XIdListValue,XId,SortedSet<IPerson>,IPerson> t = new CollectionProxy.ITransformer<XIdListValue,XId,SortedSet<IPerson>,IPerson>() {
            @Override
            public IPerson toJavaComponent(XId x) {
                return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), (XId) x);
            }
        
            @Override
            public XId toXydraComponent(IPerson javaType) {
                return javaType.getId();
            }
        
            @Override
            public XIdListValue createCollection() {
                return XV.toIdListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SortedSetProxy<XIdListValue,XId,SortedSet<IPerson>,IPerson>(this.oop.getXObject(), XX.toId("bestFriends"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Set<IPerson> friends() {
        ITransformer<XIdListValue,XId,Set<IPerson>,IPerson> t = new CollectionProxy.ITransformer<XIdListValue,XId,Set<IPerson>,IPerson>() {
            @Override
            public IPerson toJavaComponent(XId x) {
                return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), (XId) x);
            }
        
            @Override
            public XId toXydraComponent(IPerson javaType) {
                return javaType.getId();
            }
        
            @Override
            public XIdListValue createCollection() {
                return XV.toIdListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SetProxy<XIdListValue,XId,Set<IPerson>,IPerson>(this.oop.getXObject(), XX.toId("friends"), t);
    }

    /** 
     * Auto-convert enum to XStringValue [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Colors getColor() {
        String s = XValueJavaUtils.getString(this.oop.getXObject(), XX.toId("color"));
        if(s == null)
          return null;
        return Colors.valueOf(s);
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Boolean getJBoolean() {
        XBooleanValue x = ((XBooleanValue)this.oop.getValue("jBoolean"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("java.lang", "Boolean"), null, "gwt"));
        return (Boolean)mapping.toJava(x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    boolean[] getJBooleanarray() {
        return XValueJavaUtils.getBooleanArray(this.oop.getXObject(), XX.toId("jBooleanarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Double getJDouble() {
        XDoubleValue x = ((XDoubleValue)this.oop.getValue("jDouble"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("java.lang", "Double"), null, "gwt"));
        return (Double)mapping.toJava(x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    double[] getJDoublearray() {
        return XValueJavaUtils.getDoubleArray(this.oop.getXObject(), XX.toId("jDoublearray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Integer getJInteger() {
        XIntegerValue x = ((XIntegerValue)this.oop.getValue("jInteger"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("java.lang", "Integer"), null, "gwt"));
        return (Integer)mapping.toJava(x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    int[] getJIntegerarray() {
        return XValueJavaUtils.getIntegerArray(this.oop.getXObject(), XX.toId("jIntegerarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Long getJLong() {
        XLongValue x = ((XLongValue)this.oop.getValue("jLong"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("java.lang", "Long"), null, "gwt"));
        return (Long)mapping.toJava(x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    long[] getJLongarray() {
        return XValueJavaUtils.getLongArray(this.oop.getXObject(), XX.toId("jLongarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    String getJString() {
        XStringValue x = ((XStringValue)this.oop.getValue("jString"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("java.lang", "String"), null, "gwt"));
        return (String)mapping.toJava(x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XAddress[] getJaddressarray() {
        return XValueJavaUtils.getAddressArray(this.oop.getXObject(), XX.toId("jaddressarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    byte[] getJbinary() {
        XBinaryValue x = ((XBinaryValue)this.oop.getValue("jbinary"));
        if(x == null)
        // byte[]
            return null;
        return x.contents();
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    boolean getJboolean() {
        XBooleanValue x = ((XBooleanValue)this.oop.getValue("jboolean"));
        if(x == null)
        // Java primitive type
            return false;
        return x.contents();
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    boolean[] getJbooleanarray() {
        return XValueJavaUtils.getBooleanArray(this.oop.getXObject(), XX.toId("jbooleanarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    double getJdouble() {
        XDoubleValue x = ((XDoubleValue)this.oop.getValue("jdouble"));
        if(x == null)
        // Java primitive type
            return 0d;
        return x.contents();
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    double[] getJdoublearray() {
        return XValueJavaUtils.getDoubleArray(this.oop.getXObject(), XX.toId("jdoublearray"));
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XId[] getJiddarray() {
        return XValueJavaUtils.getIdArray(this.oop.getXObject(), XX.toId("jiddarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    int getJint() {
        XIntegerValue x = ((XIntegerValue)this.oop.getValue("jint"));
        if(x == null)
        // Java primitive type
            return 0;
        return x.contents();
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    int[] getJintarray() {
        return XValueJavaUtils.getIntegerArray(this.oop.getXObject(), XX.toId("jintarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    long getJlong() {
        XLongValue x = ((XLongValue)this.oop.getValue("jlong"));
        if(x == null)
        // Java primitive type
            return 0l;
        return x.contents();
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    long[] getJlongarray() {
        return XValueJavaUtils.getLongArray(this.oop.getXObject(), XX.toId("jlongarray"));
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    String[] getJstringarray() {
        return XValueJavaUtils.getStringArray(this.oop.getXObject(), XX.toId("jstringarray"));
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    MyLongBasedType getMyLongBasedType() {
        XLongValue x = ((XLongValue)this.oop.getValue("myLongBasedType"));
        if(x == null)
            return null;
        // Extended types with a mapping
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec("org.xydra.oo.testgen.alltypes.shared", "MyLongBasedType"), null, "gwt"));
        return (MyLongBasedType)mapping.toJava(x);
    }

    /** 
     * Proxy type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    IPerson getPartner() {
        XId id = XValueJavaUtils.getId(this.oop.getXObject(), XX.toId("partner"));
        if(id == null)
            return null;
        return GwtFactory.wrapPerson(this.oop.getXModel(), id);
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XAddress getXaddress() {
        XAddress x = ((XAddress)this.oop.getValue("xaddress"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XAddressListValue getXaddresslist() {
        XAddressListValue x = ((XAddressListValue)this.oop.getValue("xaddresslist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XAddressSetValue getXaddressset() {
        XAddressSetValue x = ((XAddressSetValue)this.oop.getValue("xaddressset"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XAddressSortedSetValue getXaddresssortedset() {
        XAddressSortedSetValue x = ((XAddressSortedSetValue)this.oop.getValue("xaddresssortedset"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XBinaryValue getXbinary() {
        XBinaryValue x = ((XBinaryValue)this.oop.getValue("xbinary"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XBooleanValue getXboolean() {
        XBooleanValue x = ((XBooleanValue)this.oop.getValue("xboolean"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XBooleanListValue getXbooleanlist() {
        XBooleanListValue x = ((XBooleanListValue)this.oop.getValue("xbooleanlist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XDoubleValue getXdouble() {
        XDoubleValue x = ((XDoubleValue)this.oop.getValue("xdouble"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XDoubleListValue getXdoublelist() {
        XDoubleListValue x = ((XDoubleListValue)this.oop.getValue("xdoublelist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XId getXid() {
        XId x = ((XId)this.oop.getValue("xid"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XIdListValue getXidlist() {
        XIdListValue x = ((XIdListValue)this.oop.getValue("xidlist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XIdSetValue getXidset() {
        XIdSetValue x = ((XIdSetValue)this.oop.getValue("xidset"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XIdSortedSetValue getXidsortedset() {
        XIdSortedSetValue x = ((XIdSortedSetValue)this.oop.getValue("xidsortedset"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XIntegerValue getXinteger() {
        XIntegerValue x = ((XIntegerValue)this.oop.getValue("xinteger"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XIntegerListValue getXintegerlist() {
        XIntegerListValue x = ((XIntegerListValue)this.oop.getValue("xintegerlist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XLongValue getXlong() {
        XLongValue x = ((XLongValue)this.oop.getValue("xlong"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XLongListValue getXlonglist() {
        XLongListValue x = ((XLongListValue)this.oop.getValue("xlonglist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XStringValue getXstring() {
        XStringValue x = ((XStringValue)this.oop.getValue("xstring"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XStringListValue getXstringlist() {
        XStringListValue x = ((XStringListValue)this.oop.getValue("xstringlist"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     * Mapped Xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    XStringSetValue getXstringset() {
        XStringSetValue x = ((XStringSetValue)this.oop.getValue("xstringset"));
        if(x == null)
            return null;
        // Xydra value type
        return x;
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Set<XAddress> jaddresset() {
        ITransformer<XAddressSetValue,XAddress,Set<XAddress>,XAddress> t = new CollectionProxy.ITransformer<XAddressSetValue,XAddress,Set<XAddress>,XAddress>() {
            @Override
            public XAddress toJavaComponent(XAddress x) {
                return x;
            }
        
            @Override
            public XAddress toXydraComponent(XAddress javaType) {
                return javaType;
            }
        
            @Override
            public XAddressSetValue createCollection() {
                return org.xydra.base.value.XV.toAddressSetValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SetProxy<XAddressSetValue,XAddress,Set<XAddress>,XAddress>(this.oop.getXObject(), XX.toId("jaddresset"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<XAddress> jaddresslist() {
        ITransformer<XAddressListValue,XAddress,List<XAddress>,XAddress> t = new CollectionProxy.ITransformer<XAddressListValue,XAddress,List<XAddress>,XAddress>() {
            @Override
            public XAddress toJavaComponent(XAddress x) {
                return x;
            }
        
            @Override
            public XAddress toXydraComponent(XAddress javaType) {
                return javaType;
            }
        
            @Override
            public XAddressListValue createCollection() {
                return org.xydra.base.value.XV.toAddressListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XAddressListValue,XAddress,List<XAddress>,XAddress>(this.oop.getXObject(), XX.toId("jaddresslist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    SortedSet<XAddress> jaddresssortedset() {
        ITransformer<XAddressSortedSetValue,XAddress,SortedSet<XAddress>,XAddress> t = new CollectionProxy.ITransformer<XAddressSortedSetValue,XAddress,SortedSet<XAddress>,XAddress>() {
            @Override
            public XAddress toJavaComponent(XAddress x) {
                return x;
            }
        
            @Override
            public XAddress toXydraComponent(XAddress javaType) {
                return javaType;
            }
        
            @Override
            public XAddressSortedSetValue createCollection() {
                return org.xydra.base.value.XV.toAddressSortedSetValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SortedSetProxy<XAddressSortedSetValue,XAddress,SortedSet<XAddress>,XAddress>(this.oop.getXObject(), XX.toId("jaddresssortedset"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<Boolean> jbooleanlist() {
        ITransformer<XBooleanListValue,Boolean,List<Boolean>,Boolean> t = new CollectionProxy.ITransformer<XBooleanListValue,Boolean,List<Boolean>,Boolean>() {
            @Override
            public Boolean toJavaComponent(Boolean x) {
                return x;
            }
        
            @Override
            public Boolean toXydraComponent(Boolean javaType) {
                return javaType;
            }
        
            @Override
            public XBooleanListValue createCollection() {
                return org.xydra.base.value.XV.toBooleanListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XBooleanListValue,Boolean,List<Boolean>,Boolean>(this.oop.getXObject(), XX.toId("jbooleanlist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<Double> jdoublelist() {
        ITransformer<XDoubleListValue,Double,List<Double>,Double> t = new CollectionProxy.ITransformer<XDoubleListValue,Double,List<Double>,Double>() {
            @Override
            public Double toJavaComponent(Double x) {
                return x;
            }
        
            @Override
            public Double toXydraComponent(Double javaType) {
                return javaType;
            }
        
            @Override
            public XDoubleListValue createCollection() {
                return org.xydra.base.value.XV.toDoubleListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XDoubleListValue,Double,List<Double>,Double>(this.oop.getXObject(), XX.toId("jdoublelist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<Integer> jintegerlist() {
        ITransformer<XIntegerListValue,Integer,List<Integer>,Integer> t = new CollectionProxy.ITransformer<XIntegerListValue,Integer,List<Integer>,Integer>() {
            @Override
            public Integer toJavaComponent(Integer x) {
                return x;
            }
        
            @Override
            public Integer toXydraComponent(Integer javaType) {
                return javaType;
            }
        
            @Override
            public XIntegerListValue createCollection() {
                return org.xydra.base.value.XV.toIntegerListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XIntegerListValue,Integer,List<Integer>,Integer>(this.oop.getXObject(), XX.toId("jintegerlist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<Long> jlonglist() {
        ITransformer<XLongListValue,Long,List<Long>,Long> t = new CollectionProxy.ITransformer<XLongListValue,Long,List<Long>,Long>() {
            @Override
            public Long toJavaComponent(Long x) {
                return x;
            }
        
            @Override
            public Long toXydraComponent(Long javaType) {
                return javaType;
            }
        
            @Override
            public XLongListValue createCollection() {
                return org.xydra.base.value.XV.toLongListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XLongListValue,Long,List<Long>,Long>(this.oop.getXObject(), XX.toId("jlonglist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<String> jstringlist() {
        ITransformer<XStringListValue,String,List<String>,String> t = new CollectionProxy.ITransformer<XStringListValue,String,List<String>,String>() {
            @Override
            public String toJavaComponent(String x) {
                return x;
            }
        
            @Override
            public String toXydraComponent(String javaType) {
                return javaType;
            }
        
            @Override
            public XStringListValue createCollection() {
                return org.xydra.base.value.XV.toStringListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XStringListValue,String,List<String>,String>(this.oop.getXObject(), XX.toId("jstringlist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Set<String> jstringset() {
        ITransformer<XStringSetValue,String,Set<String>,String> t = new CollectionProxy.ITransformer<XStringSetValue,String,Set<String>,String>() {
            @Override
            public String toJavaComponent(String x) {
                return x;
            }
        
            @Override
            public String toXydraComponent(String javaType) {
                return javaType;
            }
        
            @Override
            public XStringSetValue createCollection() {
                return org.xydra.base.value.XV.toStringSetValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SetProxy<XStringSetValue,String,Set<String>,String>(this.oop.getXObject(), XX.toId("jstringset"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<XId> jxidlist() {
        ITransformer<XIdListValue,XId,List<XId>,XId> t = new CollectionProxy.ITransformer<XIdListValue,XId,List<XId>,XId>() {
            @Override
            public XId toJavaComponent(XId x) {
                return x;
            }
        
            @Override
            public XId toXydraComponent(XId javaType) {
                return javaType;
            }
        
            @Override
            public XIdListValue createCollection() {
                return org.xydra.base.value.XV.toIdListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XIdListValue,XId,List<XId>,XId>(this.oop.getXObject(), XX.toId("jxidlist"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    Set<XId> jxidset() {
        ITransformer<XIdSetValue,XId,Set<XId>,XId> t = new CollectionProxy.ITransformer<XIdSetValue,XId,Set<XId>,XId>() {
            @Override
            public XId toJavaComponent(XId x) {
                return x;
            }
        
            @Override
            public XId toXydraComponent(XId javaType) {
                return javaType;
            }
        
            @Override
            public XIdSetValue createCollection() {
                return org.xydra.base.value.XV.toIdSetValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SetProxy<XIdSetValue,XId,Set<XId>,XId>(this.oop.getXObject(), XX.toId("jxidset"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    SortedSet<XId> jxidsortedset() {
        ITransformer<XIdSortedSetValue,XId,SortedSet<XId>,XId> t = new CollectionProxy.ITransformer<XIdSortedSetValue,XId,SortedSet<XId>,XId>() {
            @Override
            public XId toJavaComponent(XId x) {
                return x;
            }
        
            @Override
            public XId toXydraComponent(XId javaType) {
                return javaType;
            }
        
            @Override
            public XIdSortedSetValue createCollection() {
                return org.xydra.base.value.XV.toIdSortedSetValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new SortedSetProxy<XIdSortedSetValue,XId,SortedSet<XId>,XId>(this.oop.getXObject(), XX.toId("jxidsortedset"), t);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @return ... 
     */
    List<IPerson> nextBirthdays() {
        ITransformer<XIdListValue,XId,List<IPerson>,IPerson> t = new CollectionProxy.ITransformer<XIdListValue,XId,List<IPerson>,IPerson>() {
            @Override
            public IPerson toJavaComponent(XId x) {
                return GwtFactory.wrapPerson(GwtHasAllType.this.oop.getXModel(), (XId) x);
            }
        
            @Override
            public XId toXydraComponent(IPerson javaType) {
                return javaType.getId();
            }
        
            @Override
            public XIdListValue createCollection() {
                return XV.toIdListValue(Collections.EMPTY_LIST);
            }
        
        };
            
        return new ListProxy<XIdListValue,XId,List<IPerson>,IPerson>(this.oop.getXObject(), XX.toId("nextBirthdays"), t);
    }

    /** 
     * Enum types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param color  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setColor(Colors color) {
        XValueJavaUtils.setString(this.oop.getXObject(), XX.toId("color"), color.name());
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jBooleanarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJBooleanarray(boolean[] jBooleanarray) {
        XValueJavaUtils.setBooleanArray(this.oop.getXObject(), XX.toId("jBooleanarray"), jBooleanarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jBoolean  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJBoolean(Boolean jBoolean) {
        // non-xydra type with mapping: Boolean
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "Boolean"), null, "gwt"));
        XBooleanValue x = (XBooleanValue)mapping.toXydra(jBoolean);
        this.oop.setValue("jBoolean", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jDoublearray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJDoublearray(double[] jDoublearray) {
        XValueJavaUtils.setDoubleArray(this.oop.getXObject(), XX.toId("jDoublearray"), jDoublearray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jDouble  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJDouble(Double jDouble) {
        // non-xydra type with mapping: Double
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "Double"), null, "gwt"));
        XDoubleValue x = (XDoubleValue)mapping.toXydra(jDouble);
        this.oop.setValue("jDouble", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jIntegerarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJIntegerarray(int[] jIntegerarray) {
        XValueJavaUtils.setIntegerArray(this.oop.getXObject(), XX.toId("jIntegerarray"), jIntegerarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jInteger  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJInteger(Integer jInteger) {
        // non-xydra type with mapping: Integer
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "Integer"), null, "gwt"));
        XIntegerValue x = (XIntegerValue)mapping.toXydra(jInteger);
        this.oop.setValue("jInteger", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jLongarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJLongarray(long[] jLongarray) {
        XValueJavaUtils.setLongArray(this.oop.getXObject(), XX.toId("jLongarray"), jLongarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jLong  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJLong(Long jLong) {
        // non-xydra type with mapping: Long
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "Long"), null, "gwt"));
        XLongValue x = (XLongValue)mapping.toXydra(jLong);
        this.oop.setValue("jLong", x);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jString  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJString(String jString) {
        // non-xydra type with mapping: String
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "String"), null, "gwt"));
        XStringValue x = (XStringValue)mapping.toXydra(jString);
        this.oop.setValue("jString", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jaddressarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJaddressarray(XAddress[] jaddressarray) {
        XValueJavaUtils.setAddressArray(this.oop.getXObject(), XX.toId("jaddressarray"), jaddressarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jbinary  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJbinary(byte[] jbinary) {
        // non-xydra type with mapping: byte[]
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "byte[]"), null, "gwt"));
        XBinaryValue x = (XBinaryValue)mapping.toXydra(jbinary);
        this.oop.setValue("jbinary", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jbooleanarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJbooleanarray(boolean[] jbooleanarray) {
        XValueJavaUtils.setBooleanArray(this.oop.getXObject(), XX.toId("jbooleanarray"), jbooleanarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jboolean  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJboolean(boolean jboolean) {
        // non-xydra type with mapping: boolean
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "boolean"), null, "gwt"));
        XBooleanValue x = (XBooleanValue)mapping.toXydra(jboolean);
        this.oop.setValue("jboolean", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jdoublearray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJdoublearray(double[] jdoublearray) {
        XValueJavaUtils.setDoubleArray(this.oop.getXObject(), XX.toId("jdoublearray"), jdoublearray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jdouble  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJdouble(double jdouble) {
        // non-xydra type with mapping: double
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "double"), null, "gwt"));
        XDoubleValue x = (XDoubleValue)mapping.toXydra(jdouble);
        this.oop.setValue("jdouble", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jiddarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJiddarray(XId[] jiddarray) {
        XValueJavaUtils.setIdArray(this.oop.getXObject(), XX.toId("jiddarray"), jiddarray);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jintarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJintarray(int[] jintarray) {
        XValueJavaUtils.setIntegerArray(this.oop.getXObject(), XX.toId("jintarray"), jintarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jint  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJint(int jint) {
        // non-xydra type with mapping: int
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "int"), null, "gwt"));
        XIntegerValue x = (XIntegerValue)mapping.toXydra(jint);
        this.oop.setValue("jint", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jlongarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJlongarray(long[] jlongarray) {
        XValueJavaUtils.setLongArray(this.oop.getXObject(), XX.toId("jlongarray"), jlongarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jlong  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJlong(long jlong) {
        // non-xydra type with mapping: long
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "long"), null, "gwt"));
        XLongValue x = (XLongValue)mapping.toXydra(jlong);
        this.oop.setValue("jlong", x);
    }

    /** 
     * Java types corresponding to Xydra types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param jstringarray  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setJstringarray(String[] jstringarray) {
        XValueJavaUtils.setStringArray(this.oop.getXObject(), XX.toId("jstringarray"), jstringarray);
    }

    /** 
     *  [generated from: 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param myLongBasedType  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setMyLongBasedType(MyLongBasedType myLongBasedType) {
        // non-xydra type with mapping: MyLongBasedType
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(
          "org.xydra.oo.testgen.alltypes.shared", "MyLongBasedType"), null, "gwt"));
        XLongValue x = (XLongValue)mapping.toXydra(myLongBasedType);
        this.oop.setValue("myLongBasedType", x);
    }

    /** 
     * Proxy types [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param partner  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setPartner(IPerson partner) {
        XValueJavaUtils.setId(this.oop.getXObject(), XX.toId("partner"), partner.getId());
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xaddresslist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXaddresslist(XAddressListValue xaddresslist) {
        this.oop.setValue("xaddresslist", xaddresslist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xaddressset  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXaddressset(XAddressSetValue xaddressset) {
        this.oop.setValue("xaddressset", xaddressset);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xaddresssortedset  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXaddresssortedset(XAddressSortedSetValue xaddresssortedset) {
        this.oop.setValue("xaddresssortedset", xaddresssortedset);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xaddress  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXaddress(XAddress xaddress) {
        this.oop.setValue("xaddress", xaddress);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xbinary  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXbinary(XBinaryValue xbinary) {
        this.oop.setValue("xbinary", xbinary);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xbooleanlist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXbooleanlist(XBooleanListValue xbooleanlist) {
        this.oop.setValue("xbooleanlist", xbooleanlist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xboolean  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXboolean(XBooleanValue xboolean) {
        this.oop.setValue("xboolean", xboolean);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xdoublelist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXdoublelist(XDoubleListValue xdoublelist) {
        this.oop.setValue("xdoublelist", xdoublelist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xdouble  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXdouble(XDoubleValue xdouble) {
        this.oop.setValue("xdouble", xdouble);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xidlist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXidlist(XIdListValue xidlist) {
        this.oop.setValue("xidlist", xidlist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xidset  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXidset(XIdSetValue xidset) {
        this.oop.setValue("xidset", xidset);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xidsortedset  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXidsortedset(XIdSortedSetValue xidsortedset) {
        this.oop.setValue("xidsortedset", xidsortedset);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xid  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXid(XId xid) {
        this.oop.setValue("xid", xid);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xintegerlist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXintegerlist(XIntegerListValue xintegerlist) {
        this.oop.setValue("xintegerlist", xintegerlist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xinteger  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXinteger(XIntegerValue xinteger) {
        this.oop.setValue("xinteger", xinteger);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xlonglist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXlonglist(XLongListValue xlonglist) {
        this.oop.setValue("xlonglist", xlonglist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xlong  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXlong(XLongValue xlong) {
        this.oop.setValue("xlong", xlong);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xstringlist  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXstringlist(XStringListValue xstringlist) {
        this.oop.setValue("xstringlist", xstringlist);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xstringset  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXstringset(XStringSetValue xstringset) {
        this.oop.setValue("xstringset", xstringset);
    }

    /** 
     * Trivial xydra type [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     *  
     * @param xstring  [generated from:  
     * 'org.xydra.oo.testgen.alltypes.shared.IHasAllType'] 
     */
    void setXstring(XStringValue xstring) {
        this.oop.setValue("xstring", xstring);
    }

}
