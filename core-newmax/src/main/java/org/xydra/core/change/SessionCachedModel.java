package org.xydra.core.change;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.delta.DeltaUtils.IFieldDiff;
import org.xydra.core.model.delta.DeltaUtils.IObjectDiff;
import org.xydra.core.util.DumpUtils;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.TransformingIterator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * Read and write caching. All once read objects are not read again. All once
 * read fields are not read again. Read-caches existent and non-existent
 * requests. Allows write changes of all kinds, which are kept in an internal
 * buffer. This internal buffer is used to answer queries.
 * 
 * Unable to report correct revision numbers. Instead, the constant -3 is
 * returned.
 * 
 * A SessionModels state looks like this: <li>knowsAllObjects?</li> <li>For each
 * object: one {@link EntityState} or Unknown</li>.
 * 
 * The SessionModel get to know state only via
 * {@link #indexModel(XReadableModel)} and {@link #indexObject(XReadableObject)}
 * .
 * 
 * @author xamde
 */
public class SessionCachedModel implements XWritableModel, DeltaUtils.IModelDiff {
    
    private long rev = UNDEFINED;
    
    private static class CachedEntity {
        private XAddress address;
        protected EntityState state;
        
        public CachedEntity(XAddress address, EntityState state) {
            XyAssert.xyAssert(address != null);
            assert address != null;
            XyAssert.xyAssert(state != null);
            assert state != null;
            this.address = address;
            this.state = state;
        }
        
        public XAddress getAddress() {
            return this.address;
        }
        
        protected long rev = UNDEFINED;
        
        public long getRevisionNumber() {
            return this.rev;
        }
        
        public boolean isAdded() {
            return this.state == EntityState.Added;
        }
        
        public boolean isPresent() {
            return this.state.isPresent();
        }
        
        public boolean isRemoved() {
            return this.state == EntityState.Removed;
        }
        
    }
    
    private static class CachedField extends CachedEntity implements XWritableField, IFieldDiff {
        
        private XValue current;
        private XValue initial;
        
        public CachedField(XAddress address, EntityState state, XValue initialValue) {
            super(address, state);
            this.initial = initialValue;
            this.current = this.initial;
        }
        
        @Override
        public XId getId() {
            return super.address.getField();
        }
        
        @Override
        public XValue getInitialValue() {
            return this.initial;
        }
        
        @Override
        public XType getType() {
            return XType.XFIELD;
        }
        
        @Override
        public XValue getValue() {
            return this.current;
        }
        
        @Override
        public boolean isChanged() {
            return this.state.isChanged() || isValueChanged();
        }
        
        @Override
        public boolean isEmpty() {
            return this.current == null;
        }
        
        public boolean isValueAdded() {
            return this.initial == null && this.current != null;
        }
        
        public boolean isValueChanged() {
            return (this.initial == null && this.current != null)
                    || (this.initial != null && this.current == null)
                    || (this.initial != null && !this.initial.equals(this.current));
        }
        
        public boolean isValueRemoved() {
            return this.initial != null && this.current == null;
        }
        
        @Override
        public boolean setValue(XValue value) {
            this.current = value;
            return isValueChanged();
        }
        
        @Override
        public String toString() {
            return "F:" + this.getId() + " {\n" + DumpUtils.toStringBuffer(this) + "Changes: \n"
                    + DumpUtils.changesToString(this).toString() + "}";
        }
        
        public void discardChanges() {
            this.current = this.initial;
        }
        
        @Override
        public boolean exists() {
            return true;
        }
        
    }
    
    private static class CachedObject extends CachedEntity implements XWritableObject,
            DeltaUtils.IObjectDiff {
        /** FieldId -> CachedObject */
        private Map<XId,CachedField> cachedFields;
        
        public CachedObject(XAddress address, EntityState state) {
            super(address, state);
            this.cachedFields = new HashMap<XId,SessionCachedModel.CachedField>(2);
        }
        
        @Override
        public CachedField createField(XId fieldId) {
            return setFieldState(fieldId, EntityState.Added, null);
        }
        
        @Override
        public Collection<? extends XReadableField> getAdded() {
            List<XReadableField> list = new LinkedList<XReadableField>();
            for(CachedField cf : this.cachedFields.values()) {
                if(cf.isAdded()) {
                    list.add(cf);
                }
            }
            return list;
        }
        
        @SuppressWarnings("unused")
        @Override
        public XWritableField getField(XId fieldId) {
            CachedField cf = this.cachedFields.get(fieldId);
            if(WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && cf == null) {
                log.warn("Field '" + fieldId + "' not prefetched in " + this.getAddress()
                        + ". Return getField=null.");
            }
            if(cf != null && !cf.isPresent()) {
                return null;
            }
            return cf;
        }
        
        @Override
        public XId getId() {
            return super.address.getObject();
        }
        
        @Override
        public Collection<? extends IFieldDiff> getPotentiallyChanged() {
            List<IFieldDiff> list = new LinkedList<IFieldDiff>();
            for(CachedField cf : this.cachedFields.values()) {
                if(cf.isChanged() && !cf.isAdded() && !cf.isRemoved()) {
                    list.add(cf);
                }
            }
            return list;
        }
        
        @Override
        public Collection<XId> getRemoved() {
            List<XId> list = new LinkedList<XId>();
            for(CachedField cf : this.cachedFields.values()) {
                if(cf.isRemoved()) {
                    list.add(cf.getId());
                }
            }
            return list;
        }
        
        @Override
        public XType getType() {
            return XType.XOBJECT;
        }
        
        @Override
        public boolean hasChanges() {
            if(this.state.isChanged()) {
                return true;
            }
            for(CachedField cf : this.cachedFields.values()) {
                if(cf.isChanged()) {
                    return true;
                }
            }
            return false;
        }
        
        @SuppressWarnings("unused")
        @Override
        public boolean hasField(XId fieldId) {
            CachedField cf = this.cachedFields.get(fieldId);
            if(cf != null) {
                return cf.isPresent();
            }
            if(WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && cf == null) {
                log.warn("Field '" + fieldId + "' not prefetched in " + this.getAddress()
                        + ". Return hasField=false.");
            }
            return false;
        }
        
        /**
         * Does not overwrite already changed fields
         * 
         * @param baseObject base object
         */
        public void indexFieldsFrom(XReadableObject baseObject) {
            for(XId fieldId : baseObject) {
                CachedField cf = this.cachedFields.get(fieldId);
                XReadableField baseField = baseObject.getField(fieldId);
                XValue baseValue = baseField.getValue();
                if(cf == null) {
                    cf = setFieldState(fieldId, EntityState.Present, baseValue);
                } else {
                    if(!cf.isChanged()) {
                        cf.state = EntityState.Present;
                        cf.setValue(baseValue);
                    }
                }
                cf.rev = baseField.getRevisionNumber();
            }
        }
        
        @Override
        public boolean isEmpty() {
            for(CachedField cf : this.cachedFields.values()) {
                if(cf.isPresent()) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public Iterator<XId> iterator() {
            // return all Added + Present fields only
            return new TransformingIterator<Map.Entry<XId,CachedField>,XId>(
            
            new AbstractFilteringIterator<Map.Entry<XId,CachedField>>(this.cachedFields.entrySet()
                    .iterator()) {
                @Override
                protected boolean matchesFilter(Map.Entry<XId,CachedField> entry) {
                    return entry.getValue().isPresent();
                }
            }, new TransformingIterator.Transformer<Map.Entry<XId,CachedField>,XId>() {
                
                @Override
                public XId transform(Map.Entry<XId,CachedField> entry) {
                    return entry.getKey();
                }
            });
        }
        
        @SuppressWarnings("unused")
        @Override
        public boolean removeField(XId fieldId) {
            CachedField cf = this.cachedFields.get(fieldId);
            if(cf == null) {
                setFieldState(fieldId, EntityState.Removed, null);
                if(WARN_ON_UNCACHED_ACCESS && WARN_ON_FIELDS && WARN_ON_REMOVES) {
                    log.warn("Field '" + fieldId + "' not prefetched in " + this.getAddress()
                            + ". Removing anyway.");
                }
                return false;
            } else {
                boolean b = cf.isPresent();
                cf.state = EntityState.Removed;
                return b;
            }
        }
        
        private CachedField setFieldState(XId fieldId, EntityState state, XValue initialValue) {
            CachedField cf = new CachedField(XX.resolveField(getAddress(), fieldId), state,
                    initialValue);
            this.cachedFields.put(fieldId, cf);
            return cf;
        }
        
        @Override
        public String toString() {
            return "O:" + this.getId() + " {\n" + DumpUtils.toStringBuffer(this) + "Changes: \n"
                    + DumpUtils.changesToString(this).toString() + "}";
        }
        
        public void discardChanges() {
            Set<XId> fieldsToDelete = new HashSet<XId>();
            
            Iterator<XId> iterator = this.cachedFields.keySet().iterator();
            while(iterator.hasNext()) {
                XId key = iterator.next();
                CachedField cf = this.cachedFields.get(key);
                if(cf.isAdded()) {
                    fieldsToDelete.add(key);
                } else if(cf.isRemoved() || cf.isChanged())
                    if(cf.isRemoved()) {
                        if(cf.getRevisionNumber() < 0) {
                            fieldsToDelete.add(key);
                        } else {
                            cf.state = EntityState.Present;
                        }
                    }
                
                cf.discardChanges();
            }
            
            for(XId xid : fieldsToDelete) {
                this.cachedFields.remove(xid);
            }
        }
        
        @Override
        public boolean exists() {
            return true;
        }
        
    }
    
    /**
     * The state a {@link CachedObject} or {@link CachedField} can be in
     */
    private static enum EntityState {
        
        /**
         * Added. Not present in base model; added later.
         * 
         * Object: Implies: knows all fields. All fields can only be: Added, Not
         * present. Object has a number of fields.
         * 
         * Field: Value can be: Added, Not present
         */
        Added,
        
        /**
         * Not present in base model and not added.
         * 
         * Object: Implies: There are 0 fields.
         * 
         * Field: Implies: Value not present.
         */
        NotPresent,
        
        /**
         * Present. Was present in base model and has not been removed.
         * 
         * Object: (IMPROVE Distinguish: knows all fields or not). This state
         * includes changed objects, in which only fields or values changed. Has
         * a number of fields.
         * 
         * Field: Value can be: Present, Added, Changed, Removed, Not present
         */
        Present,
        
        /**
         * Removed. Was present in base model; removed later.
         * 
         * Object: Implies: There are 0 fields.
         * 
         * Field: Implies: Value not present.
         */
        Removed;
        
        public EntityState afterSuccesfullCommit() {
            switch(this) {
            case Added:
                return EntityState.Present;
            case NotPresent:
            case Present:
                return this;
            case Removed:
                return EntityState.NotPresent;
            }
            XyAssert.xyAssert(false);
            return null;
        }
        
        public boolean isChanged() {
            return this == Added || this == Removed;
        }
        
        public boolean isPresent() {
            return this == Present || this == Added;
        }
        
    }
    
    public static class PrefetchException extends RuntimeException {
        
        private static final long serialVersionUID = 8681544474510411429L;
        
        public PrefetchException(String msg) {
            super(msg);
        }
        
    }
    
    private static final Logger log = LoggerFactory.getLogger(SessionCachedModel.class);
    
    private static final long UNDEFINED = -3;
    
    private static final boolean WARN_ON_FIELDS = false;
    
    private static final boolean WARN_ON_REMOVES = false;
    
    /**
     * Compile-time flag to help debugging. If on, all read-accesses to
     * non-pre-fetched entities results in a warning.
     */
    private static final boolean WARN_ON_UNCACHED_ACCESS = false;
    
    private XAddress address;
    
    /** ObjectId -> CachedObject */
    private Map<XId,CachedObject> cachedObjects;
    
    private boolean knowsAllObjectIds = false;
    
    // FIXME maybe allow to change
    private boolean exists = true;
    
    public SessionCachedModel(XAddress address) {
        this.address = address;
        this.cachedObjects = new HashMap<XId,SessionCachedModel.CachedObject>(2);
    }
    
    /**
     * Does not change the sessionCachedModel itself.
     * 
     * @param txnBuilder
     */
    public void commitTo(XTransactionBuilder txnBuilder) {
        for(CachedObject co : this.cachedObjects.values()) {
            if(co.state == EntityState.Added) {
                txnBuilder.addObjectForced(getAddress(), co);
            } else if(co.state == EntityState.Removed) {
                txnBuilder.removeObject(getAddress(), XCommand.FORCED, co.getId());
            } else {
                // fields might have changed
                for(CachedField cf : co.cachedFields.values()) {
                    if(cf.state == EntityState.Added) {
                        txnBuilder.addFieldForced(co.getAddress(), cf);
                    } else if(cf.state == EntityState.Removed) {
                        txnBuilder.removeField(co.getAddress(), XCommand.FORCED, cf.getId());
                    } else {
                        // value might have changed
                        if(cf.isValueAdded()) {
                            txnBuilder.addValue(cf.getAddress(), XCommand.FORCED, cf.current);
                        } else if(cf.isValueRemoved()) {
                            txnBuilder.removeValue(cf.getAddress(), XCommand.FORCED);
                        } else if(cf.isValueChanged()) {
                            txnBuilder.setValue(cf.getAddress(), cf.current);
                        }
                    }
                }
            }
        }
    }
    
    public void commitTo(XWritableModel writableModel) {
        for(CachedObject co : this.cachedObjects.values()) {
            if(co.state == EntityState.Added) {
                XWritableObject writableObject = writableModel.createObject(co.getId());
                XCopyUtils.copyData(co, writableObject);
            } else if(co.state == EntityState.Removed) {
                writableModel.removeObject(co.getId());
            } else {
                XWritableObject writableObject = writableModel.getObject(co.getId());
                assert writableObject != null : "base model should know object " + co.getId();
                // fields might have changed
                for(CachedField cf : co.cachedFields.values()) {
                    if(cf.state == EntityState.Added) {
                        XWritableField writableField = writableObject.createField(cf.getId());
                        XCopyUtils.copyData(cf, writableField);
                    } else if(cf.state == EntityState.Removed) {
                        writableObject.removeField(cf.getId());
                    } else {
                        XWritableField writableField = writableObject.getField(cf.getId());
                        // value might have changed
                        if(cf.isValueAdded()) {
                            writableField.setValue(cf.getValue());
                        } else if(cf.isValueRemoved()) {
                            writableField.setValue(null);
                        } else if(cf.isValueChanged()) {
                            writableField.setValue(cf.getValue());
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public XWritableObject createObject(@NeverNull XId objectId) {
        // first, consult caches
        CachedObject co = this.cachedObjects.get(objectId);
        if(co != null) {
            // create only if possible
            if(co.state == EntityState.NotPresent) {
                co.state = EntityState.Added;
            }
            if(co.state == EntityState.Removed) {
                co.state = EntityState.Present;
            }
            return co;
        }
        
        // not known, might be present in base, record as added anyway
        return setObjectState(objectId, EntityState.Added);
    }
    
    @Override
    public Collection<? extends XReadableObject> getAdded() {
        List<XReadableObject> list = new LinkedList<XReadableObject>();
        for(CachedObject co : this.cachedObjects.values()) {
            if(co.isAdded()) {
                list.add(co);
            }
        }
        return list;
    }
    
    @Override
    public XAddress getAddress() {
        return this.address;
    }
    
    @Override
    public XId getId() {
        return this.address.getModel();
    }
    
    @SuppressWarnings("unused")
    @Override
    public XWritableObject getObject(@NeverNull XId objectId) {
        XWritableObject xo = this.cachedObjects.get(objectId);
        if(WARN_ON_UNCACHED_ACCESS && xo == null) {
            log.warn("Object '" + objectId + "' not prefetched in " + this.getAddress()
                    + ". Return getObject=null.");
        }
        return xo;
    }
    
    @Override
    public Collection<? extends IObjectDiff> getPotentiallyChanged() {
        List<IObjectDiff> list = new LinkedList<IObjectDiff>();
        for(CachedObject co : this.cachedObjects.values()) {
            if(!co.isAdded() && !co.isRemoved() && co.hasChanges()) {
                list.add(co);
            }
        }
        return list;
    }
    
    @Override
    public Collection<XId> getRemoved() {
        List<XId> list = new LinkedList<XId>();
        for(CachedObject co : this.cachedObjects.values()) {
            if(co.isRemoved()) {
                list.add(co.getId());
            }
        }
        return list;
    }
    
    @Override
    public long getRevisionNumber() {
        return this.rev;
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
    public boolean hasChanges() {
        for(CachedObject co : this.cachedObjects.values()) {
            if(co.hasChanges()) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unused")
    @Override
    public boolean hasObject(@NeverNull XId objectId) {
        XyAssert.xyAssert(objectId != null);
        assert objectId != null;
        CachedObject co = this.cachedObjects.get(objectId);
        if(co != null) {
            return co.isPresent();
        }
        if(WARN_ON_UNCACHED_ACCESS && co == null) {
            log.warn("Object '" + objectId + "' not prefetched in " + this.getAddress()
                    + ". Return hasObject=false.");
        }
        return false;
    }
    
    /**
     * Set the objects in the baseModel as the current state, not overwriting
     * the state of already changed objects.
     * 
     * @param baseModel
     */
    public void indexModel(XReadableModel baseModel) {
        long c = 0;
        for(XId id : baseModel) {
            indexObject(baseModel.getObject(id));
            c++;
        }
        log.info("Indexed " + c + " object in model " + baseModel.getAddress());
        this.rev = baseModel.getRevisionNumber();
        this.knowsAllObjectIds = true;
    }
    
    public void indexObject(XReadableObject baseObject) {
        CachedObject co = this.cachedObjects.get(baseObject.getId());
        if(co == null) {
            co = setObjectState(baseObject.getId(), EntityState.Present);
        } else {
            log.trace("Avoid re-indexing object " + baseObject.getAddress());
        }
        
        co.indexFieldsFrom(baseObject);
        co.rev = baseObject.getRevisionNumber();
    }
    
    @Override
    public boolean isEmpty() {
        if(this.knowsAllObjectIds && this.cachedObjects.isEmpty()) {
            return true;
        }
        // else
        for(Map.Entry<XId,CachedObject> e : this.cachedObjects.entrySet()) {
            if(e.getValue().isPresent()) {
                return false;
            }
        }
        // else:
        /* we checked all objects and know all of them */
        if(this.knowsAllObjectIds) {
            return true;
        }
        
        // // else: we don't know it and may not ask base
        // throw new PrefetchException(
        // "Not all objects are known and further loading from base is not permitted. State unknown.");
        // IMPROVE throw exception?
        return true;
        
    }
    
    @Override
    public Iterator<XId> iterator() {
        // IMPROVE throw exception?
        if(!this.knowsAllObjectIds) {
            log.warn("We don't know all object ids - returning incomplete set for " + this.address);
        }
        
        return new TransformingIterator<Map.Entry<XId,CachedObject>,XId>(
        
        new AbstractFilteringIterator<Map.Entry<XId,CachedObject>>(this.cachedObjects.entrySet()
                .iterator()) {
            @Override
            protected boolean matchesFilter(Map.Entry<XId,CachedObject> entry) {
                return entry.getValue().isPresent();
            }
        }, new TransformingIterator.Transformer<Map.Entry<XId,CachedObject>,XId>() {
            
            @Override
            public XId transform(Map.Entry<XId,CachedObject> entry) {
                return entry.getKey();
            }
        });
    }
    
    /**
     * Moves all added/removed objects/fields and all changed values to
     * read-cache.
     */
    public void markAsCommitted() {
        for(CachedObject co : this.cachedObjects.values()) {
            co.state = co.state.afterSuccesfullCommit();
            for(CachedField cf : co.cachedFields.values()) {
                cf.state = cf.state.afterSuccesfullCommit();
                cf.initial = cf.current;
            }
        }
    }
    
    @SuppressWarnings("unused")
    @Override
    public boolean removeObject(@NeverNull XId objectId) {
        CachedObject co = this.cachedObjects.get(objectId);
        if(co == null) {
            if(WARN_ON_UNCACHED_ACCESS && WARN_ON_REMOVES) {
                log.warn("Object '" + objectId + "' not prefetched in " + this.getAddress()
                        + ". Removing anyway.");
            }
            setObjectState(objectId, EntityState.Removed);
            return false;
        } else {
            boolean b = co.isPresent();
            co.state = EntityState.Removed;
            return b;
        }
    }
    
    /**
     * Allows to set knowsAllObjectIds to true even if
     * {@link #indexModel(XReadableModel)} was never called. All relevant
     * objects should have been indexed via
     * {@link #indexObject(XReadableObject)}. Everything else will be considered
     * non-existent.
     * 
     * @param b
     */
    public void setKnowsAllObjectIds(boolean b) {
        XyAssert.xyAssert(b || !this.knowsAllObjectIds);
        this.knowsAllObjectIds = b;
    }
    
    private CachedObject setObjectState(XId id, EntityState objectState) {
        CachedObject co = new CachedObject(XX.resolveObject(getAddress(), id), objectState);
        this.cachedObjects.put(id, co);
        XyAssert.xyAssert(co.getId().equals(id));
        return co;
    }
    
    @Override
    public String toString() {
        return "State including changes:\nM:" + this.getId() + " {\n"
                + DumpUtils.toStringBuffer(this) + "Changes: \n"
                + DumpUtils.changesToString(this).toString() + "}";
    }
    
    public boolean isKnownObject(XId objectId) {
        CachedObject co = this.cachedObjects.get(objectId);
        return co != null;
    }
    
    public boolean knowsAllObjects() {
        return this.knowsAllObjectIds;
    }
    
    /**
     * Removes all changes in the SessionCachedModel, its XObjects, their
     * XFields their XValues
     */
    public void discardAllChanges() {
        
        Set<XId> objectsToDelete = new HashSet<XId>();
        
        for(Entry<XId,CachedObject> e : this.cachedObjects.entrySet()) {
            XId key = e.getKey();
            CachedObject co = e.getValue();
            
            if(co.isAdded()) {
                objectsToDelete.add(key);
            } else if(co.isRemoved() || co.hasChanges()) {
                if(co.isRemoved()) {
                    if(co.getRevisionNumber() < 0) {
                        objectsToDelete.add(key);
                    } else {
                        co.state = EntityState.Present;
                    }
                }
                co.discardChanges();
            }
        }
        
        for(XId xid : objectsToDelete) {
            this.cachedObjects.remove(xid);
        }
    }
    
    @Override
    public boolean exists() {
        return this.exists;
    }
    
}
