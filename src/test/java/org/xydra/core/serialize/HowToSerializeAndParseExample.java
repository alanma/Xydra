package org.xydra.core.serialize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XV;
import org.xydra.core.XCompareUtils;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


public class HowToSerializeAndParseExample {
	
	public static void main(String[] args) {
		// given a model1
		XID actorId = XX.toId("actor1");
		String passwordHash = "secret";
		MemoryRepository repo = new MemoryRepository(actorId, passwordHash, XX.toId("repo1"));
		XModel model1 = repo.createModel(XX.toId("model1"));
		model1.createObject(XX.toId("john")).createField(XX.toId("phone"))
		        .setValue(XV.toValue("123-456"));
		
		// set up corresponding serialiser & parser
		JsonSerializer serializer = new JsonSerializer();
		JsonParser parser = new JsonParser();
		
		// serialise with revisions
		XydraOut out = serializer.create();
		out.enableWhitespace(true, true);
		SerializedModel.serialize(model1, out);
		assertTrue(out.isClosed());
		String data = out.getData();
		// store data
		
		System.out.println(data);
		
		// later: load dater
		XydraElement xydraElement = parser.parse(data);
		XModel modelAgain = SerializedModel.toModel(actorId, passwordHash, xydraElement);
		assertTrue(XCompareUtils.equalState(model1, modelAgain));
		
		// check that there is a change log
		XChangeLog changeLog = modelAgain.getChangeLog();
		assertNotNull(changeLog);
	}
	
}
