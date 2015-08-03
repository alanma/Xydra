package org.xydra.core.model.impl.memory.garbage;

import java.util.List;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


@SuppressWarnings("javadoc")
public interface XLocalChanges {

    /**
     * @return
     */
    public List<LocalChange> getList();

    /**
     *
     */
    public void clear();

    /**
     * @param command
     * @param event
     */
    public void append(XCommand command, XEvent event);

    /**
     * @return
     */
    public int countUnappliedLocalChanges();

    /**
     * @param syncRevision
     */
    public void setSyncRevision(long syncRevision);

    /**
     * @return
     */
    public long getSynchronizedRevision();

}
