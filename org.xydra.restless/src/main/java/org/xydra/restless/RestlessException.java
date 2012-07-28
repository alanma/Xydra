package org.xydra.restless;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ThreadSafe;


@ThreadSafe
public class RestlessException extends RuntimeException {
	
	public static final int Moved_permanently = 301;
	
	public static final int Bad_request = 400;
	
	/**
	 * Similar to 403 Forbidden, but specifically for use when authentication is
	 * possible but has failed or not yet been provided. The response must
	 * include a WWW-Authenticate header field containing a challenge applicable
	 * to the requested resource. See Basic access authentication and Digest
	 * access authentication.
	 */
	public static final int Unauthorized = 401;
	
	/**
	 * The request was a legal request, but the server is refusing to respond to
	 * it. Unlike a 401 Unauthorized response, authenticating will make no
	 * difference.
	 */
	public static final int Forbidden = 403;
	
	/**
	 * The requested resource could not be found but may be available again in
	 * the future. Subsequent requests by the client are permissible.
	 */
	public static final int Not_found = 404;
	
	/**
	 * A request was made of a resource using a request method not supported by
	 * that resource; for example, using GET on a form which requires data to be
	 * presented via POST, or using PUT on a read-only resource.
	 */
	public static final int Method_Not_Allowed = 405;
	
	// 406 Not Acceptable
	// The requested resource is only capable of generating content not
	// acceptable according to the Accept headers sent in the request.[2]
	
	// 407 Proxy Authentication Required[2]
	
	// 408 Request Timeout
	// The server timed out waiting for the request. According to W3 HTTP
	// specifications:
	// "The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time."
	
	/**
	 * Indicates that the request could not be processed because of conflict in
	 * the request, such as an edit conflict.
	 */
	public static final int Conflict = 409;
	
	// 410 Gone
	// Indicates that the resource requested is no longer available and will not
	// be available again.[2] This should be used when a resource has been
	// intentionally removed; however, it is not necessary to return this code
	// and a 404 Not Found can be issued instead. Upon receiving a 410 status
	// code, the client should not request the resource again in the future.
	// Clients such as search engines should remove the resource from their
	// indexes.
	
	// 411 Length Required
	// The request did not specify the length of its content, which is required
	// by the requested resource.[2]
	
	// 412 Precondition Failed
	// The server does not meet one of the preconditions that the requester put
	// on the request.[2]
	
	// 413 Request Entity Too Large
	// The request is larger than the server is willing or able to process.[2]
	
	/** The URI provided was too long for the server to process. */
	public static final int Request_URI_Too_Long = 414;
	
	/**
	 * The request entity has a media type which the server or resource does not
	 * support. For example the client uploads an image as image/svg+xml, but
	 * the server requires that images use a different format.
	 */
	public static final int Unsupported_Media_Type = 415;
	
	// 416 Requested Range Not Satisfiable
	// The client has asked for a portion of the file, but the server cannot
	// supply that portion.[2] For example, if the client asked for a part of
	// the file that lies beyond the end of the file.
	
	// 417 Expectation Failed
	// The server cannot meet the requirements of the Expect request-header
	// field.[2]
	
	// 418 I'm a teapot
	// The HTCPCP server is a teapot.[8] The responding entity MAY be short and
	// stout.[8] This code was defined as one of the traditional IETF April
	// Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol, and is
	// not expected to be implemented by actual HTTP servers.
	
	// 421 There are too many connections from your internet address
	
	// 422 Unprocessable Entity (WebDAV) (RFC 4918)
	// The request was well-formed but was unable to be followed due to semantic
	// errors.[4]
	
	// 423 Locked (WebDAV) (RFC 4918)
	// The resource that is being accessed is locked[4]
	
	// 424 Failed Dependency (WebDAV) (RFC 4918)
	// The request failed due to failure of a previous request (e.g. a
	// PROPPATCH).[4]
	
	// 425 Unordered Collection (RFC 3648)
	// Defined in drafts of "WebDAV Advanced Collections Protocol",[9] but not
	// present in
	// "Web Distributed Authoring and Versioning (WebDAV) Ordered Collections Protocol".[10]
	
	// 426 Upgrade Required (RFC 2817)
	// The client should switch to a different protocol such as TLS/1.0.[11]
	
	// 449 Retry With
	// A Microsoft extension. The request should be retried after doing the
	// appropriate action.[12]
	
	// 450 Blocked by Windows Parental Controls
	// A Microsoft extension. This error is given when Windows Parental Controls
	// are turned on and are blocking access to the given webpage.[13]
	
	/**
	 * A generic error message, given when no more specific message is suitable.
	 */
	public static final int Internal_Server_Error = 500;
	
	/**
	 * The server either does not recognise the request method, or it lacks the
	 * ability to fulfill the request.
	 */
	public static final int Not_Implemented = 501;
	
	/**
	 * The server was acting as a gateway or proxy and received an invalid
	 * response from the upstream server.
	 */
	public static final int Bad_Gateway = 502;
	
	/**
	 * The server is currently unavailable (because it is overloaded or down for
	 * maintenance). Generally, this is a temporary state.
	 */
	public static final int Service_Unavailable = 503;
	
	// The server was acting as a gateway or proxy and did not receive a timely
	// request from the upstream server.[2]
	public static final int Gateway_Timeout = 504;
	
	// The server does not support the HTTP protocol version used in the
	// request.[2]
	// 505 HTTP Version Not Supported
	
	// Transparent content negotiation for the request, results in a circular
	// reference.[14]
	// 506 Variant Also Negotiates (RFC 2295)
	
	// 507 Insufficient Storage (WebDAV) (RFC 4918)[4]
	
	// This status code, while used by many servers, is not specified in any
	// RFCs.
	// 509 Bandwidth Limit Exceeded (Apache bw/limited extension)
	
	// Further extensions to the request are required for the server to fulfill
	// it.[15]
	// 510 Not Extended (RFC 2774)
	
	// 530 User access denied
	
	private static final long serialVersionUID = -7856139346392732069L;
	
	private final int statusCode;
	
	/**
	 * @param statusCode a HTTP status code @NeverNull
	 * @param message a short message to be displayed to the user. Don't put
	 *            confidential information here. @CanBeNull
	 */
	public RestlessException(@NeverNull int statusCode, @CanBeNull String message) {
		super(message);
		this.statusCode = statusCode;
	}
	
	/**
	 * @param statusCode a HTTP status code @NeverNull
	 * @param message a short message to be displayed to the user. Don't put
	 *            confidential information here. @CanBeNull
	 * @param t TODO make sure this {@link Throwable} is not displayed to remote
	 *            users @CanBeNull
	 */
	public RestlessException(@NeverNull int statusCode, @CanBeNull String message,
	        @CanBeNull Throwable t) {
		super(message, t);
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}
	
}
