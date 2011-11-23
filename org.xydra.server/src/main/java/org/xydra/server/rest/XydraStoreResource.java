package org.xydra.server.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
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
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.store.BatchedResult;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.RequestException;
import org.xydra.store.ModelRevision;
import org.xydra.store.StoreException;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.rest.XydraStoreRestInterface;


/**
 * A Restless resource exposing a REST API that allows implementing a remote
 * {@link XydraStore}.
 * 
 * @author dscharrer
 * 
 */
public class XydraStoreResource {
	
	private static class InitException extends RuntimeException {
		
		private static final long serialVersionUID = -1357932793964520833L;
		
		public InitException(String message) {
			super(message);
		}
		
	}
	
	private final static XydraParser jsonParser = new JsonParser();
	private final static XydraParser xmlParser = new XmlParser();
	private final static Set<String> jsonMimes = new HashSet<String>();
	private final static Set<String> xmlMimes = new HashSet<String>();
	private final static Set<String> mimes = new HashSet<String>();
	
	private static final Pattern callbackRegex = Pattern.compile("^[a-z0-9_]+$");
	
	public static void restless(Restless restless, String apiLocation) {
		
		jsonMimes.add("application/json");
		jsonMimes.add("application/x-javascript");
		jsonMimes.add("text/javascript");
		jsonMimes.add("text/x-javascript");
		jsonMimes.add("text/x-json");
		jsonMimes.add("application/javascript");
		jsonMimes.add("text/ecmascript");
		jsonMimes.add("application/ecmascript");
		mimes.addAll(jsonMimes);
		
		xmlMimes.add("application/xml");
		xmlMimes.add("text/xml");
		mimes.addAll(xmlMimes);
		
		String prefix = apiLocation + "/";
		
		RestlessParameter actorId = new RestlessParameter(XydraStoreRestInterface.ARG_ACTOR_ID);
		RestlessParameter passwordHash = new RestlessParameter(
		        XydraStoreRestInterface.ARG_PASSWORD_HASH);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_LOGIN, "GET",
		        XydraStoreResource.class, "checkLogin", false, actorId, passwordHash);
		
		RestlessParameter addresses = new RestlessParameter(XydraStoreRestInterface.ARG_ADDRESS,
		        true);
		RestlessParameter from = new RestlessParameter(XydraStoreRestInterface.ARG_BEGIN_REVISION,
		        true);
		RestlessParameter to = new RestlessParameter(XydraStoreRestInterface.ARG_END_REVISION, true);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_EXECUTE, "POST",
		        XydraStoreResource.class, "executeCommands", false, actorId, passwordHash,
		        addresses, from, to);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_EVENTS, "GET",
		        XydraStoreResource.class, "getEvents", false, actorId, passwordHash, addresses,
		        from, to);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_REVISIONS, "GET",
		        XydraStoreResource.class, "getModelRevisions", false, actorId, passwordHash,
		        addresses);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_SNAPSHOTS, "GET",
		        XydraStoreResource.class, "getSnapshots", false, actorId, passwordHash, addresses);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_MODEL_IDS, "GET",
		        XydraStoreResource.class, "getModelIds", false, actorId, passwordHash);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_REPOSITORY_ID, "GET",
		        XydraStoreResource.class, "getRepositoryId", false, actorId, passwordHash);
		
		restless.addMethod(prefix + XydraStoreRestInterface.URL_PING, "GET",
		        XydraStoreResource.class, "ping", false);
		
	}
	
	public boolean onException(Throwable t, IRestlessContext context) {
		
		XydraRuntime.finishRequest();
		
		if(t instanceof InitException) {
			try {
				context.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		
		if(!(t instanceof StoreException) && !(t instanceof IllegalArgumentException)) {
			return false;
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		SerializedStore.serializeException(t, out);
		out.flush();
		
		return true;
	}
	
	private XID getActorId(String actorIdString) {
		
		try {
			return XX.toId(actorIdString);
		} catch(Throwable t) {
			throw new RequestException("invalid actor XID: " + actorIdString);
		}
		
	}
	
	private static String getBestContentType(IRestlessContext context, Set<String> choices,
	        String def) {
		
		HttpServletRequest req = context.getRequest();
		
		String mime = req.getParameter(XydraStoreRestInterface.ARG_ACCEPT);
		if(mime != null) {
			mime = mime.trim().toLowerCase();
			if(!choices.contains(mime)) {
				throw new InitException("Unexpected content type: " + mime);
			}
			return mime;
		}
		
		String best = def;
		float bestScore = Float.MIN_VALUE;
		
		@SuppressWarnings("unchecked")
		Enumeration<String> headers = req.getHeaders("Accept");
		
		while(headers.hasMoreElements()) {
			
			String accept = headers.nextElement().toLowerCase();
			
			String[] types = accept.split(",");
			for(String type : types) {
				
				String[] parts = type.split(";");
				if(!choices.contains(parts[0])) {
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
		
		String callback = context.getRequest().getParameter(XydraStoreRestInterface.ARG_CALLBACK);
		String mime;
		if(callback != null) {
			// Validate the callback to prevent cross-site scripting
			// vulnerabilities.
			if(!callbackRegex.matcher(callback).matches()) {
				throw new InitException("Invalid callback: " + callback);
			}
			mime = getBestContentType(context, jsonMimes,
			        XydraStoreRestInterface.DEFAULT_CALLBACK_CONTENT_TYPE);
		} else {
			mime = getBestContentType(context, mimes, XydraStoreRestInterface.DEFAULT_CONTENT_TYPE);
		}
		
		res.setStatus(statusCode);
		res.setContentType(mime + "; charset=UTF-8");
		res.setCharacterEncoding("utf-8");
		try {
			MiniWriter writer = new MiniStreamWriter(res.getOutputStream());
			XydraOut out;
			if(xmlMimes.contains(mime)) {
				out = new XmlOut(writer);
			} else if(jsonMimes.contains(mime)) {
				out = callback != null ? new JsonOut(writer, callback) : new JsonOut(writer);
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
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<Boolean> callback = new WaitingCallback<Boolean>();
		store.checkLogin(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeAuthenticationResult(callback.getResult(), out);
		
		out.flush();
		
		XydraRuntime.finishRequest();
		
	}
	
	public void executeCommands(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses, String[] from, String[] to) throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
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
			String mime = context.getRequest().getContentType();
			if(mime != null && jsonMimes.contains(mime.toLowerCase())) {
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
		
		XydraRuntime.finishRequest();
	}
	
	private EventsRequest parseEventsRequest(String[] addresses, String[] from, String[] to) {
		
		if(addresses.length < from.length || addresses.length < to.length) {
			throw new RequestException("illegal parameter combination: "
			        + XydraStoreRestInterface.ARG_ADDRESS + "es=" + Arrays.toString(addresses)
			        + ", " + XydraStoreRestInterface.ARG_BEGIN_REVISION + "s="
			        + Arrays.toString(from) + ", " + XydraStoreRestInterface.ARG_END_REVISION
			        + "s=" + Arrays.toString(to));
		}
		
		StoreException[] exceptions = new StoreException[addresses.length];
		GetEventsRequest[] requests = new GetEventsRequest[addresses.length];
		
		for(int i = 0; i < addresses.length; i++) {
			XAddress address;
			try {
				address = XX.toAddress(addresses[i]);
			} catch(Exception e) {
				exceptions[i] = new RequestException("invalid "
				        + XydraStoreRestInterface.ARG_ADDRESS + ": " + addresses[i]);
				continue;
			}
			long begin = 0;
			long end = Long.MAX_VALUE;
			if(i < from.length && !from[i].isEmpty()) {
				try {
					begin = Long.parseLong(from[i]);
				} catch(Exception e) {
					exceptions[i] = new RequestException("invalid "
					        + XydraStoreRestInterface.ARG_BEGIN_REVISION + ": " + from[i]);
					continue;
				}
			}
			if(i < to.length && !to[i].isEmpty()) {
				try {
					end = Long.parseLong(to[i]);
				} catch(Exception e) {
					exceptions[i] = new RequestException("invalid "
					        + XydraStoreRestInterface.ARG_END_REVISION + ": " + to[i]);
					continue;
				}
			}
			requests[i] = new GetEventsRequest(address, begin, end);
		}
		
		return new EventsRequest(exceptions, requests);
	}
	
	public void getEvents(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses, String[] from, String[] to) throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
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
		
		XydraRuntime.finishRequest();
		
	}
	
	public void getModelRevisions(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addresses) throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
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
		
		WaitingCallback<BatchedResult<ModelRevision>[]> callback = new WaitingCallback<BatchedResult<ModelRevision>[]>();
		store.getModelRevisions(actorId, passwordHash, modelAddresses, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeModelRevisions(callback.getResult(), out);
		
		out.flush();
		
		XydraRuntime.finishRequest();
		
	}
	
	public void getSnapshots(IRestlessContext context, String actorIdStr, String passwordHash,
	        String[] addressStrs) throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
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
		
		XydraRuntime.finishRequest();
		
	}
	
	public void getModelIds(IRestlessContext context, String actorIdStr, String passwordHash)
	        throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<Set<XID>> callback = new WaitingCallback<Set<XID>>();
		store.getModelIds(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeModelIds(callback.getResult(), out);
		
		out.flush();
		
		XydraRuntime.finishRequest();
		
	}
	
	public void getRepositoryId(IRestlessContext context, String actorIdStr, String passwordHash)
	        throws Throwable {
		
		XydraRuntime.startRequest();
		
		XydraStore store = XydraRestServer.getStore(context.getRestless());
		XID actorId = getActorId(actorIdStr);
		
		WaitingCallback<XID> callback = new WaitingCallback<XID>();
		store.getRepositoryId(actorId, passwordHash, callback);
		
		if(callback.getException() != null) {
			throw callback.getException();
		}
		
		XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		
		SerializedStore.serializeRepositoryId(callback.getResult(), out);
		
		out.flush();
		
		XydraRuntime.finishRequest();
		
	}
	
	public String ping() {
		return "Hello World!";
	}
	
}
