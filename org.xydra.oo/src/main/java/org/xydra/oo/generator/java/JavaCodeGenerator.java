package org.xydra.oo.generator.java;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.xydra.annotations.CanBeNull;
import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.id.UUID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.oo.generator.Comment;
import org.xydra.oo.generator.codespec.CodeWriter;
import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.generator.codespec.NameUtils;
import org.xydra.oo.generator.codespec.SpecWriter;
import org.xydra.oo.generator.codespec.impl.ClassSpec;
import org.xydra.oo.generator.codespec.impl.ConstructorSpec;
import org.xydra.oo.generator.codespec.impl.FieldSpec;
import org.xydra.oo.generator.codespec.impl.MethodSpec;
import org.xydra.oo.generator.codespec.impl.PackageSpec;
import org.xydra.oo.generator.gwt.GwtCodeGenerator;
import org.xydra.oo.generator.java.GwtModuleXmlSpec.GenerateWith;
import org.xydra.oo.runtime.java.JavaReflectionUtils;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.runtime.java.OOReflectionUtils;
import org.xydra.oo.runtime.shared.IBaseType;
import org.xydra.oo.runtime.shared.IType;
import org.xydra.oo.runtime.shared.TypeSpec;

import com.google.gwt.core.client.GWT;


/**
 * Generates shared interfaces ({@link #generateInterfaces(Class, File, String)}
 * ) and Java factory. For GWT factory, see {@link GwtCodeGenerator}.
 * 
 * @author xamde
 */
public class JavaCodeGenerator {
    
    private static final PackageSpec HasIdPackage = new PackageSpec(IHasXId.class.getPackage()
            .getName(), true);
    
    private static final ClassSpec HasIdClass = HasIdPackage.addInterface(IHasXId.class
            .getSimpleName());
    
    private static final Logger log = LoggerFactory.getLogger(JavaCodeGenerator.class);
    
    static void addCollectionGetter(ClassSpec classSpec, String name, IType t, String comment,
            String generatedFrom) {
        MethodSpec getter = classSpec.addMethod(NameUtils.firstLetterLowercased(name), t,
                generatedFrom);
        getter.returnType.setComment("a writable collection proxy, never null");
        getter.annotateWith(org.xydra.oo.Field.class, NameUtils.toXFieldName(name));
    }
    
    /**
     * @param classSpec to which the getter/setter belong
     * @param name of bean property, e.g. "name" or "age"
     * @param typeSpec specifies the type to be set or get
     * @param comment ends in javadoc
     * @param generatedFrom debug comment
     */
    static void addGetterSetter(ClassSpec classSpec, String name, IType typeSpec,
            @CanBeNull String comment, String generatedFrom) {
        MethodSpec getter = classSpec.addMethod("get" + NameUtils.toJavaName(name), typeSpec,
                generatedFrom);
        getter.returnType.setComment("the current value or null if not defined");
        getter.annotateWith(org.xydra.oo.Field.class, NameUtils.toXFieldName(name));
        
        MethodSpec setter = classSpec.addMethod("set" + NameUtils.toJavaName(name),
                classSpec.asType(), generatedFrom);
        
        /*
         * setter variant that returns void:
         * 
         * MethodSpec setter = classSpec.addVoidMethod("set" +
         * NameUtils.toJavaName(name), generatedFrom);
         */
        FieldSpec param = setter.addParam(NameUtils.toXFieldName(name), typeSpec, generatedFrom);
        param.setComment("the value to set");
        setter.setComment("Set a value, silently overwriting existing values, if any.");
        setter.annotateWith(org.xydra.oo.Field.class, NameUtils.toXFieldName(name));
    }
    
    private static void addGwtGenerateWith(GwtModuleXmlSpec gwtSpec, PackageSpec packageSpec) {
        for(ClassSpec classSpec : packageSpec.classes) {
            ClassSpec currentClass = classSpec;
            
            while(currentClass != null) {
                GenerateWith gw = gwtSpec.new GenerateWith();
                gw.generateWith = GwtCodeGenerator.class;
                gw.whenTypeAssignable = classSpec.getCanonicalName();
                gwtSpec.generateWith.add(gw);
                
                currentClass = currentClass.superClass;
            }
        }
        for(PackageSpec subPackage : packageSpec.subPackages) {
            addGwtGenerateWith(gwtSpec, subPackage);
        }
    }
    
    /**
     * @param clazz
     * @param toBeGeneratedTypesto know during code generation which java
     *            classes will have been translated in the end
     * @param sharedPackage
     * @return a set of members (fields and methods) mapped to the Xydra type
     *         system
     */
    private static Set<IMember> collectMappedMembers(Class<?> clazz,
            Set<Class<?>> toBeGeneratedTypes, String sharedPackage) {
        Set<IMember> classMemberSpecs = new HashSet<IMember>();
        
        for(Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            Class<?> compType = JavaReflectionUtils.getComponentType(field);
            
            FieldSpec fieldSpec = null;
            
            if(field.getName().equals("this$0")) {
                // internal synthetic reference to outer class, ignore
            } else if(OOReflectionUtils.isTranslatableSingleType(type)) {
                fieldSpec = new FieldSpec(field.getName(), type, clazz.getCanonicalName());
            } else if(OOReflectionUtils.isTranslatableCollectionType(type, compType)) {
                fieldSpec = new FieldSpec(field.getName(), type, compType, clazz.getCanonicalName());
            } else if(isToBeGeneratedType(type, toBeGeneratedTypes)) {
                fieldSpec = new FieldSpec(field.getName(), sharedPackage, "I"
                        + type.getSimpleName(), clazz.getCanonicalName());
            } else if(OOReflectionUtils.isToBeGeneratedCollectionType(field, toBeGeneratedTypes)) {
                if(toBeGeneratedTypes.contains(compType)) {
                    fieldSpec = new FieldSpec(field.getName(), type, sharedPackage, "I"
                            + compType.getSimpleName(), clazz.getCanonicalName());
                } else {
                    fieldSpec = new FieldSpec(field.getName(), type, compType,
                            clazz.getCanonicalName());
                }
            } else {
                log.warn("Ignoring field '" + field.getName() + "' of type '" + type + "' in '"
                        + clazz.getCanonicalName() + "' - type could not be translated");
            }
            
            if(fieldSpec != null) {
                String commentText = tryToGetAnnotatedComment(field);
                if(commentText != null)
                    fieldSpec.setComment(commentText);
                fieldSpec.annotateWith(org.xydra.oo.Field.class,
                        NameUtils.toXFieldName(field.getName()));
                classMemberSpecs.add(fieldSpec);
            }
        }
        
        for(Method method : clazz.getDeclaredMethods()) {
            try {
                MethodSpec methodSpec = new MethodSpec(method, clazz.getCanonicalName());
                String commentText = tryToGetAnnotatedComment(method);
                if(commentText != null)
                    methodSpec.setComment(commentText);
                
                // add parameters
                Type[] genericTypes = method.getGenericParameterTypes();
                int n = 0;
                for(Type t : genericTypes) {
                    FieldSpec typeSpec = new FieldSpec("param" + n++, t, clazz.getCanonicalName());
                    methodSpec.params.add(typeSpec);
                }
                classMemberSpecs.add(methodSpec);
            } catch(AssertionError e) {
                log.warn("Problem in " + clazz.getCanonicalName() + "." + method.getName() + "(..)");
                throw e;
            }
        }
        
        return classMemberSpecs;
    }
    
    /**
     * @param basePackage fq name
     * @param sharedPackage subpackage, e.g. "shared"
     * @param specificationCollectionClass
     * @return
     */
    private static PackageSpec convertInnerClassesToPackageSpec(String basePackage,
            String sharedPackage, Class<?> specificationCollectionClass) {
        // round 1: collect all declared inner classes
        Set<Class<?>> toBeGeneratedTypes = new HashSet<Class<?>>();
        for(Class<?> specificationMemberClass : specificationCollectionClass.getDeclaredClasses()) {
            // don't inspect/translate inner Enum-types
            if(specificationMemberClass.isEnum())
                continue;
            
            toBeGeneratedTypes.add(specificationMemberClass);
        }
        // round 2: convert inner classes to PackageSpec
        PackageSpec packageSpec = new PackageSpec(basePackage + "." + sharedPackage, false);
        packageSpec.generatedFrom = specificationCollectionClass;
        for(Class<?> specificationMemberClass : specificationCollectionClass.getDeclaredClasses()) {
            // don't inspect/translate inner Enum-types
            if(specificationMemberClass.isEnum())
                continue;
            
            log.debug("Processing '" + specificationMemberClass.getCanonicalName() + "'");
            ClassSpec result = toClassSpec(packageSpec, specificationMemberClass,
                    toBeGeneratedTypes);
            convertFieldsToGettersAndSetters(result);
        }
        
        return packageSpec;
    }
    
    /**
     * generate getter/setter for all primitive types; generate getter for all
     * object-types; generate method stubs for all methods;
     */
    private static void convertFieldsToGettersAndSetters(ClassSpec classSpec) {
        List<IMember> members = new ArrayList<IMember>(classSpec.members);
        for(IMember t : members) {
            if(t instanceof FieldSpec) {
                FieldSpec fieldSpec = (FieldSpec)t;
                // no collection proxies for arrays
                if(!fieldSpec.getType().isArray()
                        && JavaReflectionUtils.isJavaCollectionType(fieldSpec.getType())) {
                    // expose collection proxy
                    addCollectionGetter(classSpec, t.getName(), fieldSpec.t, t.getComment(),
                            t.getGeneratedFrom());
                } else {
                    /*
                     * map arrays of wrappers for Java primitive types to the
                     * real Java primitive types. Xydra has no nulls in arrays
                     * anyways
                     */
                    TypeSpec normalisedType = new TypeSpec(fieldSpec.t);
                    if(normalisedType.isArray()) {
                        IBaseType normalisedCompType = JavaReflectionUtils
                                .getPrimitiveTypeForWrapperClass(normalisedType.getComponentType());
                        if(normalisedCompType != null) {
                            normalisedType = new TypeSpec(normalisedType.getBaseType(),
                                    normalisedCompType, "normalised");
                        }
                    }
                    addGetterSetter(classSpec, t.getName(), normalisedType, t.getComment(),
                            t.getGeneratedFrom());
                }
                classSpec.members.remove(fieldSpec);
            }
        }
    }
    
    private static void generateFactories(PackageSpec clientPackage, PackageSpec sharedPackage,
            PackageSpec javaPackage) {
        PackageSpec builtIn = new PackageSpec("org.xydra.oo.runtime.shared", true);
        ClassSpec builtInAbstractFactory = builtIn.addClass("AbstractFactory");
        
        ClassSpec abstractSharedFactory = sharedPackage.addAbstractClass("AbstractSharedFactory");
        abstractSharedFactory.superClass = builtInAbstractFactory;
        ConstructorSpec c1 = abstractSharedFactory.addConstructor("generateFactories-" + "HqDUE");
        c1.addParam("model", XWritableModel.class, "generateFactories-" + "76emU");
        c1.sourceLines.add("super(model);");
        
        ClassSpec clientFactory = clientPackage.addClass("GwtFactory");
        clientFactory.setComment("Generated on " + new Date());
        clientFactory.superClass = abstractSharedFactory;
        ConstructorSpec c2 = clientFactory.addConstructor("generateFactories-" + "oTz9R");
        c2.addParam("model", XWritableModel.class, "generateFactories-" + "mqFtF");
        c2.sourceLines.add("super(model);");
        
        ClassSpec javaFactory = javaPackage.addClass("JavaFactory");
        javaFactory.superClass = abstractSharedFactory;
        ConstructorSpec c3 = javaFactory.addConstructor("generateFactories-" + "WaYjk");
        c3.addParam("model", XWritableModel.class, "generateFactories-" + "dctg4");
        c3.sourceLines.add("super(model);");
        
        for(ClassSpec c : sharedPackage.classes) {
            if(!c.isBuiltIn() && c.getName().startsWith("I")) {
                String n = NameUtils.firstLetterUppercased(c.getName().substring(1));
                // public ITask createTask(String idStr) {
                // return createTask(XX.toId(idStr));
                // }
                //
                abstractSharedFactory
                        .addMethod("create" + n, c.getPackageName(), c.getName(),
                                "generateFactories-" + "gn0BV").setAccess("public")
                        .addParam("idStr", String.class, "generateFactories-" + "myHOp")
                        .addSourceLine("return create" + n + "(XX.toId(idStr));");
                abstractSharedFactory.addRequiredImports(XX.class);
                
                // public ITask getTask(String idStr) {
                // return getTask(XX.toId(idStr));
                // }
                abstractSharedFactory
                        .addMethod("get" + n, c.getPackageName(), c.getName(),
                                "generateFactories-" + "UN0yH").setAccess("public")
                        .addParam("idStr", String.class, "generateFactories-" + "2QL32")
                        .addSourceLine("return get" + n + "(XX.toId(idStr));");
                abstractSharedFactory.addRequiredImports(XX.class);
                
                // public ITask createTask(XId id) {
                // if(!hasXObject(id)) { createXObject(id); }
                // return getTaskInternal(this.model, id);
                // }
                abstractSharedFactory
                        .addMethod("create" + n, c.getPackageName(), c.getName(),
                                "generateFactories-" + "2m6U1").setAccess("public")
                        .addParam("id", XId.class, "generateFactories-" + "n8c8H")
                        .addSourceLine("if(!hasXObject(id)) { createXObject(id); }")
                        .addSourceLine("return get" + n + "Internal(this.model, id);");
                //
                // public ITask getTask(XId id) {
                // if(!hasXObject(id)) {
                // return null;
                // }
                // return getTaskInternal(this.model, id);
                // }
                abstractSharedFactory
                        .addMethod("get" + n, c.getPackageName(), c.getName(),
                                "generateFactories-" + "Xz0r2").setAccess("public")
                        .addParam("id", XId.class, "generateFactories-" + "usnO3")
                        .addSourceLine("if (!hasXObject(id)) { return null; }")
                        .addSourceLine("return get" + n + "Internal( this.model, id); ");
                
                // protected abstract ITask getTaskInternal(XWritableModel
                // model, XId id);
                String getInternal = "get" + n + "Internal";
                abstractSharedFactory
                        .addMethod(getInternal, c.getPackageName(), c.getName(),
                                "generateFactories-" + "n6Zvf").setAccess("protected")
                        .setAbstract(true)
                        .addParam("model", XWritableModel.class, "generateFactories-" + "bnAB6")
                        .addParam("id", XId.class, "generateFactories-" + "SEML4");
                
                clientFactory
                        .addMethod(getInternal, c.getPackageName(), c.getName(),
                                "generateFactories-" + "21utp")
                        
                        .annotateWith(Override.class).setAccess("protected")
                        .addParam("model", XWritableModel.class, "generateFactories-" + "Zl7Qm")
                        .addParam("id", XId.class, "generateFactories-" + "aXRfh")
                        
                        .addSourceLine("return wrap" + n + "(model, id);");
                
                clientFactory
                        .addMethod("wrap" + n, c.getPackageName(), c.getName(),
                                "generateFactories-" + "4C46E")
                        
                        .setAccess("public").setStatic(true)
                        .addParam("model", XWritableModel.class, "generateFactories-" + "5uhaQ")
                        .addParam("id", XId.class, "generateFactories-" + "fzf9t")
                        
                        .addSourceLine(c.getName() + " w = GWT.create(" + c.getName() + ".class);")
                        .addSourceLine("w.init(model, id);").addSourceLine("return w;");
                clientFactory.addRequiredImports(GWT.class);
                
                javaFactory
                        .addMethod(getInternal, c.getPackageName(), c.getName(),
                                "generateFactories-" + "Kus1F")
                        .addParam("model", XWritableModel.class, "generateFactories-" + "PIZOa")
                        .addParam("id", XId.class, "generateFactories-" + "vxB4a")
                        .setAccess("protected")
                        .annotateWith(Override.class)
                        .addSourceLine(
                                c.getName() + " w = (" + c.getName() + ") Proxy.newProxyInstance("
                                        + c.getName() + ".class.getClassLoader(),")
                        .addSourceLine(
                                "    new Class<?>[] { " + c.getName()
                                        + ".class }, new OOJavaOnlyProxy(model, id));")
                        .addSourceLine("return w;");
                javaFactory.addRequiredImports(OOJavaOnlyProxy.class);
                javaFactory.addRequiredImports(Proxy.class);
            }
        }
        
    }
    
    private static GwtModuleXmlSpec generateGwtModuleXmlSpec(PackageSpec packageSpec) {
        // prepare GWT module xml
        GwtModuleXmlSpec gwtSpec = new GwtModuleXmlSpec(packageSpec.getFQPackageName(),
                "OODomainModel", null);
        addGwtGenerateWith(gwtSpec, packageSpec);
        gwtSpec.inherits.add("org.xydra.oo.runtime.XydraOoRuntime");
        return gwtSpec;
    }
    
    /**
     * @param spec
     * @param srcDir e.g. "/src"
     * @param basePackage interfaces automatically end in {basePackage}/shared
     *            and get.xml file ends in {basePackage}
     * @throws IOException
     */
    public static void generateInterfaces(Class<?> spec, File srcDir, String basePackage)
            throws IOException {
        log.info("Generating from '" + spec.getCanonicalName() + "'");
        PackageSpec packageSpec = new PackageSpec(basePackage, false);
        
        PackageSpec shared = convertInnerClassesToPackageSpec(basePackage, "shared", spec);
        packageSpec.subPackages.add(shared);
        
        PackageSpec client = new PackageSpec(basePackage + ".client", false);
        packageSpec.subPackages.add(client);
        
        PackageSpec java = new PackageSpec(basePackage + ".java", false);
        packageSpec.subPackages.add(java);
        
        GwtModuleXmlSpec gwtSpec = generateGwtModuleXmlSpec(packageSpec);
        
        generateFactories(client, shared, java);
        
        // generate source code
        packageSpec.dump();
        shared.dump();
        client.dump();
        java.dump();
        
        log.info("Writing to " + srcDir.getAbsolutePath());
        SpecWriter.writePackage(packageSpec, new File(srcDir, "/main/java"));
        writeGwtXml(gwtSpec, basePackage, new File(srcDir, "/main/resources"));
    }
    
    private static boolean isToBeGeneratedType(Class<?> type, Set<Class<?>> mappedTypes) {
        return mappedTypes.contains(type);
    }
    
    /**
     * @param packageSpec @NeverNull
     * @param specClass @NeverNull
     * @param toBeGeneratedTypes @NeverNull
     * @return a new {@link ClassSpec}, attached to the given packageSpec.
     *         Content is based on specClass.
     */
    private static ClassSpec toClassSpec(PackageSpec packageSpec, Class<?> specClass,
            Set<Class<?>> toBeGeneratedTypes) {
        assert packageSpec != null;
        assert specClass != null;
        assert toBeGeneratedTypes != null;
        
        ClassSpec resultSpec = packageSpec.addInterface(NameUtils.toClassName(specClass));
        /*
         * Look in this class and all super-types. Put members at the right
         * level in the hierarchy. Make sure the higher, abstract classes
         * contain as many members as possible. This avoids code duplication in
         * the generated code.
         */
        ClassSpec currentClassSpec = resultSpec;
        Class<?> currentClass = specClass;
        while(currentClass != null && !currentClass.equals(Object.class)) {
            // process this level
            Set<IMember> currentClassMembers = collectMappedMembers(currentClass,
                    toBeGeneratedTypes, packageSpec.getFQPackageName());
            for(IMember member : currentClassMembers) {
                if(!currentClassSpec.members.contains(member)) {
                    log.trace("Adding member '" + member.getName() + "' to '" + currentClassSpec
                            + "'");
                    currentClassSpec.members.add(member);
                }
            }
            // move one level up
            currentClass = currentClass.getSuperclass();
            if(!currentClass.equals(Object.class)) {
                ClassSpec superSpec = packageSpec.addInterface(NameUtils.toClassName(currentClass));
                currentClassSpec.superClass = superSpec;
                currentClassSpec = superSpec;
            } else {
                currentClassSpec.superClass = HasIdClass;
            }
        }
        
        resultSpec.addVoidMethod("init", "toClassSpec 1")
                .addParam("model", XWritableModel.class, "toClassSpec 2")
                .addParam("id", XId.class, "toClassSpec 3").setComment("For GWT-internal use only");
        
        return resultSpec;
    }
    
    private static String tryToGetAnnotatedComment(AccessibleObject ao) {
        String commentText = null;
        Comment comment = ao.getAnnotation(Comment.class);
        if(comment != null)
            commentText = comment.value();
        return commentText;
    }
    
    private static void writeGwtXml(GwtModuleXmlSpec gwtSpec, String basePackage, File outDir)
            throws IOException {
        File dir = new File(outDir.getAbsolutePath() + "/" + basePackage.replace(".", "/"));
        File moduleFile = new File(dir, gwtSpec.moduleName + ".gwt.xml");
        log.info("Writing GWT module file into " + moduleFile.getAbsolutePath());
        Writer w = CodeWriter.openWriter(moduleFile);
        w.write(gwtSpec.toString());
        w.close();
    }
    
    public static void main(String[] args) throws IOException {
        
        String s = FileUtils
                .readFileToString(new File(
                        "/Users/xamde/_data_/_p_/2013/org.xydra.oo/src/main/java/org/xydra/oo/generator/java/JavaCodeGenerator.java"));
        
        while(s.contains("\"a\"")) {
            s = s.replaceFirst("[\"]a[\"]", "\"" + UUID.uuid(5) + "\"");
        }
        FileUtils
                .writeStringToFile(
                        new File(
                                "/Users/xamde/_data_/_p_/2013/org.xydra.oo/src/main/java/org/xydra/oo/generator/java/JavaCodeGenerator.java"),
                        s);
        
    }
    
}
