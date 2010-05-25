/**
 * This package contains simple state-entities. This layer serves as the minimal
 * service provider interface (SPI) for implementing different persistence
 * strategies.
 * 
 * State-entities know their children by {@link org.xydra.core.model.XID} and their
 * parents by {@link org.xydra.core.model.XAddress}. This allows truly lazy loading,
 * e.g., of an object with a given XAddress.
 * 
 * The persistence keys are based on the full entities address.
 * <em>Therefore all parents must be set BEFORE persisting an entity.</em>
 * 
 * Creation and loading of State-entities is performed via
 * {@link XSPI#getStateStore()}.create.../load...
 */
package org.xydra.core.model.state;

