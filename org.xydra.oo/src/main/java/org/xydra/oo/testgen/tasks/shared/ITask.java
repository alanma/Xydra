package org.xydra.oo.testgen.tasks.shared;

import java.util.List;
import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;
import org.xydra.oo.testgen.tasks.shared.ITask;

/**
 * Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of
 * xydra.org:oo
 */
public interface ITask extends IHasXId {

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("checked")
	boolean getChecked();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("completionDate")
	long getCompletionDate();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("dueDate")
	long getDueDate();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("note")
	String getNote();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("rECENTLY_COMPLETED")
	long getRECENTLY_COMPLETED();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("remindDate")
	long getRemindDate();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("starred")
	boolean getStarred();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return the current value or null if not defined
	 */
	@Field("title")
	String getTitle();

	/**
	 * For GWT-internal use only [generated from: 'toClassSpec 1']
	 * 
	 * @param model
	 *            [generated from: 'toClassSpec 2']
	 * @param id
	 *            [generated from: 'toClassSpec 3']
	 */
	void init(XWritableModel model, XId id);

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return ...
	 */
	boolean isRecentlyCompleted();

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param checked
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("checked")
	ITask setChecked(boolean checked);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param completionDate
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("completionDate")
	ITask setCompletionDate(long completionDate);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param dueDate
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("dueDate")
	ITask setDueDate(long dueDate);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param note
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("note")
	ITask setNote(String note);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param rECENTLY_COMPLETED
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("rECENTLY_COMPLETED")
	ITask setRECENTLY_COMPLETED(long rECENTLY_COMPLETED);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param remindDate
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("remindDate")
	ITask setRemindDate(long remindDate);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param starred
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("starred")
	ITask setStarred(boolean starred);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @param title
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Task']
	 * @return ...
	 */
	@Field("title")
	ITask setTitle(String title);

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Task']
	 * 
	 * @return a writable collection proxy, never null
	 */
	@Field("subTasks")
	List<ITask> subTasks();

}
