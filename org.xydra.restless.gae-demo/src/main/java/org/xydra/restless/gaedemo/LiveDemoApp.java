package org.xydra.restless.gaedemo;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.restless.example.ExampleResource;


public class LiveDemoApp {
    
    public void restless(Restless r, String path) {
        ExampleResource.restless(r);
        TimeResource.restless(r);
        
        /** a rather useless exception handler */
        r.addExceptionHandler(new RestlessExceptionHandler() {
            
            @Override
            public boolean handleException(Throwable t, IRestlessContext restlessContext) {
                System.err.println("Restless error");
                throw new RuntimeException("Something went wrong for URI: "
                        + restlessContext.getRequest().getRequestURI(), t);
            }
            
        });
        
    }
    
}
