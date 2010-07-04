package org.xydra.core.model.delta;

import org.xydra.core.XX;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XID;
import org.xydra.core.value.XValue;


/**
 * An {@link XBaseField}/{@link DeltaField} that represents changes to an
 * {@link XBaseField}.
 * 
 * An {@link XBaseField} is passed as an argument of the constructor. This
 * ChangedField will than basically represent the given {@link XBaseField} and
 * allow changes on its {@link XValue}. The changes do not happen directly on
 * the passed {@link XBaseField} but rather on a sort of copy that emulates the
 * passed {@link XBaseField}. A ChangedField provides methods to compare the
 * current state to the state the passed {@link XBaseField} was in at creation
 * time.
 * 
 * @author dscharrer
 * 
 */
public class ChangedField implements DeltaField {
	
	private XValue value;
	private final XBaseField base;
	boolean changed = false;
	
	/**
	 * @param base The {@link XBaseField} this ChangedField will encapsulate and
	 *            represent
	 */
	/*
	 * TODO Woudln't it be better to actually copy the given base entitiy?
	 * (think about synchronization problems - somebody might change the base
	 * entity while this "changed" entity is being used, which may result in
	 * complete confusion (?))
	 */
	public ChangedField(XBaseField base) {
		this.value = base.getValue();
		this.base = base;
	}
	
	public void setValue(XValue value) {
		this.changed = this.changed || !XX.equals(value, this.base.getValue());
		/*
		 * TODO I added an disjunction with this.changed to makes sure, that
		 * this.changed stays true, even if the value gets changed to the
		 * original value again - due to a lack of documentation I don't know if
		 * it was actually the purpose of this method to reset this.changed to
		 * false if this happens, but to me it seems like this is not the case
		 * and this.changed should stay true after at least one real change
		 * happened
		 */
		this.value = value;
	}
	
	public XID getID() {
		return this.base.getID();
	}
	
	/**
	 * @return the revision number of the original {@link XBaseField}
	 */
	// TODO Maybe a method for returning the revision number this ChangedField
	// would have if it would be a real field would be a good idea?
	public long getRevisionNumber() {
		return this.base.getRevisionNumber();
	}
	
	public XValue getValue() {
		return this.value;
	}
	
	/**
	 * @return The {@link XValue} the encapsulated {@link XBaseField} had at the
	 *         creation time of this ChangedField.
	 */
	public XValue getOldValue() {
		return this.base.getValue();
	}
	
	/**
	 * @return true, if the current {@link XValue} of this ChangedField was
	 *         changed since its creation time.
	 */
	public boolean isChanged() {
		return this.changed;
	}
	
	public XAddress getAddress() {
		return this.base.getAddress();
	}
	
	public boolean isEmpty() {
		return this.value == null;
	}
	
	/*
	 * TODO What is the purpose of this method? It currently does not provide
	 * the functionality suggested by its name. Furthermore, the passed argument
	 * is not used at all.
	 */
	public int countChanges(int i) {
		return isChanged() ? 1 : 0;
	}
	
}
