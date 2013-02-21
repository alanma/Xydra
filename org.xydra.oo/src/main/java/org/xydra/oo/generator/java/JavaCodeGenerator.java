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

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.generator.Comment;
import org.xydra.oo.generator.codespec.ClassSpec;
import org.xydra.oo.generator.codespec.CodeWriter;
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
        PackageSpec packageSpec = convertInnerClassesToPackageSpec(basePackage, spec);
        packageSpec.fullPackageName = basePackage + ".shared";
        
        // generate source code
        log.info("Writing to " + outDir.getAbsolutePath());
        SpecWriter.writePackage(packageSpec, outDir);
        writeGwtXml(packageSpec, basePackage, outDir);
    }
    
    private static boolean isToBeGeneratedType(Class<?> type, Set<Class<?>> mappedTypes) {
        return mappedTypes.contains(type);
    }
    
    private static ClassSpec toClassSpec(Class<?> specClass, Set<Class<?>> toBeGeneratedTypes) {
        ClassSpec resultSpec = new ClassSpec("interface", NameUtils.toClassName(specClass));
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
                ClassSpec superSpec = new ClassSpec("interface", NameUtils.toClassName(superClass));
                classSpec.superClass = superSpec;
                classSpec = superSpec;
            }
        }
        return resultSpec;
    }
    
    private static PackageSpec convertInnerClassesToPackageSpec(String basePackage, Class<?> spec) {
        // collect all declared inner classes
        Set<Class<?>> toBeGeneratedTypes = new HashSet<>();
        for(Class<?> specClass : spec.getDeclaredClasses()) {
            toBeGeneratedTypes.add(specClass);
        }
        // convert inner classes to PackageSpec
        PackageSpec packageSpec = new PackageSpec();
        packageSpec.generatedFrom = spec;
        for(Class<?> specClass : spec.getDeclaredClasses()) {
            ClassSpec classSpec = toClassSpec(specClass, toBeGeneratedTypes);
            packageSpec.classes.add(classSpec);
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
    
    private static void writeGwtXml(PackageSpec packageSpec, String basePackage, File outDir)
            throws IOException {
        // prepare GWT module xml
        GwtModuleXmlSpec gwtSpec = new GwtModuleXmlSpec();
        gwtSpec.moduleName = "OODomainModel";
        
        for(ClassSpec classSpec : packageSpec.classes) {
            ClassSpec currentClass = classSpec;
            
            while(currentClass != null) {
                GenerateWith gw = gwtSpec.new GenerateWith();
                gw.generateWith = GwtCodeGenerator.class;
                gw.whenTypeAssignable = packageSpec.fullPackageName + "." + classSpec.getName();
                gwtSpec.generateWith.add(gw);
                
                currentClass = currentClass.superClass;
            }
        }
        
        // write GWT module XML
        File dir = new File(outDir.getAbsolutePath() + "/" + basePackage.replace(".", "/"));
        File moduleFile = new File(dir, gwtSpec.moduleName + ".gwt.xml");
        log.info("Writing GWT module file into " + moduleFile.getAbsolutePath());
        Writer w = CodeWriter.openWriter(moduleFile);
        w.write(gwtSpec.toString());
        w.close();
    }
}
