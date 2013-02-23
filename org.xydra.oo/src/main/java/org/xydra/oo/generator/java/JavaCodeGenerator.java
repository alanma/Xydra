package org.xydra.oo.generator.java;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.xydra.base.IHasXID;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.generator.Comment;
import org.xydra.oo.generator.codespec.AnnotationSpec;
import org.xydra.oo.generator.codespec.ClassSpec;
import org.xydra.oo.generator.codespec.CodeWriter;
import org.xydra.oo.generator.codespec.ConstructorSpec;
import org.xydra.oo.generator.codespec.FieldSpec;
import org.xydra.oo.generator.codespec.IMember;
import org.xydra.oo.generator.codespec.MethodSpec;
import org.xydra.oo.generator.codespec.NameUtils;
import org.xydra.oo.generator.codespec.PackageSpec;
import org.xydra.oo.generator.codespec.SpecWriter;
import org.xydra.oo.generator.gwt.GwtCodeGenerator;
import org.xydra.oo.generator.java.GwtModuleXmlSpec.GenerateWith;
import org.xydra.oo.runtime.java.TypeTool;
import org.xydra.oo.runtime.shared.OOTypeBridge;


public class JavaCodeGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(JavaCodeGenerator.class);
    
    private static Set<IMember> collectMappedMembers(Class<?> clazz,
            Set<Class<?>> toBeGeneratedTypes) {
        Set<IMember> classMemberSpecs = new HashSet<>();
        
        for(Field field : clazz.getDeclaredFields()) {
            Class<?> type = field.getType();
            Class<?> compType = TypeTool.getComponentType(field);
            
            String commentText = tryToGetAnnotatedComment(field);
            FieldSpec fieldSpec = null;
            
            if(field.getName().equals("this$0")) {
                // internal reference to outer class, ignore
            } else if(OOTypeBridge.isTranslatableSingleType(type)) {
                fieldSpec = new FieldSpec(field.getName(), type, clazz.getCanonicalName());
            } else if(OOTypeBridge.isTranslatableCollectionType(type, compType)) {
                fieldSpec = new FieldSpec(field.getName(), type, compType, clazz.getCanonicalName());
            } else if(isToBeGeneratedType(type, toBeGeneratedTypes)) {
                fieldSpec = new FieldSpec(field.getName(), "I" + type.getSimpleName(),
                        clazz.getCanonicalName());
            } else if(OOTypeBridge.isToBeGeneratedCollectionType(field, toBeGeneratedTypes)) {
                if(toBeGeneratedTypes.contains(compType)) {
                    fieldSpec = new FieldSpec(field.getName(), type,
                            "I" + compType.getSimpleName(), clazz.getCanonicalName());
                } else {
                    fieldSpec = new FieldSpec(field.getName(), type, compType,
                            clazz.getCanonicalName());
                }
            } else {
                log.warn("Ignoring field '" + field.getName() + "' of type '" + type + "' in '"
                        + clazz.getCanonicalName() + "'");
            }
            
            if(fieldSpec != null) {
                if(commentText != null)
                    fieldSpec.comment = commentText;
                fieldSpec.annotations.add(new AnnotationSpec<>(org.xydra.oo.Field.class, NameUtils
                        .toXFieldName(field.getName())));
                classMemberSpecs.add(fieldSpec);
            }
        }
        
        for(Method method : clazz.getDeclaredMethods()) {
            MethodSpec methodSpec = new MethodSpec(method, clazz.getCanonicalName());
            String commentText = tryToGetAnnotatedComment(method);
            methodSpec.comment = commentText;
            
            // add parameters
            Type[] genericTypes = method.getGenericParameterTypes();
            int n = 0;
            for(Type t : genericTypes) {
                FieldSpec typeSpec = new FieldSpec("param" + n++, t, clazz.getCanonicalName());
                methodSpec.params.add(typeSpec);
            }
            classMemberSpecs.add(methodSpec);
        }
        
        return classMemberSpecs;
    }
    
    /**
     * @param spec
     * @param outDir e.g. "/src/main/java"
     * @param basePackage interfaces automatically end in {basePackage}/shared
     *            and get.xml file ends in {basePackage}
     * @throws IOException
     */
    public static void generateInterfaces(Class<?> spec, File outDir, String basePackage)
            throws IOException {
        log.info("Generating from " + spec.getCanonicalName());
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
        
        log.info("Writing to " + outDir.getAbsolutePath());
        SpecWriter.writePackage(packageSpec, outDir);
        writeGwtXml(gwtSpec, basePackage, outDir);
    }
    
    private static void generateFactories(PackageSpec clientPackage, PackageSpec sharedPackage,
            PackageSpec javaPackage) {
        PackageSpec builtIn = new PackageSpec("org.xydra.oo.runtime.shared", true);
        ClassSpec builtInAbstractFactory = new ClassSpec(builtIn, "class", "AbstractFactory");
        
        ClassSpec sharedFactory = new ClassSpec(sharedPackage, "abstract class",
                "AbstractSharedFactory");
        sharedFactory.superClass = builtInAbstractFactory;
        ConstructorSpec c1 = new ConstructorSpec(sharedFactory, "generateFactories");
        c1.params.add(new FieldSpec("model", XWritableModel.class, "generateFactories"));
        c1.sourceLines.add("super(model);");
        sharedFactory.members.add(c1);
        
        ClassSpec clientFactory = new ClassSpec(clientPackage, "class", "GwtFactory");
        clientFactory.superClass = sharedFactory;
        ConstructorSpec c2 = new ConstructorSpec(clientFactory, "generateFactories");
        c2.params.add(new FieldSpec("model", XWritableModel.class, "generateFactories"));
        c2.sourceLines.add("super(model);");
        clientFactory.members.add(c2);
        
        ClassSpec javaFactory = new ClassSpec(javaPackage, "class", "JavaFactory");
        javaFactory.superClass = sharedFactory;
        ConstructorSpec c3 = new ConstructorSpec(javaFactory, "generateFactories");
        c3.params.add(new FieldSpec("model", XWritableModel.class, "generateFactories"));
        c3.sourceLines.add("super(model);");
        javaFactory.members.add(c3);
    }
    
    private static boolean isToBeGeneratedType(Class<?> type, Set<Class<?>> mappedTypes) {
        return mappedTypes.contains(type);
    }
    
    private static final PackageSpec HasIdPackage = new PackageSpec(IHasXID.class.getPackage()
            .getName(), true);
    
    private static final ClassSpec HasIdClass = new ClassSpec(HasIdPackage, "interface",
            IHasXID.class.getSimpleName());
    
    private static ClassSpec toClassSpec(PackageSpec packageSpec, Class<?> specClass,
            Set<Class<?>> toBeGeneratedTypes) {
        ClassSpec resultSpec = new ClassSpec(packageSpec, "interface",
                NameUtils.toClassName(specClass));
        ClassSpec classSpec = resultSpec;
        
        /*
         * Look in this class and all super-types. Add lowest members first, to
         * account for overwriting in the inheritance tree.
         */
        Class<?> superClass = specClass;
        while(superClass != null && !superClass.equals(Object.class)) {
            Set<IMember> extracted = collectMappedMembers(superClass, toBeGeneratedTypes);
            for(IMember t : extracted) {
                if(!classSpec.members.contains(t)) {
                    classSpec.members.add(t);
                }
            }
            superClass = superClass.getSuperclass();
            if(!superClass.equals(Object.class)) {
                ClassSpec superSpec = new ClassSpec(packageSpec, "interface",
                        NameUtils.toClassName(superClass));
                classSpec.superClass = superSpec;
                classSpec = superSpec;
            } else {
                classSpec.superClass = HasIdClass;
            }
        }
        return resultSpec;
    }
    
    /**
     * @param basePackage fq name
     * @param sharedPackage subpackage, e.g. "shared"
     * @param spec
     * @return
     */
    private static PackageSpec convertInnerClassesToPackageSpec(String basePackage,
            String sharedPackage, Class<?> spec) {
        // collect all declared inner classes
        Set<Class<?>> toBeGeneratedTypes = new HashSet<>();
        for(Class<?> specClass : spec.getDeclaredClasses()) {
            toBeGeneratedTypes.add(specClass);
        }
        // convert inner classes to PackageSpec
        PackageSpec packageSpec = new PackageSpec(basePackage + "." + sharedPackage, false);
        packageSpec.generatedFrom = spec;
        for(Class<?> specClass : spec.getDeclaredClasses()) {
            toClassSpec(packageSpec, specClass, toBeGeneratedTypes);
        }
        return packageSpec;
    }
    
    private static String tryToGetAnnotatedComment(AccessibleObject ao) {
        String commentText = null;
        Comment comment = ao.getAnnotation(Comment.class);
        if(comment != null)
            commentText = comment.value();
        return commentText;
    }
    
    private static GwtModuleXmlSpec generateGwtModuleXmlSpec(PackageSpec packageSpec) {
        // prepare GWT module xml
        GwtModuleXmlSpec gwtSpec = new GwtModuleXmlSpec(packageSpec.getFQPackageName(),
                "OODomainModel");
        addGwtGenerateWith(gwtSpec, packageSpec);
        gwtSpec.inherits.add("org.xydra.oo.runtime.XydraOoRuntime");
        return gwtSpec;
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
    
}
