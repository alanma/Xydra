package org.xydra.client.sync;

public abstract class AbstractCommandCallback implements XCommandCallback {
	
	public void applied(long revision) {
		// ignore
	}
	
	public void failedPost() {
		// ignore
	}
	
	public void failed() {
		// ignore
	}
	
}
