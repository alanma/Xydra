package org.xydra.store.impl.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.serialize.SerializedCommand;
import org.xydra.core.serialize.SerializedStore;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.index.query.Pair;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


/**
 * {@link XydraStore} implementation that connects to a xydra store REST server
 * using synchronous network operations.
 * 
 * @author dscharrer
 * 
 */
@RunsInGWT(false)
public class XydraStoreRestClient extends AbstractXydraStoreRestClient {
	
	private final URI prefix;
	
	public XydraStoreRestClient(URI apiLocation, XydraSerializer serializer, XydraParser parser) {
		super(serializer, parser);
		this.prefix = apiLocation;
	}
	
	private final void setLoginData(HttpURLConnection con, XID actorId, String passwordHash) {
		
		if(actorId == null) {
			throw new IllegalArgumentException("actorId must not be null");
		}
		if(passwordHash == null) {
			throw new IllegalArgumentException("passwordHash must not be null");
		}
		
		try {
			con.setRequestProperty("Cookie", "actorId="
			        + URLEncoder.encode(actorId.toString(), "UTF-8") + "; passwordHash="
			        + URLEncoder.encode(passwordHash.toString(), "UTF-8"));
		} catch(UnsupportedEncodingException e) {
			assert false;
		}
		
	}
	
	@Override
	public void checkLogin(XID actorId, String passwordHash, Callback<Boolean> callback)
	        throws IllegalArgumentException {
		
		XydraElement element = get("login", actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		boolean auth = SerializedStore.toAuthenticationResult(element);
		
		callback.onSuccess(auth);
	}
	
	protected String readAll(InputStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[4096];
		Reader reader = new InputStreamReader(stream, "UTF-8");
		int nRead;
		while((nRead = reader.read(buf)) != -1)
			sb.append(buf, 0, nRead);
		return sb.toString();
	}
	
	private String getResponse(HttpURLConnection con) throws IOException {
		
		InputStream es = con.getErrorStream();
		if(es != null) {
			return readAll(es);
		}
		
		InputStream is = (InputStream)con.getContent();
		if(is == null) {
			return null;
		}
		
		return readAll(is);
	}
	
	private XydraElement post(String uri, XID actorId, String passwordHash, XydraOut data,
	        Callback<?> callback) {
		
		URL url;
		try {
			url = this.prefix.resolve(uri).toURL();
		} catch(MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		
		HttpURLConnection con;
		
		try {
			
			con = (HttpURLConnection)url.openConnection();
			
			setLoginData(con, actorId, passwordHash);
			
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", data.getContentType());
			con.setRequestProperty("Accept", this.parser.getContentType());
			Writer w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			w.write(data.getData());
			w.flush();
			
			con.connect();
			
			String content = getResponse(con);
			if(content == null) {
				if(callback != null) {
					callback.onFailure(new ConnectException("no content, response is "
					        + con.getResponseCode() + " " + con.getResponseMessage()));
				}
				return null;
			}
			
			XydraElement element = this.parser.parse(content);
			
			Throwable t = SerializedStore.toException(element);
			if(t != null) {
				if(callback != null) {
					callback.onFailure(t);
				}
				return null;
			}
			
			return element;
			
		} catch(IOException e) {
			if(callback != null) {
				callback.onFailure(new ConnectException(e.getMessage()));
			}
			return null;
		}
	}
	
	@Override
	public void executeCommands(XID actorId, String passwordHash, XCommand[] commands,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		
		if(commands == null) {
			throw new IllegalArgumentException("commands array must not be null");
		}
		
		XydraOut out = this.serializer.create();
		SerializedCommand.serialize(Arrays.asList(commands).iterator(), out, null);
		
		XydraElement element = post("execute", actorId, passwordHash, out, callback);
		if(element == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] res = new BatchedResult[commands.length];
		
		SerializedStore.toCommandResults(element, null, res, null);
		
		if(callback != null) {
			callback.onSuccess(res);
		}
		
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventsRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException {
		
		if(commands == null) {
			throw new IllegalArgumentException("commands array must not be null");
		}
		
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests array must not be null");
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<XEvent[]>[] eventsRes = new BatchedResult[getEventsRequests.length];
		String req = encodeEventsRequests(getEventsRequests, eventsRes);
		
		String uri = req == null ? "execute" : "execute?" + req;
		
		XydraOut out = this.serializer.create();
		SerializedCommand.serialize(Arrays.asList(commands).iterator(), out, null);
		
		XydraElement element = post(uri, actorId, passwordHash, out, callback);
		if(element == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] commandsRes = new BatchedResult[commands.length];
		
		SerializedStore.toCommandResults(element, getEventsRequests, commandsRes, eventsRes);
		
		callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(commandsRes,
		        eventsRes));
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequests,
	        Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException {
		
		if(getEventsRequests == null) {
			throw new IllegalArgumentException("getEventsRequests array must not be null");
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<XEvent[]>[] res = new BatchedResult[getEventsRequests.length];
		String req = encodeEventsRequests(getEventsRequests, res);
		if(req == null) {
			callback.onSuccess(res);
			return;
		}
		
		String uri = "events?" + req;
		
		XydraElement element = get(uri, actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		SerializedStore.toEventResults(element, getEventsRequests, res);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException {
		
		XydraElement element = get("repository/models", actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		Set<XID> modelIds = SerializedStore.toModelIds(element);
		
		callback.onSuccess(modelIds);
		
	}
	
	private XydraElement get(String uri, XID actorId, String passwordHash, Callback<?> callback) {
		
		if(callback == null) {
			throw new IllegalArgumentException("callback may not be null");
		}
		
		URL url;
		try {
			url = this.prefix.resolve(uri).toURL();
		} catch(MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		
		try {
			
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			
			setLoginData(con, actorId, passwordHash);
			con.setRequestProperty("Accept", this.parser.getContentType());
			
			con.connect();
			
			String content = getResponse(con);
			if(content == null) {
				callback.onFailure(new ConnectException("no content, response is "
				        + con.getResponseCode() + " " + con.getResponseMessage()));
				return null;
			}
			
			XydraElement element = this.parser.parse(content);
			
			Throwable t = SerializedStore.toException(element);
			if(t != null) {
				callback.onFailure(t);
				return null;
			}
			
			return element;
			
		} catch(IOException e) {
			callback.onFailure(new ConnectException(e.getMessage()));
			return null;
		}
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		
		if(modelAddresses == null) {
			throw new IllegalArgumentException("modelAddresses array must not be null");
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] res = new BatchedResult[modelAddresses.length];
		String req = encodeAddresses(modelAddresses, res, XType.XMODEL);
		if(req == null) {
			callback.onSuccess(res);
			return;
		}
		
		String uri = "revisions?" + req;
		
		XydraElement element = get(uri, actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		SerializedStore.toModelRevisions(element, res);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XReadableModel>[]> callback) throws IllegalArgumentException {
		
		if(modelAddresses == null) {
			throw new IllegalArgumentException("modelAddresses array must not be null");
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<XReadableModel>[] res = new BatchedResult[modelAddresses.length];
		String req = encodeAddresses(modelAddresses, res, XType.XMODEL);
		if(req == null) {
			callback.onSuccess(res);
			return;
		}
		
		String uri = "snapshots?" + req;
		
		XydraElement element = get(uri, actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		List<Object> snapshots = SerializedStore.toSnapshots(element, modelAddresses);
		
		toBatchedResults(snapshots, res, true);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {
		
		if(objectAddresses == null) {
			throw new IllegalArgumentException("objectAddresses array must not be null");
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<XReadableObject>[] res = new BatchedResult[objectAddresses.length];
		String req = encodeAddresses(objectAddresses, res, XType.XOBJECT);
		if(req == null) {
			callback.onSuccess(res);
			return;
		}
		
		String uri = "snapshots?" + req;
		
		XydraElement element = get(uri, actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		List<Object> snapshots = SerializedStore.toSnapshots(element, objectAddresses);
		
		toBatchedResults(snapshots, res, false);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException {
		
		XydraElement element = get("repository/id", actorId, passwordHash, callback);
		if(element == null) {
			return;
		}
		
		XID repoId = SerializedStore.toRepositoryId(element);
		
		callback.onSuccess(repoId);
		
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return null;
	}
	
	@Override
	protected String urlencode(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
