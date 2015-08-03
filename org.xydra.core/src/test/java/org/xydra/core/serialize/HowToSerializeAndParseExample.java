package org.xydra.core.serialize;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.xydra.base.Base;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.json.JsonSerializer;


public class HowToSerializeAndParseExample {

	public static void main(final String[] args) {
		// given a model1
		final XId actorId = Base.toId("actor1");
		final String passwordHash = "secret";
		final MemoryRepository repo = new MemoryRepository(actorId, passwordHash, Base.toId("repo1"));
		final XModel model1 = repo.createModel(Base.toId("model1"));
		model1.createObject(Base.toId("john")).createField(Base.toId("phone"))
		        .setValue(XV.toValue("123-456"));

		// set up corresponding serialiser & parser
		final JsonSerializer serializer = new JsonSerializer();
		final JsonParser parser = new JsonParser();

		// serialise with revisions
		final XydraOut out = serializer.create();
		out.enableWhitespace(true, true);
		SerializedModel.serialize(model1, out);
		assertTrue(out.isClosed());
		final String data = out.getData();
		// store data

		System.out.println(data);

		// later: load dater
		final XydraElement xydraElement = parser.parse(data);
		final XModel modelAgain = SerializedModel.toModel(actorId, passwordHash, xydraElement);
		assertTrue(XCompareUtils.equalState(model1, modelAgain));

		// check that there is a change log
		final XChangeLog changeLog = modelAgain.getChangeLog();
		assertNotNull(changeLog);
	}

}
