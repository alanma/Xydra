package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.store.AccessException;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.StoreException;
import org.xydra.store.TimeoutException;
import org.xydra.store.XydraStore;


/**
 * Serialization functions to en- and decode request and result objects of the
 * {@link XydraStore} API.
 * 
 * @author dscharrer
 * 
 */
public class XmlStore {
	
	private static final String NAME_EVENTS = "events";
	private static final String NAME_AUTHENTICATED = "authenticated";
	private static final String NAME_MESSAGE = "message";
	private static final String NAME_REVISION = "revision";
	private static final String NAME_REVISIONS = "revisions";
	private static final String NAME_SNAPSHOTS = "snapshots";
	private static final String ELEMENT_REPOSITORY_ID = "repositoryId";
	private static final String ELEMENT_MODEL_IDS = "modelIds";
	private static final String ELEMENT_AUTHENTICATED = "authenticated";
	private static final String ELEMENT_EVENTRESULTS = "eventResults";
	private static final String ELEMENT_COMMANDRESULTS = "commandResults";
	private static final String ELEMENT_MODEL_REVISIONS = NAME_REVISIONS;
	private static final String ELEMENT_XREVISION = "xrevision";
	private static final String ELEMENT_RESULTS = "results";
	private static final String ELEMENT_SNAPSHOTS = NAME_SNAPSHOTS;
	private static final String ELEMENT_XERROR = "xerror";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String TYPE_ACCESS = "access";
	private static final String TYPE_AUTHORIZATION = "authorization";
	private static final String TYPE_TIMEOUT = "timeout";
	private static final String TYPE_CONNECTION = "connection";
	private static final String TYPE_INTERNAL = "internal";
	private static final String TYPE_QUOTA = "quota";
	private static final String TYPE_REQUEST = "request";
	private static final String TYPE_STORE = "store";
	private static final String NAME_XID = "xid";
	
	public static void toXml(Throwable t, XydraOut out) {
		
		out.open(ELEMENT_XERROR);
		
		if(t instanceof StoreException) {
			if(t instanceof AccessException) {
				out.attribute(ATTRIBUTE_TYPE, TYPE_ACCESS);
			} else if(t instanceof AuthorisationException) {
				out.attribute(ATTRIBUTE_TYPE, TYPE_AUTHORIZATION);
			} else if(t instanceof ConnectionException) {
				if(t instanceof TimeoutException) {
					out.attribute(ATTRIBUTE_TYPE, TYPE_TIMEOUT);
				} else {
					out.attribute(ATTRIBUTE_TYPE, TYPE_CONNECTION);
				}
			} else if(t instanceof InternalStoreException) {
				out.attribute(ATTRIBUTE_TYPE, TYPE_INTERNAL);
			} else if(t instanceof QuotaException) {
				out.attribute(ATTRIBUTE_TYPE, TYPE_QUOTA);
			} else if(t instanceof RequestException) {
				out.attribute(ATTRIBUTE_TYPE, TYPE_REQUEST);
			} else {
				out.attribute(ATTRIBUTE_TYPE, TYPE_STORE);
			}
		}
		
		String m = t.getMessage();
		if(m != null && !m.isEmpty()) {
			out.content(NAME_MESSAGE, t.getMessage());
		}
		
		out.close(ELEMENT_XERROR);
		
	}
	
	public static Throwable toException(MiniElement xml) {
		
		if(!ELEMENT_XERROR.equals(xml.getType())) {
			return null;
		}
		
		String type = xml.getAttribute(ATTRIBUTE_TYPE).toString();
		Object messageObj = xml.getContent(NAME_MESSAGE);
		String message = messageObj == null ? "" : messageObj.toString();
		
		if(TYPE_ACCESS.equals(type)) {
			return new AccessException(message);
		} else if(TYPE_AUTHORIZATION.equals(type)) {
			return new AuthorisationException(message);
		} else if(TYPE_TIMEOUT.equals(type)) {
			return new TimeoutException(message);
		} else if(TYPE_CONNECTION.equals(type)) {
			return new ConnectionException(message, true);
		} else if(TYPE_INTERNAL.equals(type)) {
			return new InternalStoreException(message);
		} else if(TYPE_QUOTA.equals(type)) {
			return new QuotaException(message);
		} else if(TYPE_REQUEST.equals(type)) {
			return new RequestException(message);
		} else if(TYPE_STORE.equals(type)) {
			return new StoreException(message);
		} else {
			return new RuntimeException(message);
		}
		
	}
	
	public static void toAuthenticationResult(boolean result, XydraOut out) {
		
		out.open(ELEMENT_AUTHENTICATED);
		out.content(NAME_AUTHENTICATED, result);
		out.close(ELEMENT_AUTHENTICATED);
		
	}
	
	public static boolean toAuthenticationResult(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_AUTHENTICATED);
		
		return XmlValue.toBoolean(xml.getContent(NAME_AUTHENTICATED));
	}
	
	public static class EventsRequest {
		
		public final StoreException[] exceptions;
		public final GetEventsRequest[] requests;
		
		public EventsRequest(StoreException[] exceptions, GetEventsRequest[] requests) {
			this.exceptions = exceptions;
			this.requests = requests;
		}
		
	}
	
	public static void toXml(BatchedResult<Long>[] commandRes, EventsRequest ger,
	        BatchedResult<XEvent[]>[] eventsRes, XydraOut out) {
		
		out.open(ELEMENT_RESULTS);
		
		out.open(ELEMENT_COMMANDRESULTS);
		setRevisionListContents(commandRes, out);
		out.close(ELEMENT_COMMANDRESULTS);
		
		if(eventsRes != null) {
			toXml(ger, eventsRes, out);
		}
		
		out.close(ELEMENT_RESULTS);
		
	}
	
	public static void toCommandResults(MiniElement xml, GetEventsRequest[] context,
	        BatchedResult<Long>[] commandResults, BatchedResult<XEvent[]>[] eventResults) {
		
		XmlUtils.checkElementName(xml, ELEMENT_RESULTS);
		
		MiniElement commandsEle = xml.getChild(ELEMENT_COMMANDRESULTS);
		if(commandsEle == null) {
			throw new IllegalArgumentException("missing command results");
		}
		
		XmlUtils.checkElementName(commandsEle, ELEMENT_COMMANDRESULTS);
		
		getRevisionListContents(commandsEle, commandResults);
		
		MiniElement eventsEle = xml.getChild(ELEMENT_EVENTRESULTS);
		if(eventResults != null && eventsEle != null) {
			toEventResults(eventsEle, context, eventResults);
		}
		
	}
	
	public static void toXml(EventsRequest ger, BatchedResult<XEvent[]>[] results, XydraOut out) {
		
		out.open(ELEMENT_EVENTRESULTS);
		
		out.children(NAME_EVENTS, true);
		assert results.length == ger.requests.length;
		for(int i = 0; i < results.length; i++) {
			BatchedResult<XEvent[]> result = results[i];
			
			if(ger.requests[i] == null) {
				toXml(ger.exceptions[i], out);
				continue;
			}
			
			if(result.getException() != null) {
				toXml(result.getException(), out);
				continue;
			}
			
			XEvent[] events = result.getResult();
			if(events == null) {
				XmlValue.saveNullElement(out);
			} else {
				XmlEvent.toXml(Arrays.asList(events).iterator(), out, ger.requests[i].address);
			}
			
		}
		
		out.close(ELEMENT_EVENTRESULTS);
		
	}
	
	public static void toEventResults(MiniElement xml, GetEventsRequest[] context,
	        BatchedResult<XEvent[]>[] results) {
		
		XmlUtils.checkElementName(xml, ELEMENT_EVENTRESULTS);
		
		assert context.length == results.length;
		
		int i = 0;
		
		Iterator<MiniElement> it = xml.getChildren(NAME_EVENTS);
		while(it.hasNext()) {
			MiniElement ele = it.next();
			
			while(results[i] != null) {
				i++;
			}
			assert i < results.length;
			
			Throwable t = toException(ele);
			if(t != null) {
				results[i] = new BatchedResult<XEvent[]>(t);
				continue;
			}
			
			if(XmlValue.isNullElement(ele)) {
				results[i] = new BatchedResult<XEvent[]>((XEvent[])null);
				continue;
			}
			
			try {
				
				List<XEvent> events = XmlEvent.toEventList(ele, context[i].address);
				
				results[i] = new BatchedResult<XEvent[]>(events.toArray(new XEvent[events.size()]));
				
			} catch(Throwable th) {
				results[i] = new BatchedResult<XEvent[]>(th);
			}
			
		}
		
		for(; i < results.length; i++) {
			assert results[i] != null;
		}
	}
	
	public static void toModelRevisions(BatchedResult<Long>[] result, XydraOut out) {
		
		out.open(ELEMENT_MODEL_REVISIONS);
		setRevisionListContents(result, out);
		out.close(ELEMENT_MODEL_REVISIONS);
		
	}
	
	public static void toModelRevisions(MiniElement xml, BatchedResult<Long>[] res) {
		
		XmlUtils.checkElementName(xml, ELEMENT_MODEL_REVISIONS);
		
		getRevisionListContents(xml, res);
	}
	
	private static void setRevisionListContents(BatchedResult<Long>[] results, XydraOut out) {
		
		out.children(NAME_REVISIONS, true);
		for(BatchedResult<Long> result : results) {
			
			if(result.getException() != null) {
				toXml(result.getException(), out);
				continue;
			}
			
			out.open(ELEMENT_XREVISION);
			assert result.getResult() != null;
			out.content(NAME_REVISION, result.getResult());
			out.close(ELEMENT_XREVISION);
			
		}
		
	}
	
	private static void getRevisionListContents(MiniElement xml, BatchedResult<Long>[] results) {
		
		int i = 0;
		
		Iterator<MiniElement> it = xml.getChildren(NAME_REVISIONS);
		while(it.hasNext()) {
			MiniElement ele = it.next();
			
			while(results[i] != null) {
				i++;
			}
			assert i < results.length;
			
			Throwable t = toException(ele);
			if(t != null) {
				results[i] = new BatchedResult<Long>(t);
				continue;
			}
			
			try {
				
				XmlUtils.checkElementName(ele, ELEMENT_XREVISION);
				
				long rev = XmlValue.toLong(ele.getContent(NAME_REVISION));
				
				results[i] = new BatchedResult<Long>(rev);
				
			} catch(Throwable th) {
				results[i] = new BatchedResult<Long>(th);
			}
			
		}
		
		for(; i < results.length; i++) {
			assert results[i] != null;
		}
	}
	
	public static void toXml(StoreException[] ex, boolean[] isModel,
	        BatchedResult<XReadableModel>[] mr, BatchedResult<XReadableObject>[] or, XydraOut out) {
		
		out.open(ELEMENT_SNAPSHOTS);
		
		int mi = 0, oi = 0;
		
		out.children(NAME_SNAPSHOTS, true);
		for(int i = 0; i < isModel.length; i++) {
			
			if(ex[i] != null) {
				toXml(ex[i], out);
				continue;
			}
			
			Throwable t = isModel[i] ? mr[mi++].getException() : or[oi++].getException();
			if(t != null) {
				toXml(t, out);
				continue;
			}
			
			if(isModel[i]) {
				XReadableModel model = mr[mi - 1].getResult();
				if(model == null) {
					XmlValue.saveNullElement(out);
				} else {
					XmlModel.toXml(model, out);
				}
			} else {
				XReadableObject object = or[oi - 1].getResult();
				if(object == null) {
					XmlValue.saveNullElement(out);
				} else {
					XmlModel.toXml(object, out);
				}
			}
			
		}
		
		out.close(ELEMENT_SNAPSHOTS);
		
	}
	
	public static List<Object> toSnapshots(MiniElement xml, XAddress[] context) {
		
		XmlUtils.checkElementName(xml, ELEMENT_SNAPSHOTS);
		
		List<Object> results = new ArrayList<Object>();
		
		int i = 0 - 1;
		
		Iterator<MiniElement> it = xml.getChildren(NAME_SNAPSHOTS);
		while(it.hasNext()) {
			MiniElement ele = it.next();
			i++;
			
			Throwable t = toException(xml);
			if(t != null) {
				results.add(t);
				continue;
			}
			
			try {
				
				if(XmlValue.isNullElement(ele)) {
					results.add(null);
				} else if(XmlModel.isModel(ele)) {
					results.add(XmlModel.toModelState(ele, context[i]));
				} else {
					results.add(XmlModel.toObjectState(ele, context[i]));
				}
				
			} catch(Throwable th) {
				results.add(null);
			}
			
		}
		
		return results;
	}
	
	public static void toModelIds(Set<XID> result, XydraOut out) {
		
		out.open(ELEMENT_MODEL_IDS);
		XmlValue.setIdListContents(result, out);
		out.close(ELEMENT_MODEL_IDS);
		
	}
	
	public static Set<XID> toModelIds(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_MODEL_IDS);
		
		List<XID> ids = XmlValue.getIdListContents(xml);
		
		return new HashSet<XID>(ids);
	}
	
	public static void toRepositoryId(XID result, XydraOut out) {
		
		out.open(ELEMENT_REPOSITORY_ID);
		out.content(NAME_XID, result);
		out.close(ELEMENT_REPOSITORY_ID);
		
	}
	
	public static XID toRepositoryId(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_REPOSITORY_ID);
		
		Object id = xml.getContent(NAME_XID);
		
		return XX.toId(id.toString());
		
	}
	
}
