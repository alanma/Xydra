package org.xydra.webadmin.gwt.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;


/**
 * All resources used within the application
 * 
 * @author alpha
 */
public interface BundledRes extends ClientBundle {
    
    public interface Images extends ClientBundle {
        
        @Source("img/foo.png")
        ImageResource foo();
        
    }
    
    public static final BundledRes INSTANCE = GWT.create(BundledRes.class);
    
    public Images images();
    
}
