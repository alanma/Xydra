package org.xydra.minio;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;


@RunsInGWT
@RunsInAppEngine
@RunsInJava
public interface MiniWriter {
	
	void write(String string) throws MiniIOException;
	
	void write(char c) throws MiniIOException;
	
	void flush() throws MiniIOException;
	
	void close() throws MiniIOException;
	
}
