package org.xydra.core.model.session;

import org.xydra.core.model.IHasXAddress;


public interface XProtectedSynchronizesChanges extends IHasXAddress, XProtectedExecutesCommands,
        XProtectedExecutesTransactions {
}
