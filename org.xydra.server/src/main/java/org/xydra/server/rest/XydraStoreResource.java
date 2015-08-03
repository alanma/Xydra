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

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.minio.MiniStreamWriter;
import org.xydra.base.minio.MiniWriter;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.StoreException;
import org.xydra.core.XX;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.json.JsonOut;
import org.xydra.core.serialize.json.JsonParser;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessParameter;
import org.xydra.restless.utils.Delay;
import org.xydra.store.BatchedResult;
import org.xydra.store.RequestException;
import org.xydra.store.WaitingCallback;
import org.xydra.store.XydraRuntime;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.rest.XydraStoreRestInterface;
import org.xydra.store.serialize.SerializedStore;
import org.xydra.store.serialize.SerializedStore.EventsRequest;

/**
 * A Restless resource exposing a REST API that allows implementing a remote
 * {@link XydraStore}.
 *
 * @author dscharrer
 *
 */
public class XydraStoreResource {

	private static final Logger log = LoggerFactory.getLogger(XydraStoreResource.class);

	private static class InitException extends RuntimeException {

		private static final long serialVersionUID = -1357932793964520833L;

		public InitException(final String message) {
			super(message);
		}

	}

	private final static XydraParser jsonParser = new JsonParser();
	private final static XydraParser xmlParser = new XmlParser();
	private final static Set<String> jsonMimes = new HashSet<String>();
	private final static Set<String> xmlMimes = new HashSet<String>();
	private final static Set<String> mimes = new HashSet<String>();

	private static final Pattern callbackRegex = Pattern.compile("^[a-z0-9_]+$");

	public static void restless(final Restless restless, final String apiLocation) {
		log.info("Init at apiLocation=" + apiLocation);

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

		final String prefix = apiLocation + "/";

		final RestlessParameter actorId = new RestlessParameter(XydraStoreRestInterface.ARG_ACTOR_ID);
		final RestlessParameter passwordHash = new RestlessParameter(
				XydraStoreRestInterface.ARG_PASSWORD_HASH);

		restless.addMethod(prefix + XydraStoreRestInterface.URL_LOGIN, "GET",
				XydraStoreResource.class, "checkLogin", false, actorId, passwordHash);

		final RestlessParameter addresses = new RestlessParameter(XydraStoreRestInterface.ARG_ADDRESS,
				true);
		final RestlessParameter from = new RestlessParameter(XydraStoreRestInterface.ARG_BEGIN_REVISION,
				true);
		final RestlessParameter to = new RestlessParameter(XydraStoreRestInterface.ARG_END_REVISION, true);

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
		log.info("Exposing ping service at " + prefix + XydraStoreRestInterface.URL_PING);

	}

	public boolean onException(final Throwable t, final IRestlessContext context) {

		XydraRuntime.finishRequest();

		if (t instanceof InitException) {
			try {
				context.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, t.getMessage());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		if (!(t instanceof StoreException) && !(t instanceof IllegalArgumentException)) {
			return false;
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);
		SerializedStore.serializeException(t, out);
		out.flush();

		return true;
	}

	private static XId getActorId(final String actorIdString) {

		try {
			return Base.toId(actorIdString);
		} catch (final Throwable t) {
			throw new RequestException("invalid actor XId: " + actorIdString);
		}

	}

	private static String getBestContentType(final IRestlessContext context, final Set<String> choices,
			final String def) {

		final HttpServletRequest req = context.getRequest();

		String mime = req.getParameter(XydraStoreRestInterface.ARG_ACCEPT);
		if (mime != null) {
			mime = mime.trim().toLowerCase();
			if (!choices.contains(mime)) {
				throw new InitException("Unexpected content type: " + mime);
			}
			return mime;
		}

		String best = def;
		float bestScore = Float.MIN_VALUE;

		final Enumeration<String> headers = req.getHeaders("Accept");

		while (headers.hasMoreElements()) {

			final String accept = headers.nextElement().toLowerCase();

			final String[] types = accept.split(",");
			for (final String type : types) {

				final String[] parts = type.split(";");
				if (!choices.contains(parts[0])) {
					continue;
				}
				float score = 1.f;
				if (parts.length > 1) {
					final String[] param = parts[1].split("=");
					if (param.length > 1 && param[0].trim() == "q") {
						score = Float.parseFloat(param[1].trim());
					}
				}
				if (score > bestScore) {
					best = parts[0];
					bestScore = score;
				}
			}

		}

		return best;
	}

	private static XydraOut startOutput(final IRestlessContext context, final int statusCode) {

		final HttpServletResponse res = context.getResponse();

		final String callback = context.getRequest().getParameter(XydraStoreRestInterface.ARG_CALLBACK);
		String mime;
		if (callback != null) {
			// Validate the callback to prevent cross-site scripting
			// vulnerabilities.
			if (!callbackRegex.matcher(callback).matches()) {
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
			final MiniWriter writer = new MiniStreamWriter(res.getOutputStream());
			XydraOut out;
			if (xmlMimes.contains(mime)) {
				out = new XmlOut(writer);
			} else if (jsonMimes.contains(mime)) {
				out = callback != null ? new JsonOut(writer, callback) : new JsonOut(writer);
			} else {
				throw new AssertionError();
			}
			final String format = context.getRequest().getParameter("format");
			if ("pretty".equals(format)) {
				out.enableWhitespace(true, true);
			}
			return out;
		} catch (final IOException e) {
			throw new RuntimeException("re-throw", e);
		}
	}

	/**
	 * Let caller determine login infos without executing anything else
	 *
	 * @param context
	 * @param actorIdStr
	 * @param passwordHash
	 * @throws Throwable
	 */
	public void checkLogin(final IRestlessContext context, final String actorIdStr, final String passwordHash)
			throws Throwable {

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final WaitingCallback<Boolean> callback = new WaitingCallback<Boolean>();
		store.checkLogin(actorId, passwordHash, callback);

		if (callback.getException() != null) {
			throw callback.getException();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeAuthenticationResult(callback.getResult(), out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public void executeCommands(final IRestlessContext context, final String actorIdStr, final String passwordHash,
			final String[] addresses, final String[] from, final String[] to) throws Throwable {

		if (Delay.isSimulateDelay()) {
			Delay.ajax();
		}

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final EventsRequest ger = parseEventsRequest(addresses, from, to);

		final WaitingCallback<XId> repoId = new WaitingCallback<XId>();
		// implicitly check security
		store.getRepositoryId(actorId, passwordHash, repoId);

		if (repoId.getException() != null) {
			throw repoId.getException();
		}
		final XAddress repoAddr = Base.toAddress(repoId.getResult(), null, null, null);

		final String rawCommands = XydraRestServer.readPostData(context.getRequest());
		List<XCommand> commandsList;
		try {
			XydraElement element;
			final String mime = context.getRequest().getContentType();
			if (mime != null && jsonMimes.contains(mime.toLowerCase())) {
				try {
					element = jsonParser.parse(rawCommands);
				} catch (final Exception e) {
					element = xmlParser.parse(rawCommands);
				}
			} else {
				try {
					element = xmlParser.parse(rawCommands);
				} catch (final Exception e) {
					element = jsonParser.parse(rawCommands);
				}
			}
			commandsList = SerializedCommand.toCommandList(element, repoAddr);
		} catch (final Exception e) {
			throw new RequestException("error parsing commands list: " + e.getMessage());
		}
		final XCommand[] commands = commandsList.toArray(new XCommand[commandsList.size()]);

		BatchedResult<Long>[] commandRes;
		BatchedResult<XEvent[]>[] eventsRes;

		if (ger.requests.length == 0) {

			final WaitingCallback<BatchedResult<Long>[]> callback = new WaitingCallback<BatchedResult<Long>[]>();
			store.executeCommands(actorId, passwordHash, commands, callback);

			if (callback.getException() != null) {
				throw callback.getException();
			}

			commandRes = callback.getResult();
			eventsRes = null;

		} else {

			final WaitingCallback<Pair<BatchedResult<Long>[], BatchedResult<XEvent[]>[]>> callback = new WaitingCallback<Pair<BatchedResult<Long>[], BatchedResult<XEvent[]>[]>>();
			store.executeCommandsAndGetEvents(actorId, passwordHash, commands, ger.requests,
					callback);

			if (callback.getException() != null) {
				throw callback.getException();
			}

			assert callback.getResult() != null;
			commandRes = callback.getResult().getFirst();
			eventsRes = callback.getResult().getSecond();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeCommandResults(commandRes, ger, eventsRes, out);

		out.flush();

		XydraRuntime.finishRequest();
	}

	private static EventsRequest parseEventsRequest(final String[] addresses, final String[] from, final String[] to) {

		if (addresses.length < from.length || addresses.length < to.length) {
			throw new RequestException("illegal parameter combination: "
					+ XydraStoreRestInterface.ARG_ADDRESS + "es=" + Arrays.toString(addresses)
					+ ", " + XydraStoreRestInterface.ARG_BEGIN_REVISION + "s="
					+ Arrays.toString(from) + ", " + XydraStoreRestInterface.ARG_END_REVISION
					+ "s=" + Arrays.toString(to));
		}

		final StoreException[] exceptions = new StoreException[addresses.length];
		final GetEventsRequest[] requests = new GetEventsRequest[addresses.length];

		for (int i = 0; i < addresses.length; i++) {
			XAddress address;
			try {
				address = Base.toAddress(addresses[i]);
			} catch (final Exception e) {
				exceptions[i] = new RequestException("invalid "
						+ XydraStoreRestInterface.ARG_ADDRESS + ": " + addresses[i]);
				continue;
			}
			long begin = 0;
			long end = Long.MAX_VALUE;
			if (i < from.length && !from[i].isEmpty()) {
				try {
					begin = Long.parseLong(from[i]);
				} catch (final Exception e) {
					exceptions[i] = new RequestException("invalid "
							+ XydraStoreRestInterface.ARG_BEGIN_REVISION + ": " + from[i]);
					continue;
				}
			}
			if (i < to.length && !to[i].isEmpty()) {
				try {
					end = Long.parseLong(to[i]);
				} catch (final Exception e) {
					exceptions[i] = new RequestException("invalid "
							+ XydraStoreRestInterface.ARG_END_REVISION + ": " + to[i]);
					continue;
				}
			}
			requests[i] = new GetEventsRequest(address, begin, end);
		}

		return new EventsRequest(exceptions, requests);
	}

	public void getEvents(final IRestlessContext context, final String actorIdStr, final String passwordHash,
			final String[] addresses, final String[] from, final String[] to) throws Throwable {

		if (Delay.isSimulateDelay()) {
			Delay.ajax();
		}

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final EventsRequest ger = parseEventsRequest(addresses, from, to);

		final WaitingCallback<BatchedResult<XEvent[]>[]> callback = new WaitingCallback<BatchedResult<XEvent[]>[]>();
		store.getEvents(actorId, passwordHash, ger.requests, callback);

		if (callback.getException() != null) {
			throw callback.getException();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeEventsResults(ger, callback.getResult(), out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public void getModelRevisions(final IRestlessContext context, final String actorIdStr, final String passwordHash,
			final String[] addresses) throws Throwable {

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final StoreException[] ex = new StoreException[addresses.length];
		final GetWithAddressRequest[] modelAddressRequests = new GetWithAddressRequest[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			try {
				modelAddressRequests[i] = new GetWithAddressRequest(Base.toAddress(addresses[i]));
			} catch (final Exception e) {
				ex[i] = new RequestException("invalid address: " + addresses[i]);
				continue;
			}
		}

		final WaitingCallback<BatchedResult<ModelRevision>[]> callback = new WaitingCallback<BatchedResult<ModelRevision>[]>();
		store.getModelRevisions(actorId, passwordHash, modelAddressRequests, callback);

		if (callback.getException() != null) {
			throw callback.getException();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeModelRevisions(callback.getResult(), out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public void getSnapshots(final IRestlessContext context, final String actorIdStr, final String passwordHash,
			final String[] addressStrs) throws Throwable {

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final List<XAddress> modelAddrs = new ArrayList<XAddress>();
		final List<XAddress> objectAddrs = new ArrayList<XAddress>();

		final StoreException[] ex = new StoreException[addressStrs.length];
		final boolean[] isModel = new boolean[addressStrs.length];
		for (int i = 0; i < addressStrs.length; i++) {

			XAddress addr;
			try {
				addr = Base.toAddress(addressStrs[i]);
			} catch (final Exception e) {
				ex[i] = new RequestException("invalid address: " + addressStrs[i]);
				continue;
			}

			final XType type = addr.getAddressedType();
			if (type == XType.XMODEL) {
				modelAddrs.add(addr);
				isModel[i] = true;
			} else if (type == XType.XOBJECT) {
				objectAddrs.add(addr);
				isModel[i] = false;
			} else {
				ex[i] = new RequestException("address does not refer to a model or object: " + addr);
			}

		}

		final GetWithAddressRequest[] mreq = new GetWithAddressRequest[modelAddrs.size()];
		for (int i = 0; i < mreq.length; i++) {
			mreq[i] = new GetWithAddressRequest(modelAddrs.get(i));
		}

		final GetWithAddressRequest[] oreq = new GetWithAddressRequest[objectAddrs.size()];
		for (int i = 0; i < oreq.length; i++) {
			oreq[i] = new GetWithAddressRequest(objectAddrs.get(i));
		}

		final WaitingCallback<BatchedResult<XReadableModel>[]> mc = new WaitingCallback<BatchedResult<XReadableModel>[]>();
		store.getModelSnapshots(actorId, passwordHash, mreq, mc);
		if (mc.getException() != null) {
			throw mc.getException();
		}
		assert mc.getResult() != null && mc.getResult().length == mreq.length;

		final WaitingCallback<BatchedResult<XReadableObject>[]> oc = new WaitingCallback<BatchedResult<XReadableObject>[]>();
		store.getObjectSnapshots(actorId, passwordHash, oreq, oc);
		if (oc.getException() != null) {
			throw oc.getException();
		}
		assert oc.getResult() != null && oc.getResult().length == oreq.length;

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		final BatchedResult<XReadableModel>[] mres = mc.getResult();
		final BatchedResult<XReadableObject>[] ores = oc.getResult();

		SerializedStore.serializeSnapshots(ex, isModel, mres, ores, out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public void getModelIds(final IRestlessContext context, final String actorIdStr, final String passwordHash)
			throws Throwable {

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final WaitingCallback<Set<XId>> callback = new WaitingCallback<Set<XId>>();
		store.getModelIds(actorId, passwordHash, callback);

		if (callback.getException() != null) {
			throw callback.getException();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeModelIds(callback.getResult(), out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public void getRepositoryId(final IRestlessContext context, final String actorIdStr, final String passwordHash)
			throws Throwable {

		XydraRuntime.startRequest();

		final XydraStore store = XydraRestServer.getStore(context.getRestless());
		final XId actorId = getActorId(actorIdStr);

		final WaitingCallback<XId> callback = new WaitingCallback<XId>();
		store.getRepositoryId(actorId, passwordHash, callback);

		if (callback.getException() != null) {
			throw callback.getException();
		}

		final XydraOut out = startOutput(context, HttpServletResponse.SC_OK);

		SerializedStore.serializeRepositoryId(callback.getResult(), out);

		out.flush();

		XydraRuntime.finishRequest();

	}

	public String ping() {
		return "Hello World!";
	}

}
