package org.xydra.oo.generator.codespec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.xydra.annotations.CanBeNull;


/**
 * Knows nothing about ...Specs. Knows just how to write certain things as Java
 * source code to text files.
 * 
 * @author xamde
 */
public class CodeWriter {
    
    public static void writeJavaDocComment(Writer w, String indent, String value)
            throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(value));
        String line = br.readLine();
        while(line != null) {
            line = firstLetterCapital(line);
            lines.add(line);
            line = br.readLine();
        }
        
        for(int i = 0; i < lines.size(); i++) {
            line = lines.get(i);
            if(line.length() > 70) {
                // split into more lines
                List<String> shorterLines = typeset(line, 70);
                lines.remove(i);
                lines.addAll(i, shorterLines);
            }
        }
        
        if(lines.size() == 1) {
            w.write(indent + "/** " + lines.get(0) + " */\n");
        } else {
            w.write(indent + "/** \n");
            for(String l : lines) {
                w.write(indent + " * " + l + " \n");
            }
            w.write(indent + " */\n");
        }
    }
    
    private static String firstLetterCapital(String s) {
        if(s == null)
            return null;
        if(s.length() == 0)
            return "";
        if(s.length() == 1)
            return s.toUpperCase();
        assert s.length() >= 2;
        return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
    }
    
    public static List<String> typeset(String longline, int maxLen) {
        assert !longline.contains("\n");
        
        List<String> shortLines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int lineLen = 0;
        String[] words = longline.split(" ");
        for(String word : words) {
            if(lineLen + word.length() < maxLen) {
                current.append(word + " ");
                lineLen += word.length();
            } else {
                shortLines.add(current.toString());
                current = new StringBuilder();
                current.append(word + " ");
                lineLen = 0;
            }
        }
        shortLines.add(current.toString());
        return shortLines;
    }
    
    private static final int MB1 = 1024 * 1024;
    
    static void writeAnnotation(Writer w, String indent, String annotationName, String stringValue)
            throws IOException {
        w.write("    @");
        w.write(annotationName);
        w.write("(\"");
        w.write(stringValue);
        w.write("\")\n");
    }
    
    static void writeGetterSetter(Writer w, String name, String typeName,
            @CanBeNull String comment, String generatedFrom) throws IOException {
        MethodSpec getter = new MethodSpec("get" + NameUtils.toJavaName(name), typeName,
                generatedFrom);
        getter.returnType.comment = "the current value or null if not defined\n";
        getter.comment = (comment == null ? "" : comment + ".\n");
        getter.comment += "Generated from " + generatedFrom + ".";
        SpecWriter.writeMethodComment(w, "    ", getter);
        writeAnnotation(w, "    ", "Field", NameUtils.toXFieldName(name));
        SpecWriter.writeInterfaceMethod(w, "    ", getter);
        w.write("\n");
        
        MethodSpec setter = new MethodSpec("set" + NameUtils.toJavaName(name), "void",
                generatedFrom);
        FieldSpec param = new FieldSpec(NameUtils.toXFieldName(name), typeName, generatedFrom);
        param.comment = "the value to set";
        setter.params.add(param);
        setter.comment = "Set a value, silently overwriting existing values, if any.\n";
        setter.comment += (comment == null ? "" : comment + ".\n");
        setter.comment += "Generated from " + generatedFrom + ".";
        
        SpecWriter.writeMethodComment(w, "    ", setter);
        writeAnnotation(w, "    ", "Field", NameUtils.toXFieldName(name));
        SpecWriter.writeInterfaceMethod(w, "    ", setter);
        w.write("\n");
    }
    
    public static File toJavaSourceFile(File outDir, String basePackage, String className) {
        assert basePackage != null;
        File sourceDir = new File(outDir, basePackage.replace(".", "/"));
        File javaFile = new File(sourceDir, className + ".java");
        return javaFile;
    }
    
    public static Writer openWriter(File f) throws IOException {
        f.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(f);
        OutputStreamWriter w = new OutputStreamWriter(fos, "utf-8");
        BufferedWriter bw = new BufferedWriter(w, MB1);
        return bw;
    }
    
}
