package org.xydra.core.serialize.rdf;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.MemoryModel;


public class NtriplesTest {

	@Test
	public void test() {
		final MiniWriter writer = new MiniStreamWriter(System.out);
		final NTriplesWriter nt = new NTriplesWriter(writer, "http://localhost:8765/admin/rdf");
		final XId actorId = Base.toId("actor");
		final XModel model = new MemoryModel(actorId, "secret", Base.toId("model1"));
		final XObject john = model.createObject(Base.toId("john"));
		final XField phone = john.createField(Base.toId("phone"));
		phone.setValue(XV.toValue(1877));
		nt.triple(john.getAddress(), phone.getAddress(), model.getAddress());
		nt.flush();
	}

}
