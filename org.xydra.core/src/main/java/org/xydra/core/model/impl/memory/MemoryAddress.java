package org.xydra.core.model.impl.memory;

import org.xydra.core.model.XAddress;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XType;
import org.xydra.index.XI;


/**
 * An implementation of {@link XAddress}
 * 
 * @author dscharrer
 * 
 */
public class MemoryAddress implements XAddress {
	
	private static final long serialVersionUID = -8011996037214695776L;
	
	private final XID repository;
	private final XID model;
	private final XID object;
	private final XID field;
	
	/**
	 * Creates a new MemoryAddress.
	 * 
	 * @param repository The {@link XID} of the {@link XRepository} which
	 *            holds/is the element this address refers to (may be null)
	 * @param model The {@link XID} of the {@link XModel} which holds/is the
	 *            element this address refers to (may be null)
	 * @param object The {@link XID} of the {@link XObject} which holds/is the
	 *            element this address refers to (may be null)
	 * @param field The {@link XID} of the {@link XField} which is the element
	 *            this address refers to (may be null)
	 * 
	 * @throws IllegalArgumentException if the given tuple of {@link XID XIDs}
	 *             specify an illegal {@link XAddress}. Illegal {@link XAddress
	 *             XAddresses} are of the form (null, null, null, null),
	 *             (repoID, null, objectID, null or fieldID), (repoID, null,
	 *             null, fieldID) or (repoID or null, modelID, null, fieldID)
	 */
	protected MemoryAddress(XID repository, XID model, XID object, XID field) {
		
		if((repository != null || model != null) && object == null && field != null) {
			throw new IllegalArgumentException(
			        "Repository or model, and field not null, but object is null. This is not allowed!");
		}
		
		if(repository != null && model == null && (object != null || field != null)) {
			throw new IllegalArgumentException("Repository given, model is null, object is "
			        + (object == null ? "null" : "not null") + ", field is "
			        + (field == null ? "null" : "not null") + ". This is not allowed!");
		}
		
		if(repository == null && model == null && object == null && field == null) {
			throw new IllegalArgumentException(
			        "Repository, model, object and field all null. This is not allowed!");
		}
		
		this.repository = repository;
		this.model = model;
		this.object = object;
		this.field = field;
	}
	
	public XID getField() {
		return this.field;
	}
	
	public XID getObject() {
		return this.object;
	}
	
	public XID getModel() {
		return this.model;
	}
	
	public XID getRepository() {
		return this.repository;
	}
	
	public XAddress getParent() {
		
		if(this.field != null) {
			// ???F
			if(this.object == null) {
				// ---F => no parent
				assert this.model == null && this.repository == null;
				return null;
			}
			// ??OF => ??O-
			return new MemoryAddress(this.repository, this.model, this.object, null);
		}
		
		if(this.object != null) {
			// ??O-
			if(this.model == null) {
				// --O- => no parent
				assert this.repository == null;
				return null;
			}
			// ?MO- => ?M--
			return new MemoryAddress(this.repository, this.model, null, null);
		}
		
		if(this.model != null) {
			// ?M--
			if(this.repository == null) {
				// -M-- => no parent
				return null;
			}
			// RM-- => R---
			return new MemoryAddress(this.repository, null, null, null);
		}
		
		// R---
		assert this.repository != null;
		return null;
	}
	
	/**
	 * @return a unique, complete representation of this {@link XAddress} with
	 *         the fixed format = '/' + repoID + '/' + modelID + '/' + objectID
	 *         + '/' + fieldID. Empty {@link XID XIDs} are represented by '-'.
	 */
	@Override
	public String toString() {
		
		StringBuffer uri = new StringBuffer();
		
		uri.append('/');
		if(this.repository != null) {
			uri.append(this.repository.toString());
		} else {
			uri.append("-");
		}
		
		uri.append('/');
		if(this.model != null) {
			uri.append(this.model.toString());
		} else {
			uri.append("-");
		}
		
		uri.append('/');
		if(this.object != null) {
			uri.append(this.object.toString());
		} else {
			uri.append("-");
		}
		
		uri.append('/');
		if(this.field != null) {
			uri.append(this.field.toString());
		} else {
			uri.append("-");
		}
		
		return uri.toString();
	}
	
	@Override
	public int hashCode() {
		
		int hash = 0;
		
		if(this.repository != null) {
			hash ^= this.repository.hashCode();
		}
		if(this.model != null) {
			hash ^= this.model.hashCode();
		}
		if(this.object != null) {
			hash ^= this.object.hashCode();
		}
		if(this.field != null) {
			hash ^= this.field.hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof XAddress)) {
			return false;
		}
		XAddress other = (XAddress)obj;
		return XI.equals(this.repository, other.getRepository())
		        && XI.equals(this.model, other.getModel())
		        && XI.equals(this.object, other.getObject())
		        && XI.equals(this.field, other.getField());
	}
	
	public String toURI() {
		return toString();
	}
	
	public XType getAddressedType() {
		
		if(this.field != null) {
			// ???F => field
			return XType.XFIELD;
		} else if(this.object != null) {
			// ??O- => object
			return XType.XOBJECT;
		} else if(this.model != null) {
			// ?M-- => model
			return XType.XMODEL;
		} else {
			// R--- => repository
			assert this.repository != null;
			return XType.XREPOSITORY;
		}
	}
	
	public boolean contains(XAddress descendant) {
		
		if(!XI.equals(this.repository, descendant.getRepository())) {
			return false;
		}
		
		// same repository (may be null)
		
		if(!XI.equals(this.model, descendant.getModel())) {
			return this.repository != null && this.model == null;
		}
		
		// same repository and model (may be null)
		
		if(!XI.equals(this.object, descendant.getObject())) {
			return this.model != null && this.object == null;
		}
		
		// same repository, model and object (may be null)
		
		return this.object != null && this.field == null && descendant.getField() != null;
		
	}
	
	public boolean equalsOrContains(XAddress descendant) {
		
		if(!XI.equals(this.repository, descendant.getRepository())) {
			return false;
		}
		
		// same repository (may be null)
		
		if(!XI.equals(this.model, descendant.getModel())) {
			return this.repository != null && this.model == null;
		}
		
		// same repository and model (may be null)
		
		if(!XI.equals(this.object, descendant.getObject())) {
			return this.model != null && this.object == null;
		}
		
		// same repository, model and object (may be null)
		
		return XI.equals(this.field, descendant.getField())
		        || (this.object != null && this.field == null);
		
	}
	
	public boolean isParentOf(XAddress child) {
		
		if(!XI.equals(this.repository, child.getRepository())) {
			return false;
		}
		
		// same repository (may be null)
		
		if(!XI.equals(this.model, child.getModel())) {
			return this.repository != null && this.model == null && child.getObject() == null;
		}
		
		// same repository and model (may be null)
		
		if(!XI.equals(this.object, child.getObject())) {
			return this.model != null && this.object == null && child.getField() == null;
		}
		
		// same repository, model and object (may be null)
		
		return this.object != null && this.field == null && child.getField() != null;
		
	}
	
}
