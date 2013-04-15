package org.xydra.core.serialize.json;

import org.junit.Test;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XV;
import org.xydra.core.XX;
import org.xydra.core.model.XObject;
import org.xydra.core.serialize.LineBreaks;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.util.DumpUtils;


public class JsonKnownBugsTest {
	
	@Test
	public void deserializeWithLineBreaks() {
		String enc = "{\"$t\":\"xobject\",\"xid\":\"obj1\",\"revision\":0,\"fields\":{\"field1\":{\"revision\":0,\"value\":{\"$t\":\"xstring\",\"data\":\"aaa\\nbbb\\nccc\\nddd\\neee\"}}}}";
		JsonParser parser = new JsonParser();
		XydraElement element = parser.parse(enc);
		
		XObject xo = SerializedModel.toObject(XX.toId("actor"), null, element);
		
		DumpUtils.dump("xo", xo);
	}
	
	@Test
	public void serializeWithLineBreaks() {
		
		SimpleObject so = new SimpleObject(XX.toAddress("/repo1/model1/obj1/-"));
		so.createField(XX.toId("field1")).setValue(XV.toValue(
		
		"aaa" + LineBreaks.LF + // normal line break
		        
		        "bbb" + LineBreaks.LF + // normal line break
		        
		        "ccc" + LineBreaks.CR + // other line break
		        
		        "ddd" + LineBreaks.CRLF + // third popular line break CRLF
		        
		        "eee"));
		
		JsonOut jo = new JsonOut();
		SerializedModel.serialize(so, jo);
		String enc = jo.getData();
		System.out.println(enc);
		
		JsonParser parser = new JsonParser();
		XydraElement element = parser.parse(enc);
		
		XObject xo = SerializedModel.toObject(XX.toId("actor"), null, element);
		
		DumpUtils.dump("xo", xo);
	}
	
}
