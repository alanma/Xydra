package org.xydra.base.id;

import java.io.Serializable;

import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.value.ValueType;
import org.xydra.index.XI;
import org.xydra.sharedutils.XyAssert;

/**
 * An implementation of {@link XAddress}
 *
 * @author dscharrer
 *
 */
public class MemoryAddress implements XAddress, Serializable {

	private static final long serialVersionUID = -8011996037214695776L;

	// all fields are non-final, so that this can be used as GWT-DTO
	private XId field;
	private XId model;
	private XId object;
	private XId repository;

	/** For GWT only */
	protected MemoryAddress() {
	}

	/**
	 * Creates a new MemoryAddress.
	 *
	 * @param repository The {@link XId} of the repository which holds/is the
	 *            element this address refers to (may be null)
	 * @param model The {@link XId} of the model which holds/is the element this
	 *            address refers to (may be null)
	 * @param object The {@link XId} of the object which holds/is the element
	 *            this address refers to (may be null)
	 * @param field The {@link XId} of the field which is the element this
	 *            address refers to (may be null)
	 *
	 * @throws IllegalArgumentException if the given tuple of {@link XId XIds}
	 *             specify an illegal {@link XAddress}. Illegal {@link XAddress
	 *             XAddresses} are of the form (null, null, null, null),
	 *             (repoID, null, objectId, null or fieldId), (repoID, null,
	 *             null, fieldId) or (repoID or null, modelId, null, fieldId)
	 */
	protected MemoryAddress(final XId repository, final XId model, final XId object, final XId field) {

		if ((repository != null || model != null) && object == null && field != null) {
			throw new IllegalArgumentException(
					"Repository or model, and field not null, but object is null. This is not allowed!");
		}

		if (repository != null && model == null && (object != null || field != null)) {
			throw new IllegalArgumentException("Repository given, model is null, object is "
					+ (object == null ? "null" : "not null") + ", field is "
					+ (field == null ? "null" : "not null") + ". This is not allowed!");
		}

		if (repository == null && model == null && object == null && field == null) {
			throw new IllegalArgumentException(
					"Repository, model, object and field all null. This is not allowed!");
		}

		this.repository = repository;
		this.model = model;
		this.object = object;
		this.field = field;
	}

	@Override
	public int compareTo(final XAddress other) {
		// compare repos
		if (getRepository() == null) {
			if (other.getRepository() != null) {
				return 1;
			}
		} else {
			if (other.getRepository() == null) {
				return -1;
			}
			final int comparison = getRepository().compareTo(other.getRepository());
			if (comparison != 0) {
				return comparison;
			}
		}
		/* repos are either both null or equal => compare models */
		if (getModel() == null) {
			if (other.getModel() != null) {
				return 1;
			}
		} else {
			if (other.getModel() == null) {
				return -1;
			}
			final int comparison = getModel().compareTo(other.getModel());
			if (comparison != 0) {
				return comparison;
			}
		}
		/* models are either both null or equal => compare objects */
		if (getObject() == null) {
			if (other.getObject() != null) {
				return 1;
			}
		} else {
			if (other.getObject() == null) {
				return -1;
			}
			final int comparison = getObject().compareTo(other.getObject());
			if (comparison != 0) {
				return comparison;
			}
		}
		/* objects are either both null or equal => compare fields */
		if (getField() == null) {
			if (other.getField() != null) {
				return 1;
			}
		} else {
			if (other.getField() == null) {
				return -1;
			}
			final int comparison = getField().compareTo(other.getField());
			if (comparison != 0) {
				return comparison;
			}
		}
		/* all entities have been each either null or equal */
		return 0;
	}

	@Override
	public boolean contains(final XAddress descendant) {

		if (!XI.equals(this.repository, descendant.getRepository())) {
			return false;
		}

		// same repository (may be null)

		if (!XI.equals(this.model, descendant.getModel())) {
			return this.repository != null && this.model == null;
		}

		// same repository and model (may be null)

		if (!XI.equals(this.object, descendant.getObject())) {
			return this.model != null && this.object == null;
		}

		// same repository, model and object (may be null)

		return this.object != null && this.field == null && descendant.getField() != null;

	}

	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof XAddress)) {
			return false;
		}
		final XAddress other = (XAddress) obj;
		return XI.equals(this.repository, other.getRepository())
				&& XI.equals(this.model, other.getModel())
				&& XI.equals(this.object, other.getObject())
				&& XI.equals(this.field, other.getField());
	}

	@Override
	public boolean equalsOrContains(final XAddress descendant) {

		if (!XI.equals(this.repository, descendant.getRepository())) {
			return false;
		}

		// same repository (may be null)

		if (!XI.equals(this.model, descendant.getModel())) {
			return this.repository != null && this.model == null;
		}

		// same repository and model (may be null)

		if (!XI.equals(this.object, descendant.getObject())) {
			return this.model != null && this.object == null;
		}

		// same repository, model and object (may be null)

		return XI.equals(this.field, descendant.getField())
				|| this.object != null && this.field == null;

	}

	@Override
	public XType getAddressedType() {

		if (this.field != null) {
			// ???F => field
			return XType.XFIELD;
		} else if (this.object != null) {
			// ??O- => object
			return XType.XOBJECT;
		} else if (this.model != null) {
			// ?M-- => model
			return XType.XMODEL;
		} else {
			// R--- => repository
			XyAssert.xyAssert(this.repository != null);
			assert this.repository != null;
			return XType.XREPOSITORY;
		}
	}

	@Override
	public XId getField() {
		return this.field;
	}

	@Override
	public XId getModel() {
		return this.model;
	}

	@Override
	public XId getObject() {
		return this.object;
	}

	@Override
	public XAddress getParent() {

		if (this.field != null) {
			// ???F
			if (this.object == null) {
				// ---F => no parent
				XyAssert.xyAssert(this.model == null && this.repository == null);
				return null;
			}
			// ??OF => ??O-
			return BaseRuntime.getIDProvider().fromComponents(this.repository, this.model,
					this.object, null);
		}

		if (this.object != null) {
			// ??O-
			if (this.model == null) {
				// --O- => no parent
				XyAssert.xyAssert(this.repository == null);
				return null;
			}
			// ?MO- => ?M--
			return BaseRuntime.getIDProvider().fromComponents(this.repository, this.model, null,
					null);
		}

		if (this.model != null) {
			// ?M--
			if (this.repository == null) {
				// -M-- => no parent
				return null;
			}
			// RM-- => R---
			return BaseRuntime.getIDProvider().fromComponents(this.repository, null, null, null);
		}

		// R---
		XyAssert.xyAssert(this.repository != null);
		assert this.repository != null;
		return null;
	}

	@Override
	public XId getRepository() {
		return this.repository;
	}

	@Override
	public int hashCode() {

		int hash = 0;

		if (this.repository != null) {
			hash ^= this.repository.hashCode();
		}
		if (this.model != null) {
			hash ^= this.model.hashCode();
		}
		if (this.object != null) {
			hash ^= this.object.hashCode();
		}
		if (this.field != null) {
			hash ^= this.field.hashCode();
		}
		return hash;
	}

	@Override
	public boolean isParentOf(final XAddress child) {

		if (!XI.equals(this.repository, child.getRepository())) {
			return false;
		}

		// same repository (may be null)

		if (!XI.equals(this.model, child.getModel())) {
			return this.repository != null && this.model == null && child.getObject() == null;
		}

		// same repository and model (may be null)

		if (!XI.equals(this.object, child.getObject())) {
			return this.model != null && this.object == null && child.getField() == null;
		}

		// same repository, model and object (may be null)

		return this.object != null && this.field == null && child.getField() != null;

	}

	@Override
	public String toString() {
		return toURI();
	}

	/**
	 * @return a unique, complete representation of this {@link XAddress} with
	 *         the fixed format = '/' + repoID + '/' + modelId + '/' + objectId
	 *         + '/' + fieldId. Empty {@link XId XIds} are represented by '-'.
	 *
	 *         Always starts with '/', never ends with '/'.
	 */
	@Override
	public String toURI() {
		final StringBuffer uri = new StringBuffer();

		uri.append('/');
		if (this.repository != null) {
			uri.append(this.repository.toString());
		} else {
			uri.append("-");
		}

		uri.append('/');
		if (this.model != null) {
			uri.append(this.model.toString());
		} else {
			uri.append("-");
		}

		uri.append('/');
		if (this.object != null) {
			uri.append(this.object.toString());
		} else {
			uri.append("-");
		}

		uri.append('/');
		if (this.field != null) {
			uri.append(this.field.toString());
		} else {
			uri.append("-");
		}

		return uri.toString();
	}

	@Override
	public ValueType getType() {
		return ValueType.Address;
	}

	@Override
	public XAddress getValue() {
		return this;
	}

}
