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
        this.w.write(PropertyFileEscaping.escape(key));
        this.w.write("=");
        this.w.write(value == null ? "" : PropertyFileEscaping.escape(value));
        this.w.write("\n");
    }
    
    public void comment(String comment) throws IOException {
        assert comment != null;
        this.w.write("# " + comment + "\n");
    }
    
}
