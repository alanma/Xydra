package org.xydra.core.util;

import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.value.XValue;
import org.xydra.index.ITripleIndex;
import org.xydra.index.impl.TripleIndex;
import org.xydra.index.query.Constraint;
import org.xydra.index.query.EqualsConstraint;
import org.xydra.index.query.Wildcard;


public class ModelIndex {
	
	private ITripleIndex<XID,XID,XValue> index = new TripleIndex<XID,XID,XValue>();
	
	public ModelIndex(XModel model) {
		for(XID oID : model) {
			XObject object = model.getObject(oID);
			for(XID fID : object) {
				XField field = object.getField(fID);
				this.index.index(oID, fID, field.getValue());
			}
		}
	}
	
	public boolean matches(XidOrVariable object, XidOrVariable field, XvalueOrVariable value) {
		return this.index.contains(toConstraint(object), toConstraint(field), toConstraint(value));
		
		// if (object == Variable.ANY) {
		// if (field == Variable.ANY) {
		// if (value == Variable.ANY) {
		// // (***) TODO return true if model contains any value
		// } else {
		// // (**V)
		// }
		// } else {
		// if (value == Variable.ANY) {
		// // (*F*)
		// } else {
		// // (*FV)
		// }
		// }
		// } else {
		// if (field == Variable.ANY) {
		// if (value == Variable.ANY) {
		// // (O**) TODO return true if model contains any value
		// // TODO answer without index
		// } else {
		// // (O*V)
		// }
		// } else {
		// if (value == Variable.ANY) {
		// // (OF*)
		// // TODO answer without index
		// } else {
		// // (OFV)
		// // TODO answer without index
		// }
		// }
		// }
	}
	
	private Constraint<XID> toConstraint(XidOrVariable xidOrVariable) {
		if(xidOrVariable == Variable.ANY) {
			return new Wildcard<XID>();
		} else {
			return new EqualsConstraint<XID>(xidOrVariable);
		}
	}
	
	private Constraint<XValue> toConstraint(XvalueOrVariable xvalueOrVariable) {
		if(xvalueOrVariable == Variable.ANY) {
			return new Wildcard<XValue>();
		} else {
			return new EqualsConstraint<XValue>(xvalueOrVariable);
		}
	}
}
