package org.xydra.base.id;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.value.ValueType;
import org.xydra.core.XX;


public class MemoryImplTest {

    @Test
    public void testMemoryAddress() {
    	final XId repo = XX.toId("data");
    	final XId model = XX.toId("main");
    	final XId object = XX.toId("cds-rel-hasInstancecds-rel-hasTypecds-item-type_relation");
    	final XId field = null;
    	final MemoryAddress ma = new MemoryAddress(repo,model,object,field);

    	assert ma.getType() == ValueType.Address;
    	assert ma.getAddressedType() == XType.XOBJECT;

    	final XAddress ma2 = ma.getParent();
    	assert ma2.getAddressedType() == XType.XMODEL;
    }

}
