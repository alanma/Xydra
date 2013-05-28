package org.xydra.core.model.delta;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.impl.XExistsReadable;
import org.xydra.base.rmof.impl.XExistsWritableModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.BagUnionIterator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;


/**
 * An {@link XWritableModel} that represents changes to an
 * {@link XReadableModel}.
 * 
 * An {@link XReadableModel} is passed as an argument of the constructor. This
 * ChangedModel will then basically represent the given {@link XReadableModel}
 * and allow changes on its set of {@link XReadableObject XBaseObjects}. The
 * changes do not happen directly on the passed {@link XReadableModel} but
 * rather on a sort of copy that emulates the passed {@link XReadableModel}. A
 * ChangedModel provides methods to compare the current state to the state the
 * passed {@link XReadableModel} was in at creation time.
 * 
 * De-facto this class defines the semantics of executing commands.
 * 
 * @author dscharrer
 * 
 */
public class ChangedModel implements XWritableModel, IModelDiff, XExistsWritableModel {
    
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ChangedModel.class);
    
    /**
     * Get the number of {@link XCommand XCommands} needed to create this
     * object.
     */
    private static int countChanges(XReadableObject object, int max) {
        int n = 1; // one to create the object
        if(n < max) {
            for(XId fieldId : object) {
                n += object.getField(fieldId).isEmpty() ? 1 : 2;
                if(n >= max) {
                    break;
                }
            }
        }
        return n;
    }
    
    // Fields that are not in base and have been added.
    // Contains no XIds that are in removed or changed.
    private final Map<XId,SimpleObject> added = new HashMap<XId,SimpleObject>(2);
    
    private final XReadableModel base;
    
    // Fields that are in base and have not been removed.
    // While they were changed once, those changes might have been reverted.
    // Contains no XIds that are in added or removed.
    private final Map<XId,ChangedObject> changed = new HashMap<XId,ChangedObject>(2);
    
    // Fields that are in base but have been removed.
    // Contains no XIds that are in added or changed.
    private final Set<XId> removed = new HashSet<XId>(2);
    
    private boolean modelExists;
    
    /**
     * Wrap an {@link XReadableModel} to record a set of changes made. Multiple
     * changes will be combined as much as possible such that a minimal set of
     * changes remains.
     * 
     * Note that this is a very lightweight wrapper intended for a short
     * lifetime. As a consequence, the wrapped {@link XReadableModel} is not
     * copied and changes to it or any contained objects and fields (as opposed
     * to this {@link ChangedModel}) may result in undefined behavior of the
     * {@link ChangedModel}.
     * 
     * @param base The {@link XReadableModel} this ChangedModel will encapsulate
     *            and represent @NeverNull
     */
    public ChangedModel(XReadableModel base) {
        assert base != null;
        this.base = base;
        this.modelExists = base instanceof XExistsReadable ? ((XExistsReadable)base).exists()
                : true;
    }
    
    /**
     * Run a number of assert statements.
     * 
     * @return always true. A return value allows this method to be used in
     *         assert statements.
     */
    public boolean checkSetInvariants() {
        
        for(XId id : this.removed) {
            XyAssert.xyAssert(!this.added.containsKey(id) && !this.changed.containsKey(id));
            XyAssert.xyAssert(this.base.hasObject(id));
        }
        
        for(XId id : this.added.keySet()) {
            XyAssert.xyAssert(!this.removed.contains(id) && !this.changed.containsKey(id));
            XyAssert.xyAssert(!this.base.hasObject(id), "baseModel contains added object '" + id
                    + "'");
            XyAssert.xyAssert(id.equals(this.added.get(id).getId()));
        }
        
        for(XId id : this.changed.keySet()) {
            XyAssert.xyAssert(!this.removed.contains(id) && !this.added.containsKey(id));
            XyAssert.xyAssert(this.base.hasObject(id));
            XyAssert.xyAssert(id.equals(this.changed.get(id).getId()));
        }
        
        return true;
    }
    
    /**
     * remove all objects.
     */
    public void clear() {
        
        this.added.clear();
        this.changed.clear();
        for(XId id : this.base) {
            // IMPROVE maybe add a "cleared" flag to remove all fields more
            // efficiently?
            this.removed.add(id);
        }
        
        XyAssert.xyAssert(checkSetInvariants());
    }
    
    /**
     * Count the minimal number of {@link XCommand XCommands} that would be
     * needed to transform the original {@link XReadableModel} to the current
     * state which is represented by this ChangedModel.
     * 
     * This is different to {@link #countEventsNeeded} in that a removed object
     * or field may cause several events while only needing one command.
     * 
     * @param max An upper bound for counting the amount of needed
     *            {@link XCommand XCommands}. Note that setting this bound to
     *            little may result in the return of an integer which does not
     *            actually represent the minimal amount of needed
     *            {@link XCommand XCommands} for the transformation.
     * @return the amount of needed {@link XCommand XCommands} for the
     *         transformation
     */
    public int countCommandsNeeded(int max) {
        int n = this.removed.size() + this.added.size();
        if(n < max) {
            if(modelWasCreated() || modelWasRemoved()) {
                n++;
            }
            for(XReadableObject object : this.added.values()) {
                n += countChanges(object, max - n + 1) - 1;
                if(n >= max) {
                    return n;
                }
            }
            for(ChangedObject object : this.changed.values()) {
                n += object.countCommandsNeeded(max - n);
                if(n >= max) {
                    return n;
                }
            }
            // implied removed field - expensive
            for(XId removedObjectId : this.removed) {
                XReadableObject removedObject = getOldObject(removedObjectId);
                Iterator<XId> it = removedObject.iterator();
                while(it.hasNext() && n < max) {
                    it.next();
                    n += 1;
                }
                if(n >= max) {
                    return n;
                }
            }
        }
        return n;
    }
    
    /**
     * @return true there are changes compared to the initially supplied model
     */
    public boolean hasChanges() {
        return !this.added.isEmpty() || !this.removed.isEmpty() || countCommandsNeeded(1) > 0;
    }
    
    /**
     * Count the number of {@link XEvent XEvents} that would be needed to log
     * the transformation of the original {@link XReadableModel} to the current
     * state which is represented by this ChangedModel.
     * 
     * This is different to {@link #countCommandsNeeded} in that a removed
     * object or field may cause several events while only needing one command.
     * 
     * @param max An upper bound for counting the amount of needed
     *            {@link XEvent XEvents}. Note that setting this bound to little
     *            may result in the return of an integer which does not actually
     *            represent the minimal amount of needed {@link XEvent XEvents}
     *            for the transformation.
     * @return the amount of needed {@link XEvent XEvents} for the
     *         transformation
     */
    public int countEventsNeeded(int max) {
        int n = this.removed.size() + this.added.size();
        if(n < max) {
            if(this.base instanceof XExistsReadable) {
                XExistsReadable existsBase = (XExistsReadable)this.base;
                if(existsBase.exists() != exists()) {
                    n++;
                }
            }
            for(XId objectId : this.removed) {
                // removing object itself already counted
                XReadableObject oldObject = getOldObject(objectId);
                for(XId fieldId : oldObject) {
                    n++; // removing the field
                    if(n >= max) {
                        return n;
                    }
                    XReadableField oldField = oldObject.getField(fieldId);
                    if(!oldField.isEmpty()) {
                        n++; // removing the value
                        if(n >= max) {
                            return n;
                        }
                    }
                }
            }
            for(XReadableObject object : this.added.values()) {
                n += countChanges(object, max - n + 1) - 1;
                if(n >= max) {
                    break;
                }
            }
            if(n < max) {
                for(ChangedObject object : this.changed.values()) {
                    n += object.countEventsNeeded(max - n);
                    if(n >= max) {
                        break;
                    }
                }
            }
        }
        return n;
    }
    
    @Override
    public XWritableObject createObject(@NeverNull XId objectId) {
        assert exists();
        
        XWritableObject oldObject = getObject(objectId);
        if(oldObject != null) {
            return oldObject;
        }
        
        XReadableObject object = this.base.getObject(objectId);
        if(object != null) {
            assert this.base.hasObject(objectId);
            
            // If the field previously existed it must have been removed
            // previously and we can merge the remove and add changes.
            XyAssert.xyAssert(this.removed.contains(objectId));
            XyAssert.xyAssert(!this.changed.containsKey(objectId));
            this.removed.remove(objectId);
            ChangedObject newObject = new ChangedObject(object);
            newObject.clear();
            XyAssert.xyAssert(newObject.getId().equals(object.getId()));
            this.changed.put(objectId, newObject);
            
            XyAssert.xyAssert(checkSetInvariants());
            
            return newObject;
            
        } else {
            assert !this.base.hasObject(objectId);
            
            // Otherwise, the object is completely new.
            XAddress fieldAddr = XX.resolveObject(getAddress(), objectId);
            SimpleObject newObject = new SimpleObject(fieldAddr);
            newObject.setRevisionNumber(getRevisionNumber());
            this.added.put(objectId, newObject);
            
            XyAssert.xyAssert(checkSetInvariants());
            
            return newObject;
        }
        
    }
    
    /**
     * Apply the given command to this changed mode. Failed commands may be left
     * partially applied.
     * 
     * @param command
     * 
     * @return true if the command succeeded, false otherwise.
     */
    public boolean executeCommand(XCommand command) {
        return ChangeExecutor.executeAnyCommand(command, this);
    }
    
    /**
     * Checks if the given {@link XFieldCommand} is valid and can be
     * successfully executed on this ChangedModel or if the attempt to execute
     * it will fail.
     * 
     * @param command The {@link XFieldCommand} which is to be checked.
     * 
     * @return true, if the {@link XFieldCommand} is valid and can be executed,
     *         false otherwise
     */
    public boolean executeCommand(XFieldCommand command) {
        return ChangeExecutor.executeFieldCommand(command, this);
    }
    
    public boolean executeCommand(XRepositoryCommand command) {
        return ChangeExecutor.executeRepositoryCommand(command, this);
    }
    
    /**
     * Checks if the given {@link XModelCommand} is valid and can be
     * successfully executed on this ChangedModel or if the attempt to execute
     * it will fail.
     * 
     * @param command The {@link XModelCommand} which is to be checked.
     * 
     * @return true, if the {@link XModelCommand} is valid and can be executed,
     *         false otherwise
     */
    public boolean executeCommand(XModelCommand command) {
        return ChangeExecutor.executeModelCommand(command, this);
    }
    
    /**
     * Checks if the given {@link XObjectCommand} is valid and can be
     * successfully executed on this ChangedModel or if the attempt to execute
     * it will fail.
     * 
     * @param command The {@link XObjectCommand} which is to be checked.
     * 
     * @return true, if the {@link XObjectCommand} is valid and can be executed,
     *         false otherwise
     */
    public boolean executeCommand(XObjectCommand command) {
        assert exists();
        return ChangeExecutor.executeObjectCommand(command, this);
    }
    
    /**
     * Apply the {@link XCommand XCommands} contained in the given
     * {@link XTransaction} and return true, if all {@link XCommand XCommands}
     * could be applied. If one of the {@link XCommand XCommands} failed, the
     * {@link XTransaction} will remain partially applied, already executed
     * {@link XCommand XCommands} will not be rolled back.
     * 
     * @param transaction The {@link XTransaction} which is to be executed
     * @return true, if the given {@link XTransaction} could be executed, false
     *         otherwise
     * 
     *         TODO it might be a good idea to tell the caller of this method
     *         which commands of the transaction were executed and not only
     *         return false
     */
    public boolean executeCommand(XTransaction transaction) {
        return ChangeExecutor.executeTransaction(transaction, this);
    }
    
    @Override
    public XAddress getAddress() {
        return this.base.getAddress();
    }
    
    /**
     * @return an iterable of the objects that already existed in the original
     *         {@link XReadableModel} but have been changed. Note: their current
     *         state might be the same as the original one
     */
    public Iterable<ChangedObject> getChangedObjects() {
        return this.changed.values();
    }
    
    @Override
    public XId getId() {
        return this.base.getId();
    }
    
    /**
     * @return the {@link SimpleObject SimpleObjects} that have been added to
     *         this ChangedModel and were not contained in the original
     *         {@link XReadableModel}
     */
    public Iterable<SimpleObject> getNewObjects() {
        assert containsAllDifferentElements(this.added.values()) : "duplicates found";
        return this.added.values();
    }
    
    private static <T> boolean containsAllDifferentElements(Collection<T> c) {
        Set<T> set = new HashSet<T>(4);
        set.addAll(c);
        return set.size() == c.size();
    }
    
    @Override
    public XWritableObject getObject(@NeverNull XId objectId) {
        
        XWritableObject newObject = this.added.get(objectId);
        if(newObject != null) {
            return newObject;
        }
        
        ChangedObject changedObject = this.changed.get(objectId);
        if(changedObject != null) {
            return changedObject;
        }
        
        if(this.removed.contains(objectId)) {
            return null;
        }
        
        /* look in base */
        XReadableObject object = this.base.getObject(objectId);
        if(object == null) {
            return null;
        }
        changedObject = new ChangedObject(object);
        XyAssert.xyAssert(changedObject.getId().equals(object.getId()));
        this.changed.put(objectId, changedObject);
        
        XyAssert.xyAssert(checkSetInvariants());
        
        return changedObject;
    }
    
    /**
     * @param objectId
     * @return the {@link XReadableObject} with the given {@link XId} as it
     *         exists in the original {@link XReadableModel}.
     */
    public XReadableObject getOldObject(XId objectId) {
        return this.base.getObject(objectId);
    }
    
    /**
     * @return the {@link XId XIds} of objects that existed in the original
     *         model but have been removed from this ChangedModel
     */
    public Iterable<XId> getRemovedObjects() {
        return this.removed;
    }
    
    /**
     * Return the revision number of the wrapped {@link XReadableModel}. The
     * revision number does not increase with changes to this
     * {@link ChangedModel}.
     * 
     * @return the revision number of the original {@link XReadableModel}
     */
    @Override
    public long getRevisionNumber() {
        return this.base.getRevisionNumber();
    }
    
    @Override
    public boolean hasObject(XId objectId) {
        if(this.added.containsKey(objectId))
            return true;
        if(this.removed.contains(objectId)) {
            return false;
        }
        return this.base.hasObject(objectId);
    }
    
    @Override
    public boolean isEmpty() {
        
        if(!this.added.isEmpty()) {
            return false;
        }
        
        if(this.removed.isEmpty()) {
            return this.base.isEmpty();
        }
        
        if(this.changed.size() > this.removed.size()) {
            return false;
        }
        
        for(XId objectId : this.base) {
            if(!this.removed.contains(objectId)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public Iterator<XId> iterator() {
        
        Iterator<XId> filtered = new AbstractFilteringIterator<XId>(this.base.iterator()) {
            @Override
            protected boolean matchesFilter(XId entry) {
                return !ChangedModel.this.removed.contains(entry);
            }
        };
        
        return new BagUnionIterator<XId>(filtered, this.added.keySet().iterator());
    }
    
    @Override
    public boolean removeObject(XId objectId) {
        if(this.added.containsKey(objectId)) {
            // Never existed in base, so removing from added is sufficient.
            XyAssert.xyAssert(!this.base.hasObject(objectId) && !this.changed.containsKey(objectId));
            XyAssert.xyAssert(!this.removed.contains(objectId));
            
            this.added.remove(objectId);
            XyAssert.xyAssert(checkSetInvariants());
            return true;
        } else if(!this.removed.contains(objectId) && this.base.hasObject(objectId)) {
            // Exists in base and not removed yet.
            XyAssert.xyAssert(!this.added.containsKey(objectId));
            
            this.removed.add(objectId);
            this.changed.remove(objectId);
            XyAssert.xyAssert(checkSetInvariants());
            return true;
        }
        return false;
    }
    
    @Override
    public XType getType() {
        return XType.XMODEL;
    }
    
    /**
     * Apply the changes encoded in changedModel to the given model. Both should
     * have the same Id.
     * 
     * @param changedModel
     * @param model never null, should be a writable version of the baseModel
     *            used to created the changedModel
     */
    public static void commitTo(@NeverNull ChangedModel changedModel, XWritableModel model) {
        for(SimpleObject changedObject : changedModel.getNewObjects()) {
            XWritableObject baseObject = model.createObject(changedObject.getId());
            XCopyUtils.copyData(changedObject, baseObject);
        }
        for(ChangedObject changedObject : changedModel.getChangedObjects()) {
            if(changedObject.isChanged()) {
                XWritableObject baseObject = model.getObject(changedObject.getId());
                ChangedObject.commitTo(changedObject, baseObject);
            }
        }
        for(XId removed : changedModel.getRemovedObjects()) {
            model.removeObject(removed);
        }
    }
    
    @Override
    public Collection<? extends XReadableObject> getAdded() {
        return this.added.values();
    }
    
    @Override
    public Collection<? extends IObjectDiff> getPotentiallyChanged() {
        return this.changed.values();
    }
    
    @Override
    public Collection<XId> getRemoved() {
        return this.removed;
    }
    
    /**
     * Forget all changes and reset to initial state, does not change anything
     * in the base model
     */
    public void reset() {
        this.added.clear();
        this.removed.clear();
        this.changed.clear();
        this.modelExists = this.base instanceof XExistsReadable ? ((XExistsReadable)this.base)
                .exists() : true;
    }
    
    @Override
    public boolean exists() {
        return this.modelExists;
    }
    
    @Override
    public void setExists(boolean modelExists) {
        this.modelExists = modelExists;
        if(!modelExists) {
            List<XId> toBeRemoved = new LinkedList<XId>();
            IteratorUtils.addAll(this.iterator(), toBeRemoved);
            for(XId objectId : toBeRemoved) {
                removeObject(objectId);
            }
        }
    }
    
    public boolean modelWasCreated() {
        return this.base instanceof XExistsReadable
        
        && !((XExistsReadable)this.base).exists() && exists();
    }
    
    public boolean modelWasRemoved() {
        return this.base instanceof XExistsReadable
        
        && ((XExistsReadable)this.base).exists() && !exists();
    }
}
