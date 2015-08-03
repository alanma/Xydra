package org.xydra.store.access.impl.delegate;

/**
 * Provides hooks for listeners. They can listen to
 * {@link IHookListener#beforeRead()} and/or {@link IHookListener#beforeWrite()}
 * which are invoked before calling the read/write functionality of the
 * instance.
 *
 * @author xamde
 */
public interface ISendHookEvents {

	void addHookListener(IHookListener listener);

	void removeHookListener(IHookListener listener);

}
