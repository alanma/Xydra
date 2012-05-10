package org.xydra.store.impl.gae.ng;

import org.xydra.annotations.NeverNull;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XValue;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.gae.changes.GaeChange;


/**
 * Decides if execution of a command fails or succeeds
 * 
 * @author xamde
 * 
 */
class ExecuteDecideAlgorithms {
	
	/**
	 * @param command may NOT be a {@link XRepositoryCommand}
	 * @param change
	 * @param inTransaction
	 * @param tsm is used within transactions
	 * @return ..
	 */
	public static ExecutionResult checkPreconditionsAndComputeEvents_atomic(XAtomicCommand command,
	        GaeChange change, boolean inTransaction, @NeverNull ITentativeSnapshotManager tsm) {
		XyAssert.xyAssert(command.getTarget().getAddressedType() != XType.XREPOSITORY);
		XyAssert.xyAssert(command.getChangedEntity().getObject() != null,
		        "MOF commands each must have an objectId in their address");
		XyAssert.xyAssert(tsm != null);
		assert tsm != null;
		
		switch(command.getTarget().getAddressedType()) {
		case XMODEL:
			return ExecuteDecideAlgorithms.executeCommand((XModelCommand)command, change, tsm,
			        inTransaction);
		case XOBJECT:
			return ExecuteDecideAlgorithms.executeCommand((XObjectCommand)command, change, tsm,
			        inTransaction);
		case XFIELD:
			return ExecuteDecideAlgorithms.executeCommand((XFieldCommand)command, change, tsm,
			        inTransaction);
		case XREPOSITORY:
		default:
			throw new AssertionError("Cannot happen");
		}
	}
	
	public static ExecutionResult executeCommand(XFieldCommand command, GaeChange change,
	        @NeverNull ITentativeSnapshotManager tsm, boolean inTransaction) {
		
		TentativeObjectSnapshot tos = tsm.getTentativeObjectSnapshot(tsm.getInfo(),
		        XX.resolveObject(command.getTarget()));
		
		if(!tos.isObjectExists()) {
			return ExecutionResult.failed("No objectSnapshot for field command");
		}
		
		XyAssert.xyAssert(tos != null, "XObjectCommand is invalid: " + command + ", object is null");
		assert tos != null;
		
		XReadableField field = tos.getField(command.getFieldId());
		if(field == null) {
			return ExecutionResult.failed("Command { " + command + "} is invalid. Field '"
			        + command.getFieldId() + "' not found in object '" + command.getObjectId()
			        + "' in rev=" + tos.getRevisionNumber() + "/" + tos.getModelRevision());
		}
		
		if(!command.isForced()) {
			if(command.getChangeType() == ChangeType.ADD && !field.isEmpty()) {
				return ExecutionResult.failed("field has already a value, cannot add one");
			}
			if(command.getChangeType() == ChangeType.CHANGE && !field.isEmpty()) {
				return ExecutionResult.failed("field has no value, cannot change it");
			}
			if(command.getChangeType() == ChangeType.REMOVE && !field.isEmpty()) {
				return ExecutionResult.failed("field has no value, cannot remove it");
			}
			if(field.getRevisionNumber() != command.getRevisionNumber()) {
				return ExecutionResult.failed("Safe FieldCommand {" + command
				        + "} is invalid (wrong revision) field=" + field.getRevisionNumber()
				        + " command=" + command.getRevisionNumber());
			}
		}
		/* ADD or CHANGE */
		if(command.getChangeType() == ChangeType.ADD
		        || command.getChangeType() == ChangeType.CHANGE) {
			XValue oldValue = field.getValue();
			XValue newValue = command.getValue();
			XyAssert.xyAssert(newValue != null);
			boolean sameValue = oldValue != null && oldValue.equals(newValue);
			if(sameValue) {
				return ExecutionResult.successNoChange("had already the same value");
			} else {
				return ExecutionResult.successValue(command, change, tos, tsm, inTransaction);
			}
		} else {
			/* REMOVE */
			if(field.isEmpty()) {
				return ExecutionResult.successNoChange("was empty before, nothing to remove");
			} else {
				return ExecutionResult.successValue(command, change, tos, tsm, inTransaction);
			}
		}
	}
	
	/**
	 * Checks if the given {@link XModelCommand} is valid and can be
	 * successfully executed on this ChangedModel or if the attempt to execute
	 * it will fail.
	 * 
	 * @param command The {@link XModelCommand} which is to be checked.
	 * @param change
	 * @param tsm
	 * @param inTransaction
	 * 
	 * @return true, if the {@link XModelCommand} is valid and can be executed,
	 *         false otherwise
	 */
	public static ExecutionResult executeCommand(XModelCommand command, GaeChange change,
	        @NeverNull ITentativeSnapshotManager tsm, boolean inTransaction) {
		
		TentativeObjectSnapshot tos = tsm.getTentativeObjectSnapshot(tsm.getInfo(),
		        command.getChangedEntity());
		
		switch(command.getChangeType()) {
		case ADD:
			if(tos.isObjectExists()) {
				XyAssert.xyAssert(tos.getModelRevision() >= 0);
				// command is invalid or doesn't change anything
				return command.isForced() ? ExecutionResult.successNoChange("tos.objectExists "
				        + command.getChangedEntity()) : ExecutionResult.failed("XModelCommand "
				        + command + " ADDs object which is already there");
			}
			// command is OK and adds a new object
			return ExecutionResult.successCreatedObject(command, change, tsm,
			        tsm.getModelRevision(tsm.getInfo()), inTransaction);
			
		case REMOVE:
			if(!tos.isObjectExists()) {
				// command is invalid or doesn't change anything
				return command.isForced() ? ExecutionResult.successNoChange("!tos.objectExists")
				        : ExecutionResult.failed("XModelCommand REMOVE " + command
				                + " is invalid or doesn't change anything");
			} else if(tos.getRevisionNumber() != command.getRevisionNumber() && !command.isForced()) {
				// command is invalid
				return ExecutionResult.failed("Safe XModelCommand " + command
				        + " is invalid (revNr mismatch)");
			}
			// command is OK and removes an existing object
			return ExecutionResult.successRemovedObject(command, change, tos, tsm, inTransaction);
		default:
			throw new AssertionError("impossible type for model command " + command);
		}
	}
	
	public static ExecutionResult executeCommand(XObjectCommand command, GaeChange change,
	        @NeverNull ITentativeSnapshotManager tsm, boolean inTransaction) {
		
		TentativeObjectSnapshot tos = tsm.getTentativeObjectSnapshot(tsm.getInfo(),
		        XX.resolveObject(command.getTarget()));
		
		if(!tos.isObjectExists()) {
			return ExecutionResult.failed("TOS says, object '" + command.getChangedEntity()
			        + "' does not exist");
		}
		
		XyAssert.xyAssert(tos != null, "XObjectCommand is invalid: " + command + ", object is null");
		assert tos != null;
		
		XID fieldId = command.getFieldId();
		switch(command.getChangeType()) {
		case ADD:
			if(tos.hasField(fieldId)) {
				return command.isForced() ? ExecutionResult.successNoChange("tos.hasField")
				        : ExecutionResult.failed(command + " object has already field '" + fieldId
				                + "' and foced=" + command.isForced());
			}
			// command is OK and adds a new field
			return ExecutionResult.successCreatedField(change, tos, tsm, fieldId, inTransaction);
			
		case REMOVE:
			XReadableField field = tos.getField(fieldId);
			if(field == null) {
				// command is invalid or doesn't change anything
				return command.isForced() ? ExecutionResult.successNoChange("field==null")
				        : ExecutionResult.failed("XObjectCommand REMOVE '" + command
				                + "'is invalid or doesn't change anything, forced="
				                + command.isForced());
			}
			XyAssert.xyAssert(field != null);
			XyAssert.xyAssert(tos.hasField(fieldId));
			if(field.getRevisionNumber() != command.getRevisionNumber() && !command.isForced()) {
				// command is invalid
				return ExecutionResult.failed("Safe XObjectCommand REMOVE " + command
				        + " revNr mismatch");
			}
			// command is OK and removes an existing field
			return ExecutionResult.successRemovedField(change, tos, tsm, fieldId, inTransaction);
			
		default:
			throw new AssertionError("impossible type for object command " + command);
		}
		
	}
	
}
