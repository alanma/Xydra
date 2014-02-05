package org.xydra.oo.generator.gwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValueJavaUtils;
import org.xydra.core.XX;
import org.xydra.oo.generator.codespec.NameUtils;
import org.xydra.oo.generator.codespec.SpecWriter;
import org.xydra.oo.generator.codespec.impl.ClassSpec;
import org.xydra.oo.generator.codespec.impl.MethodSpec;
import org.xydra.oo.generator.codespec.impl.PackageSpec;
import org.xydra.oo.runtime.client.GwtXydraMapped;
import org.xydra.oo.runtime.java.JavaReflectionUtils;
import org.xydra.oo.runtime.java.JavaTypeSpecUtils;
import org.xydra.oo.runtime.java.KindOfMethod;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.runtime.java.OOReflectionUtils;
import org.xydra.oo.runtime.java.XydraReflectionUtils;
import org.xydra.oo.runtime.shared.BaseTypeSpec;
import org.xydra.oo.runtime.shared.CollectionProxy.IComponentTransformer;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.ListProxy;
import org.xydra.oo.runtime.shared.SetProxy;
import org.xydra.oo.runtime.shared.SharedTypeMapping;
import org.xydra.oo.runtime.shared.SharedTypeSystem;
import org.xydra.oo.runtime.shared.SortedSetProxy;
import org.xydra.oo.runtime.shared.TypeSpec;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;


/**
 * Generates GWT classes during gwt:compile -- does the same in code what the
 * {@link OOJavaOnlyProxy} does via reflection
 * 
 * TODO super source factory
 * 
 * TODO take method comment from @Comment
 * 
 * TODO dont copy .gen module
 * 
 * @author xamde
 */
public class GwtCodeGenerator extends Generator {
    
    public static final String INTERFACES_PACKAGE = "shared";
    
    private static String tryToGetAnnotatedFieldId(AccessibleObject ao) {
        String text = null;
        org.xydra.oo.Field annotation = ao.getAnnotation(org.xydra.oo.Field.class);
        if(annotation != null)
            text = annotation.value();
        return text;
    }
    
    @Override
    public String generate(TreeLogger logger, GeneratorContext ctx, String requestedClass)
            throws UnableToCompleteException {
        
        TypeOracle typeOracle = ctx.getTypeOracle();
        assert (typeOracle != null);
        
        JClassType remoteService = typeOracle.findType(requestedClass);
        if(remoteService == null) {
            logger.log(TreeLogger.ERROR, "Unable to find metadata for type '" + requestedClass
                    + "'", null);
            throw new UnableToCompleteException();
        }
        
        if(remoteService.isInterface() == null) {
            logger.log(TreeLogger.ERROR, remoteService.getQualifiedSourceName()
                    + " is not an interface", null);
            throw new UnableToCompleteException();
        }
        
        // org.xydra.oo.test.tasks . shared.I Task
        
        // org.xydra.oo.test.tasks . client.Gwt Task
        
        String javaInterfaceFqName = remoteService.getQualifiedSourceName();
        int index = javaInterfaceFqName.indexOf("." + INTERFACES_PACKAGE);
        if(index < 0) {
            logger.log(TreeLogger.ERROR, remoteService.getQualifiedSourceName()
                    + " is not in a package named '" + INTERFACES_PACKAGE + "'", null);
            throw new UnableToCompleteException();
        }
        String basePackage = javaInterfaceFqName.substring(0, index);
        String typeName = javaInterfaceFqName.replace(
                basePackage + "." + INTERFACES_PACKAGE + ".I", "");
        String gwtPackageName = basePackage + ".client";
        String gwtSimpleName = "Gwt" + typeName;
        String gwtFqName = gwtPackageName + "." + gwtSimpleName;
        
        logger.log(
                TreeLogger.DEBUG,
                "Generating '" + gwtFqName + "' for remote service interface '"
                        + remoteService.getQualifiedSourceName() + "'", null);
        
        generateSourceCode(logger, ctx, javaInterfaceFqName, gwtPackageName, gwtSimpleName);
        
        return gwtFqName;
    }
    
    /**
     * @param logger
     * @param ctx
     * @param javaInterfaceFqName
     * @param gwtPackageName foo.client
     * @param gwtSimpleName
     */
    private static void generateSourceCode(TreeLogger logger, GeneratorContext ctx,
            String javaInterfaceFqName, String gwtPackageName, String gwtSimpleName) {
        TreeLogger logger2 = logger.branch(Type.INFO, "Generating source code to implement "
                + javaInterfaceFqName + " into " + gwtPackageName + "." + gwtSimpleName);
        
        // construct
        try {
            PackageSpec packageSpec = new PackageSpec(gwtPackageName, false);
            ClassSpec c = constructClassSpec(packageSpec, gwtPackageName, javaInterfaceFqName,
                    gwtSimpleName);
            // write
            PrintWriter pw = ctx.tryCreate(logger, gwtPackageName, gwtSimpleName);
            if(pw == null) {
                /*
                 * If the named types already exists, null is returned to
                 * indicate that no work needs to be done.
                 */
            } else {
                try {
                    SpecWriter.writeClass(pw, gwtPackageName, c);
                } catch(IOException e) {
                    logger2.log(Type.ERROR, "IO", e);
                }
                /*
                 * The file is not committed until commit(TreeLogger,
                 * PrintWriter) is called.
                 */
                ctx.commit(logger2, pw);
            }
        } catch(ClassNotFoundException e) {
            logger2.log(Type.ERROR, "Could not instantiate the java interface '"
                    + javaInterfaceFqName
                    + "'\n  Make sure your code is compiled, e.g. 'mvn compile' first.", e);
        }
    }
    
    /**
     * @param packageSpec
     * @param gwtPackagename
     * @param javaInterfaceFqName
     * @param gwtSimpleName
     * @return ...
     * @throws ClassNotFoundException
     */
    public static ClassSpec constructClassSpec(PackageSpec packageSpec, String gwtPackagename,
            String javaInterfaceFqName, String gwtSimpleName) throws ClassNotFoundException {
        ClassSpec c = packageSpec.addClass(gwtSimpleName);
        PackageSpec builtIn = new PackageSpec(GwtXydraMapped.class.getPackage().getName(), true);
        c.superClass = builtIn.addClass(GwtXydraMapped.class.getSimpleName());
        c.implementedInterfaces.add(javaInterfaceFqName);
        Class<?> javaInterface;
        javaInterface = Class.forName(javaInterfaceFqName);
        String generatedFrom = "" + javaInterface.getCanonicalName();
        for(Method m : javaInterface.getDeclaredMethods()) {
            String fieldId = tryToGetAnnotatedFieldId(m);
            if(fieldId != null) {
                MethodSpec methodSpec = c.addMethod(m, generatedFrom);
                methodSpec.setAccess("public");
                KindOfMethod kindOfMethod = OOReflectionUtils.extractKindOfMethod(m);
                switch(kindOfMethod) {
                
                case Get:
                    addGetter(gwtPackagename, c, methodSpec, fieldId, generatedFrom);
                    break;
                
                case Set:
                    assert m.getParameterTypes().length == 1;
                    java.lang.reflect.Type t = m.getGenericParameterTypes()[0];
                    TypeSpec type = JavaTypeSpecUtils.createTypeSpec(
                            JavaReflectionUtils.getRawType(t),
                            JavaReflectionUtils.getComponentType(t), generatedFrom);
                    addSetter(gwtPackagename, c, methodSpec, fieldId, type, generatedFrom);
                    break;
                
                case GetCollection:
                    addCollectionGetter(gwtPackagename, gwtSimpleName, c, methodSpec, fieldId);
                    break;
                
                default:
                    throw new AssertionError("Unknown method type: " + kindOfMethod);
                }
            }
        }
        
        return c;
    }
    
    private static void addCollectionGetter(String gwtPackagename, String gwtSimpleName,
            ClassSpec c, MethodSpec methodSpec, String fieldId) {
        IType returnType = methodSpec.returnType;
        
        /** <J> java base type */
        /** <C> java component type */
        IBaseType typeJ = returnType.getBaseType();
        String gJ = typeJ.getCanonicalName();
        String sJ = typeJ.getSimpleName();
        IBaseType typeC = returnType.getComponentType();
        String gC = typeC.getCanonicalName();
        String sC = typeC.getSimpleName();
        
        /** <X> xydra base type, extends XCollectionValue<T> */
        /** <T> xydra OR java component type, NOT always extends XValue */
        Class<?> classX;
        Class<?> classT;
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(returnType);
        if(mapping == null) {
            assert OOReflectionUtils.hasAnId(typeC) : "no mapping found for type="
                    + returnType.id();
            classX = XIdListValue.class;
            classT = XId.class;
        } else {
            
            ValueType valueTypeX = mapping.getXydraBaseValueType();
            assert valueTypeX.isCollection() : "Should be a collection type: " + valueTypeX
                    + " in " + c.getCanonicalName() + "." + methodSpec.getName() + "(..)";
            // component type is already a java type
            classX = mapping.getXydraBaseType();
            classT = valueTypeX.getComponentType().getJavaClass();
        }
        c.addRequiredImports(classX);
        c.addRequiredImports(classT);
        IBaseType typeX = JavaTypeSpecUtils.createBaseTypeSpec(classX);
        IBaseType typeT = JavaTypeSpecUtils.createBaseTypeSpec(classT);
        String gX = typeX.getCanonicalName();
        String gT = typeT.getCanonicalName();
        
        // e.g. X, T extends XValue, J, C =
        // XIdSetValue,XAddressSetValue,Set<XAddress>,XAddress
        
        // CODE: ITransformer<X, T extends XValue, J, C> {
        methodSpec.sourceLines.add("IComponentTransformer<" + gX + "," + gT + "," + gJ + "<" + gC
                + ">," + gC + "> t = new IComponentTransformer<" + gX + "," + gT + "," + gJ + "<"
                + gC + ">," + gC + ">() {");
        
        // CODE: C toJavaComponent(T componentType);
        methodSpec.sourceLines.add("    @Override");
        methodSpec.sourceLines.add("    public " + gC + " toJavaComponent(" + gT + " x) {");
        String x2j;
        if(mapping != null) {
            if(typeC.equals(typeT)) {
                // no need to convert
                x2j = "x";
            } else {
                ValueType baseValueType = mapping.getXydraBaseValueType();
                x2j = "XValueJavaUtils.from" + baseValueType.name() + "(x)";
                c.addRequiredImports(XValueJavaUtils.class);
            }
        } else {
            String gC_basename = NameUtils.firstLetterUppercased(sC.substring(1));
            x2j = "GwtFactory.wrap" + gC_basename + "(" + gwtSimpleName
                    + ".this.oop.getXModel(), (XId) x)";
            c.addRequiredImports(XId.class);
        }
        methodSpec.sourceLines.add("        return " + x2j + ";");
        methodSpec.sourceLines.add("    }");
        methodSpec.sourceLines.add("");
        
        // CODE: T toXydraComponent(C javaType);
        methodSpec.sourceLines.add("    @Override");
        methodSpec.sourceLines.add("    public " + gT + " toXydraComponent(" + gC + " javaType) {");
        String j2x;
        if(mapping != null) {
            if(typeC.equals(typeT)) {
                // no need to convert
                j2x = "javaType";
            } else {
                ValueType baseValueType = mapping.getXydraBaseValueType();
                j2x = "XValueJavaUtils.to" + baseValueType.name() + "(javaType)";
                c.addRequiredImports(XValueJavaUtils.class);
            }
        } else {
            j2x = "javaType.getId()";
        }
        methodSpec.sourceLines.add("        return " + j2x + ";");
        
        methodSpec.sourceLines.add("    }");
        methodSpec.sourceLines.add("");
        
        // CODE: X createCollection();
        methodSpec.sourceLines.add("    @Override");
        methodSpec.sourceLines.add("    public " + gX + " createCollection() {");
        
        if(mapping == null) {
            if(OOReflectionUtils.isProxyType(returnType.getComponentType())) {
                methodSpec
                        .addSourceLine("        return XV.toIdListValue(java.util.Collections.EMPTY_LIST);");
                c.addRequiredImports(XV.class);
            } else {
                methodSpec.addSourceLine("        return (" + gX
                        + ") SharedTypeSystem.createCollection(" + typeJ.getSimpleName()
                        + ".class, " + typeC.getSimpleName() + ".class);");
                c.addRequiredImports(SharedTypeSystem.class);
            }
        } else {
            methodSpec.addSourceLine("        return "
                    + mapping.getCollectionFactory().createEmptyCollection_asSourceCode() + ";");
            // c.addRequiredImports(Collections.class);
        }
        
        methodSpec.sourceLines.add("    }");
        methodSpec.sourceLines.add("");
        methodSpec.sourceLines.add("};");
        methodSpec.sourceLines.add("    ");
        methodSpec.sourceLines.add("return new " + sJ + "Proxy<" + gX + "," + gT + "," + gJ + "<"
                + gC + ">," + gC + ">(this.oop.getXObject(), XX.toId(\"" + fieldId + "\"), t);");
        if(sJ.equals(Set.class.getSimpleName())) {
            c.addRequiredImports(SetProxy.class);
        } else if(sJ.equals(List.class.getSimpleName())) {
            c.addRequiredImports(ListProxy.class);
        } else if(sJ.equals(SortedSet.class.getSimpleName())) {
            c.addRequiredImports(SortedSetProxy.class);
        }
        
        c.addRequiredImports(gwtPackagename + ".GwtFactory");
        c.addRequiredImports(IComponentTransformer.class);
        // c.addRequiredImports(CollectionProxy.class);
        c.addRequiredImports(XX.class);
    }
    
    private static void addGetter(String gwtPackagename, ClassSpec classSpec, MethodSpec m,
            String fieldId, String generatedFrom) {
        
        IType returnType = m.getReturnType();
        IBaseType baseType = returnType.getBaseType();
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(returnType);
        
        /* 1) Mapped types */
        if(mapping != null) {
            ValueType valueType = mapping.getXydraBaseValueType();
            String xydraType = valueType.getXydraInterface().getSimpleName();
            m.addSourceLine(xydraType + " x = ((" + xydraType + ")this.oop.getValue(\"" + fieldId
                    + "\"));");
            m.addSourceLine("if(x == null)");
            if(JavaReflectionUtils.isJavaPrimitiveType(baseType)) {
                /* 1.1) Java primitive type */
                m.addSourceLine("// Java primitive type");
                String returnValue = JavaReflectionUtils
                        .returnDefaultValueOfPrimitiveTypeAsSourceCodeLiteral(baseType
                                .getSimpleName());
                m.addSourceLine("    return " + returnValue + ";");
                m.addSourceLine("return x.contents();");
            } else if(JavaReflectionUtils.equalsClass(returnType, byte[].class)) {
                /* 1.2) byte[] */
                m.addSourceLine("// byte[]");
                m.addSourceLine("    return null;");
                m.addSourceLine("return x.contents();");
            } else {
                m.addSourceLine("    return null;");
                
                if(XydraReflectionUtils.isXydraValueType(baseType)) {
                    /* 1.3) Xydra value type */
                    m.addSourceLine("// Xydra value type");
                    m.addSourceLine("return x;");
                } else {
                    
                    /* 1.4) Extended types with a mapping */
                    m.addSourceLine("// Extended types with a mapping");
                    m.addSourceLine("SharedTypeMapping mapping = SharedTypeMapping.getMapping("
                            + "new TypeSpec(new BaseTypeSpec(\"" + baseType.getPackageName()
                            + "\", \"" + baseType.getSimpleName() + "\"), null, \"gwt\"));");
                    m.addSourceLine("return (" + baseType.getSimpleName() + ")mapping.toJava(x);");
                    classSpec.addRequiredImports(SharedTypeMapping.class);
                    classSpec.addRequiredImports(TypeSpec.class);
                    classSpec.addRequiredImports(BaseTypeSpec.class);
                }
            }
            m.setComment("Mapped Xydra type");
            return;
        }
        
        /* 2) Proxy types */
        if(OOReflectionUtils.isProxyType(returnType)) {
            m.addSourceLine("XId id = XValueJavaUtils.getId(this.oop.getXObject(), XX.toId(\""
                    + fieldId + "\"));");
            classSpec.addRequiredImports(XX.class);
            classSpec.addRequiredImports(XId.class);
            classSpec.addRequiredImports(XValueJavaUtils.class);
            
            m.addSourceLine("if(id == null)");
            m.addSourceLine("    return null;");
            m.addSourceLine("return "
                    + new MethodCallSpec(gwtPackagename, "GwtFactory", "wrap"
                            + NameUtils.firstLetterUppercased(returnType.getTypeString().substring(
                                    1))).addParam("this.oop.getXModel()").addParam("id")
                            .toMethodCall() + ";");
            m.setComment("Proxy type");
            return;
        }
        
        /* 3) Collections of built-in Xydra types */
        ValueType componentValueType = returnType.getComponentType() == null ? null
                : SharedTypeMapping.getValueType(returnType.getComponentType(), null);
        if(JavaReflectionUtils.isJavaCollectionType(returnType) && !returnType.isArray()
                && componentValueType != null) {
            MethodCallSpec methodCallSpec = new MethodCallSpec(XValueJavaUtils.class, "get"
                    + componentValueType.name()).addParam("this.oop.getXObject()").addParam(
                    "XX.toId(\"" + fieldId + "\")");
            m.addSourceLine("return " + methodCallSpec.toMethodCall() + ";");
            m.setComment("Collections of built-in Xydra types");
            return;
        }
        
        /* 4) Enum types */
        if(JavaReflectionUtils.isEnumType(returnType)) {
            MethodCallSpec methodCallSpec = new MethodCallSpec(XValueJavaUtils.class, "getString")
                    .addParam("this.oop.getXObject()").addParam("XX.toId(\"" + fieldId + "\")");
            m.addSourceLine("String s = " + methodCallSpec.toMethodCall() + ";");
            m.addSourceLine("if(s == null)");
            m.addSourceLine("  return null;");
            m.addSourceLine("return " + returnType.getBaseType().getSimpleName() + ".valueOf(s);");
            m.setComment("Auto-convert enum to XStringValue");
            classSpec.addRequiredImports(XValueJavaUtils.class);
            return;
        }
        
        /* 5) Java types corresponding to Xydra types */
        // determine correct method in XValueJavaUtils
        String getterMethod = "get" + getPropertyName(returnType);
        MethodCallSpec methodCallSpec = new MethodCallSpec(XValueJavaUtils.class, getterMethod)
                .addParam("this.oop.getXObject()").addParam("XX.toId(\"" + fieldId + "\")");
        m.addSourceLine("return " + methodCallSpec.toMethodCall() + ";");
        m.setComment("Java types corresponding to Xydra types");
        classSpec.addRequiredImports(XValueJavaUtils.class);
    }
    
    // all setters return the class itself for more fluent api usage
    private static void addSetter(String gwtPackagename, ClassSpec c, MethodSpec m, String fieldId,
            TypeSpec type, String generatedFrom) {
        m.addParam(fieldId, type, generatedFrom);
        SharedTypeMapping mapping = SharedTypeMapping.getMapping(type);
        if(mapping != null) {
            /* 1) Mapped types */
            if(XydraReflectionUtils.isXydraValueType(type.getBaseType())) {
                /* 1.1) xydra types */
                m.sourceLines.add("this.oop.setValue(\"" + fieldId + "\", " + fieldId + ");");
                m.setComment("Trivial xydra type");
                m.sourceLines.add("return this;");
                return;
            } else {
                /* 1.2) other types with a mapping */
                
                m.sourceLines.add("// non-xydra type '" + type.getTypeString() + "' with mapping");
                
                m.sourceLines
                        .add("SharedTypeMapping mapping = SharedTypeMapping.getMapping(new TypeSpec(new BaseTypeSpec(");
                // FIXME was "org.xydra.oo.testgen.alltypes.shared"
                String packageName = type.getBaseType().getPackageName() == null ? "null" : "\""
                        + type.getBaseType().getPackageName() + "\"";
                String simpleName = type.getTypeString();
                m.sourceLines.add("  " + packageName + ", \"" + simpleName
                        + "\"), null, \"gwt\"));");
                m.sourceLines.add("" + mapping.getXydraBaseType().getSimpleName() + " x = ("
                        + mapping.getXydraBaseType().getSimpleName() + ")mapping.toXydra("
                        + fieldId + ");");
                c.addRequiredImports(SharedTypeMapping.class);
                c.addRequiredImports(mapping.getXydraBaseType());
                m.sourceLines.add("this.oop.setValue(\"" + fieldId + "\", x);");
                m.sourceLines.add("return this;");
                return;
            }
        }
        String propertyName = getPropertyName(type);
        if(OOReflectionUtils.isProxyType(type)) {
            /* 2) Proxy types */
            m.sourceLines.add("XValueJavaUtils.setId" + "(this.oop.getXObject(), XX.toId(\""
                    + fieldId + "\"), " + fieldId + ".getId()" + ");");
            c.addRequiredImports(XValueJavaUtils.class);
            m.setComment("Proxy types");
            m.sourceLines.add("return this;");
            return;
        }
        
        /* 3) Collections of built-in Xydra types */
        ValueType componentValueType = type.getComponentType() == null ? null : SharedTypeMapping
                .getValueType(type.getComponentType(), null);
        if(JavaReflectionUtils.isJavaCollectionType(type) && !type.isArray()
                && componentValueType != null) {
            MethodCallSpec methodCallSpec = new MethodCallSpec(XValueJavaUtils.class, "set"
                    + componentValueType.name()).addParam("this.oop.getXObject()")
                    .addParam("XX.toId(\"" + fieldId + "\")").addParam(fieldId);
            m.addSourceLine("return " + methodCallSpec.toMethodCall() + ";");
            m.setComment("Collections of built-in Xydra types");
            c.addRequiredImports(XValueJavaUtils.class);
            m.sourceLines.add("return this;");
            return;
        }
        
        if(JavaReflectionUtils.isEnumType(type)) {
            /* 4) Enum types */
            m.sourceLines.add("XValueJavaUtils.setString" + "(this.oop.getXObject(), XX.toId(\""
                    + fieldId + "\"), " + fieldId + ".name()" + ");");
            m.setComment("Enum types");
            c.addRequiredImports(XValueJavaUtils.class);
            m.sourceLines.add("return this;");
            return;
        }
        
        /* 5) Java types corresponding to Xydra types */
        m.sourceLines.add("XValueJavaUtils.set" + propertyName
                + "(this.oop.getXObject(), XX.toId(\"" + fieldId + "\"), " + fieldId + ");");
        m.setComment("Java types corresponding to Xydra types");
        c.addRequiredImports(XValueJavaUtils.class);
        m.sourceLines.add("return this;");
    }
    
    /**
     * @param returnType
     * @return a name starting with "get" or throws an exception @NeverNull
     */
    private static String getPropertyName(IType typeSpec) {
        String s = null;
        ValueType valueType = XydraReflectionUtils.getValueType(typeSpec);
        if(valueType != null) {
            s = valueType.name();
        }
        
        if(s == null && typeSpec.isArray()) {
            valueType = XydraReflectionUtils.getValueType(typeSpec.getComponentType());
            if(valueType != null) {
                s = valueType.name() + "Array";
            }
        }
        
        if(s == null) {
            s = NameUtils.firstLetterUppercased(typeSpec.getTypeString());
        }
        return s;
    }
    
    public static void main(String[] args) {
        assert XydraReflectionUtils.getValueType(new TypeSpec(BaseTypeSpec.ARRAY, JavaTypeSpecUtils
                .createBaseTypeSpec(byte.class), "test")) == ValueType.Binary;
    }
}
