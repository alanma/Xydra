package org.xydra.store.impl.rest;

import java.util.List;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.store.BatchedResult;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
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
public abstract class AbstractXydraStoreRestClient implements XydraStore {
	
	protected final XydraSerializer serializer;
	protected final XydraParser parser;
	
	public AbstractXydraStoreRestClient(XydraSerializer serializer, XydraParser parser) {
		this.serializer = serializer;
		this.parser = parser;
	}
	
	protected String encodeEventsRequests(GetEventsRequest[] getEventRequests,
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
			sb.append(urlencode(ger.address.toString()));
			
			sb.append("&beginRevision=");
			sb.append(ger.beginRevision);
			
			sb.append("&endRevision=");
			if(ger.endRevision != Long.MAX_VALUE) {
				sb.append(ger.beginRevision);
			}
			
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
	}
	
	protected <T> String encodeAddresses(XAddress[] addresses, BatchedResult<T>[] res, XType type) {
		
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
			sb.append(urlencode(address.toString()));
		}
		
		if(first) {
			return null;
		}
		
		return sb.toString();
	}
	
	abstract protected String urlencode(String string);
	
	protected <T> void toBatchedResults(List<Object> snapshots, BatchedResult<T>[] result,
	        boolean isModel) {
		
		int i = 0;
		for(Object o : snapshots) {
			
			while(result[i] != null) {
				i++;
			}
			
			assert i < result.length;
			
			if(o == null) {
				result[i] = new BatchedResult<T>((T)null);
			} else if(isModel ? o instanceof XReadableModel : o instanceof XReadableObject) {
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
	public XydraStoreAdmin getXydraStoreAdmin() {
		return null;
	}
	
}
