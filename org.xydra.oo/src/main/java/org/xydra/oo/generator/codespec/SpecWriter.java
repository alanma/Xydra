package org.xydra.oo.generator.codespec;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.CanBeNull;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.Field;
import org.xydra.oo.generator.java.JavaCodeGenerator;


/**
 * Writes ...Spec data objects into Java source code via {@link CodeWriter}.
 * 
 * @author xamde
 */
public class SpecWriter {
    
    private static final Logger log = LoggerFactory.getLogger(JavaCodeGenerator.class);
    
    static void writeClass(Writer w, ClassSpec classSpec) throws IOException {
        writeClass(w, classSpec.getPackageName(), classSpec);
    }
    
    public static void writeClass(Writer w, String packageName, ClassSpec classSpec)
            throws IOException {
        // package org.xydra.oo.test.tasks;
        //
        w.write("package " + packageName + ";\n");
        w.write("\n");
        
        // import java.util.*;
        // import org.xydra.oo.*;
        //
        Set<String> imports = new HashSet<>();
        if(classSpec.superClass != null) {
            imports.add(classSpec.superClass.getCanonicalName());
        }
        for(IMember t : classSpec.members) {
            for(String req : t.getRequiredImports()) {
                imports.add(req);
            }
        }
        for(String imp : imports) {
            w.write("import " + imp + ";\n");
        }
        w.write("\n");
        
        // /**
        // * Generated on @@ by @@
        // */
        // public interface ITaskList {
        //
        CodeWriter.writeJavaDocComment(w, "", "Generated on " + new Date() + " by "
                + SpecWriter.class.getSimpleName() + ", a part of xydra.org:oo");
        w.write("public " + classSpec.kind + " " + classSpec.getName() + " ");
        if(classSpec.superClass != null) {
            w.write("extends " + classSpec.superClass.getName());
        }
        if(!classSpec.implementedInterfaces.isEmpty()) {
            w.write("implements ");
            for(int i = 0; i < classSpec.implementedInterfaces.size(); i++) {
                String s = classSpec.implementedInterfaces.get(i);
                w.write(s);
                if(i < classSpec.implementedInterfaces.size()) {
                    w.write(", ");
                }
            }
        }
        w.write(" {\n");
        w.write("\n");
        
        Collections.sort(classSpec.members);
        writeMethodsAndFields(w, classSpec);
        
        // }
        w.write("}\n");
    }
    
    private static void writeClassMethod(Writer w, String indent, MethodSpec method)
            throws IOException {
        writeMethodHead(w, indent, method);
        writeMethodBody(w, indent, method);
    }
    
    private static void writeCollectionGetter(Writer w, String name, TypeSpec t, String comment,
            String generatedFrom) throws IOException {
        MethodSpec getter = new MethodSpec(NameUtils.firstLetterLowercased(name), t.getTypeName(),
                generatedFrom);
        getter.returnType.comment = "a writable collection proxy, never null\n";
        getter.comment = (comment == null ? "" : comment + ".\n");
        getter.comment += "Generated from " + generatedFrom + ".";
        writeMethodComment(w, "    ", getter);
        
        CodeWriter.writeAnnotation(w, "    ", "Field", NameUtils.toXFieldName(name));
        writeInterfaceMethod(w, "    ", getter);
        w.write("\n");
    }
    
    static void writeInterfaceMethod(Writer w, String indent, MethodSpec method) throws IOException {
        writeMethodHead(w, indent, method);
        w.write(";\n");
    }
    
    static void writeMethodComment(Writer w, String indent,
            AbstractConstructorOrMethodSpec methodOrConstructor) throws IOException {
        StringBuilder comment = new StringBuilder();
        if(methodOrConstructor.comment != null) {
            comment.append(methodOrConstructor.comment);
            comment.append("\n");
            comment.append("\n");
        }
        for(FieldSpec p : methodOrConstructor.params) {
            comment.append("@param ");
            comment.append(p.getName());
            comment.append(" ");
            comment.append(p.comment == null ? "" : p.comment);
        }
        if(methodOrConstructor instanceof MethodSpec) {
            MethodSpec method = (MethodSpec)methodOrConstructor;
            if(!method.isVoid()) {
                comment.append("@return ");
                comment.append(method.returnType.comment == null ? "..."
                        : method.returnType.comment);
            }
        }
        CodeWriter.writeJavaDocComment(w, indent, comment.toString());
    }
    
    static void writeMethodHead(Writer w, String indent, MethodSpec method) throws IOException {
        for(AnnotationSpec<?> ann : method.annotations) {
            CodeWriter.writeAnnotation(w, indent, ann.annot.getSimpleName(), ann.value.toString());
        }
        w.write(indent);
        w.write(method.getReturnTypeName());
        w.write(" ");
        w.write(method.getName());
        writeMethodParams(w, method);
    }
    
    private static void writeMethodParams(Writer w, AbstractConstructorOrMethodSpec com)
            throws IOException {
        w.write("(");
        for(FieldSpec p : com.params) {
            w.write(p.getTypeName());
            w.write(" ");
            w.write(p.getName());
        }
        w.write(")");
        
    }
    
    /**
     * generate getter/setter for all primitive types; generate getter for all
     * object-types; generate method stubs for all methods;
     * 
     * @param w
     * @param superClass
     * @throws IOException
     */
    private static void writeMethodsAndFields(Writer w, ClassSpec classSpec) throws IOException {
        for(IMember t : classSpec.members) {
            if(t instanceof FieldSpec) {
                FieldSpec fieldSpec = (FieldSpec)t;
                
                if(fieldSpec.isCollectionType()) {
                    // expose collection proxy
                    writeCollectionGetter(w, t.getName(), fieldSpec.t, t.getComment(),
                            t.getGeneratedFrom());
                } else {
                    SpecWriter.writeGetterSetter(w, t.getName(), fieldSpec.getTypeName(),
                            t.getComment(), t.getGeneratedFrom());
                }
            } else if(t instanceof ConstructorSpec) {
                ConstructorSpec constructorSpec = (ConstructorSpec)t;
                constructorSpec.comment = combinedComment(t);
                writeMethodComment(w, "    ", constructorSpec);
                writeConstructor(w, "    ", constructorSpec);
            } else {
                assert t instanceof MethodSpec;
                MethodSpec methodSpec = (MethodSpec)t;
                methodSpec.comment = combinedComment(t);
                writeMethodComment(w, "    ", methodSpec);
                if(classSpec.kind.equals("interface")) {
                    writeInterfaceMethod(w, "    ", methodSpec);
                } else {
                    writeClassMethod(w, "   ", methodSpec);
                }
                
                // String comment = (t.getComment() != null ? t.getComment() +
                // "\n\n" : "");
                // comment += "Generated from " + t.getGeneratedFrom();
                // JavaSourceCodeWriter.writeJavaDocComment(w, "    ", comment);
                // StringBuilder params = new StringBuilder();
                // for(int i = 0; i < methodSpec.params.size(); i++) {
                // FieldSpec p = methodSpec.params.get(i);
                // params.append(p.getTypeName());
                // params.append(" ");
                // params.append(p.getName());
                // if(i < methodSpec.params.size()) {
                // params.append(", ");
                // }
                // }
                // w.write("    " + methodSpec.getReturnTypeName() + " " +
                // methodSpec.getName() + "("
                // + params.toString() + ");\n");
                w.write("\n");
            }
        }
        
    }
    
    private static void writeConstructor(Writer w, String indent, ConstructorSpec method)
            throws IOException {
        w.write(indent);
        w.write("public ");
        w.write(method.getName());
        writeMethodParams(w, method);
        writeMethodBody(w, indent, method);
    }
    
    private static void writeMethodBody(Writer w, String indent, AbstractConstructorOrMethodSpec com)
            throws IOException {
        w.write(" {\n");
        for(String line : com.sourceLines) {
            w.write(indent + "    " + line + "\n");
        }
        w.write(indent + "}\n");
    }
    
    private static String combinedComment(IMember t) {
        String comment = (t.getComment() != null ? t.getComment() + "\n\n" : "");
        comment += "Generated from " + t.getGeneratedFrom();
        return comment;
    }
    
    public static void writeClass(ClassSpec classSpec, File outDir) throws IOException {
        if(classSpec.isBuiltIn())
            return;
        
        File javaFile = CodeWriter.toJavaSourceFile(outDir, classSpec.getPackageName(),
                classSpec.getName());
        Writer w = CodeWriter.openWriter(javaFile);
        writeClass(w, classSpec);
        w.close();
    }
    
    public static void writePackage(PackageSpec packageSpec, File outDir) throws IOException {
        log.info("Writing package " + packageSpec.getFQPackageName() + " to "
                + outDir.getAbsolutePath());
        
        if(!packageSpec.isBuiltIn()) {
            int i = 0;
            for(ClassSpec classSpec : packageSpec.classes) {
                ClassSpec currentClass = classSpec;
                
                while(currentClass != null) {
                    writeClass(currentClass, outDir);
                    i++;
                    
                    currentClass = currentClass.superClass;
                }
            }
            log.info("Wrote " + i + " files");
        }
        for(PackageSpec subPackage : packageSpec.subPackages) {
            writePackage(subPackage, outDir);
        }
    }
    
    static void writeGetterSetter(Writer w, String name, String typeName,
            @CanBeNull String comment, String generatedFrom) throws IOException {
        MethodSpec getter = new MethodSpec("get" + NameUtils.toJavaName(name), typeName,
                generatedFrom);
        getter.returnType.comment = "the current value or null if not defined\n";
        getter.comment = (comment == null ? "" : comment + ".\n");
        getter.comment += "Generated from " + generatedFrom + ".";
        writeMethodComment(w, "    ", getter);
        getter.annotations
                .add(new AnnotationSpec<String>(Field.class, NameUtils.toXFieldName(name)));
        writeInterfaceMethod(w, "    ", getter);
        w.write("\n");
        
        MethodSpec setter = new MethodSpec("set" + NameUtils.toJavaName(name), "void",
                generatedFrom);
        FieldSpec param = new FieldSpec(NameUtils.toXFieldName(name), typeName, generatedFrom);
        param.comment = "the value to set";
        setter.params.add(param);
        setter.comment = "Set a value, silently overwriting existing values, if any.\n";
        setter.comment += (comment == null ? "" : comment + ".\n");
        setter.comment += "Generated from " + generatedFrom + ".";
        writeMethodComment(w, "    ", setter);
        setter.annotations
                .add(new AnnotationSpec<String>(Field.class, NameUtils.toXFieldName(name)));
        writeInterfaceMethod(w, "    ", setter);
        w.write("\n");
    }
    
}
