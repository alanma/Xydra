package org.xydra.json;

import java.io.PrintWriter;

import org.junit.Test;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.serialize.json.JSONException;
import org.xydra.json.JSONWriter;



public class TestJsonWriter {
	
	@Test
	public void testWriter() throws JSONException {
		MiniWriter w = new MiniStreamWriter(new PrintWriter(System.out));
		JSONWriter jw = new JSONWriter(w);
		jw.objectStart();
		jw.key("john");
		jw.value("hello");
		jw.objectEnd();
		w.flush();
		
	}
	
}
