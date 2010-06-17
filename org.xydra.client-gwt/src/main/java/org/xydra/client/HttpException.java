package org.xydra.client;

public class HttpException extends ServiceException {
	
	private static final long serialVersionUID = -1316683211161143105L;
	
	private final int status;
	private final String text;
	
	public HttpException(int status, String text) {
		super("HTTP error " + status + ": " + text);
		this.status = status;
		this.text = text;
	}
	
	public int getStatus() {
		return this.status;
	}
	
	public String getText() {
		return this.text;
	}
	
}
