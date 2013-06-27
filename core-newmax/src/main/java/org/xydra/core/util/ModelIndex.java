package org.xydra.core.util;

import org.xydra.base.XId;
import org.xydra.base.value.XValue;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.index.ITripleIndex;
import org.xydra.index.impl.TripleIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Wildcard;


public class ModelIndex {
	
	private ITripleIndex<XId,XId,XValue> index = new TripleIndex<XId,XId,XValue>();
	
	public ModelIndex(XModel model) {
		for(XId oID : model) {
			XObject object = model.getObject(oID);
			for(XId fID : object) {
				XField field = object.getField(fID);
				this.index.index(oID, fID, field.getValue());
			}
		}
	}
	
	public boolean matches(XidOrVariable object, XidOrVariable field, XvalueOrVariable value) {
		return this.index.contains(toConstraint(object), toConstraint(field), toConstraint(value));
		
	}
	
	private static Constraint<XId> toConstraint(XidOrVariable xidOrVariable) {
		if(xidOrVariable == Variable.ANY) {
			return new Wildcard<XId>();
		} else {
			return new EqualsConstraint<XId>(xidOrVariable);
		}
	}
	
	private static Constraint<XValue> toConstraint(XvalueOrVariable xvalueOrVariable) {
		if(xvalueOrVariable == Variable.ANY) {
			return new Wildcard<XValue>();
		} else {
			return new EqualsConstraint<XValue>(xvalueOrVariable);
		}
	}
}
