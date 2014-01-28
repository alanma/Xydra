package org.xydra.doc;

import java.io.Serializable;

import org.xydra.base.IHasXAddress;
import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XSynchronizesChanges;


/**
 * Xydra uses a client-server concept to let several clients communicate via a
 * persistent store. Clients can be used without a server.
 * 
 * A client-side model uses the {@link XModel} API.
 * 
 * The inheritance graph is:
 * <ul>
 * <li>{@link XModel} <br />
 * <i>provides easy changes</i>
 * <ul>
 * <li>{@link XModel#createObject(XId)}</li>
 * <li>{@link XModel#getObject(XId)}</li>
 * <li>{@link XModel#removeObject(XId)}</li>
 * </ul>
 * <i>provides commands</i>
 * <ul>
 * <li>{@link XModel#executeModelCommand(org.xydra.base.change.XModelCommand)}</li>
 * </ul>
 * <b>extends</b>
 * <ul>
 * <li>{@link Serializable} <br />
 * <i>usage in GWT</i></li>
 * <li>{@link XLoggedModel} <br />
 * <i>provides reading the change log</i>
 * <ul>
 * <li>{@link XChangeLog} {@link XLoggedModel#getChangeLog()}</li>
 * </ul>
 * <b>extends</b>
 * <ul>
 * <li>{@link XReadableModel} <br />
 * <i>provides state read operations</i> <br />
 * <b>extends</b>
 * <ul>
 * <li>{@link IHasXAddress}</li>
 * <li>{@link IHasXId}</li>
 * <li>{@link Iterable}<XId></li>
 * </ul>
 * </li>
 * <li>{@link XSendsModelEvents} <br />
 * <i>provides model event listener</i></li>
 * <li>{@link XSendsObjectEvents} <br />
 * <i>provides object event listener</i></li>
 * <li>{@link XSendsFieldEvents} <br />
 * <i>provides field event listener</i></li>
 * <li>{@link XSendsTransactionEvents} <br />
 * <i>provides transaction event listener</i></li>
 * </ul>
 * </li>
 * <li>{@link XSynchronizesChanges} <br />
 * <i>allows to fetch and apply remote changes</i>
 * <ul>
 * <li>{@link XChangeLog} {@link XSynchronizesChanges#getChangeLog()}</li>
 * <li>{@link XSynchronizesChanges}</li>
 * <li>
 * </ul>
 * <b>extends</b>
 * <ul>
 * <li>{@link XExecutesCommands}</li>
 * <li>{@link IHasXAddress}</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * <h3>Orthogonal features</h3>
 * <ul>
 * <li>serialisable</li>
 * <li>can be used in GWT</li>
 * <li>read-only</li>
 * <li>access control</li>
 * <li>transactions</li>
 * <li>read changelog</li>
 * <li>rollback</li>
 * <li>synchronize client-server</li>
 * <li>event listener</li>
 * <li>usage over HTTP</li>
 * <li>persistence</li>
 * </ul>
 * 
 * <h3>Usage scenarios</h3>
 * <ul>
 * <li>XModel client standalone in-memory in plain old Java</li>
 * <li>XModel client standalone in-memory on AppEngine</li>
 * <li>XModel client standalone in-memory in GWT</li>
 * <li>XModel client standalone in-memory in plain old Java, synchronising
 * changes to a server via Java method calls <br />
 * Potential errors: Failed commands</li>
 * <li>XModel client in-memory on AppEngine, synchronising changes to a server
 * via Java method calls <br />
 * Potential errors: Failed commands</li>
 * <li>XModel client in-memory in plain old Java, synchronising changes to a
 * server via REST (XML over HTTP) <br />
 * Potential errors: Failed commands; HTTP errors</li>
 * <li>XModel client in-memory on AppEngine, synchronising changes to a server
 * via REST (XML over HTTP) <br />
 * Potential errors: Failed commands; HTTP errors</li>
 * <li>XModel client in-memory in GWT, synchronising changes to a server via
 * REST (XML over HTTP) <br />
 * Potential errors: Failed commands; HTTP errors</li>
 * </ul>
 * 
 * 
 * 
 * @author voelkel
 * 
 */
public class ArchitectureOverview {
    
}
