package org.xydra.doc;

import java.io.Serializable;

import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.IHasXID;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XID;
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
 * <li>{@link XModel#createObject(XID)}</li>
 * <li>{@link XModel#getObject(XID)}</li>
 * <li>{@link XModel#removeObject(XID)}</li>
 * </ul>
 * <i>provides commands</i>
 * <ul>
 * <li>{@link XModel#executeModelCommand(org.xydra.core.change.XCommand)}</li>
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
 * <li>{@link XBaseModel} <br />
 * <i>provides state read operations</i> <br />
 * <b>extends</b>
 * <ul>
 * <li>{@link IHasXAddress}</li>
 * <li>{@link IHasXID}</li>
 * <li>{@link Iterable}<XID></li>
 * </ul>
 * </li>
 * <li>{@link XSendsModelEvent} <br />
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
 * <li>{@link XSynchronizesChanges#rollback(long)}</li>
 * <li>
 * {@link XSynchronizesChanges#synchronize(java.util.List, long, XID, java.util.List, java.util.List)}
 * </li>
 * </ul>
 * <b>extends</b>
 * <ul>
 * <li>{@link XExecutesCommands}</li>
 * <li>{@link XExecutesTransactions}</li>
 * <li>{@link IHasXAddress}</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * 
 * TODO Why is a {@link XBaseModel} not {@link Serializable} ?
 * 
 * TODO Why does {@link XSynchronizesChanges} does not extend
 * {@link XLoggedModel} or why is there no common super-interface for the
 * getChangeLog operation?
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
