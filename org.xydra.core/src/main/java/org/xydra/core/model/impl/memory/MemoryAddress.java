package org.xydra.core.model.impl.memory;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XType;


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
	
	/*
	 * M=Model, O = Object, F = Field, - = null.
	 * 
	 * From Address to Parent:
	 * 
	 * RMOF -> RMO
	 * 
	 * RMO- -> RM
	 * 
	 * RM-- -> R
	 * 
	 * R--- -> null, no parent defined
	 * 
	 * -MOF -> -MO-
	 * 
	 * -MO- -> -M--
	 * 
	 * -M-- -> null, no parent defined
	 * 
	 * --OF -> --O-
	 * 
	 * --O- -> null, no parent defined
	 * 
	 * ---F -> null, no parent defined
	 */
	public XAddress getParent() {
		// start looking at highest entity
		if(this.repository == null) {
			if(this.model == null) {
				if(this.object == null) {
					// ---F => no parent defined
					return null;
				} else {
					// object is defined
					if(this.field == null) {
						// --O- => no parent defined
						return null;
					} else {
						// --OF => O
						return new MemoryAddress(null, null, this.object, null);
					}
				}
			} else {
				// model is defined
				if(this.object == null) {
					// -M-- -> no parent defined
					return null;
				} else {
					// object is defined
					if(this.field == null) {
						// -MO- => M
						return new MemoryAddress(null, this.model, null, null);
					} else {
						// -MOF => O
						return new MemoryAddress(null, this.model, this.object, null);
					}
				}
			}
		} else {
			// repository != null
			if(this.model == null) {
				// R--- => a repository has no parent
				return null;
			} else {
				// model is defined
				if(this.object == null) {
					// RM-- -> parent is repo
					return new MemoryAddress(this.repository, null, null, null);
				} else {
					// object is defined
					if(this.field == null) {
						// RMO- => M
						return new MemoryAddress(this.repository, this.model, null, null);
					} else {
						// RMOF => O
						return new MemoryAddress(this.repository, this.model, this.object, null);
					}
				}
			}
		}
	}
	
	/**
	 * @return a unique, complete representation of this {@link XAddress} with
	 *         the fixed format = '/' + repoID + '/' + modelID + '/' + objectID
	 *         + '/' + fieldID. Empty IDs are represented by '-'.
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
		if(obj == null)
			return false;
		if(!(obj instanceof XAddress))
			return false;
		XAddress other = (XAddress)obj;
		return XX.equals(this.repository, other.getRepository())
		        && XX.equals(this.model, other.getModel())
		        && XX.equals(this.object, other.getObject())
		        && XX.equals(this.field, other.getField());
	}
	
	public String toURI() {
		return toString();
	}
	
	public static XType getAddressedType(XAddress address) {
		// what is this address addressing?
		if(address.getRepository() == null) {
			if(address.getModel() == null) {
				if(address.getObject() == null) {
					if(address.getField() == null) {
						// (-,-,-,-) ERROR
						throw new AssertionError("This Address should never have been created");
					} else {
						// (-,-,-,+) FIELD
						return XType.XFIELD;
					}
				} else {
					if(address.getField() == null) {
						// (-,-,+,-) OBJECT
						return XType.XOBJECT;
					} else {
						// (-,-,+,+) FIELD
						return XType.XFIELD;
					}
				}
			} else {
				if(address.getObject() == null) {
					if(address.getField() == null) {
						// (-,+,-,-) MODEL
						return XType.XMODEL;
					} else {
						// (-,+,-,+) ERROR
						throw new AssertionError("This Address should never have been created");
					}
				} else {
					if(address.getField() == null) {
						// (-,+,+,-) OBJECT
						return XType.XOBJECT;
					} else {
						// (-,+,+,+) FIELD
						return XType.XFIELD;
					}
				}
			}
		} else {
			if(address.getModel() == null) {
				if(address.getObject() == null) {
					if(address.getField() == null) {
						// (+,-,-,-) REPO
						return XType.XREPOSITORY;
					} else {
						// (+,-,-,+) ERROR
						throw new AssertionError("This Address should never have been created");
					}
				} else {
					if(address.getField() == null) {
						// (+,-,+,-) ERROR
						throw new AssertionError("This Address should never have been created");
					} else {
						// (+,-,+,+) ERROR
						throw new AssertionError("This Address should never have been created");
					}
				}
			} else {
				if(address.getObject() == null) {
					if(address.getField() == null) {
						// (+,+,-,-) MODEL
						return XType.XMODEL;
					} else {
						// (+,+,-,+) ERROR
						throw new AssertionError("This Address should never have been created");
					}
				} else {
					if(address.getField() == null) {
						// (+,+,+,-) OBJECT
						return XType.XOBJECT;
					} else {
						// (+,+,+,+) FIELD
						return XType.XFIELD;
					}
				}
			}
		}
	}
	
}
