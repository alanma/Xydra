package org.xydra.base.minio;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public interface MiniWriter {
	
	void write(String string) throws MiniIOException;
	
	void write(char c) throws MiniIOException;
	
	void flush() throws MiniIOException;
	
	void close() throws MiniIOException;
	
}
