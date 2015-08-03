package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.persistence.XydraPersistence;


/**
 * The binding for platform-specific {@link XydraRuntime} implementation.
 *
 * @author xamde
 *
 */
public interface XydraPlatformRuntime {

    /**
     * @param repositoryId XId of new repository
     * @return a new instance of a platform specific {@link XydraPersistence}
     *         implementation with the given repositoryId
     */
    public XydraPersistence createPersistence(XId repositoryId);

    /**
     * Signals the Xydra platform that the current (web) request has been
     * finished.
     *
     * A server implementation might wish to e.g. clear thread-local variables
     * to defend against a web container which is recycling threads.
     */
    public void finishRequest();

    /**
     * Signals the Xydra platform that a new (web) request has been started.
     *
     * A server implementation might wish to e.g. clear thread-local variables
     * to defend against a web container which is recycling threads.
     */
    public void startRequest();

    /**
     * @return a short descriptive name if platform implementation
     */
    public String getName();

}
