package org.xydra.minio;

public interface MiniReader {
	
	void close();
	
	boolean markSupported();
	
	int read();
	
	int read(char[] buffer, int pos, int i);
	
	void mark(int maxValue);
	
	void reset();
	
	boolean ready();
	
}
