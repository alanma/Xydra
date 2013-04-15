package org.xydra.base.change;

/**
 * An {@link XEvent} that is not made up of other {@link XEvent XEvents}.
 * 
 */
public interface XAtomicEvent extends XEvent {
    
    /**
     * {@inheritDoc}
     * 
     * Never returns {@link ChangeType#TRANSACTION}.
     */
    @Override
    ChangeType getChangeType();
    
}
