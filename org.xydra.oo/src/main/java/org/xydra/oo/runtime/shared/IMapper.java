package org.xydra.oo.runtime.shared;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.value.XValue;


/**
 * Maps from a java type + component type to a Xydra type -- and back
 * 
 * @author xamde
 */
@RunsInGWT(true)
public interface IMapper {
    
    /**
     * @param x xydra value @NeverNull
     * @return @NeverNull
     * @param <J> Java type
     * @param <C> Java component type
     * @param <X> Xydra type
     */
    <J, C, X extends XValue> J toJava(@NeverNull X x);
    
    /**
     * @param j java object @NeverNull
     * @return @NeverNull
     * @param <J> Java type
     * @param <C> Java component type
     * @param <X> Xydra type
     */
    <J, C, X extends XValue> X toXydra(@NeverNull J j);
    
}
