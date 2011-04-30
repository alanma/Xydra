package org.xydra.store.impl.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.XmlCommand;
import org.xydra.core.xml.XmlStore;
import org.xydra.core.xml.XmlStore.Snapshots;
import org.xydra.core.xml.impl.MiniXMLParserImpl;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
import org.xydra.index.query.Pair;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
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
		
		// TODO escaping
		con.setRequestProperty("Cookie", "actorId=" + actorId + "; passwordHash=" + passwordHash);
		
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
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(Arrays.asList(commands).iterator(), out, null);
		
		MiniElement xml = post("execute", actorId, passwordHash, out.getXml(), callback);
		if(xml == null) {
			return;
		}
		
		Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> res = XmlStore.toCommandResults(xml,
		        null);
		
		assert res.getSecond() == null;
		
		if(callback != null) {
			callback.onSuccess(res.getFirst());
		}
		
	}
	
	@Override
	public void executeCommandsAndGetEvents(XID actorId, String passwordHash, XCommand[] commands,
	        GetEventsRequest[] getEventRequests,
	        Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback)
	        throws IllegalArgumentException {
		String uri = "execute?" + encodeEventsRequests(getEventRequests);
		
		XmlOutStringBuffer out = new XmlOutStringBuffer();
		XmlCommand.toXml(Arrays.asList(commands).iterator(), out, null);
		
		MiniElement xml = post(uri, actorId, passwordHash, out.getXml(), callback);
		if(xml == null) {
			return;
		}
		
		Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> res = XmlStore.toCommandResults(xml,
		        getEventRequests);
		
		callback.onSuccess(res);
	}
	
	private String encodeEventsRequests(GetEventsRequest[] getEventRequests) {
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(GetEventsRequest ger : getEventRequests) {
			
			if(first) {
				first = false;
			} else {
				sb.append('&');
			}
			
			// TODO escaping
			
			sb.append("address=");
			sb.append(ger.address);
			
			sb.append("beginRevision=");
			sb.append(ger.beginRevision);
			
			sb.append("endRevision=");
			if(ger.endRevision != Long.MAX_VALUE) {
				sb.append(ger.beginRevision);
			}
			
		}
		
		return sb.toString();
	}
	
	@Override
	public void getEvents(XID actorId, String passwordHash, GetEventsRequest[] getEventsRequest,
	        Callback<BatchedResult<XEvent[]>[]> callback) throws IllegalArgumentException {
		
		String uri = "events?" + encodeEventsRequests(getEventsRequest);
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		BatchedResult<XEvent[]>[] res = XmlStore.toEventResults(xml, getEventsRequest);
		
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
	
	private String encodeAddresses(XAddress[] addresses) {
		
		if(addresses == null) {
			throw new IllegalArgumentException("address array must not be null");
		}
		
		// TODO check address type
		
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		
		for(XAddress address : addresses) {
			
			if(first) {
				first = false;
			} else {
				sb.append('&');
			}
			
			// TODO escaping
			
			sb.append("address=");
			sb.append(address);
			
		}
		
		return sb.toString();
	}
	
	@Override
	public void getModelRevisions(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<Long>[]> callback) throws IllegalArgumentException {
		
		String uri = "revisions?" + encodeAddresses(modelAddresses);
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		BatchedResult<Long>[] revisions = XmlStore.toModelRevisions(xml);
		
		callback.onSuccess(revisions);
		
	}
	
	public <T, V> BatchedResult<T>[] merge(Throwable[] exceptions, T[] results, V[] third) {
		
		assert exceptions.length == results.length;
		assert third.length == results.length;
		
		@SuppressWarnings("unchecked")
		BatchedResult<T>[] merged = new BatchedResult[results.length];
		
		for(int i = 0; i < results.length; i++) {
			
			assert exceptions[i] == null || results[i] == null;
			assert results[i] == null || third[i] == null;
			
			if(exceptions[i] != null) {
				merged[i] = new BatchedResult<T>(exceptions[i]);
			} else {
				merged[i] = new BatchedResult<T>(results[i]);
			}
			
		}
		
		return merged;
	}
	
	@Override
	public void getModelSnapshots(XID actorId, String passwordHash, XAddress[] modelAddresses,
	        Callback<BatchedResult<XReadableModel>[]> callback) throws IllegalArgumentException {
		
		String uri = "snapshots?" + encodeAddresses(modelAddresses);
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		Snapshots snapshots = XmlStore.toSnapshots(xml);
		
		BatchedResult<XReadableModel>[] result = merge(snapshots.exceptions, snapshots.models,
		        snapshots.objects);
		
		assert result.length == modelAddresses.length;
		
		callback.onSuccess(result);
		
	}
	
	@Override
	public void getObjectSnapshots(XID actorId, String passwordHash, XAddress[] objectAddresses,
	        Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {
		
		String uri = "snapshots?" + encodeAddresses(objectAddresses);
		
		MiniElement xml = get(uri, actorId, passwordHash, callback);
		if(xml == null) {
			return;
		}
		
		Snapshots snapshots = XmlStore.toSnapshots(xml);
		
		BatchedResult<XReadableObject>[] result = merge(snapshots.exceptions, snapshots.objects,
		        snapshots.models);
		
		callback.onSuccess(result);
		
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
