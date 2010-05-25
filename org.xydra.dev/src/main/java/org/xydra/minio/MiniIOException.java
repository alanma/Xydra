package org.xydra.minio;

import java.io.IOException;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;



@RunsInGWT
@RunsInAppEngine
@RunsInJava
public class MiniIOException extends RuntimeException {
	
	public MiniIOException(IOException e) {
		super(e);
	}
	
	public MiniIOException(String msg) {
		super(msg);
	}
	
	private static final long serialVersionUID = -4688233089137736280L;
	
}
