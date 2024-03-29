package org.xydra.core.model;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XCommand XCommands}.
 */
public interface XExecutesCommands {

    /**
     * Execute the given {@link XCommand} if possible.
     *
     * Not all implementations will be able to execute all commands.
     *
     * @param command The {@link XCommand} which is to be executed
     *
     * @return {@link XCommand#FAILED} if the command failed,
     *         {@link XCommand#NOCHANGE} if the command didn't change anything
     *         or the revision number of the {@link XEvent} caused by the
     *         command.
     * @throws IllegalStateException if this entity has already been removed
     */
    @ModificationOperation
    long executeCommand(XCommand command);

}
