package org.xydra.core;



/**
 * An exception type which will be thrown if someone tries to access (e.g. read,
 * write or administer) a protected repository, model, object or field without
 * the allowance to do so. Access protection is defined and implemented in Xydra
 * Store.
 *
 * @author dscharrer
 */
public class AccessException extends StoreException {

    private static final long serialVersionUID = -5273702667318966040L;

    public AccessException(final String message) {
        super(message);
    }

}
