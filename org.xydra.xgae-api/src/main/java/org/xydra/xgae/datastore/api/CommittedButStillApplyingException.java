package org.xydra.xgae.datastore.api;

public class CommittedButStillApplyingException extends RuntimeException {
    
    private static final long serialVersionUID = 5181603692209183255L;
    
    public CommittedButStillApplyingException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public CommittedButStillApplyingException(String msg) {
        super(msg);
    }
    
}
