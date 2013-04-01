package org.xydra.core.serialize.rdf;

import org.junit.Test;
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
		MiniWriter writer = new MiniStreamWriter(System.out);
		NTriplesWriter nt = new NTriplesWriter(writer, "http://localhost:8765/admin/rdf");
		XId actorId = XX.toId("actor");
		XModel model = new MemoryModel(actorId, "secret", XX.toId("model1"));
		XObject john = model.createObject(XX.toId("john"));
		XField phone = john.createField(XX.toId("phone"));
		phone.setValue(XV.toValue(1877));
		nt.triple(john.getAddress(), phone.getAddress(), model.getAddress());
		nt.flush();
	}
	
}
