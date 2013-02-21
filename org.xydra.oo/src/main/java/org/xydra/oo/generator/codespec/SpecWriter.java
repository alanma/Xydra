package org.xydra.oo.generator.codespec;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.xydra.base.IHasXID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.oo.generator.java.JavaCodeGenerator;


/**
 * Writes ...Spec data objects into Java source code via {@link CodeWriter}.
 * 
 * @author xamde
 */
public class SpecWriter {
    
    private static final Logger log = LoggerFactory.getLogger(JavaCodeGenerator.class);
    
    static void writeClass(Writer w, PackageSpec packageSpec, ClassSpec classSpec)
            throws IOException {
        writeClass(w, packageSpec.fullPackageName, classSpec);
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
        if(classSpec.superClass == null) {
            imports.add(IHasXID.class.getCanonicalName());
        }
        imports.add(org.xydra.oo.Field.class.getCanonicalName());
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
        CodeWriter.writeJavaDocComment(w, "", "Generated on " + new Date()
                + " by PersistenceSpec2InterfacesGenerator, a part of Xydra.org");
        w.write("public " + classSpec.kind + " " + classSpec.getName() + " ");
        if(classSpec.superClass != null) {
            w.write("extends " + classSpec.superClass.getName());
        } else {
            w.write("extends " + IHasXID.class.getSimpleName());
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
        w.write(" {\n");
        for(String line : method.sourceLines) {
            w.write(line + "\n");
        }
        w.write("}\n");
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
    
    static void writeMethodComment(Writer w, String indent, MethodSpec method) throws IOException {
        StringBuilder comment = new StringBuilder();
        if(method.comment != null) {
            comment.append(method.comment);
            comment.append("\n");
            comment.append("\n");
        }
        for(FieldSpec p : method.params) {
            comment.append("@param ");
            comment.append(p.getName());
            comment.append(" ");
            comment.append(p.comment);
        }
        if(!method.isVoid()) {
            comment.append("@return ");
            comment.append(method.returnType.comment == null ? "..." : method.returnType.comment);
        }
        CodeWriter.writeJavaDocComment(w, indent, comment.toString());
    }
    
    static void writeMethodHead(Writer w, String indent, MethodSpec method) throws IOException {
        w.write(indent);
        w.write(method.getReturnTypeName());
        w.write(" ");
        w.write(method.getName());
        w.write("(");
        for(FieldSpec p : method.params) {
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
                    CodeWriter.writeGetterSetter(w, t.getName(), fieldSpec.getTypeName(),
                            t.getComment(), t.getGeneratedFrom());
                }
            } else {
                assert t instanceof MethodSpec;
                MethodSpec methodSpec = (MethodSpec)t;
                
                String comment = (t.getComment() != null ? t.getComment() + "\n\n" : "");
                comment += "Generated from " + t.getGeneratedFrom();
                methodSpec.comment = comment;
                
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
    
    public static void write(PackageSpec packageSpec, ClassSpec classSpec, File outDir)
            throws IOException {
        File javaFile = CodeWriter.toJavaSourceFile(outDir, packageSpec.fullPackageName,
                classSpec.getName());
        Writer w = CodeWriter.openWriter(javaFile);
        SpecWriter.writeClass(w, packageSpec, classSpec);
        w.close();
    }
    
    public static void writePackage(PackageSpec packageSpec, File outDir) throws IOException {
        log.info("Writing package " + packageSpec.fullPackageName + " to "
                + outDir.getAbsolutePath());
        
        int i = 0;
        for(ClassSpec classSpec : packageSpec.classes) {
            ClassSpec currentClass = classSpec;
            
            while(currentClass != null) {
                write(packageSpec, currentClass, outDir);
                i++;
                
                currentClass = currentClass.superClass;
            }
        }
        log.info("Wrote " + i + " files");
    }
    
}
