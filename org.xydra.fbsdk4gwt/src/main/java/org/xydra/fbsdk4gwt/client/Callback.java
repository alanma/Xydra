package org.xydra.fbsdk4gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * Callback that by default logs the response.
 */
public class Callback<T> implements AsyncCallback<T> {

    
    public Callback () {
    }

    @Override
    public void onFailure(Throwable caught) {
        throw new RuntimeException ( caught );
    }

    @Override
    public void onSuccess(T result) {
        GWT.log ( result + "" , null );
    }
    
    

}