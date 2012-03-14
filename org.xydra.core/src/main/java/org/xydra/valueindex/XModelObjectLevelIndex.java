package org.xydra.valueindex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Pair;


/**
 * An index for {@link XReadableModel XReadableModels}. Indexes the contents of
 * the {@link XValue XValues} and stores them together with the {@link XAddress}
 * of the {@link XReadableObject} containing the {@link XReadableField} which
 * holds the value.
 * 
 * The index entries are "String -> XAddress & XValue". An {@link XValueIndexer}
 * is needed to get the String representations used for indexing. This index
 * works on the Object-level, which means that {@link XValue XValues} are
 * associated with the {@link XAddress} of the {@link XReadableObject}
 * containing the {@link XReadableField} which holds the value. Since different
 * {@link XReadableField} might contain the same {@link XValue}, it is possible
 * that there are multiple entries for it in the index. Therefore deindexing
 * will not always directly result in completely deindexing the {@link XValue}.
 * To completely deindex an entry, it needs to be deindexed as many times as the
 * represented {@link XValue} was indexed.
 * 
 * @author Kaidel
 * 
 */

/*
 * Keep in mind that it is NOT possible to iterate over all existing keys in the
 * planned implementation (see {@link StringValueIndex} and {@link StringMap}).
 */

/*
 * TODO Update documentation (include/exclude) and test the new features
 */

public class XModelObjectLevelIndex {
	private XValueIndexer indexer;
	private ValueIndex index;
	private XAddress modelAddress;
	private boolean defaultIncludeAll;
	private Set<XID> includeFieldIds;
	private Set<XID> excludeFieldIds;
	
	/**
	 * Creates a new index for the given {@link XReadableModel} using the given
	 * {@link XValueIndexer}. The given {@link XReadableModel} will be
	 * completely indexed during the creation.
	 * 
	 * @param model The {@link XReadableModel} which will be indexed.
	 * @param indexer The {@link XValueIndexer} which is to be used to get the
	 *            Strings used for indexing.
	 */
	public XModelObjectLevelIndex(XReadableModel model, XValueIndexer indexer,
	        boolean defaultIncludeAll, Set<XID> includeFieldIds, Set<XID> excludeFieldIds) {
		this.indexer = indexer;
		this.index = indexer.getIndex();
		
		this.defaultIncludeAll = defaultIncludeAll;
		this.includeFieldIds = includeFieldIds;
		this.excludeFieldIds = excludeFieldIds;
		
		this.index(model);
		this.modelAddress = model.getAddress();
	}
	
	// should only be executed once (document this!) - executing more than once
	// is dangerous
	
	/**
	 * Completely indexes the given {@link XReadableModel}. Since this method
	 * does not check for changes, updates etc. this should only be called once
	 * at the time the Index is created, hence it is private (declaring it as
	 * public would actually be dangerous, since no checks are done and values
	 * might be indexed multiple times, leading to an inconsistent state). It is
	 * only used during the indexing procedure of the constructor.
	 * 
	 * @param model The {@link XReadableModel} which is to be indexed (i.e. the
	 *            {@link XReadableModel} given to the constructor
	 *            {@link XModelObjectLevelIndex#XModelObjectLevelIndex(XReadableModel, XValueIndexer)}
	 *            ).
	 */
	private void index(XReadableModel model) {
		for(XID objectId : model) {
			XReadableObject object = model.getObject(objectId);
			
			index(object);
		}
	}
	
	/**
	 * Completely indexes the given {@link XReadableObject}. Since this method
	 * does not check for changes, updates etc. and calling this method on a
	 * completely new {@link XReadableObject} without any {@link XReadableField
	 * XReadableFields} (and therefore without any {@link XValue XValues}) makes
	 * no sense, this method should only be called once, hence it is private
	 * (declaring it as public would actually be dangerous, since no checks are
	 * done and values might be indexed multiple times, leading to an
	 * inconsistent state). It is only used during the indexing procedure of the
	 * constructor.
	 * 
	 * @param object The {@link XReadableObject} which is to be indexed
	 */
	private void index(XReadableObject object) {
		for(XID fieldId : object) {
			
			if(this.isToBeIndexed(fieldId)) {
				XReadableField field = object.getField(fieldId);
				index(field);
			}
		}
	}
	
	private boolean isToBeIndexed(XID fieldId) {
		boolean indexField = false;
		
		if(this.defaultIncludeAll) {
			if(!this.excludeFieldIds.contains(fieldId)) {
				indexField = true;
			}
		} else {
			if(this.includeFieldIds.contains(fieldId)) {
				indexField = true;
			}
		}
		
		return indexField;
	}
	
	/**
	 * Completely indexes the given {@link XReadableField}. Since this method
	 * does not check for changes, updates etc., this method should only be
	 * called once, hence it is private (declaring it as public would actually
	 * be dangerous, since no checks are done and values might be indexed
	 * multiple times, leading to an inconsistent state) and should only be
	 * called on {@link XReadableField XReadableFields} which were not yet
	 * indexed.
	 * 
	 * 
	 * @param field The {@link XReadableField} which is to be indexed
	 */
	private void index(XReadableField field) {
		XID fieldId = field.getID();
		
		if(this.isToBeIndexed(fieldId)) {
			this.indexer.indexValue(field.getAddress(), field.getValue());
		}
	}
	
	/**
	 * Computes the difference between the given old and new states of an
	 * {@link XReadableObject} and changes updates the index according to these
	 * changes, i.e. newly added values will be indexed, entries of changed
	 * values will be updated and entries of removed values will be deindexed.
	 * 
	 * This method assumes the given oldObject is an {@link XReadableObject}
	 * which was already indexed and is in exactly the same state as during the
	 * last time it was indexed. No guarantees to the behavior of this method
	 * can be made if this is no the case. For example, if the oldObject
	 * contains values which were not present the last time the object was
	 * indexed, these values will also not be indexed during the update
	 * procedure, even if the newObject still contains them, since they do not
	 * appear in the difference we compute.
	 * 
	 * @param oldObject the old state of the {@link XReadableObject}.
	 * @param newObject the new state of the {@link XReadableObject}.
	 * 
	 * @throws RuntimeException if the given oldObject was no object of the
	 *             {@link XReadableModel} indexed by this index, if the given
	 *             {@link XReadableObject XReadableObjects} are not
	 *             representations of the same object at different points in
	 *             time, i.e. do not have the same {@link XAddress} or if
	 *             newObjects revision number is less than oldObjects revision
	 *             number (and therefore oldObject is actually a newer
	 *             representation).
	 */
	public void updateIndex(XReadableObject oldObject, XReadableObject newObject) {
		XAddress address = oldObject.getAddress();
		
		XAddress modelAddress = XX.resolveModel(address.getRepository(), address.getModel());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the given oldObject was no object of the XReadableModel indexed by this index.");
		}
		
		if(!address.equals(newObject.getAddress())) {
			throw new RuntimeException("oldObject and newObject do not have the same address.");
		}
		
		if(newObject.getRevisionNumber() < oldObject.getRevisionNumber()) {
			throw new RuntimeException("newObject is an older revision than oldObject.");
			// TODO how about just swapping the objects?
		}
		
		/*
		 * if the revision numbers are equal, nothing has changed and therefore
		 * there's nothing to update.
		 */
		
		if(newObject.getRevisionNumber() > oldObject.getRevisionNumber()) {
			// objects changed
			
			/*
			 * used to remember which fields exist in both models and do not
			 * need to be checked again later.
			 */
			HashSet<XID> intersection = new HashSet<XID>();
			
			for(XID fieldId : oldObject) {
				XReadableField oldField = oldObject.getField(fieldId);
				boolean isToBeIndexed = this.isToBeIndexed(fieldId);
				
				if(newObject.hasField(fieldId)) {
					/*
					 * add the field to the intersection, since both objects
					 * contain it
					 */
					intersection.add(fieldId);
					
					if(isToBeIndexed) {
						XReadableField newField = newObject.getField(fieldId);
						
						updateFieldEntry(oldField, newField);
					}
				} else {
					// field was completely removed
					if(isToBeIndexed) {
						deIndex(oldField);
					}
				}
			}
			
			for(XID fieldId : newObject) {
				/*
				 * TODO is there a faster way to calculate to calculate the
				 * difference between the newObject and the intersection than
				 * calling contains again and again? Is there a fast difference
				 * algorithm, maybe already implemented in the Java API? Is
				 * removeAll faster?
				 */
				if(!intersection.contains(fieldId) && this.isToBeIndexed(fieldId)) {
					// field is new
					index(newObject.getField(fieldId));
				}
			}
		}
	}
	
	/*
	 * deIndex methods for event types apart from XFieldEvents are not possible,
	 * because:
	 * 
	 * XModelEvents: Only the REMOVE-Case is interesting here. We would need to
	 * remove a complete object only by it's address, which is not possible,
	 * because we do not have the possibility to iterate over all keys (to look
	 * up the entries which contain the object address) nor do we have the
	 * possibility to look up the values which were stored in the XObject.
	 * 
	 * XObjectEvents: Only the REMOVE-Case is interesting here. We would need to
	 * remove a complete field only by it's address. The argumentation why this
	 * is not possible is analogous to the argumentation for XModelEvents.
	 * 
	 * XTransactionEvents: Typically contain both XModel- and XObjectEvents,
	 * which cannot be handled. Transaction consisting only of XFieldEvents also
	 * cannot be handled, since the Transaction contains no information over the
	 * removed values etc., which is needed for deindexing.
	 */
	
	/**
	 * Updates the index according to the given {@link XFieldEvent}.
	 * 
	 * This method assumes that the given event refers to a revision of the
	 * changed field which is newer than the revision which state is currently
	 * represented in the index and that the changed field was already indexed.
	 * If not, this method will leave the index in a state inconsistent to the
	 * state of the {@link XReadableModel} which is indexed by this Index.
	 * 
	 * @param event The {@link XFieldEvent} which specifies what was changed in
	 *            the {@link XReadableModel} and what needs to be updated in the
	 *            index.
	 * @param oldValue the old {@link XValue} of the {@link XReadableField}
	 *            which was changed.
	 * @throws RuntimeException if the given {@link XFieldEvent} refers to an
	 *             {@link XReadableField} which is not a field of an object of
	 *             the {@link XReadableModel} indexed by this index.
	 */
	public void updateIndex(XFieldEvent event, XValue oldValue) {
		XAddress fieldAddress = event.getChangedEntity();
		XID fieldId = fieldAddress.getField();
		
		XAddress modelAddress = XX.resolveModel(event.getRepositoryId(), event.getModelId());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the changed field was no field of an object of the XReadableModel indexed by this index.");
		}
		
		if(!this.isToBeIndexed(fieldId)) {
			return;
		}
		
		switch(event.getChangeType()) {
		case ADD:
			XValue addedValue = event.getNewValue();
			this.indexer.indexValue(fieldAddress, addedValue);
			break;
		case REMOVE:
			this.indexer.deIndexValue(fieldAddress, oldValue);
			break;
		case CHANGE:
			this.indexer.deIndexValue(fieldAddress, oldValue);
			addedValue = event.getNewValue();
			this.indexer.indexValue(fieldAddress, addedValue);
			break;
		case TRANSACTION: // TRANSACTION is not possible for XFieldEvents
			break;
		}
	}
	
	/**
	 * Computes the difference between the given old and new states of an
	 * {@link XReadableField} and changes updates the index according to these
	 * changes, i.e. newly added values will be indexed, entries of changed
	 * values will be updated and entries of removed values will be deindexed.
	 * 
	 * This method assumes the given oldField is an {@link XReadableField} which
	 * was already indexed and is in exactly the same state as during the last
	 * time it was indexed. If this is not the case, the index will be left in a
	 * state inconsistent to the state of the {@link XReadableModel} which is
	 * indexed by this index.
	 * 
	 * @param oldField the old state of the {@link XReadableField}.
	 * @param newField the new state of the {@link XReadableField}.
	 * 
	 * @throws RuntimeException if the given oldField was no field of an object
	 *             of the {@link XReadableModel} indexed by this index, if the
	 *             given {@link XReadableField XReadableFields} are not
	 *             representations of the same object at different points in
	 *             time, i.e. do not have the same {@link XAddress} or if
	 *             newFields revision number is less than oldFields revision
	 *             number (and therefore oldObject is actually a newer
	 *             representation).
	 */
	public void updateIndex(XReadableField oldField, XReadableField newField) {
		XAddress oldAddress = oldField.getAddress();
		XAddress newAddress = newField.getAddress();
		
		XAddress modelAddress = XX.resolveModel(oldAddress.getRepository(), oldAddress.getModel());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the given oldField was no field of an object of the XReadableModel indexed by this index.");
		}
		
		if(newField.getRevisionNumber() < oldField.getRevisionNumber()) {
			throw new RuntimeException("newField is an older revision than oldField.");
			// TODO how about just swapping the objects?
		}
		
		if(!oldAddress.equals(newAddress)) {
			throw new RuntimeException(
			        "oldField and newField do not have the same address and therefore aren't different versions of the same field.");
		}
		
		if(!this.isToBeIndexed(oldField.getID())) {
			return;
		}
		
		updateFieldEntry(oldField, newField);
	}
	
	/**
	 * Deindexes the given oldValue and indexes the newValue for the specified
	 * object.
	 * 
	 * This method assumes that the given newValue is actually newer then the
	 * given oldValue. If this is not the case, the index will be left in a
	 * state inconsistent to the state of the {@link XReadableModel} indexed by
	 * this index.
	 * 
	 * @param fieldAddress The {@link XAddress} of the {@link XReadableField}
	 *            which stores the newValue and in which the oldValue was
	 *            stored.
	 * @param oldValue The old {@link XValue}.
	 * @param newValue The new {@link XValue}.
	 * @throws RuntimeException if the given fieldAddress is no address of an
	 *             {@link XReadableField}.
	 */
	public void updateIndex(XAddress fieldAddress, XValue oldValue, XValue newValue) {
		if(fieldAddress.getAddressedType() != XType.XFIELD) {
			throw new RuntimeException("fieldAddress is no valid Field-XAddress, but an "
			        + fieldAddress.getAddressedType() + "-Address.");
		}
		
		XAddress modelAddress = XX.resolveModel(fieldAddress.getRepository(),
		        fieldAddress.getModel());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the given field address was not an address of a field of the model indexed by this index.");
		}
		
		if(!this.isToBeIndexed(fieldAddress.getField())) {
			return;
		}
		
		/*
		 * nothing needs to be done if oldValue and newValue are equal
		 */
		if(!oldValue.equals(newValue)) {
			this.indexer.deIndexValue(fieldAddress, oldValue);
			this.indexer.indexValue(fieldAddress, newValue);
		}
	}
	
	/**
	 * A convenience method for updating the entries of two fields. Checks
	 * whether the given newField revision is higher than the revision number of
	 * the oldField and does nothing if this is not the case.
	 * 
	 * @param oldField
	 * @param newField
	 */
	private void updateFieldEntry(XReadableField oldField, XReadableField newField) {
		// nothing needs to be updated if the revision numbers are equal or
		// newField is not "newer" than oldField
		if(newField.getRevisionNumber() > oldField.getRevisionNumber()) {
			// value of field was changed
			deIndex(oldField);
			index(newField);
		}
	}
	
	/**
	 * Deindexes the content of the given {@link XReadableObject}. Should only
	 * be called on {@link XReadableObject XReadableObjects} which were
	 * completely removed from the {@link XReadableModel} indexed by this index.
	 * 
	 * @param object The {@link XReadableObject} which is to be deindexed.
	 * @throws RuntimeException if the given {@link XReadableObject} was no
	 *             object of the {@link XReadableModel} indexed by this Index.
	 */
	public void deIndex(XReadableObject object) {
		XAddress objectAddress = object.getAddress();
		
		XAddress modelAddress = XX.resolveModel(objectAddress.getRepository(),
		        objectAddress.getModel());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the given XReadableObject was no object of the XReadableModel indexed by this index.");
		}
		
		for(XID fieldId : object) {
			if(this.isToBeIndexed(fieldId)) {
				XReadableField field = object.getField(fieldId);
				this.indexer.deIndexValue(field.getAddress(), field.getValue());
			}
		}
	}
	
	/**
	 * Deindexes the content of the given {@link XReadableField}. Should only be
	 * called on {@link XReadableField XReadableFields} which were completely
	 * removed from the {@link XReadableModel} indexed by this index.
	 * 
	 * @param field The {@link XReadableField} which is to be deindexed.
	 * @throws RuntimeException if the given {@link XReadableField} was no field
	 *             of an object of the {@link XReadableModel} indexed by this
	 *             Index.
	 */
	public void deIndex(XReadableField field) {
		XAddress fieldAddress = field.getAddress();
		XAddress objectAddress = XX.resolveObject(fieldAddress.getRepository(),
		        fieldAddress.getModel(), fieldAddress.getObject());
		
		XAddress modelAddress = XX.resolveModel(objectAddress.getRepository(),
		        objectAddress.getModel());
		if(!this.modelAddress.equals(modelAddress)) {
			throw new RuntimeException(
			        "the given XReadableField was no field of an object of the XReadableModel indexed by this index.");
		}
		
		if(this.isToBeIndexed(fieldAddress.getField())) {
			this.indexer.deIndexValue(fieldAddress, field.getValue());
		}
	}
	
	/**
	 * Returns a set of pairs of {@link XAddress XAddresses} and {@link XValue
	 * XValues}. The {@link XAddress XAddresses} are addresses of objects
	 * containing fields which hold {@link XValue XValues} corresponding to the
	 * given key. The {@link XValue XValues} in the pair are exactly this
	 * corresponding {@link XValue}, as long as they are not {@link XAddress
	 * XAddresses}.
	 * 
	 * Warning: If an {@link XValue} in a pair is an instance of
	 * {@link XAddress}, it either is the corresponding {@link XValue} or the
	 * {@link XAddress} holding the corresponding {@link XValue}. The second
	 * case can only occur if the index stores the {@link XAddress} of the field
	 * if an {@link XValue} is too large. If your index is not configured in
	 * this manner, this will never occur.
	 * 
	 * Which {@link XValue XValues} corresponds to a given key is determined by
	 * the used {@link XValueIndexer} which was set in the constructor
	 * (XValueIndexer in
	 * {@link XModelObjectLevelIndex#XModelObjectLevelIndex(XReadableModel, XValueIndexer, boolean, Set, Set)
	 * )} )
	 * 
	 * @param key The key for which corresponding will be searched
	 * @return a set of {@link Pair Pairs} of {@link XAddress XAddresses} and
	 *         {@link XValue XValues}. The {@link XAddress XAddresses} of
	 *         objects containing fields which hold {@link XValue XValues}
	 *         corresponding to the given key. Please see the description above
	 *         for a warning concerning the {@link XValue XValues} in the
	 *         {@link Pair Pairs}.
	 */
	public Set<Pair<XAddress,XValue>> search(String key) {
		// IMPROVE rather simple search algorithm at the moment...
		
		/*
		 * the index uses lower case strings only, so we need to transform the
		 * given key appropriately
		 */
		String indexKey = key.toLowerCase();
		HashSet<Pair<XAddress,XValue>> set = new HashSet<Pair<XAddress,XValue>>();
		
		EqualsConstraint<String> constraint = new EqualsConstraint<String>(indexKey);
		Iterator<ValueIndexEntry> iterator = this.index.constraintIterator(constraint);
		
		while(iterator.hasNext()) {
			ValueIndexEntry entry = iterator.next();
			Pair<XAddress,XValue> pair = new Pair<XAddress,XValue>(entry.getAddress(),
			        entry.getValue());
			
			set.add(pair);
		}
		
		return set;
	}
}
