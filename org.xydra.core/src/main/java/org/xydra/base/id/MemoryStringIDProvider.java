package org.xydra.base.id;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;


/**
 * An implementation of {@link XIdProvider}
 *
 * @author xamde
 * @author kaidel
 */

@RunsInGWT(true)
@RequiresAppEngine(false)
public class MemoryStringIDProvider extends BaseStringIDProvider implements XIdProvider {

    @Override
    protected XId createInstance(final String uriString) {
        return new MemoryStringID(uriString);
    }

}
