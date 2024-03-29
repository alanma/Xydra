package org.xydra.oo.testgen.alltypes.client;

import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.oo.testgen.alltypes.shared.AbstractSharedFactory;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;

import com.google.gwt.core.client.GWT;

/**
 * Generated on Fri Jul 04 01:02:18 CEST 2014 Generated on Fri Jul 04 01:02:18
 * CEST 2014 by SpecWriter, a part of xydra.org:oo
 */
public class GwtFactory extends AbstractSharedFactory {

	/**
	 * [generated from: 'generateFactories-oTz9R']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-mqFtF']
	 */
	public GwtFactory(final XWritableModel model) {
		super(model);
	}

	/**
	 * [generated from: 'generateFactories-21utp']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-Zl7Qm']
	 * @param id
	 *            [generated from: 'generateFactories-aXRfh']
	 * @return ...
	 */
	@Override
	protected IHasAllType getHasAllTypeInternal(final XWritableModel model, final XId id) {
		return wrapHasAllType(model, id);
	}

	/**
	 * [generated from: 'generateFactories-21utp']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-Zl7Qm']
	 * @param id
	 *            [generated from: 'generateFactories-aXRfh']
	 * @return ...
	 */
	@Override
	protected IPerson getPersonInternal(final XWritableModel model, final XId id) {
		return wrapPerson(model, id);
	}

	/**
	 * [generated from: 'generateFactories-4C46E']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-5uhaQ']
	 * @param id
	 *            [generated from: 'generateFactories-fzf9t']
	 * @return ...
	 */
	public static IHasAllType wrapHasAllType(final XWritableModel model, final XId id) {
		final IHasAllType w = GWT.create(IHasAllType.class);
		w.init(model, id);
		return w;
	}

	/**
	 * [generated from: 'generateFactories-4C46E']
	 *
	 * @param model
	 *            [generated from: 'generateFactories-5uhaQ']
	 * @param id
	 *            [generated from: 'generateFactories-fzf9t']
	 * @return ...
	 */
	public static IPerson wrapPerson(final XWritableModel model, final XId id) {
		final IPerson w = GWT.create(IPerson.class);
		w.init(model, id);
		return w;
	}

}
