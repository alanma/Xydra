package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.index.query.Pair;


public class TestValueDeSerializer {
	
	@Test
	public void testSimple() {
		XRepository repo = new MemoryRepository(XX.toId("actor"), "secret", XX.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XObject xo = model.getObject(DemoModelUtil.JOHN_ID);
		for(XId fid : xo) {
			XField field = xo.getField(fid);
			XValue value = field.getValue();
			if(value != null) {
				Pair<String,String> pair = ValueDeSerializer.toStringPair(value);
				System.out.println(pair);
				XValue value2 = ValueDeSerializer.fromStringPair(pair);
				assertEquals(value, value2);
			}
		}
		
	}
	
}
