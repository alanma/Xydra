package org.xydra.conf.impl;

import org.xydra.conf.escape.Escaping;

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
        this.w.write(Escaping.escape(key, true, false));
        this.w.write("=");
        this.w.write(value == null ? "" : Escaping.escape(value, true, false));
        this.w.write("\n");
    }
    
    public void comment(String comment) throws IOException {
        assert comment != null;
        this.w.write("# " + comment + "\n");
    }
    
}
