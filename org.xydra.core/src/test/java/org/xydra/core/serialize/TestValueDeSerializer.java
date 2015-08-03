package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.index.query.Pair;


public class TestValueDeSerializer {

	@Test
	public void testSimple() {
		final XRepository repo = new MemoryRepository(Base.toId("actor"), "secret", Base.toId("repo"));
		DemoModelUtil.addPhonebookModel(repo);
		final XModel model = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		final XObject xo = model.getObject(DemoModelUtil.JOHN_ID);
		for(final XId fid : xo) {
			final XField field = xo.getField(fid);
			final XValue value = field.getValue();
			if(value != null) {
				final Pair<String,String> pair = ValueDeSerializer.toStringPair(value);
				System.out.println(pair);
				final XValue value2 = ValueDeSerializer.fromStringPair(pair);
				assertEquals(value, value2);
			}
		}

	}

}
