package org.xydra.oo.generator.gwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.xydra.base.XID;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.oo.generator.codespec.ClassSpec;
import org.xydra.oo.generator.codespec.FieldSpec;
import org.xydra.oo.generator.codespec.MethodSpec;
import org.xydra.oo.generator.codespec.SpecWriter;
import org.xydra.oo.runtime.client.GwtXydraMapped;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.runtime.java.ReflectionTool;
import org.xydra.oo.runtime.java.ReflectionTool.KindOfMethod;

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
    
    private static void generateSourceCode(TreeLogger logger, GeneratorContext ctx,
            String javaInterfaceFqName, String gwtPackageName, String gwtSimpleName) {
        TreeLogger logger2 = logger.branch(Type.INFO, "Generating source code to implement "
                + javaInterfaceFqName + " into " + gwtPackageName + "." + gwtSimpleName);
        
        // construct
        try {
            ClassSpec c = constructClassSpec(javaInterfaceFqName, gwtSimpleName);
            // write
            PrintWriter pw = ctx.tryCreate(logger, gwtPackageName, gwtSimpleName);
            try {
                SpecWriter.writeClass(pw, gwtPackageName, c);
            } catch(IOException e) {
                logger2.log(Type.ERROR, "IO", e);
            }
            ctx.commit(logger2, pw);
        } catch(ClassNotFoundException e) {
            logger2.log(Type.ERROR, "Could not instantiate the java interface", e);
        }
    }
    
    public static ClassSpec constructClassSpec(String javaInterfaceFqName, String gwtSimpleName)
            throws ClassNotFoundException {
        ClassSpec c = new ClassSpec("class", gwtSimpleName);
        c.superClass = new ClassSpec("class", GwtXydraMapped.class.getSimpleName());
        Class<?> javaInterface;
        javaInterface = Class.forName(javaInterfaceFqName);
        for(Method m : javaInterface.getDeclaredMethods()) {
            String fieldId = tryToGetAnnotatedFieldId(m);
            if(fieldId != null) {
                MethodSpec ms = new MethodSpec(m, "" + javaInterface.getCanonicalName());
                
                KindOfMethod kindOfMethod = ReflectionTool.extractKindOfMethod(m);
                String line;
                switch(kindOfMethod) {
                case Get:
                    line = "return XValueJavaUtils.get" + ms.returnType.getTypeName()
                            + "(this.oop.getXObject(), XX.toId(\"" + fieldId + "\"));";
                    ms.sourceLines.add(line);
                    break;
                case Set:
                    assert m.getParameterTypes().length == 1;
                    java.lang.reflect.Type t = m.getGenericParameterTypes()[0];
                    FieldSpec fs = new FieldSpec(fieldId, t, "" + javaInterfaceFqName);
                    ms.params.add(fs);
                    line = "XValueJavaUtils.set" + ms.params.get(0).getTypeName()
                            + "(this.oop.getXObject(), XX.toId(\"" + fieldId + "\"), " + fieldId
                            + ");";
                    ms.sourceLines.add(line);
                    break;
                case GetCollection:
                    // * @param <X> xydra type
                    // * @param <T> xydra component type
                    // * @param <J> java type
                    // * @param <C> java component type
                    Class<?> classJ = ms.returnType.type;
                    Class<?> classC = ms.returnType.componentType;
                    
                    assert ReflectionTool.isMappedToXydra(classC) : classJ.getCanonicalName();
                    // OOTypeMapping mapping = OOTypeMapping.getMapping(classJ,
                    // classC);
                    // if(mapping == null) {
                    // throw new RuntimeException("Cannot handle type="
                    // + classJ.getCanonicalName() + " compType="
                    // + classC.getCanonicalName()
                    // + " yet. Maybe you need to add an OOTypeMapping.");
                    // }
                    // Class<?> classX = mapping.getXydraType();
                    // ValueType vt = ValueType.valueType(classX);
                    // Class<?> classT = ValueType.getComponentType(vt);
                    
                    Class<?> classX;
                    if(classJ.equals(Set.class)) {
                        classX = XIDSetValue.class;
                    } else if(classJ.equals(List.class)) {
                        classX = XIDListValue.class;
                    } else if(classJ.equals(SortedSet.class)) {
                        classX = XIDSortedSetValue.class;
                    } else {
                        throw new IllegalArgumentException("Cannot handle collection type "
                                + classJ.getCanonicalName());
                    }
                    Class<?> classT = XID.class;
                    
                    String gX = classX.getSimpleName();
                    String gT = classT.getSimpleName();
                    String gJ = classJ.getSimpleName();
                    String gC = classC.getSimpleName();
                    ms.sourceLines.add("ITransformer<" + gX + "," + gT + "," + gJ + "<" + gC + ">,"
                            + gC + "> t = new CollectionProxy.ITransformer<" + gX + "," + gT + ","
                            + gJ + "<" + gC + ">," + gC + ">() {");
                    ms.sourceLines.add("    @Override");
                    ms.sourceLines.add("    public " + gC + " toJavaComponent(" + gC + " xid) {");
                    ms.sourceLines.add("        return new " + gwtSimpleName + "(" + gwtSimpleName
                            + ".this.oop.getXModel(), xid);");
                    ms.sourceLines.add("    }");
                    ms.sourceLines.add("");
                    ms.sourceLines.add("    @Override");
                    ms.sourceLines.add("    public " + gT + " toXydraComponent(" + gC
                            + " javaType) {");
                    ms.sourceLines.add("        return javaType.getId();");
                    ms.sourceLines.add("    }");
                    ms.sourceLines.add("");
                    ms.sourceLines.add("    @Override");
                    ms.sourceLines.add("    public " + gX + " createCollection() {");
                    ms.sourceLines.add("        return XV.toID" + gJ
                            + "Value(Collections.EMPTY_LIST);");
                    ms.sourceLines.add("    }");
                    ms.sourceLines.add("");
                    ms.sourceLines.add("};");
                    ms.sourceLines.add("    ");
                    ms.sourceLines.add("return new " + gJ + "Proxy<" + gX + "," + gT + "," + gJ
                            + "<" + gC + ">," + gC
                            + ">(this.oop.getXObject(), XX.toId(\"subTasks\"), t);");
                    break;
                default:
                    break;
                }
                c.members.add(ms);
            }
        }
        return c;
    }
    
}
