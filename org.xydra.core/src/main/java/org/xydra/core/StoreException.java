package org.xydra.core;

/**
 * Common super-class for Xydra Store exceptions.
 *
 * @author xamde
 */
public class StoreException extends RuntimeException {

    private static final long serialVersionUID = -9078969178565352270L;

    public StoreException(final String message) {
        super(message);
    }

    public StoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
