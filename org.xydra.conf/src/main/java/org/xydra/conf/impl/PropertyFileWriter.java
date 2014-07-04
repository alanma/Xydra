package org.xydra.conf.impl;

import java.io.IOException;
import java.io.Writer;


/**
 * Writes java property files with comments
 * 
 * @author xamde
 * 
 */
public class PropertyFileWriter {
    
    private Writer w;
    
    public PropertyFileWriter(Writer w) {
        this.w = w;
    }
    
    /**
     * @param key
     * @param value @CanBeNull
     * @throws IOException
     */
    public void keyValue(String key, String value) throws IOException {
        assert key != null;
        this.w.write(escape(key));
        this.w.write("=");
        this.w.write(value == null ? "" : escape(value));
        this.w.write("\n");
    }
    
    private static String escape(String raw) {
        assert raw != null;
        String s = raw;
        s = s.replace("\n", "\\\n");
        s.replace(":", "\\:");
        s.replace("=", "\\=");
        return s;
    }
    
    public void comment(String comment) throws IOException {
        assert comment != null;
        this.w.write("# " + comment + "\n");
    }
    
}
