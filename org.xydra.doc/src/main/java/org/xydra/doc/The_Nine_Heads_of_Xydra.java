package org.xydra.doc;

import org.xydra.base.XReadableField;
import org.xydra.base.XReadableModel;
import org.xydra.base.XReadableObject;
import org.xydra.base.XReadableRepository;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableField;
import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableObject;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.store.NamingUtils;
import org.xydra.store.XydraStore;
import org.xydra.store.base.SimpleField;
import org.xydra.store.base.SimpleModel;
import org.xydra.store.base.SimpleObject;
import org.xydra.store.base.SimpleRepository;
import org.xydra.store.impl.delegate.DelegateToPersistenceAndArm;
import org.xydra.store.impl.delegate.XAuthorisationArm;
import org.xydra.store.impl.delegate.XydraBlockingStore;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.delegate.XydraSingleOperationStore;
import org.xydra.store.impl.gae.GaePersistence;
import org.xydra.store.impl.memory.MemoryPersistence;

import com.sun.org.apache.xpath.internal.objects.XObject;


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
 * {@link XBooleanValue}) or identifier ({@link XID}). Xydra also treats
 * collections such as lists ({@link XStringListValue}) and sets (
 * {@link XStringSetValue}) as values. Values are created via a
 * {@link XValueFactory} which you get from {@link X#getValueFactory()}.</dd>
 * <dt>{@link XField}</dt>
 * <dd>Fields are the variables in Xydra. One field has none or one value.
 * Fields are like fields in an object-oriented class, hence the name. A field
 * always has an identifier which is a {@link XID}.</dd>
 * <dt>{@link XObject}</dt>
 * <dd>Objects group several fields (and their values). In this respect, objects
 * are like objects in object-oriented programming. Each object has an
 * identifier which is also an {@link XID}.</dd>
 * <dt>{@link XModel}</dt>
 * <dd>Models are data management units and store a set of objects (and their
 * fields and values). Each model has an {@link XID} as well. Like objects and
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
 * Java Generics syntax: a Map<XID,Map<XID,Map<XID,XValue>>.
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
 * {@link DelegateToPersistenceAndArm}) uses internally two parts: A
 * {@link XydraPersistence} to ultimately persist data and a
 * {@link XAuthorisationArm} to decide on authorisation and access rights.
 * 
 * The {@link XydraPersistence} is the service provider interface (SPI) for
 * different storage systems. Currently there are two implementations: In-memory
 * ({@link MemoryPersistence}) and on Google AppEngine ({@link GaePersistence}).
 * Future implementations for HTML5/IndexedDB and persistent on Java (likely via
 * OrientDB.org) are planned. Many implementations of {@link XydraPersistence}
 * use internally {@link SimpleRepository} / {@link SimpleModel} /
 * {@link SimpleObject} / {@link SimpleField} (see below).
 * 
 * The user accounts and access rights managed via {@link XAuthorisationArm} are
 * also stored in a {@link XydraPersistence}, usually the same as the user data.
 * Naming conventions (defined in {@link NamingUtils}) keep different data sets
 * separate.
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
 * {@link XydraPersistence}         {@link XAuthorisationArm}
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
 * {@link XReadableRepository} / {@link XReadableModel} / {@link XReadableObject} /
 * {@link XReadableField}. Values themselves are already modelled as unchangeable
 * value objects. The read-only variants are implemented several times FIXME too
 * many times, it seems.
 * 
 * XBase{RMO} offers e.g. {@link XReadableModel#getID()},
 * {@link XReadableModel#iterator()} , {@link XReadableModel#getRevisionNumber()}.
 * {@link XReadableField} has {@link XReadableField#getValue()}.
 * 
 * The next bootstrapping level are simple variants that support read and write,
 * without any semantic checks. The interfaces for them are
 * {@link XHalfWritableRepository} / {@link XHalfWritableModel} /
 * {@link XHalfWritableObject} / {@link XHalfWritableField}.
 * 
 * XWritable{RMOF} extends XBase{RMOF} and offers additionally e.g.
 * {@link XHalfWritableModel#createObject(XID)} and
 * {@link XHalfWritableModel#removeObject(XID)}. {@link XHalfWritableField} has
 * respectively {@link XHalfWritableField#setValue(XValue)}. Note that XWritable...
 * does not allow to set the revision number.
 * 
 * XWritable{RMOF} is implemented by Simple{RMOF} in its simplest version.
 * Simple{RMOF} offers in addition e.g.
 * {@link SimpleModel#addObject(org.xydra.store.base.SimpleObject)}, and
 * {@link SimpleModel#setRevisionNumber(long)} which make it suitable to act as
 * the data container for simple in-memory implementations.
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
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
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

// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .
// .

