package org.xydra.oo.runtime.shared;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * A mapping defines a relationship between Java and Xydra types. This is the
 * basis for Java dynamic proxies and generated GWT code to map Java POJOs to
 * Xydra objects.
 * 
 * The Java type is defined via a {@link TypeSpec} = a base type and an optional
 * component type.
 * 
 * The Xydra type is defined via its {@link ValueType}.
 * 
 * Optionally the mapping might have an {@link IMapper} which converts between
 * Xydra and Java types. Or it can optionally have an
 * {@link IXydraCollectionFactory} which can create empty collections.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class SharedTypeMapping {
    
    private static final Logger log = LoggerFactory.getLogger(SharedTypeMapping.class);
    
    private static Map<IType,SharedTypeMapping> _mappings = new HashMap<IType,SharedTypeMapping>();
    
    // mappers
    private static IMapper<Boolean,XBooleanValue> _javaBooleanMapper = new IMapper<Boolean,XBooleanValue>() {
        
        @Override
        public java.lang.Boolean toJava(XBooleanValue x) {
            return (Boolean)x.contents();
        }
        
        @Override
        public XBooleanValue toXydra(java.lang.Boolean j) {
            return (XBooleanValue)XV.toValue((java.lang.Boolean)j);
        }
        
        @Override
        public java.lang.String toJava_asSourceCode() {
            return "(java.lang.Boolean) x.contents();";
        }
        
        @Override
        public java.lang.String toXydra_asSourceCode() {
            return TypeConstants.XV + ".toValue(j)";
        }
        
    };
    
    private static IMapper<byte[],XBinaryValue> _javaByteArrayMapper = new IMapper<byte[],XBinaryValue>() {
        
        @Override
        public byte[] toJava(XBinaryValue x) {
            return (byte[])((XBinaryValue)x).contents();
        }
        
        @Override
        public XBinaryValue toXydra(byte[] j) {
            return (XBinaryValue)XV.toValue((byte[])j);
        }
        
        @Override
        public java.lang.String toJava_asSourceCode() {
            return "(byte[]) x.contents()";
        }
        
        @Override
        public java.lang.String toXydra_asSourceCode() {
            return "(" + TypeConstants.VALUE + ".XBinaryValue) " + TypeConstants.XV + ".toValue(j)";
        }
    };
    
    private static IMapper<Double,XDoubleValue> _javaDoubleMapper = new IMapper<Double,XDoubleValue>() {
        
        @Override
        public java.lang.Double toJava(XDoubleValue x) {
            return (Double)((XDoubleValue)x).contents();
        }
        
        @Override
        public XDoubleValue toXydra(java.lang.Double j) {
            return (XDoubleValue)XV.toValue((java.lang.Double)j);
        }
        
        @Override
        public java.lang.String toJava_asSourceCode() {
            return "(java.lang.Double) x.contents()";
        }
        
        @Override
        public java.lang.String toXydra_asSourceCode() {
            return "(" + TypeConstants.VALUE + ".XDoubleValue) " + TypeConstants.XV + ".toValue(j)";
        }
    };
    
    private static IMapper<Integer,XIntegerValue> _javaIntegerMapper = new IMapper<Integer,XIntegerValue>() {
        
        @Override
        public java.lang.Integer toJava(XIntegerValue x) {
            return (Integer)((XIntegerValue)x).contents();
        }
        
        @Override
        public XIntegerValue toXydra(java.lang.Integer j) {
            return (XIntegerValue)XV.toValue((Integer)j);
        }
        
        @Override
        public java.lang.String toJava_asSourceCode() {
            return "(java.lang.Integer) x.contents()";
        }
        
        @Override
        public java.lang.String toXydra_asSourceCode() {
            return "(" + TypeConstants.VALUE + ".XIntegerValue) " + TypeConstants.XV
                    + ".toValue(j)";
        }
    };
    
    private static IMapper<Long,XLongValue> _javaLongMapper = new IMapper<Long,XLongValue>() {
        
        @Override
        public java.lang.Long toJava(XLongValue x) {
            return (Long)((XLongValue)x).contents();
        }
        
        @Override
        public XLongValue toXydra(java.lang.Long j) {
            return (XLongValue)XV.toValue((Long)j);
        }
        
        @Override
        public java.lang.String toJava_asSourceCode() {
            return "(java.lang.Long) x.contents()";
        }
        
        @Override
        public java.lang.String toXydra_asSourceCode() {
            return "(" + TypeConstants.VALUE + ".XLongValue) " + TypeConstants.XV + ".toValue(j)";
        }
    };
    
    // trivial mappings for built-in Xydra types
    
    public final static SharedTypeMapping Address = SharedTypeMapping.createAndAdd(
            TypeConstants.Address, ValueType.Address, null, null);
    
    public final static SharedTypeMapping Address_List = SharedTypeMapping.createAndAdd(
    
    TypeConstants.Address_List, ValueType.AddressList,
            new IMapper<List<XAddress>,XAddressListValue>() {
                
                @Override
                public List<XAddress> toJava(XAddressListValue x) {
                    return Arrays.asList(x.contents());
                }
                
                @Override
                public XAddressListValue toXydra(List<XAddress> j) {
                    return XV.toAddressListValue(j);
                }
                
                @Override
                public java.lang.String toJava_asSourceCode() {
                    return "java.util.Arrays.asList(x.contents())";
                }
                
                @Override
                public java.lang.String toXydra_asSourceCode() {
                    return TypeConstants.XV + ".toAddressListValue(j)";
                }
            },
            
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Address_Set = SharedTypeMapping.createAndAdd(
            TypeConstants.Address_Set, ValueType.AddressSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Address_SortedSet = SharedTypeMapping.createAndAdd(
            TypeConstants.Address_SortedSet, ValueType.AddressSortedSet, null,
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSortedSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressSortedSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping AddressList = SharedTypeMapping.createAndAdd(
            TypeConstants.AddressList, ValueType.AddressList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping AddressSet = SharedTypeMapping.createAndAdd(
            TypeConstants.AddressSet, ValueType.AddressSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping AddressSortedSet = SharedTypeMapping.createAndAdd(
            TypeConstants.AddressSortedSet, ValueType.AddressSortedSet, null,
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toAddressSortedSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toAddressSortedSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Binary = SharedTypeMapping.createAndAdd(
            TypeConstants.Binary, ValueType.Binary, null, null);
    
    public final static SharedTypeMapping Boolean = SharedTypeMapping.createAndAdd(
            TypeConstants.Boolean, ValueType.Boolean, null, null);
    
    public final static SharedTypeMapping Boolean_List = SharedTypeMapping.createAndAdd(
            TypeConstants.Boolean_List, ValueType.BooleanList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toBooleanListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toBooleanListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping BooleanList = SharedTypeMapping.createAndAdd(
            TypeConstants.BooleanList, ValueType.BooleanList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toBooleanListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toBooleanListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Double = SharedTypeMapping.createAndAdd(
            TypeConstants.Double, ValueType.Double, null, null);
    
    public final static SharedTypeMapping Double_List = SharedTypeMapping.createAndAdd(
            TypeConstants.Double_List, ValueType.DoubleList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toDoubleListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toDoubleListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping DoubleList = SharedTypeMapping.createAndAdd(
            TypeConstants.DoubleList, ValueType.DoubleList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toDoubleListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toDoubleListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Id = SharedTypeMapping.createAndAdd(TypeConstants.Id,
            ValueType.Id, null, null);
    
    public final static SharedTypeMapping Id_List = SharedTypeMapping.createAndAdd(
            TypeConstants.Id_List, ValueType.IdList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toIdListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Id_Set = SharedTypeMapping.createAndAdd(
            TypeConstants.Id_Set, ValueType.IdSet,
            
            // IMPROVE add a mapper here?
            null,
            
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toIdSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Id_SortedSet = SharedTypeMapping.createAndAdd(
            TypeConstants.Id_SortedSet, ValueType.IdSortedSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdSortedSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toIdSortedSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping IdList = SharedTypeMapping.createAndAdd(
            TypeConstants.IdList, ValueType.IdList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toIdListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping IdSet = SharedTypeMapping.createAndAdd(
            TypeConstants.IdSet, ValueType.IdSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toIdSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping IdSortedSet = SharedTypeMapping.createAndAdd(
            TypeConstants.IdSortedSet, ValueType.IdSortedSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIdSortedSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toIdSortedSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Integer = SharedTypeMapping.createAndAdd(
            TypeConstants.Integer, ValueType.Integer, null, null);
    
    public final static SharedTypeMapping Integer_List = SharedTypeMapping.createAndAdd(
            TypeConstants.Integer_List, ValueType.IntegerList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIntegerListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toIntegerListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping IntegerList = SharedTypeMapping.createAndAdd(
            TypeConstants.IntegerList, ValueType.IntegerList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toIntegerListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toIntegerListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping Long = SharedTypeMapping.createAndAdd(TypeConstants.Long,
            ValueType.Long, null, null);
    
    public final static SharedTypeMapping Long_List = SharedTypeMapping.createAndAdd(
            TypeConstants.Long_List, ValueType.LongList, _javaLongMapper,
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toLongListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toLongListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping LongList = SharedTypeMapping.createAndAdd(
            TypeConstants.LongList, ValueType.LongList, _javaLongMapper,
            new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toLongListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toLongListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping String = SharedTypeMapping.createAndAdd(
            TypeConstants.String, ValueType.String, null, null);
    
    public final static SharedTypeMapping String_List = SharedTypeMapping.createAndAdd(
            TypeConstants.String_List, ValueType.StringList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toStringListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping String_Set = SharedTypeMapping.createAndAdd(
            TypeConstants.String_Set, ValueType.StringSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toStringSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping StringList = SharedTypeMapping.createAndAdd(
            TypeConstants.StringList, ValueType.StringList, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringListValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV
                            + ".toStringListValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    public final static SharedTypeMapping StringSet = SharedTypeMapping.createAndAdd(
            TypeConstants.StringSet, ValueType.StringSet, null, new IXydraCollectionFactory() {
                
                @Override
                public Object createEmptyCollection() {
                    return XV.toStringSetValue(java.util.Collections.EMPTY_LIST);
                }
                
                @Override
                public java.lang.String createEmptyCollection_asSourceCode() {
                    return TypeConstants.XV + ".toStringSetValue(java.util.Collections.EMPTY_LIST)";
                }
            });
    
    // mappings for Java types
    
    public final static SharedTypeMapping Javaboolean = SharedTypeMapping.createAndAdd(
            TypeConstants.Javaboolean, ValueType.Boolean, _javaBooleanMapper, null);
    
    public final static SharedTypeMapping JavaBoolean = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaBoolean, ValueType.Boolean, _javaBooleanMapper, null);
    public final static SharedTypeMapping JavabooleanList = SharedTypeMapping.createAndAdd(
            TypeConstants.JavabooleanList, ValueType.Boolean, _javaBooleanMapper, null);
    
    public final static SharedTypeMapping JavabyteArray = SharedTypeMapping.createAndAdd(
            TypeConstants.JavabyteArray, ValueType.Binary, _javaByteArrayMapper, null);
    
    public final static SharedTypeMapping JavabyteList = SharedTypeMapping.createAndAdd(
            TypeConstants.JavabyteList, ValueType.Binary, _javaByteArrayMapper, null);
    
    public final static SharedTypeMapping Javadouble = SharedTypeMapping.createAndAdd(
            TypeConstants.Javadouble, ValueType.Double, _javaDoubleMapper, null);
    
    public final static SharedTypeMapping JavaDouble = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaDouble, ValueType.Double, _javaDoubleMapper, null);
    
    public final static SharedTypeMapping JavadoubleList = SharedTypeMapping.createAndAdd(
            TypeConstants.JavadoubleList, ValueType.DoubleList, _javaDoubleMapper, null);
    
    public final static SharedTypeMapping Javaint = SharedTypeMapping.createAndAdd(
            TypeConstants.Javaint, ValueType.Integer, _javaIntegerMapper, null);
    
    public final static SharedTypeMapping JavaInteger = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaInteger, ValueType.Integer, _javaIntegerMapper, null);
    
    public final static SharedTypeMapping JavaintList = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaintList, ValueType.Integer, _javaIntegerMapper, null);
    
    public final static SharedTypeMapping Javalong = SharedTypeMapping.createAndAdd(
            TypeConstants.Javalong, ValueType.Long, _javaLongMapper, null);
    
    public final static SharedTypeMapping JavaLong = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaLong, ValueType.Long, _javaLongMapper, null);
    
    public final static SharedTypeMapping JavalongList = SharedTypeMapping.createAndAdd(
            TypeConstants.JavalongList, ValueType.Long, _javaLongMapper, null);
    
    public final static SharedTypeMapping JavaString = SharedTypeMapping.createAndAdd(
            TypeConstants.JavaString, ValueType.String, new IMapper<String,XStringValue>() {
                
                @Override
                public java.lang.String toJava(XStringValue x) {
                    return XV.toString(x);
                }
                
                @Override
                public XStringValue toXydra(java.lang.String j) {
                    return (XStringValue)XV.toValue((java.lang.String)j);
                }
                
                @Override
                public java.lang.String toJava_asSourceCode() {
                    return "(java.lang.String) x.contents()";
                }
                
                @Override
                public java.lang.String toXydra_asSourceCode() {
                    return "(" + TypeConstants.VALUE + ".XStringValue) " + TypeConstants.XV
                            + ".toValue(j)";
                }
            }, null);
    
    // methods
    
    /**
     * @param type @NeverNull
     * @return a mapping for the given type
     */
    public static SharedTypeMapping getMapping(@NeverNull IType type) {
        assert type != null;
        return _mappings.get(type);
    }
    
    /**
     * @param baseType @NeverNull
     * @param componentType
     * @return looks up a mapping for the given type and returns the ValueType
     *         or null if no mapping was found
     */
    public static ValueType getValueType(@NeverNull IBaseType baseType, IBaseType componentType) {
        assert baseType != null;
        return getXydraBaseValueType(new TypeSpec(baseType, componentType, "runtime"));
    }
    
    /**
     * @param type @NeverNull
     * @return looks up a mapping for the given type and returns the ValueType
     *         or null if no mapping was found
     */
    public static ValueType getXydraBaseValueType(@NeverNull TypeSpec type) {
        assert type != null;
        SharedTypeMapping mapping = getMapping(type);
        if(mapping != null) {
            return mapping.getXydraBaseValueType();
        }
        return null;
    }
    
    @CanBeNull
    private IXydraCollectionFactory factory;
    
    @NeverNull
    private TypeSpec typeSpec;
    
    @CanBeNull
    private IMapper<?,? extends XValue> mapper;
    
    @NeverNull
    private ValueType xydraBaseValueType;
    
    /**
     * @param javaBaseTypePackage
     * @param javaBaseTypeName
     * @param javaComponentTypePackage
     * @param javaComponentTypeName
     * @param xydraBaseValueType
     * @param mapper
     * @param factory
     * @return ...
     */
    public SharedTypeMapping createAndAdd(String javaBaseTypePackage, String javaBaseTypeName,
            String javaComponentTypePackage, String javaComponentTypeName,
            ValueType xydraBaseValueType, @CanBeNull IMapper<?,? extends XValue> mapper,
            IXydraCollectionFactory factory) {
        return createAndAdd(
                new TypeSpec(BaseTypeSpec.create(javaBaseTypePackage, javaBaseTypeName),
                        BaseTypeSpec.create(javaComponentTypePackage, javaComponentTypeName),
                        "OOTypeMapping"), xydraBaseValueType, mapper, factory);
    }
    
    private SharedTypeMapping() {
    }
    
    /**
     * @param typeSpec
     * @param xydraBaseValueType
     * @param mapper
     * @param factory
     * @return ...
     */
    public static SharedTypeMapping createAndAdd(TypeSpec typeSpec, ValueType xydraBaseValueType,
            @CanBeNull IMapper<?,? extends XValue> mapper, IXydraCollectionFactory factory) {
        SharedTypeMapping stm = new SharedTypeMapping();
        stm.typeSpec = typeSpec;
        stm.xydraBaseValueType = xydraBaseValueType;
        stm.mapper = mapper;
        stm.factory = factory;
        
        _mappings.put(typeSpec, stm);
        
        return stm;
    }
    
    /**
     * @return an empty Xydra collection value, a sub-type of
     *         {@link XCollectionValue}
     */
    @SuppressWarnings("unchecked")
	public <T> XCollectionValue<T> createEmptyXydraCollection() {
        return (XCollectionValue<T>)this.factory.createEmptyCollection();
    }
    
    /**
     * @return the Xydra interface of the base type @NeverNull
     */
    public Class<?> getXydraBaseType() {
        return this.xydraBaseValueType.getXydraInterface();
    }
    
    /**
     * @return the {@link ValueType} of the Xydra base type. @NeverNull
     */
    public ValueType getXydraBaseValueType() {
        return this.xydraBaseValueType;
    }
    
    public <Y extends XValue> Object toJava(Y x) {
        if(this.mapper == null)
            return x;
        
        @SuppressWarnings("unchecked")
        IMapper<Object,Y> castMapper = (IMapper<Object,Y>)this.mapper;
        
        return castMapper.toJava((Y)x);
    }
    
    @Override
    public String toString() {
        return this.typeSpec.id() + " -> " + this.xydraBaseValueType + " mapper?"
                + (this.mapper != null) + " factory?" + (this.factory != null);
    }
    
    public <Y extends XValue> XValue toXydra(Object j) {
        // do trivial conversion here
        if(j instanceof XValue) {
            @SuppressWarnings("unchecked")
            Y y = (Y)j;
            return y;
        }
        
        if(this.mapper == null) {
            try {
                @SuppressWarnings("unchecked")
                Y y = (Y)j;
                return y;
            } catch(ClassCastException e) {
                throw new RuntimeException("Maybe you need to add a mapper from "
                        + j.getClass().getName() + " to a Xydra type. No mapper found. Mapping: "
                        + this, e);
            }
        }
        
        // let mapper only deal with real conversions
        try {
            @SuppressWarnings("unchecked")
            IMapper<Object,Y> castMapper = (IMapper<Object,Y>)this.mapper;
            Y y = castMapper.toXydra(j);
            return y;
        } catch(ClassCastException e) {
            log.warn("Defect mapper in " + this.toString());
            throw e;
        }
    }
    
    public TypeSpec getTypeSpec() {
        return this.typeSpec;
    }
    
    public IXydraCollectionFactory getCollectionFactory() {
        return this.factory;
    }
    
}
