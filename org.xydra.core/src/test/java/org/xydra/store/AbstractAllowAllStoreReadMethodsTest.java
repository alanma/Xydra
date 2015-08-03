package org.xydra.store;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.impl.delegate.DelegatingAllowAllStore;


/**
 * This is an abstract class capsuling behaviour that is the same for all
 * instantiations of {@link DelegatingAllowAllStore}.
 *
 *
 * @author kaidel
 */
public abstract class AbstractAllowAllStoreReadMethodsTest extends AbstractStoreReadMethodsTest {

    /**
     * Every user can do everything, so it doesn't matter what we return here.
     * We use 'DirkCanDoAll' because that is easier in the debugger.
     */
    @Override
    protected XId getCorrectUser() {
        return Base.toId("DirkCanDoAll");
    }

    @Override
    protected String getCorrectUserPasswordHash() {
        return "Test";
    }

    /**
     * Incorrect users do not exist, so we'll need to return null and do nothing
     * else
     */
    @Override
    protected XId getIncorrectUser() {
        return null;
    }

    @Override
    protected String getIncorrectUserPasswordHash() {
        return null;
    }

    /**
     * @return a new instance of AllowAllStore initialized with the given
     *         {@link DelegatingAllowAllStore}. This makes it possible to reuse
     *         this test with different instantiations.
     */
    @Override
	public DelegatingAllowAllStore createStore() {
        return new DelegatingAllowAllStore(createPersistence());
    }

    protected abstract XydraPersistence createPersistence();

}
