package org.xydra.conf.impl;

import org.xydra.conf.IConfig;
import org.xydra.index.iterator.Iterators;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;


/**
 * Support for reading/writing {@link IConfig}
 * 
 * IMPROVE augment generated comments with info found in ConfParam.. annotations
 * 
 * @author xamde
 */
public class ConfigFiles {
    
    public static void write(IConfig conf, File file) throws IOException {
        
        FileOutputStream fos = new FileOutputStream(file);
        Writer w = new OutputStreamWriter(fos, "utf-8");
        PropertyFileWriter pfw = new PropertyFileWriter(w);
        
        pfw.comment("=== Written on " + new Date() + " ===");
        
        Set<String> explicitly = new HashSet<String>();
        Iterators.addAll(conf.getExplicitlyDefinedKeys().iterator(), explicitly);
        
        // one alphabetically sorted list
        for(String key : conf.getDefinedKeys()) {
            String typeComment = "";
            Object o = conf.get(key);
            String value;
            if(o instanceof String) {
                value = (String)o;
            } else if(o instanceof Enum<?>) {
                typeComment += "type = Enum " + ((Enum<?>)o).getDeclaringClass().getCanonicalName();
                value = ((Enum<?>)o).name();
            } else if(o instanceof Boolean) {
                typeComment += "type = boolean";
                value = "" + o;
            } else if(o instanceof Integer) {
                typeComment += "type = integer";
                value = "" + o;
            } else if(o instanceof Long) {
                typeComment += "type = long";
                value = "" + o;
            } else {
                pfw.comment("type of '" + key + "' = unknown. Class is '"
                        + o.getClass().getCanonicalName() + "'");
                pfw.comment("  Value serialized as '" + o.toString() + "'");
                continue;
            }
            
            if(typeComment.length() > 0) {
                pfw.comment(typeComment);
            }
            String doc = conf.getDocumentation(key);
            if(doc != null) {
                pfw.comment("** " + doc + " **#");
            }
            if(explicitly.contains(key)) {
                pfw.keyValue(key, value);
            } else {
                pfw.comment("  default: " + key + " = " + (value == null ? "" : value));
            }
        }
        w.close();
        fos.close();
    }
    
    /**
     * Drop all comments from file;
     * 
     * Overwrite settings in <code>conf</code> with definitions from file
     * 
     * @param file
     * @param conf
     * @throws IOException
     */
    public static void read(File file, IConfig conf) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        Reader r = new InputStreamReader(fin, "utf-8");
        
        Properties props = new Properties();
        props.load(r);
        
        // copy props to config
        for(Object o : props.keySet()) {
            String key = (String)o;
            conf.set(key, props.get(key));
        }
        r.close();
        fin.close();
    }
}
