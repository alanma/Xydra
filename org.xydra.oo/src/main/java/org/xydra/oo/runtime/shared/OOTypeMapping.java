package org.xydra.oo.runtime.shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
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
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.index.query.Pair;


@RunsInGWT(true)
public class OOTypeMapping {
    
    public static interface IFactory {
        /**
         * @return an empty xydra collection
         */
        Object createEmptyCollection();
    }
    
    private static Map<Pair<Class<?>,Class<?>>,OOTypeMapping> mapping = new HashMap<>();
    
    // trivial mappings for built-in Xydra types
    
    public final static OOTypeMapping Address = new OOTypeMapping(XAddress.class, null,
            XAddress.class, null, null);
    
    public final static OOTypeMapping Address_List = new OOTypeMapping(List.class, XAddress.class,
            XAddressListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping AddressList = new OOTypeMapping(XAddressListValue.class,
            null, XAddressListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Address_Set = new OOTypeMapping(Set.class, XAddress.class,
            XAddressSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSetValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping AddressSet = new OOTypeMapping(XAddressSetValue.class, null,
            XAddressSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSetValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Address_SortedSet = new OOTypeMapping(SortedSet.class,
            XAddress.class, XAddressSortedSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSortedSetValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping AddressSortedSet = new OOTypeMapping(
            XAddressSortedSetValue.class, null, XAddressSortedSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSortedSetValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Boolean_List = new OOTypeMapping(List.class, Boolean.class,
            XBooleanListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toBooleanListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping BooleanList = new OOTypeMapping(XBooleanListValue.class,
            null, XBooleanListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toBooleanListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Boolean = new OOTypeMapping(XBooleanValue.class, null,
            XBooleanValue.class, null, null);
    
    public final static OOTypeMapping Binary = new OOTypeMapping(XBinaryValue.class, null,
            XBinaryValue.class, null, null);
    
    public final static OOTypeMapping Double_List = new OOTypeMapping(List.class, Double.class,
            XDoubleListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toDoubleListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping DoubleList = new OOTypeMapping(XDoubleListValue.class, null,
            XDoubleListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toDoubleListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Double = new OOTypeMapping(XDoubleValue.class, null,
            XDoubleValue.class, null, null);
    
    public final static OOTypeMapping Id = new OOTypeMapping(XID.class, null, XID.class, null, null);
    
    public final static OOTypeMapping Id_List = new OOTypeMapping(List.class, XID.class,
            XIDListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping IdList = new OOTypeMapping(XIDListValue.class, null,
            XIDListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Id_Set = new OOTypeMapping(Set.class, XID.class,
            XIDSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDSetValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping IdSet = new OOTypeMapping(XIDSetValue.class, null,
            XIDSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDSetValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Id_SortedSet = new OOTypeMapping(SortedSet.class, XID.class,
            XIDSortedSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDSortedSetValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping IdSortedSet = new OOTypeMapping(XIDSortedSetValue.class,
            null, XIDSortedSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIDSortedSetValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Integer_List = new OOTypeMapping(List.class, Integer.class,
            XIntegerListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIntegerListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping IntegerList = new OOTypeMapping(XIntegerListValue.class,
            null, XIntegerListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIntegerListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Integer = new OOTypeMapping(XIntegerValue.class, null,
            XIntegerValue.class, null, null);
    
    public final static OOTypeMapping Long_List = new OOTypeMapping(List.class, Long.class,
            XLongListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toLongListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping LongList = new OOTypeMapping(XLongListValue.class, null,
            XLongListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toLongListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping Long = new OOTypeMapping(XLongValue.class, null,
            XLongValue.class, null, null);
    
    public final static OOTypeMapping StringList = new OOTypeMapping(List.class, String.class,
            XStringListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringListValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping String_List = new OOTypeMapping(XStringListValue.class, null,
            XStringListValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringListValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping String_Set = new OOTypeMapping(Set.class, String.class,
            XStringSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringSetValue(Collections.EMPTY_LIST);
                }
            });
    public final static OOTypeMapping StringSet = new OOTypeMapping(XStringSetValue.class, null,
            XStringSetValue.class, null, new IFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringSetValue(Collections.EMPTY_LIST);
                }
            });
    
    public final static OOTypeMapping String = new OOTypeMapping(XStringValue.class, null,
            XStringValue.class, null, null);
    
    // extended mappings
    
    public final static OOTypeMapping JavaString = new OOTypeMapping(java.lang.String.class, null,
            XStringValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.String)j);
                }
                
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)XV.toString(x);
                }
            }, null);
    
    public final static OOTypeMapping JavaBoolean = new OOTypeMapping(java.lang.Boolean.class,
            null, XBooleanValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.Boolean)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Boolean)((XBooleanValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping Javaboolean = new OOTypeMapping(boolean.class, null,
            XBooleanValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.Boolean)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Boolean)((XBooleanValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping JavaLong = new OOTypeMapping(java.lang.Long.class, null,
            XLongValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.Long)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Long)((XLongValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping Javalong = new OOTypeMapping(long.class, null,
            XLongValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((Long)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Long)((XLongValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping JavaInteger = new OOTypeMapping(java.lang.Integer.class,
            null, XIntegerValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.Integer)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Integer)((XIntegerValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping Javaint = new OOTypeMapping(int.class, null,
            XIntegerValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((Integer)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Integer)((XIntegerValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping JavaDouble = new OOTypeMapping(java.lang.Double.class, null,
            XDoubleValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((java.lang.Double)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Double)((XDoubleValue)x).contents();
                }
            }, null);
    
    public final static OOTypeMapping Javadouble = new OOTypeMapping(double.class, null,
            XDoubleValue.class, new IMapper() {
                
                @Override
                public <J, C, X extends XValue> X toXydra(J j) {
                    return (X)XV.toValue((Double)j);
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public <J, C, X extends XValue> J toJava(X x) {
                    return (J)(Double)((XDoubleValue)x).contents();
                }
            }, null);
    
    private Class<?> javaType;
    private Class<?> javaComponentType;
    private Class<?> xydraType;
    private IMapper mapper;
    
    private IFactory factory;
    
    /**
     * @param javaType Mapping from this Java type @NeverNull
     * @param javaComponentType potentially with a component type @CanBeNull
     * @param xydraType to this Xydra type @NeverNull
     * @param mapper @CanBeNull for Xydra types
     * @param factory @CanBeNull for non-collection types
     */
    public OOTypeMapping(Class<?> javaType, @CanBeNull Class<?> javaComponentType,
            Class<?> xydraType, @CanBeNull IMapper mapper, IFactory factory) {
        this.javaType = javaType;
        this.javaComponentType = javaComponentType;
        this.xydraType = xydraType;
        this.mapper = mapper;
        this.factory = factory;
        
        mapping.put(new Pair<Class<?>,Class<?>>(javaType, javaComponentType), this);
        // if(javaComponentType == null) {
        // // auto-add support for 3 collection types
        // mapping.put(new Pair<Class<?>,Class<?>>(Set.class, javaType), this);
        // mapping.put(new Pair<Class<?>,Class<?>>(List.class, javaType), this);
        // mapping.put(new Pair<Class<?>,Class<?>>(SortedSet.class, javaType),
        // this);
        // }
    }
    
    public static void addMapping(OOTypeMapping mapping) {
        // its already added in the constructor
    }
    
    public static <J, C, X extends XValue> OOTypeMapping getMapping(Class<J> javaType,
            Class<C> javaComponentType) {
        return mapping.get(new Pair<Class<J>,Class<C>>(javaType, javaComponentType));
    }
    
    public Object toJava(XValue x) {
        if(this.mapper == null)
            return x;
        
        return this.mapper.toJava(x);
    }
    
    public XValue toXydra(Object j) {
        if(this.mapper == null) {
            try {
                XValue x = (XValue)j;
                return x;
            } catch(ClassCastException e) {
                throw new RuntimeException("Maybe you need to add a mapper from "
                        + j.getClass().getCanonicalName() + " to Xydra", e);
            }
        }
        
        return this.mapper.toXydra(j);
    }
    
    public Class<?> getJavaType() {
        return this.javaType;
    }
    
    public Class<?> getJavaComponentType() {
        return this.javaComponentType;
    }
    
    public Class<?> getXydraType() {
        return this.xydraType;
    }
    
    @SuppressWarnings("unchecked")
    public <T> XCollectionValue<T> createEmptyXydraCollection() {
        return (XCollectionValue<T>)this.factory.createEmptyCollection();
    }
    
}
