package org.xydra.oo.testgen.tasks.shared;

import org.xydra.base.IHasXId;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.Field;

/**
 * Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of
 * xydra.org:oo
 */
public interface IHome extends IHasXId {

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @return the current value or null if not defined
	 */
	@Field("inbox")
	IBaseList getInbox();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @return the current value or null if not defined
	 */
	@Field("starred")
	IBaseList getStarred();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @return the current value or null if not defined
	 */
	@Field("today")
	IBaseList getToday();

	/**
	 * [generated from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @return the current value or null if not defined
	 */
	@Field("week")
	IBaseList getWeek();

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
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @param inbox
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Home']
	 * @return ...
	 */
	@Field("inbox")
	IHome setInbox(IBaseList inbox);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @param starred
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Home']
	 * @return ...
	 */
	@Field("starred")
	IHome setStarred(IBaseList starred);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @param today
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Home']
	 * @return ...
	 */
	@Field("today")
	IHome setToday(IBaseList today);

	/**
	 * Set a value, silently overwriting existing values, if any. [generated
	 * from: 'org.xydra.oo.testspecs.TasksSpec.Home']
	 *
	 * @param week
	 *            the value to set [generated from:
	 *            'org.xydra.oo.testspecs.TasksSpec.Home']
	 * @return ...
	 */
	@Field("week")
	IHome setWeek(IBaseList week);

}
