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
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlStore;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.query.Pair;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;


@RunsInGWT(false)
public class XydraStoreRestClient implements XydraStore {
	
	private final URI prefix;
	
	public XydraStoreRestClient(URI apiLocation) {
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
		
		MiniElement xml = get("login", actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		boolean auth = XmlStore.toAuthenticationResult(xml);
		
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
	
	private MiniElement post(String uri, XID actorId, String passwordHash, String data,
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
			con.setRequestProperty("Content-Type", "application/xml");
			Writer w = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			w.write(data);
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
			
			MiniElement xml = new MiniXMLParserImpl().parseXml(content);
			
			Throwable t = XmlStore.toException(xml);
			if(t != null) {
				if(callback != null) {
					callback.onFailure(t);
				}
				return null;
			}
			
			return xml;
			
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
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(Arrays.asList(commands).iterator(), out, null);
		
		MiniElement xml = post("execute", actorId, passwordHash, out.getXml(), callback);
		if(xml == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] res = new BatchedResult[commands.length];
		
		XmlStore.toCommandResults(xml, null, res, null);
		
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
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(Arrays.asList(commands).iterator(), out, null);
		
		MiniElement xml = post(uri, actorId, passwordHash, out.getXml(), callback);
		if(xml == null) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] commandsRes = new BatchedResult[commands.length];
		
		XmlStore.toCommandResults(xml, getEventsRequests, commandsRes, eventsRes);
		
		callback.onSuccess(new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(commandsRes,
		        eventsRes));
	}
	
	private String encodeEventsRequests(GetEventsRequest[] getEventRequests,
	        BatchedResult<XEvent[]>[] res) {
		
		assert getEventRequests.length == res.length;
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(int i = 0; i < getEventRequests.length; i++) {
			GetEventsRequest ger = getEventRequests[i];
			
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
			
			sb.append("address=");
			try {
				sb.append(URLEncoder.encode(ger.address.toString(), "UTF-8"));
			} catch(UnsupportedEncodingException e) {
				assert false;
			}
			
			sb.append("beginRevision=");
			sb.append(ger.beginRevision);
			
			sb.append("endRevision=");
			if(ger.endRevision != Long.MAX_VALUE) {
				sb.append(ger.beginRevision);
			}
			
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
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
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		XmlStore.toEventResults(xml, getEventsRequests, res);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getModelIds(XID actorId, String passwordHash, Callback<Set<XID>> callback)
	        throws IllegalArgumentException {
		
		MiniElement xml = get("repository/models", actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		Set<XID> modelIds = XmlStore.toModelIds(xml);
		
		callback.onSuccess(modelIds);
		
	}
	
	private MiniElement get(String uri, XID actorId, String passwordHash, Callback<?> callback) {
		
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
			
			con.connect();
			
			String content = getResponse(con);
			if(content == null) {
				callback.onFailure(new ConnectException("no content, response is "
				        + con.getResponseCode() + " " + con.getResponseMessage()));
				return null;
			}
			
			MiniElement xml = new MiniXMLParserImpl().parseXml(content);
			
			Throwable t = XmlStore.toException(xml);
			if(t != null) {
				callback.onFailure(t);
				return null;
			}
			
			return xml;
			
		} catch(IOException e) {
			callback.onFailure(new ConnectException(e.getMessage()));
			return null;
		}
	}
	
	private <T> String encodeAddresses(XAddress[] addresses, BatchedResult<T>[] res, XType type) {
		
		assert res.length == addresses.length;
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(int i = 0; i < addresses.length; i++) {
			XAddress address = addresses[i];
			
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
			
			sb.append("address=");
			try {
				sb.append(URLEncoder.encode(address.toString(), "UTF-8"));
			} catch(UnsupportedEncodingException e) {
				assert false;
			}
			
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
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
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		XmlStore.toModelRevisions(xml, res);
		
		callback.onSuccess(res);
		
	}
	
	public <T> void toBatchedResults(List<Object> snapshots, Class<T> cls, BatchedResult<T>[] result) {
		
		int i = 0;
		for(Object o : snapshots) {
			
			while(result[i] != null) {
				i++;
			}
			
			assert i < result.length;
			
			if(o == null) {
				result[i] = new BatchedResult<T>((T)null);
			} else if(cls.isAssignableFrom(o.getClass())) {
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
			assert result[i] != null;
		}
		
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
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		List<Object> snapshots = XmlStore.toSnapshots(xml, modelAddresses);
		
		toBatchedResults(snapshots, XReadableModel.class, res);
		
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
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		List<Object> snapshots = XmlStore.toSnapshots(xml, objectAddresses);
		
		toBatchedResults(snapshots, XReadableObject.class, res);
		
		callback.onSuccess(res);
		
	}
	
	@Override
	public void getRepositoryId(XID actorId, String passwordHash, Callback<XID> callback)
	        throws IllegalArgumentException {
		
		MiniElement xml = get("repository/id", actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		XID repoId = XmlStore.toRepositoryId(xml);
		
		callback.onSuccess(repoId);
		
	}
	
	@Override
	public XydraStoreAdmin getXydraStoreAdmin() {
		return null;
	}
	
}
