package org.xydra.oo.testgen.tasks.java;

import java.lang.reflect.Proxy;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.runtime.java.OOJavaOnlyProxy;
import org.xydra.oo.testgen.tasks.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.tasks.shared.IBaseList;
import org.xydra.oo.testgen.tasks.shared.IHome;
import org.xydra.oo.testgen.tasks.shared.ISettings;
import org.xydra.oo.testgen.tasks.shared.ISmartList;
import org.xydra.oo.testgen.tasks.shared.ITask;
import org.xydra.oo.testgen.tasks.shared.ITaskList;
import org.xydra.oo.testgen.tasks.shared.IUser;

/**
 * Generated on Fri Jul 04 01:02:18 CEST 2014 by SpecWriter, a part of
 * xydra.org:oo
 */
public class JavaFactory extends AbstractSharedFactory {

	/**
	 * [generated from: 'generateFactories-WaYjk']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-dctg4']
	 */
	public JavaFactory(final XWritableModel model) {
		super(model);
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected IBaseList getBaseListInternal(final XWritableModel model, final XId id) {
		final IBaseList w = (IBaseList) Proxy.newProxyInstance(IBaseList.class.getClassLoader(),
				new Class<?>[] { IBaseList.class, org.xydra.oo.runtime.java.ICanDump.class },
				new OOJavaOnlyProxy(model, id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected IHome getHomeInternal(final XWritableModel model, final XId id) {
		final IHome w = (IHome) Proxy.newProxyInstance(IHome.class.getClassLoader(), new Class<?>[] {
				IHome.class, org.xydra.oo.runtime.java.ICanDump.class }, new OOJavaOnlyProxy(model,
				id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected ISettings getSettingsInternal(final XWritableModel model, final XId id) {
		final ISettings w = (ISettings) Proxy.newProxyInstance(ISettings.class.getClassLoader(),
				new Class<?>[] { ISettings.class, org.xydra.oo.runtime.java.ICanDump.class },
				new OOJavaOnlyProxy(model, id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected ISmartList getSmartListInternal(final XWritableModel model, final XId id) {
		final ISmartList w = (ISmartList) Proxy.newProxyInstance(ISmartList.class.getClassLoader(),
				new Class<?>[] { ISmartList.class, org.xydra.oo.runtime.java.ICanDump.class },
				new OOJavaOnlyProxy(model, id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected ITask getTaskInternal(final XWritableModel model, final XId id) {
		final ITask w = (ITask) Proxy.newProxyInstance(ITask.class.getClassLoader(), new Class<?>[] {
				ITask.class, org.xydra.oo.runtime.java.ICanDump.class }, new OOJavaOnlyProxy(model,
				id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected ITaskList getTaskListInternal(final XWritableModel model, final XId id) {
		final ITaskList w = (ITaskList) Proxy.newProxyInstance(ITaskList.class.getClassLoader(),
				new Class<?>[] { ITaskList.class, org.xydra.oo.runtime.java.ICanDump.class },
				new OOJavaOnlyProxy(model, id));
		return w;
	}

	/**
	 * [generated from: 'generateFactories-Kus1F']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-PIZOa']
	 * @param id
	 *            [generated from: 'generateFactories-vxB4a']
	 * @return ...
	 */
	@Override
	protected IUser getUserInternal(final XWritableModel model, final XId id) {
		final IUser w = (IUser) Proxy.newProxyInstance(IUser.class.getClassLoader(), new Class<?>[] {
				IUser.class, org.xydra.oo.runtime.java.ICanDump.class }, new OOJavaOnlyProxy(model,
				id));
		return w;
	}

}
