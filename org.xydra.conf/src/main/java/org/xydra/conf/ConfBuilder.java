package org.xydra.conf;

public class ConfBuilder {
    
    private final String key;
    private final IConfig config;
    
    public ConfBuilder(IConfig config, String key) {
        this.config = config;
        this.key = key;
    }
    
    public IConfig setDocumentation(String documentation) {
        this.config.setDocumentation(this.key, documentation);
        return this.config;
    }
    
}
