package org.xydra.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedStore;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.SerializedStore.EventsRequest;
import org.xydra.core.serialize.json.JsonOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.index.query.Pair;
import org.xydra.minio.MiniStreamWriter;
import org.xydra.minio.MiniWriter;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.BatchedResult;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.RequestException;
import org.xydra.store.StoreException;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraStore;


public class XydraStoreResource {
	
	private final static XydraParser jsonParser = new JsonParser();
	private final static XydraParser xmlParser = new XmlParser();
	private final static Set<String> jsonMimes = new HashSet<String>();
	private final static Set<String> xmlMimes = new HashSet<String>();
	private final static Set<String> mimes = new HashSet<String>();
	
	private static final String DEFAULT_CONTENT_TYPE = "application/xml";
	
	public static void restless(Restless restless, String prefix) {
		
		jsonMimes.add("application/json");
		jsonMimes.add("application/x-javascript");
		jsonMimes.add("text/javascript");
		jsonMimes.add("text/x-javascript");
		jsonMimes.add("text/x-json");
		mimes.addAll(jsonMimes);
		
		xmlMimes.add("application/xml");
		xmlMimes.add("text/xml");
		mimes.addAll(xmlMimes);
		
		RestlessParameter actorId = new RestlessParameter("actorId");
		RestlessParameter passwordHash = new RestlessParameter("passwordHash");
		
		restless.addMethod(prefix + "/login", "GET", XydraStoreResource.class, "checkLogin", false,
		        actorId, passwordHash);
		
		RestlessParameter addresses = new RestlessParameter("address", true);
		RestlessParameter from = new RestlessParameter("beginRevision", true);
		RestlessParameter to = new RestlessParameter("endRevision", true);
		
		restless.addMethod(prefix + "/execute", "POST", XydraStoreResource.class,
		        "executeCommands", false, actorId, passwordHash, addresses, from, to);
		
		restless.addMethod(prefix + "/events", "GET", XydraStoreResource.class, "getEvents", false,
		        actorId, passwordHash, addresses, from, to);
		
		restless.addMethod(prefix + "/revisions", "GET", XydraStoreResource.class,
		        "getModelRevisions", false, actorId, passwordHash, addresses);
		
		restless.addMethod(prefix + "/snapshots", "GET", XydraStoreResource.class, "getSnapshots",
		        false, actorId, passwordHash, addresses);
		
		restless.addMethod(prefix + "/repository/models", "GET", XydraStoreResource.class,
		        "getModelIds", false, actorId, passwordHash);
		
		restless.addMethod(prefix + "/repository/id", "GET", XydraStoreResource.class,
		        "getRepositoryId", false, actorId, passwordHash);
		
		restless.addMethod(prefix + "/ping", "GET", XydraStoreResource.class, "ping", false);
		
		// TODO support class-specific exception handlers
		restless.addExceptionHandler(new RestlessExceptionHandler() {
			@Override
			public boolean handleException(Throwable t, IRestlessContext context) {
				
				// TODO remove?
				if(!(t instanceof StoreException) && !(t instanceof IllegalArgumentException)) {
					return false;
				}
				
				int statusCode = HttpServletResponse.SC_OK;
				
				/*
				 * HttpServletResponse. SC_INTERNAL_SERVER_ERROR ; if(t
				 * instanceof RequestException) { statusCode =
				 * HttpServletResponse .SC_BAD_REQUEST; } else if(t instanceof
				 * AccessException) { statusCode = HttpServletResponse
				 * .SC_FORBIDDEN; } else if(t instanceof AuthorisationException
				 * ) { statusCode = HttpServletResponse .SC_FORBIDDEN; } else
				 * if(t instanceof QuotaException) { statusCode =
				 * HttpServletResponse .SC_FORBIDDEN; }
				 */

				XydraOut out = startOutput(context, statusCode);
				SerializedStore.serializeException(t, out);
				out.flush();
				
				return true;
			}
			
		});
		
	}
	
	private XID getActorId(String actorIdString) {
		
		try {
			return XX.toId(actorIdString);
		} catch(Throwable t) {
			throw new RequestException("invalid actor XID: " + actorIdString);
		}
		
	}
	
	private static String getBestContentType(IRestlessContext context) {
		
		HttpServletRequest req = context.getRequest();
		
		String mime = req.getParameter("contentType");
		if(mime != null) {
			mime = mime.trim();
			if(!mimes.contains(mime)) {
				try {
					context.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST,
					        "Unknown content type: " + mime);
				} catch(IOException e) {
					// ignored
				}
				throw new RuntimeException();
			}
			return mime;
		}
		
		String best = DEFAULT_CONTENT_TYPE;
		float bestScore = Float.MIN_VALUE;
		
		@SuppressWarnings("unchecked")
		Enumeration<String> headers = req.getHeaders("Accept");
		
		while(headers.hasMoreElements()) {
			
			String accept = headers.nextElement();
			
			String[] types = accept.split(",");
			for(String type : types) {
				
				String[] parts = type.split(";");
				if(!mimes.contains(parts[0])) {
					continue;
				}
				float score = 1.f;
				if(parts.length > 1) {
					String[] param = parts[1].split("=");
					if(param.length > 1 && param[0].trim() == "q") {
						score = Float.parseFloat(param[1].trim());
					}
				}
				if(score > bestScore) {
					best = parts[0];
					bestScore = score;
				}
			}
			
		}
		
		return best;
	}
	
	private static XydraOut startOutput(IRestlessContext context, int statusCode) {
		
		HttpServletResponse res = context.getResponse();
		
		String mime = getBestContentType(context);
		
		res.setStatus(statusCode);
		res.setContentType(mime + "; charset=UTF-8");
		res.setCharacterEncoding("utf-8");
		try {
			MiniWriter writer = new MiniStreamWriter(res.getOutputStream());
			XydraOut out;
			if(xmlMimes.contains(mime)) {
				out = new XmlOut(writer);
			} else if(jsonMimes.contains(mime)) {
				out = new JsonOut(writer);
			} else {
				throw new AssertionError();
			}
			String format = context.getRequest().getParameter("format");
			if("pretty".equals(format)) {
				out.enableWhitespace(true, true);
			}
			return out;
		} catch(IOException e) {
			throw new RuntimeException("re-throw", e);
		}
	}
	
	public void checkLogin(IRestlessContext context, String actorIdStr, String passwordHash)
	        throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<Boolean> callback = new WaitingCallback<Boolean>();
		store.checkLogin(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeAuthenticationResult(callback.getResult(), out);
		
		out.flush();
		
	}
	
	public void executeCommands(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses, String[] from, String[] to) throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		EventsRequest ger = parseEventsRequest(addresses, from, to);
		
		WaitingCallback<XID> revId = new WaitingCallback<XID>();
		store.getRepositoryId(actorId, passwordHash, revId);
		
		if(revId.getException() != null) {
			throw revId.getException();
		}
		XAddress repoAddr = XX.toAddress(revId.getResult(), null, null, null);
		
		String rawCommands = XydraRestServer.readPostData(context.getRequest());
		List<XCommand> commandsList;
		try {
			XydraElement element;
			if(jsonMimes.contains(context.getRequest().getContentType())) {
				try {
					element = jsonParser.parse(rawCommands);
				} catch(Exception e) {
					element = xmlParser.parse(rawCommands);
				}
			} else {
				try {
					element = xmlParser.parse(rawCommands);
				} catch(Exception e) {
					element = jsonParser.parse(rawCommands);
				}
			}
			commandsList = SerializedCommand.toCommandList(element, repoAddr);
		} catch(Exception e) {
			throw new RequestException("error parsing commands list: " + e.getMessage());
		}
		XCommand[] commands = commandsList.toArray(new XCommand[commandsList.size()]);
		
		BatchedResult<Long>[] commandRes;
		BatchedResult<XEvent[]>[] eventsRes;
		
		if(ger.requests.length == 0) {
			
			WaitingCallback<BatchedResult<Long>[]> callback = new WaitingCallback<BatchedResult<Long>[]>();
			store.executeCommands(actorId, passwordHash, commands, callback);
			
			if(callback.getException() != null) {
				throw callback.getException();
			}
			
			commandRes = callback.getResult();
			eventsRes = null;
			
		} else {
			
			WaitingCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new WaitingCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>();
			store.executeCommandsAndGetEvents(actorId, passwordHash, commands, ger.requests,
			        callback);
			
			if(callback.getException() != null) {
				throw callback.getException();
			}
			
			assert callback.getResult() != null;
			commandRes = callback.getResult().getFirst();
			eventsRes = callback.getResult().getSecond();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeCommandResults(commandRes, ger, eventsRes, out);
		
		out.flush();
		
	}
	
	private EventsRequest parseEventsRequest(String[] addresses, String[] from, String[] to) {
		
		if(addresses.length < from.length || addresses.length < to.length) {
			throw new RequestException("illegal parameter combination: addresses=" + addresses
			        + ", beginRevisions=" + from + ", endRevisions=" + to);
		}
		
		StoreException[] exceptions = new StoreException[addresses.length];
		GetEventsRequest[] requests = new GetEventsRequest[addresses.length];
		
		for(int i = 0; i < addresses.length; i++) {
			XAddress address;
			try {
				address = XX.toAddress(addresses[i]);
			} catch(Exception e) {
				exceptions[i] = new RequestException("invalid address: " + addresses[i]);
				continue;
			}
			long begin = 0;
			long end = Long.MAX_VALUE;
			if(i < from.length && !from[i].isEmpty()) {
				try {
					begin = Long.parseLong(from[i]);
				} catch(Exception e) {
					exceptions[i] = new RequestException("invalid beginRevision: " + from[i]);
					continue;
				}
			}
			if(i < to.length && !to[i].isEmpty()) {
				try {
					end = Long.parseLong(to[i]);
				} catch(Exception e) {
					exceptions[i] = new RequestException("invalid endRevision: " + to[i]);
					continue;
				}
			}
			requests[i] = new GetEventsRequest(address, begin, end);
		}
		
		return new EventsRequest(exceptions, requests);
	}
	
	public void getEvents(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses, String[] from, String[] to) throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		EventsRequest ger = parseEventsRequest(addresses, from, to);
		
		WaitingCallback<BatchedResult<XEvent[]>[]> callback = new WaitingCallback<BatchedResult<XEvent[]>[]>();
		store.getEvents(actorId, passwordHash, ger.requests, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeEventsResults(ger, callback.getResult(), out);
		
		out.flush();
		
	}
	
	public void getModelRevisions(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses) throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		StoreException[] ex = new StoreException[addresses.length];
		XAddress[] modelAddresses = new XAddress[addresses.length];
		for(int i = 0; i < addresses.length; i++) {
			try {
				modelAddresses[i] = XX.toAddress(addresses[i]);
			} catch(Exception e) {
				ex[i] = new RequestException("invalid address: " + addresses[i]);
				continue;
			}
		}
		
		WaitingCallback<BatchedResult<Long>[]> callback = new WaitingCallback<BatchedResult<Long>[]>();
		store.getModelRevisions(actorId, passwordHash, modelAddresses, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeModelRevisions(callback.getResult(), out);
		
		out.flush();
		
	}
	
	public void getSnapshots(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addressStrs) throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		List<XAddress> modelAddrs = new ArrayList<XAddress>();
		List<XAddress> objectAddrs = new ArrayList<XAddress>();
		
		StoreException[] ex = new StoreException[addressStrs.length];
		boolean[] isModel = new boolean[addressStrs.length];
		for(int i = 0; i < addressStrs.length; i++) {
			
			XAddress addr;
			try {
				addr = XX.toAddress(addressStrs[i]);
			} catch(Exception e) {
				ex[i] = new RequestException("invalid address: " + addressStrs[i]);
				continue;
			}
			
			XType type = addr.getAddressedType();
			if(type == XType.XMODEL) {
				modelAddrs.add(addr);
				isModel[i] = true;
			} else if(type == XType.XOBJECT) {
				objectAddrs.add(addr);
				isModel[i] = false;
			} else {
				ex[i] = new RequestException("address does not refer to a model or object: " + addr);
			}
			
		}
		
		XAddress[] ma = modelAddrs.toArray(new XAddress[modelAddrs.size()]);
		XAddress[] oa = objectAddrs.toArray(new XAddress[objectAddrs.size()]);
		
		WaitingCallback<BatchedResult<XReadableModel>[]> mc = new WaitingCallback<BatchedResult<XReadableModel>[]>();
		store.getModelSnapshots(actorId, passwordHash, ma, mc);
		if(mc.getException() != null) {
			throw mc.getException();
		}
		assert mc.getResult() != null && mc.getResult().length == ma.length;
		
		WaitingCallback<BatchedResult<XReadableObject>[]> oc = new WaitingCallback<BatchedResult<XReadableObject>[]>();
		store.getObjectSnapshots(actorId, passwordHash, oa, oc);
		if(oc.getException() != null) {
			throw oc.getException();
		}
		assert oc.getResult() != null && oc.getResult().length == oa.length;
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		BatchedResult<XReadableModel>[] mr = mc.getResult();
		BatchedResult<XReadableObject>[] or = oc.getResult();
		
		SerializedStore.serializeSnapshots(ex, isModel, mr, or, out);
		
		out.flush();
		
	}
	
	public void getModelIds(IRestlessContext context, String actorIdStr, String passwordHash)
	        throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<Set<XID>> callback = new WaitingCallback<Set<XID>>();
		store.getModelIds(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeModelIds(callback.getResult(), out);
		
		out.flush();
		
	}
	
	public void getRepositoryId(IRestlessContext context, String actorIdStr, String passwordHash)
	        throws Throwable {
		
		XydraStore store = XydraRestServer.getXydraStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<XID> callback = new WaitingCallback<XID>();
		store.getRepositoryId(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeRepositoryId(callback.getResult(), out);
		
		out.flush();
		
	}
	
	public String ping() {
		return "Hello World!";
	}
	
}
