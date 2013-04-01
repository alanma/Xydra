package org.xydra.core.change;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.index.IndexUtils;
import org.xydra.index.impl.MapMapIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.KeyKeyEntryTuple;
import org.xydra.index.query.Wildcard;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * A helper class to minimise the number and size of persistence accesses.
 * 
 * Does not support revision numbers.
 * 
 * An implementation of {@link XWritableModel} that works as a diff on top of a
 * base {@link XWritableModel}. Via {@link #toCommandList(boolean)} a minimal
 * list of commands that changes the base model into the current state can be
 * created. The base model is changed at no times.
 * 
 * @author xamde
 */
public class DiffWritableModel extends AbstractDelegatingWritableModel implements XWritableModel {
    
    private static final Logger log = LoggerFactory.getLogger(DiffWritableModel.class);
    
    /** use two underscores to get sorted alphabetically even before _ids */
    static final XId NONE = XX.toId("__NoId_DWModel");
    
    static final XValue NOVALUE = XV.toValue("__NoValue_DWModel");
    
    /*
     * Each index has the structure (object, field, value) with the notion to
     * represent added/removed content in this model within a given repository.
     * 
     * An added/removed, empty object without fields is represented as
     * (objectId, NONE, NOVALUE).
     * 
     * An added/removed, empty field without values is represented as (objectId,
     * fieldId, NOVALUE).
     * 
     * A added/removed value is represented as (objectId, fieldId, value).
     */
    MapMapIndex<XId,XId,XValue> added, removed;
    
    private final XWritableModel base;
    
    /**
     * @param base any model
     */
    public DiffWritableModel(final XWritableModel base) {
        XyAssert.xyAssert(base != null);
        assert base != null;
        this.base = base;
        this.added = new MapMapIndex<XId,XId,XValue>();
        this.removed = new MapMapIndex<XId,XId,XValue>();
    }
    
    private XValue getFieldValueFromBase(XId objectId, XId fieldId) {
        XWritableObject o = this.base.getObject(objectId);
        if(o == null)
            return null;
        XWritableField f = o.getField(fieldId);
        if(f == null)
            return null;
        return f.getValue();
    }
    
    @Override
    public XWritableObject createObject(@NeverNull XId objectId) {
        if(hasObject(objectId)) {
            return getObject(objectId);
        }
        
        this.removed.deIndex(objectId, NONE);
        this.added.index(objectId, NONE, NOVALUE);
        return new WrappedObject(objectId);
    }
    
    @Override
    protected XValue field_getValue(XId objectId, XId fieldId) {
        assert hasObject(objectId) : "Object '" + resolveObject(objectId)
                + "' not found when looking for field '" + fieldId + "'";
        XyAssert.xyAssert(getObject(objectId).hasField(fieldId));
        
        XValue value = this.added.lookup(objectId, fieldId);
        if(value != null) {
            if(value == NOVALUE) {
                return null;
            }
            return value;
        } else {
            value = this.removed.lookup(objectId, fieldId);
            if(value != null) {
                return null;
            } else {
                return this.base.getObject(objectId).getField(fieldId).getValue();
            }
        }
    }
    
    @Override
    protected boolean field_setValue(XId objectId, XId fieldId, XValue value) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        assert hasObject(objectId) : "Missing object with id " + objectId + " to set field "
                + fieldId;
        XyAssert.xyAssert(this.removed.lookup(objectId, fieldId) == null);
        XyAssert.xyAssert(getObject(objectId).hasField(fieldId));
        
        XValue v = field_getValue(objectId, fieldId);
        if((v == null && value == null) || (v != null && v.equals(value))) {
            return false;
        }
        
        this.removed.deIndex(objectId, fieldId);
        this.added.index(objectId, fieldId, value);
        return true;
    }
    
    @Override
    public XAddress getAddress() {
        return this.base.getAddress();
    }
    
    /**
     * Allows to end a transaction and go back to using the base object.
     * 
     * @return the base object that has been used to created this wrapped
     *         {@link DiffWritableModel}.
     */
    public XWritableModel getBase() {
        return this.base;
    }
    
    @Override
    public XId getId() {
        return this.base.getId();
    }
    
    @Override
    public boolean hasObject(@NeverNull XId objectId) {
        if(this.added.containsKey(new EqualsConstraint<XId>(objectId), new Wildcard<XId>())) {
            XyAssert.xyAssert(!this.removed.containsKey(new EqualsConstraint<XId>(objectId),
                    new EqualsConstraint<XId>(NONE)));
            return true;
        } else if(this.removed.containsKey(new EqualsConstraint<XId>(objectId),
                new EqualsConstraint<XId>(NONE))) {
            XyAssert.xyAssert(!this.added.containsKey(new EqualsConstraint<XId>(objectId),
                    new Wildcard<XId>()));
            return false;
        } else {
            return this.base.hasObject(objectId);
        }
    }
    
    protected Set<XId> idsAsSet() {
        Set<XId> set = IndexUtils.diff(this.base.iterator(), this.added.key1Iterator(),
                this.removed.key1Iterator());
        set.remove(NONE);
        return set;
    }
    
    @Override
    public boolean isEmpty() {
        return this.idsAsSet().isEmpty();
    }
    
    @Override
    public Iterator<XId> iterator() {
        return this.idsAsSet().iterator();
    }
    
    @Override
    protected XWritableField object_createField(XId objectId, XId fieldId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        XyAssert.xyAssert(this.hasObject(objectId));
        if(!object_hasField(objectId, fieldId)) {
            this.removed.deIndex(objectId, fieldId);
            this.added.index(objectId, fieldId, NOVALUE);
        }
        return new WrappedField(objectId, fieldId);
    }
    
    @Override
    protected boolean object_hasField(XId objectId, XId fieldId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        if(this.added.tupleIterator(new EqualsConstraint<XId>(objectId),
                new EqualsConstraint<XId>(fieldId)).hasNext()) {
            return true;
        }
        if(this.removed.tupleIterator(new EqualsConstraint<XId>(objectId),
                new EqualsConstraint<XId>(fieldId)).hasNext()) {
            return false;
        }
        // else
        return this.base.hasObject(objectId) && this.base.getObject(objectId).hasField(fieldId);
    }
    
    @Override
    protected boolean object_isEmpty(XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        return object_idsAsSet(objectId).isEmpty();
    }
    
    @Override
    protected Iterator<XId> object_iterator(XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        return object_idsAsSet(objectId).iterator();
    }
    
    @Override
    protected boolean object_removeField(XId objectId, XId fieldId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        XyAssert.xyAssert(fieldId != null);
        assert fieldId != null;
        boolean hasField = object_hasField(objectId, fieldId);
        if(hasField) {
            if(this.added.containsKey(new EqualsConstraint<XId>(objectId),
                    new EqualsConstraint<XId>(fieldId))) {
                this.added.deIndex(objectId, fieldId);
            } else {
                this.removed.index(objectId, fieldId, NOVALUE);
            }
        }
        return hasField;
    }
    
    protected Set<XId> object_idsAsSet(XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        
        Constraint<XId> c1 = new EqualsConstraint<XId>(objectId);
        Constraint<XId> c2 = new Wildcard<XId>();
        
        // add all from base
        Set<XId> set = new HashSet<XId>();
        if(this.base.hasObject(objectId)) {
            set.addAll(IndexUtils.toSet(this.base.getObject(objectId).iterator()));
        }
        // remove all from removed
        Iterator<KeyKeyEntryTuple<XId,XId,XValue>> it = this.removed.tupleIterator(c1, c2);
        while(it.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XValue> entry = it.next();
            set.remove(entry.getKey2());
        }
        // add all from added
        it = this.added.tupleIterator(c1, c2);
        while(it.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XValue> entry = it.next();
            set.add(entry.getKey2());
        }
        // done
        set.remove(NONE);
        return set;
    }
    
    @Override
    public boolean removeObject(@NeverNull XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        
        Constraint<XId> c1 = new EqualsConstraint<XId>(objectId);
        Constraint<XId> c2 = new Wildcard<XId>();
        if(this.added.containsKey(c1, c2)) {
            // fine
            IndexUtils.deIndex(this.added, c1, c2);
            return true;
        } else if(this.removed.containsKey(c1, c2)) {
            XyAssert.xyAssert(!this.added.containsKey(c1, c2));
            return false;
        } else {
            // base
            this.removed.index(objectId, NONE, NOVALUE);
            return true;
        }
    }
    
    /**
     * @param forced if true, create forced commands
     * 
     *            TODO implement semantics of 'forced'
     * 
     * @return a list of commands which transform the base model given at
     *         creation time into this current model state
     */
    public List<XAtomicCommand> toCommandList(boolean forced) {
        List<XAtomicCommand> list = new LinkedList<XAtomicCommand>();
        
        // remove
        Iterator<KeyKeyEntryTuple<XId,XId,XValue>> it = this.removed.tupleIterator(
                new Wildcard<XId>(), new Wildcard<XId>());
        while(it.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XValue> objectFieldValue = it.next();
            if(objectFieldValue.getKey2().equals(NONE)) {
                // remove object
                list.add(X.getCommandFactory().createForcedRemoveObjectCommand(
                        resolveObject(objectFieldValue.getKey1())));
            } else {
                // remove empty field or field with value
                list.add(X.getCommandFactory().createForcedRemoveFieldCommand(
                        resolveField(objectFieldValue.getKey1(), objectFieldValue.getKey2())));
            }
        }
        
        // add
        it = this.added.tupleIterator(new Wildcard<XId>(), new Wildcard<XId>());
        while(it.hasNext()) {
            KeyKeyEntryTuple<XId,XId,XValue> e = it.next();
            if(e.getKey2().equals(NONE)) {
                // add object
                if(this.base.hasObject(e.getKey1())) {
                    log.warn("No need to create object '" + e.getKey1() + "', its already in base");
                } else {
                    if(forced) {
                        list.add(X.getCommandFactory().createForcedAddObjectCommand(getAddress(),
                                e.getKey1()));
                    } else {
                        list.add(X.getCommandFactory().createSafeAddObjectCommand(getAddress(),
                                e.getKey1()));
                    }
                }
            } else if(e.getEntry().equals(NOVALUE)) {
                // add empty field
                list.add(X.getCommandFactory().createForcedAddFieldCommand(
                        resolveObject(e.getKey1()), e.getKey2()));
            } else {
                // value
                XValue currentValue = getFieldValueFromBase(e.getKey1(), e.getKey2());
                if(currentValue == null) {
                    // maybe still add field
                    if(!this.base.hasObject(e.getKey1())
                            || !this.base.getObject(e.getKey1()).hasField(e.getKey2())) {
                        list.add(X.getCommandFactory().createForcedAddFieldCommand(
                                resolveObject(e.getKey1()), e.getKey2()));
                    }
                    // add value
                    list.add(X.getCommandFactory().createForcedAddValueCommand(
                            resolveField(e.getKey1(), e.getKey2()), e.getEntry()));
                } else {
                    // change value
                    list.add(X.getCommandFactory().createForcedChangeValueCommand(
                            resolveField(e.getKey1(), e.getKey2()), e.getEntry()));
                }
            }
        }
        
        /*
         * Make sure model commands come before object commands; object commands
         * come before field commands. This avoids e.g. deleting a field before
         * deleting its object parent.
         */
        Collections.sort(list, new Comparator<XAtomicCommand>() {
            @Override
            public int compare(XAtomicCommand a, XAtomicCommand b) {
                return b.getChangedEntity().getAddressedType()
                        .compareTo(a.getChangedEntity().getAddressedType());
            }
        });
        
        return list;
    }
    
    /**
     * @return txn or null
     */
    public XTransaction toTransaction() {
        XTransactionBuilder builder = new XTransactionBuilder(this.getAddress());
        List<XAtomicCommand> list = toCommandList(true);
        for(XAtomicCommand command : list) {
            builder.addCommand(command);
        }
        if(builder.isEmpty()) {
            log.debug("No command in txn for model '" + this.getId() + "'");
            return null;
        }
        XTransaction txn = builder.build();
        if(log.isTraceEnabled()) {
            log.debug("Commands in txn for model '" + this.getId() + "'");
            for(XAtomicCommand atomicCommand : txn) {
                log.debug("  Command " + atomicCommand);
            }
        }
        return txn;
    }
    
    public boolean hasChanges() {
        return !this.added.isEmpty() || !this.removed.isEmpty();
    }
    
    public long getRevisionNumber() {
        throw new UnsupportedOperationException();
        // log.debug("Returning outdated base-revision number");
        // return this.base.getRevisionNumber();
    }
    
    @Override
    protected long object_getRevisionNumber(XId objectId) {
        throw new UnsupportedOperationException();
        // XWritableObject object = this.base.getObject(objectId);
        // if(object == null) {
        // return UNDEFINED;
        // }
        // return object.getRevisionNumber();
    }
    
    @Override
    protected long field_getRevisionNumber(XId objectId, XId fieldId) {
        throw new UnsupportedOperationException();
        // XWritableObject object = this.base.getObject(objectId);
        // if(object == null) {
        // return UNDEFINED;
        // }
        // XWritableField field = object.getField(fieldId);
        // if(field == null) {
        // return UNDEFINED;
        // }
        // return field.getRevisionNumber();
    }
    
}
