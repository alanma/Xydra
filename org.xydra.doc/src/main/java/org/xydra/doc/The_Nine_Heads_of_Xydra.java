package org.xydra.doc;

import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleField;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.rmof.impl.memory.SimpleRepository;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStore;
import org.xydra.store.access.XAccessControlManager;
import org.xydra.store.access.XAuthenticationDatabase;
import org.xydra.store.access.XAuthorisationDatabase;
import org.xydra.store.access.XAuthorisationDatabaseWitListeners;
import org.xydra.store.access.XAuthorisationEvent;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.XGroupEvent;
import org.xydra.store.access.impl.delegate.AccessControlManagerOnPersistence;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.impl.delegate.DelegateToPersistenceAndAcm;
import org.xydra.store.impl.delegate.XydraBlockingStore;
import org.xydra.store.impl.delegate.XydraSingleOperationStore;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.memory.MemoryPersistence;


/**
 * Xydra is a module that helps to build rich social (web) applications easily.
 * This document gives a holistic overview on the 'Nine Heads of Xydra' -
 * whenever you understood a feature you find another one you were just wishing
 * Xydra would have.
 * 
 * Rich applications require some local data. Social applications need access
 * rights control. Web applications need sophisticated JavaScript. Xydra offers
 * all of it:
 * <dl>
 * <dt>Persistence</dt>
 * <dd>Data from users and from the application needs to be stored reliably.</dd>
 * <dt>Synchronisation</dt>
 * <dd>Each user can have a copy of some data and synchronise it with other
 * users or servers later.</dd>
 * <dt>Control</dt>
 * <dd>Authorisation (accounts and passwords), Access rights (hierarchical and
 * with groups) define who can see or change what.</dd>
 * <dt>Versioning</dt>
 * <dd>All changes are recorded and can be retrieved or reverted.</dd>
 * <dt>Multiple platforms</dt>
 * <dd>Xydra runs on Java, Google AppEngine (GAE/J), and in Google Web Toolkit
 * (GWT).</dd>
 * <dt>Efficiency</dt>
 * <dd>The lower-level {@link XydraStore} API supports batch operations,
 * asynchronous usage, and transaction semantic.</dd>
 * <dt>Ease of use</dt>
 * <dd>The higher-level {@link XRepository} API adds event listeners and
 * convenient Collection-style add/remove/set methods.</dd>
 * <dt>Documented and Tested</dt>
 * <dd>A growing set of JUnit tests and tutorials makes Xydra safe, fast, and
 * easy to use.</dd>
 * <dt>Future proof</dt>
 * <dd>The Xydra design is compatible with the upcoming IndexedDB standard for
 * storing data in HTML5.</dd>
 * </dl>
 * 
 * 
 * 
 * <h2>High-level API ({@link XRepository})</h2>
 * 
 * The main elements of the Xydra data model are:
 * <dl>
 * <dt>{@link XValue}</dt>
 * <dd>The smallest unit of data. A {@link XValue} can be a string (
 * {@link XStringValue}), integer ({@link XIntegerValue}), boolean (
 * {@link XBooleanValue}) or identifier ({@link XId}). Xydra also treats
 * collections such as lists ({@link XStringListValue}) and sets (
 * {@link XStringSetValue}) as values. Values are created via a
 * {@link XValueFactory} which you get from {@link X#getValueFactory()}.</dd>
 * <dt>{@link XField}</dt>
 * <dd>Fields are the variables in Xydra. One field has none or one value.
 * Fields are like fields in an object-oriented class, hence the name. A field
 * always has an identifier which is a {@link XId}.</dd>
 * <dt>{@link XObject}</dt>
 * <dd>Objects group several fields (and their values). In this respect, objects
 * are like objects in object-oriented programming. Each object has an
 * identifier which is also an {@link XId}.</dd>
 * <dt>{@link XModel}</dt>
 * <dd>Models are data management units and store a set of objects (and their
 * fields and values). Each model has an {@link XId} as well. Like objects and
 * fields also complete models can be serialised and sent around. Models are a
 * good unit for data sharing, e.g. each user can have its own model or several
 * users can share a model.</dd>
 * <dt>{@link XRepository}</dt>
 * <dd>This represents the complete set of data on certain machine. A repository
 * is the non-serializable manager that holds a set of models.</dd>
 * </dl>
 * Another way to think about the Xydra structure is to see the
 * {@link XRepository} as a key-value store where model-ID.object-ID.field-ID
 * are a hierarchical key and the {@link XValue} is the value.
 * 
 * Yet another perspective would say a {@link XRepository} is a nested map. In
 * Java Generics syntax: a Map<XId,Map<XId,Map<XId,XValue>>.
 * 
 * 
 * 
 * <h2>Low-level API ({@link XydraStore})</h2>
 * 
 * The {@link XydraStore} has been designed with network efficiency in mind. It
 * support batch operations and asynchronous usage (via call-back objects, as
 * known from GWT).
 * 
 * <h4>Design note: Snapshots vs. {@link XEvent}</h4> To be able to deliver all
 * events that happened, a {@link XydraStore} must store a list of all occurred
 * events. A snapshot can be computed from a list of events - but not vice
 * versa. If many events change parts of a model over and over again, a snapshot
 * becomes much more space-efficient than is corresponding event list. Hence
 * Xydra offers also retrieval of snapshots via its API.
 * 
 * {@link XydraSingleOperationStore} reduces {@link XydraStore} to single
 * operation methods. The {@link XydraBlockingStore} is the <em>synchronous</em>
 * version of it. The synchronous version is easier to use locally. Also,
 * locally, there is no need for batch operations.
 * 
 * The implementation of {@link XydraBlockingStore} (
 * {@link DelegateToPersistenceAndAcm}) uses internally two parts: A
 * {@link XydraPersistence} to ultimately persist data and a
 * {@link XAccessControlManager} to decide on authorisation and access rights.
 * 
 * The {@link XydraPersistence} is the service provider interface (SPI) for
 * different storage systems. Currently there are two implementations: In-memory
 * ({@link MemoryPersistence}) and on Google AppEngine ({@link GaePersistence}).
 * Future implementations for HTML5/IndexedDB and persistent on Java (likely via
 * OrientDB.org) are planned. Many implementations of {@link XydraPersistence}
 * use internally {@link SimpleRepository} / {@link SimpleModel} /
 * {@link SimpleObject} / {@link SimpleField} (see below).
 * 
 * The user accounts and access rights managed via {@link XAccessControlManager}
 * are also stored in a {@link XydraPersistence}, usually the same as the user
 * data. Naming conventions (defined in {@link NamingUtils}) keep different data
 * sets separate.
 * 
 * <pre>
 * {@link XydraStore} 
 *        |
 *        | delegates to
 *       \_/
 * {@link XydraSingleOperationStore} 
 *        |
 *        | delegates to
 *       \_/
 * {@link XydraBlockingStore} 
 *        |
 *        | persists         checks authorisation 
 *        | data in          and access rights via
 *        +--------------------------+
 *        |                          |
 *       \_/                        \_/
 * {@link XydraPersistence}         {@link XAccessControlManager}
 *        |                          |
 *        | in-memory                | synchronises account data 
 *        | implementation           | and access definitions 
 *        | persists in              | with a
 *       \_/                        \_/
 * {@link SimpleRepository}         {@link XydraPersistence}
 * </pre>
 * 
 * 
 * <h2>Design notes</h2>
 * 
 * <h4>Notations</h4> R = repository, M = model, O = object, F = field, V =
 * value.
 * 
 * 
 * <h4>{@link XCommand} and {@link XEvent}</h4> A successfully executed command
 * is logged as an event. However, in order to compute if a command (in
 * particular: a {@link XTransaction} event), implicit commands need to be
 * computed. E.g. if a command wants to remove an object this implicitly removes
 * all fields of that object first.
 * 
 * 
 * <h4>Bootstrapping</h4> The simplest possible data structure to implement
 * Xydra are <em>read-only</em> variants of {@link XRepository} / {@link XModel}
 * / {@link XObject} / {@link XField} / {@link XValue}. These are offered as
 * {@link XReadableRepository} / {@link XReadableModel} /
 * {@link XReadableObject} / {@link XReadableField}. Values themselves are
 * already modelled as unchangeable value objects. The read-only variants are
 * implemented several times for different purposes.
 * 
 * XBase{RMO} offers e.g. {@link XReadableModel#getId()},
 * {@link XReadableModel#iterator()} ,
 * {@link XReadableModel#getRevisionNumber()}. {@link XReadableField} has
 * {@link XReadableField#getValue()}.
 * 
 * The next bootstrapping level are simple variants that support read and write,
 * without any semantic checks. The interfaces for them are
 * {@link XWritableRepository} / {@link XWritableModel} /
 * {@link XWritableObject} / {@link XWritableField}.
 * 
 * XWritable{RMOF} extends XBase{RMOF} and offers additionally e.g.
 * {@link XWritableModel#createObject(XId)} and
 * {@link XWritableModel#removeObject(XId)}. {@link XWritableField} has
 * respectively {@link XWritableField#setValue(XValue)}. Note that XWritable...
 * does not allow to set the revision number.
 * 
 * XWritable{RMOF} is implemented by RevWritable{RMOF} in its simplest version.
 * RevWritable{RMOF} offers in addition e.g.
 * {@link XRevWritableModel#addObject(org.xydra.base.rmof.XRevWritableObject)} ,
 * and {@link XRevWritableModel#setRevisionNumber(long)} which make it suitable
 * to act as the data container for simple in-memory implementations.
 * 
 * 
 * <h4>{@link XydraStore} vs. {@link XRepository}</h4> A common use case
 * consists of a central server with a REST API accesses via a
 * {@link XydraStore} instance and a local in-memory partial copy of data
 * managed accessed via a {@link XRepository} instance. At runtime, changes need
 * to be synchronised. This requires the {@link XRepository} implementation to
 * record all changes made locally since the last successful synchronisation.
 * And it requires the ability to apply remove events as local commands, thereby
 * firing local change listeners. This operation is further complicated by the
 * fact that some local changes are in conflict with remote changes (e.g. local:
 * set value to A, remote: set value to B).
 * 
 * 
 * <h4>Access Control Implementation</h4> Xydra uses an
 * {@link XAccessControlManager}. Internally, this manager uses an
 * {@link XAuthenticationDatabase} for authentication and an
 * {@link XAuthorisationManager} for authorisation.
 * 
 * The {@link XAuthenticationDatabase} to persist user accounts, their password
 * and other authentication data such as the number of failed login attempts.
 * 
 * The {@link XAuthorisationManager} uses an
 * {@link XAuthorisationDatabaseWitListeners} from which access right
 * definitions are initially loaded. At runtime, the fast internal data
 * structures of {@link XAuthorisationManager} are kept up-to-date by listening
 * to {@link XAuthorisationEvent} events from the
 * {@link XAuthorisationDatabaseWitListeners}. In a similar way a
 * {@link XGroupDatabaseWithListeners} is used to initially build fast
 * actor-group indexes which are kept up-to-date by listening to the
 * {@link XGroupEvent}.
 * 
 * 
 * <h4>Access control implementation</h4> There are several challenges when
 * implementing access rights:
 * <ul>
 * <li>Speed: Access control is called for <em>every</em> access, hence it must
 * be really fast. Hence it must be implemented completely with in-memory data
 * structures.</li>
 * <li>Persistent: accounts, groups and access rights need to be persisted,
 * ultimately in a {@link XydraPersistence}.</li>
 * <li>Up-to-date: On AppEngine, another instance might change the underlying
 * data, this must be reflected in the in-memory data structures.</li>
 * </ul>
 * 
 * Speed is supported via {@link MemoryAuthorisationManager} which implements
 * {@link XAuthorisationManager} which in turn is a
 * {@link XAuthorisationDatabase}.
 * 
 * Persistence can be achieved via a {@link AccessControlManagerOnPersistence}
 * 
 * To be continued one day...
 * 
 * <h2>Document status</h2>
 * 
 * Version 1, Karlsruhe, 2011-01-18 -- see SVN details for true meta-data :-)
 * 
 * @author xamde
 * 
 */
public interface The_Nine_Heads_of_Xydra {
    
}
