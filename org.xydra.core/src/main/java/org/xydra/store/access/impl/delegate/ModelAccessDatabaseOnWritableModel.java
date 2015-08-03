package org.xydra.store.access.impl.delegate;

import java.util.HashSet;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.WritableUtils;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.access.XAccessRightDefinition;
import org.xydra.store.access.XAccessRightValue;
import org.xydra.store.access.impl.memory.MemoryAccessDefinition;


/**
 * Reads data initially from a {@link XWritableRepository} which is also used to
 * persist access rights management data.
 *
 * See {@link PartialAuthorisationDatabaseOnWritableRepository} for the used
 * mapping from internal data structures to Xydras repository/model/object/field
 * layout.
 *
 * @author xamde
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class ModelAccessDatabaseOnWritableModel {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
	        .getLogger(ModelAccessDatabaseOnWritableModel.class);

	protected XWritableModel rightsModel;

	/**
	 * @param rightsModel which is decorated with a nice access DB API
	 */
	public ModelAccessDatabaseOnWritableModel(final XWritableModel rightsModel) {
		XyAssert.xyAssert(rightsModel != null); assert rightsModel != null;
		this.rightsModel = rightsModel;
	}

	public XAccessRightValue getAccessDefinition(final XId actor, final XAddress resource, final XId access)
	        throws IllegalArgumentException {
		XyAssert.xyAssert(resource.getAddressedType() != XType.XREPOSITORY);
		return valueToAccessValue(WritableUtils.getValue(this.rightsModel, actor,
		        PartialAuthorisationDatabaseOnWritableRepository.toFieldId(resource, access)));
	}

	public Set<XAccessRightDefinition> getDefinitions() {
		final Set<XAccessRightDefinition> defs = new HashSet<XAccessRightDefinition>();
		// each objectId is an actorId
		for(final XId actorId : this.rightsModel) {
			defs.addAll(getDefinitionsFor(actorId));
		}
		return defs;
	}

	/**
	 * @param actorId Get only definitions that apply to this actor.
	 * @return all {@link XAccessRightDefinition} defined for actorId
	 */
	public Set<XAccessRightDefinition> getDefinitionsFor(final XId actorId) {
		final Set<XAccessRightDefinition> defs = new HashSet<XAccessRightDefinition>();
		final XWritableObject actorObject = this.rightsModel.getObject(actorId);
		for(final XId fieldId : actorObject) {
			final XWritableField field = actorObject.getField(fieldId);
			final XValue value = field.getValue();
			if(value != null) {
				// parse fieldId
				final Pair<XAddress,XId> pair = PartialAuthorisationDatabaseOnWritableRepository
				        .fromFieldId(fieldId);
				final XAddress resource = pair.getFirst();
				final XId access = pair.getSecond();
				final boolean allowed = ((XBooleanValue)value).contents();
				final MemoryAccessDefinition mad = new MemoryAccessDefinition(access, resource, actorId,
				        allowed);
				defs.add(mad);
			}
		}
		return defs;
	}

	public void resetAccess(final XId actor, final XAddress resource, final XId access) {
		final XId fieldId = PartialAuthorisationDatabaseOnWritableRepository.toFieldId(resource, access);
		WritableUtils.removeValue(this.rightsModel, actor, fieldId);
	}

	public void setAccess(final XId actor, final XAddress resource, final XId access, final boolean allowed) {
		final XId fieldId = PartialAuthorisationDatabaseOnWritableRepository.toFieldId(resource, access);
		WritableUtils.setValue(this.rightsModel, actor, fieldId, BaseRuntime.getValueFactory()
		        .createBooleanValue(allowed));
	}

	private static XAccessRightValue valueToAccessValue(final XValue value) {
		if(value == null) {
			return XAccessRightValue.UNDEFINED;
		}
		if(((XBooleanValue)value).contents()) {
			return XAccessRightValue.ALLOWED;
		} else {
			return XAccessRightValue.DENIED;
		}
	}

}
