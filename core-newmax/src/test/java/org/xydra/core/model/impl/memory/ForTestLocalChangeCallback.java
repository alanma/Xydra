/**
 * 
 */
package org.xydra.core.model.impl.memory;

import org.xydra.base.change.XCommand;
import org.xydra.core.model.XLocalChangeCallback;
import org.xydra.sharedutils.XyAssert;


/**
 * {@link XLocalChangeCallback} implementation that allows test to wait until
 * the callback is invoked.
 * 
 * @author dscharrer
 * 
 */
class ForTestLocalChangeCallback implements XLocalChangeCallback {
    
    private boolean committed = false;
    private long result;
    
    synchronized public boolean hasBeenCalled() {
        return this.committed;
    }
    
    @Override
    synchronized public void onFailure() {
        
        assert !this.committed : "double fail/apply detected";
        
        this.committed = true;
        this.result = XCommand.FAILED;
        notifyAll();
    }
    
    @Override
    synchronized public void onSuccess(long revision) {
        
        assert !this.committed : "double fail/apply detected";
        
        XyAssert.xyAssert(revision >= 0 || revision == XCommand.NOCHANGE);
        
        this.committed = true;
        this.result = revision;
        notifyAll();
    }
    
    synchronized public long waitForResult() {
        
        long time = System.currentTimeMillis();
        while(!this.committed) {
            
            assert System.currentTimeMillis() - time <= 1000 : "timeout waiting for command to apply";
            
            try {
                wait(1100);
            } catch(InterruptedException e) {
                // ignore
            }
        }
        
        return this.result;
    }
    
}
