package org.xydra.json;

import java.io.PrintWriter;

import org.junit.Test;
import org.xydra.json.JSONException;
import org.xydra.json.JSONWriter;
import org.xydra.minio.MiniStreamWriter;
import org.xydra.minio.MiniWriter;



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
