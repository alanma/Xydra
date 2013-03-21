package org.xydra.store.impl.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XEntity;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedStore;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.index.query.Pair;
import org.xydra.sharedutils.URLUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.GetWithAddressRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.ModelRevision;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


/**
 * Abstract base class for {@link XydraStore} implementations that connect to a
 * xydra store REST server.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public abstract class AbstractXydraStoreRestClient implements XydraStore {
	
	protected static final String HAEDER_CONTENT_TYPE = "Content-Type";
	protected static final String HEADER_COOKIE = "Cookie";
	protected static final String HEADER_ACCEPT = "Accept";
	
	protected final XydraSerializer serializer;
	protected final XydraParser parser;
	
	public AbstractXydraStoreRestClient(XydraSerializer serializer, XydraParser parser) {
		this.serializer = serializer;
		this.parser = parser;
	}
	
	public abstract class Request<T> {
		
		final public XId actor;
		final public String password;
		final private Callback<T> callback;
		
		protected Request(XId actor, String password, Callback<T> callback) {
			
			this.actor = actor;
			this.password = password;
			this.callback = callback;
			
			if(actor == null) {
				throw new IllegalArgumentException("actorId must not be null");
			}
			if(password == null) {
				throw new IllegalArgumentException("passwordHash must not be null");
			}
		}
		
		public void onFailure(Throwable t) {
			if(this.callback != null) {
				this.callback.onFailure(new ConnectionException(t.getMessage(), t));
			}
		}
		
		protected void onSuccess(T result) {
			if(this.callback != null) {
				this.callback.onSuccess(result);
			}
		}
		
		public void onResponse(String content, int code, String message) {
			
			if(this.callback == null) {
				return;
			}
			
			if(content == null || content.isEmpty()) {
				this.callback.onFailure(new ConnectionException("no content, response is " + code
				        + " " + message));
				return;
			}
			// verify status code to avoid failing on parsing HTML error pages
			if(code >= 400 && code < 500) {
				this.callback.onFailure(new ConnectionException("client-side error " + code + " "
				        + message));
				return;
			}
			if(code >= 500 && code < 600) {
				this.callback.onFailure(new ConnectionException("server-side error " + code + " "
				        + message));
				return;
			}
			
			XydraElement element;
			try {
				element = AbstractXydraStoreRestClient.this.parser.parse(content);
			} catch(Throwable th) {
				this.callback.onFailure(new InternalStoreException("error parsing response", th));
				return;
			}
			
			Throwable t = SerializedStore.toException(element);
			if(t != null) {
				this.callback.onFailure(t);
				return;
			}
			
			T result;
			try {
				result = parse(element);
			} catch(Throwable th) {
				this.callback.onFailure(new InternalStoreException("error parsing response", th));
				return;
			}
			
			this.callback.onSuccess(result);
		}
		
		protected abstract T parse(XydraElement element);
		
		protected void get(String uri) {
			
			if(this.callback == null) {
				throw new IllegalArgumentException("callback may not be null");
			}
			
			AbstractXydraStoreRestClient.this.get(uri, this);
		}
		
		protected void post(String uri, XydraOut data) {
			AbstractXydraStoreRestClient.this.post(uri, data, this);
		}
		
	}
	
	protected abstract void get(String uri, Request<?> req);
	
	protected abstract void post(String uri, XydraOut data, Request<?> req);
	
	private String encodeEventsRequests(GetEventsRequest[] getEventsRequests,
	        BatchedResult<XEvent[]>[] res) {
		
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests array must not be null");
		}
		
		XyAssert.xyAssert(getEventsRequests.length == res.length);
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(int i = 0; i < getEventsRequests.length; i++) {
			GetEventsRequest ger = getEventsRequests[i];
			
			if(ger == null) {
				res[i] = new BatchedResult<XEvent[]>(new RequestException(
				        "GetEventsRequest must not be null"));
				continue;
			} else if(ger.address == null) {
				res[i] = new BatchedResult<XEvent[]>(new RequestException(
				        "address must not be null"));
				continue;
			} else if(ger.address.getModel() == null) {
				res[i] = new BatchedResult<XEvent[]>(new RequestException(
				        "invalid get events adddress: " + ger.address));
				continue;
			} else if(ger.endRevision < ger.beginRevision) {
				res[i] = new BatchedResult<XEvent[]>(new RequestException(
				        "invalid GetEventsRequest range: [" + ger.beginRevision + ","
				                + ger.endRevision + "]"));
				continue;
			}
			
			if(first) {
				first = false;
			} else {
				sb.append('&');
			}
			
			sb.append(XydraStoreRestInterface.ARG_ADDRESS);
			sb.append('=');
			sb.append(urlencode(ger.address.toString()));
			
			sb.append('&');
			sb.append(XydraStoreRestInterface.ARG_BEGIN_REVISION);
			sb.append('=');
			sb.append(ger.beginRevision);
			
			sb.append('&');
			sb.append(XydraStoreRestInterface.ARG_END_REVISION);
			if(ger.endRevision != Long.MAX_VALUE) {
				sb.append('=');
				sb.append(ger.beginRevision);
			}
			
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
	}
	
	private <T> String encodeAddresses(GetWithAddressRequest[] getModelRevisionRequests,
	        BatchedResult<T>[] res, XType type) {
		
		XyAssert.xyAssert(res.length == getModelRevisionRequests.length);
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(int i = 0; i < getModelRevisionRequests.length; i++) {
			GetWithAddressRequest getModelRevisionRequest = getModelRevisionRequests[i];
			XAddress address = getModelRevisionRequest.address;
			
			if(address == null) {
				res[i] = new BatchedResult<T>(new RequestException("address must not be null"));
				continue;
			} else if(address.getAddressedType() != type) {
				res[i] = new BatchedResult<T>(new RequestException("address " + address
				        + " is not of type " + type));
				continue;
			}
			
			if(first) {
				first = false;
			} else {
				sb.append('&');
			}
			
			sb.append(XydraStoreRestInterface.ARG_ADDRESS);
			sb.append('=');
			sb.append(urlencode(address.toString()));
			if(getModelRevisionRequest.includeTentative) {
				// FIXME ! make constant + add in parser + add in docu
				sb.append("+tentative");
			}
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
	}
	
	protected String urlencode(String string) {
		return URLUtils.encode(string);
	}
	
	private static <T> void toBatchedResults(List<Object> snapshots, BatchedResult<T>[] result,
	        XType type) {
		
		int i = 0;
		for(Object o : snapshots) {
			
			while(result[i] != null) {
				i++;
			}
			
			XyAssert.xyAssert(i < result.length);
			
			if(o == null) {
				result[i] = new BatchedResult<T>((T)null);
			} else if(o instanceof XEntity && ((XEntity)o).getType() == type) {
				@SuppressWarnings("unchecked")
				T t = (T)o;
				result[i] = new BatchedResult<T>(t);
			} else if(o instanceof Throwable) {
				result[i] = new BatchedResult<T>((Throwable)o);
			} else {
				result[i] = new BatchedResult<T>(new InternalStoreException("Unexpected class: "
				        + o.getClass()));
			}
			
		}
		
		for(; i < result.length; i++) {
			XyAssert.xyAssert(result[i] != null);
			assert result[i] != null;
		}
		
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return null;
	}
	
	protected String encodeLoginCookie(XId actorId, String passwordHash) {
		return XydraStoreRestInterface.ARG_ACTOR_ID + "=" + urlencode(actorId.toString()) + "; "
		        + XydraStoreRestInterface.ARG_PASSWORD_HASH + "="
		        + urlencode(passwordHash.toString());
	}
	
	protected String encodeLoginQuery(XId actorId, String passwordHash) {
		return XydraStoreRestInterface.ARG_ACTOR_ID + "=" + urlencode(actorId.toString()) + "&"
		        + XydraStoreRestInterface.ARG_PASSWORD_HASH + "="
		        + urlencode(passwordHash.toString());
	}
	
	private class LoginRequest extends Request<Boolean> {
		
		protected LoginRequest(XId actor, String password, Callback<Boolean> callback) {
			super(actor, password, callback);
		}
		
		protected void run() {
			get(XydraStoreRestInterface.URL_LOGIN);
		}
		
		@Override
		protected Boolean parse(XydraElement element) {
			return SerializedStore.toAuthenticationResult(element);
		}
		
	}
	
	@Override
	public void checkLogin(XId actor, String password, Callback<Boolean> callback) {
		new LoginRequest(actor, password, callback).run();
	}
	
	protected XydraOut prepareExecuteRequest(XCommand[] commands) {
		
		if(commands == null) {
			throw new IllegalArgumentException("commands array must not be null");
		}
		
		XydraOut out = this.serializer.create();
		SerializedCommand.serialize(Arrays.asList(commands).iterator(), out, null);
		
		return out;
	}
	
	private class ExecuteRequest extends Request<BatchedResult<Long>[]> {
		
		private final XCommand[] commands;
		
		protected ExecuteRequest(XId actor, String password, XCommand[] commands,
		        Callback<BatchedResult<Long>[]> callback) {
			super(actor, password, callback);
			this.commands = commands;
		}
		
		protected void run() {
			XydraOut out = prepareExecuteRequest(this.commands);
			post(XydraStoreRestInterface.URL_EXECUTE, out);
		}
		
		@Override
		protected BatchedResult<Long>[] parse(XydraElement element) {
			
			@SuppressWarnings("unchecked")
			BatchedResult<Long>[] res = new BatchedResult[this.commands.length];
			
			SerializedStore.toCommandResults(element, null, res, null);
			
			return res;
		}
		
	}
	
	@Override
	public void executeCommands(XId actor, String password, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) {
		new ExecuteRequest(actor, password, commands, callback).run();
	}
	
	@SuppressWarnings("unchecked")
	private static BatchedResult<XEvent[]>[] prepareEventsResultsArray(
	        GetEventsRequest[] getEventsRequests) {
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests array must not be null");
		}
		return new BatchedResult[getEventsRequests.length];
	}
	
	private class EventsRequest extends Request<BatchedResult<XEvent[]>[]> {
		
		private final GetEventsRequest[] getEventsRequests;
		private BatchedResult<XEvent[]>[] res;
		
		protected EventsRequest(XId actor, String password, GetEventsRequest[] getEventsRequests,
		        Callback<BatchedResult<XEvent[]>[]> callback) {
			super(actor, password, callback);
			this.getEventsRequests = getEventsRequests;
			this.res = prepareEventsResultsArray(getEventsRequests);
		}
		
		protected void run() {
			
			String req = encodeEventsRequests(this.getEventsRequests, this.res);
			if(req == null) {
				onSuccess(this.res);
				return;
			}
			
			get(XydraStoreRestInterface.URL_EVENTS + "?" + req);
		}
		
		@Override
		protected BatchedResult<XEvent[]>[] parse(XydraElement element) {
			
			SerializedStore.toEventResults(element, this.getEventsRequests, this.res);
			
			return this.res;
		}
		
	}
	
	@Override
	public void getEvents(XId actor, String password, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) {
		new EventsRequest(actor, password, getEventsRequests, callback).run();
	}
	
	private class ExecuteAndEventsRequest extends
	        Request<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> {
		
		private final XCommand[] commands;
		private final GetEventsRequest[] getEventsRequests;
		private final BatchedResult<XEvent[]>[] eventsRes;
		
		protected ExecuteAndEventsRequest(XId actor, String password, XCommand[] commands,
		        GetEventsRequest[] getEventsRequests,
		        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
			super(actor, password, callback);
			this.commands = commands;
			this.getEventsRequests = getEventsRequests;
			this.eventsRes = prepareEventsResultsArray(getEventsRequests);
		}
		
		protected void run() {
			
			XydraOut out = prepareExecuteRequest(this.commands);
			
			String req = encodeEventsRequests(this.getEventsRequests, this.eventsRes);
			
			String uri = XydraStoreRestInterface.URL_EXECUTE;
			if(req != null) {
				uri += "?" + req;
			}
			
			post(uri, out);
		}
		
		@Override
		protected Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> parse(XydraElement element) {
			
			@SuppressWarnings("unchecked")
			BatchedResult<Long>[] commandsRes = new BatchedResult[this.commands.length];
			
			SerializedStore.toCommandResults(element, this.getEventsRequests, commandsRes,
			        this.eventsRes);
			
			return new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(commandsRes,
			        this.eventsRes);
		}
		
	}
	
	@Override
	public void executeCommandsAndGetEvents(XId actor, String password, XCommand[] commands,
	        GetEventsRequest[] getEventsRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
		new ExecuteAndEventsRequest(actor, password, commands, getEventsRequests, callback).run();
	}
	
	private class ModelIdsRequest extends Request<Set<XId>> {
		
		protected ModelIdsRequest(XId actor, String password, Callback<Set<XId>> callback) {
			super(actor, password, callback);
		}
		
		protected void run() {
			get(XydraStoreRestInterface.URL_MODEL_IDS);
		}
		
		@Override
		protected Set<XId> parse(XydraElement element) {
			return SerializedStore.toModelIds(element);
		}
		
	}
	
	@Override
	public void getModelIds(XId actor, String password, Callback<Set<XId>> callback) {
		new ModelIdsRequest(actor, password, callback).run();
	}
	
	private class RevisionsRequest extends Request<BatchedResult<ModelRevision>[]> {
		
		private final GetWithAddressRequest[] modelAddresses;
		private final BatchedResult<ModelRevision>[] res;
		
		@SuppressWarnings("unchecked")
		protected RevisionsRequest(XId actor, String password,
		        GetWithAddressRequest[] modelAddresses,
		        Callback<BatchedResult<ModelRevision>[]> callback) {
			super(actor, password, callback);
			this.modelAddresses = modelAddresses;
			if(this.modelAddresses == null) {
				throw new IllegalArgumentException("modelAddresses array must not be null");
			}
			this.res = new BatchedResult[this.modelAddresses.length];
		}
		
		protected void run() {
			
			String req = encodeAddresses(this.modelAddresses, this.res, XType.XMODEL);
			if(req == null) {
				onSuccess(this.res);
				return;
			}
			
			get(XydraStoreRestInterface.URL_REVISIONS + "?" + req);
		}
		
		@Override
		protected BatchedResult<ModelRevision>[] parse(XydraElement element) {
			
			SerializedStore.toModelRevisions(element, this.res);
			
			return this.res;
		}
		
	}
	
	@Override
	public void getModelRevisions(XId actor, String password,
	        GetWithAddressRequest[] modelRevisionRequests,
	        Callback<BatchedResult<ModelRevision>[]> callback) {
		new RevisionsRequest(actor, password, modelRevisionRequests, callback).run();
	}
	
	private class SnapshotsRequest<T> extends Request<BatchedResult<T>[]> {
		
		private final GetWithAddressRequest[] addressRequests;
		private final BatchedResult<T>[] res;
		private final XType type;
		
		@SuppressWarnings("unchecked")
		protected SnapshotsRequest(XId actor, String password,
		        GetWithAddressRequest[] modelAddressRequests,
		        Callback<BatchedResult<T>[]> callback, XType type) {
			super(actor, password, callback);
			this.addressRequests = modelAddressRequests;
			if(this.addressRequests == null) {
				throw new IllegalArgumentException("addresses array must not be null");
			}
			this.res = new BatchedResult[this.addressRequests.length];
			this.type = type;
		}
		
		protected void run() {
			
			String req = encodeAddresses(this.addressRequests, this.res, this.type);
			if(req == null) {
				onSuccess(this.res);
				return;
			}
			
			get(XydraStoreRestInterface.URL_SNAPSHOTS + "?" + req);
		}
		
		@Override
		protected BatchedResult<T>[] parse(XydraElement element) {
			
			XAddress[] addresses = new XAddress[this.addressRequests.length];
			for(int i = 0; i < addresses.length; i++) {
				addresses[i] = this.addressRequests[i].address;
			}
			List<Object> snapshots = SerializedStore.toSnapshots(element, addresses);
			
			toBatchedResults(snapshots, this.res, this.type);
			
			return this.res;
		}
		
	}
	
	@Override
	public void getModelSnapshots(XId actor, String password,
	        GetWithAddressRequest[] modelAddressRequests,
	        Callback<BatchedResult<XReadableModel>[]> callback) {
		new SnapshotsRequest<XReadableModel>(actor, password, modelAddressRequests, callback,
		        XType.XMODEL).run();
	}
	
	@Override
	public void getObjectSnapshots(XId actor, String password,
	        GetWithAddressRequest[] objectAddressRequests,
	        Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {
		new SnapshotsRequest<XReadableObject>(actor, password, objectAddressRequests, callback,
		        XType.XOBJECT).run();
	}
	
	private class RepositoryIdRequest extends Request<XId> {
		
		protected RepositoryIdRequest(XId actor, String password, Callback<XId> callback) {
			super(actor, password, callback);
		}
		
		protected void run() {
			get(XydraStoreRestInterface.URL_REPOSITORY_ID);
		}
		
		@Override
		protected XId parse(XydraElement element) {
			return SerializedStore.toRepositoryId(element);
		}
		
	}
	
	@Override
	public void getRepositoryId(XId actor, String password, Callback<XId> callback) {
		new RepositoryIdRequest(actor, password, callback).run();
	}
	
}
