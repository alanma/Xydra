package org.xydra.core.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
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
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class SerializedStore {
	
	private static final String NAME_EVENTRESULTS = "eventResults";
	private static final String NAME_COMMANDRESULTS = "commandResults";
	private static final String NAME_EVENTS = "results";
	private static final String NAME_AUTHENTICATED = "authenticated";
	private static final String NAME_MESSAGE = "message";
	private static final String NAME_REVISION = "revision";
	private static final String NAME_REVISIONS = "revisions";
	private static final String NAME_SNAPSHOTS = "snapshots";
	public static final String ELEMENT_REPOSITORY_ID = "repositoryId";
	public static final String ELEMENT_MODEL_IDS = "modelIds";
	public static final String ELEMENT_AUTHENTICATED = "authenticated";
	public static final String ELEMENT_EVENTRESULTS = NAME_EVENTRESULTS;
	public static final String ELEMENT_COMMANDRESULTS = NAME_COMMANDRESULTS;
	public static final String ELEMENT_MODEL_REVISIONS = NAME_REVISIONS;
	public static final String ELEMENT_XREVISION = "xrevision";
	public static final String ELEMENT_RESULTS = "results";
	public static final String ELEMENT_SNAPSHOTS = NAME_SNAPSHOTS;
	public static final String ELEMENT_XERROR = "xerror";
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
	
	public static void serializeException(Throwable t, XydraOut out) {
		
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
			} else if(t instanceof RequestException || t instanceof IllegalArgumentException) {
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
	
	public static Throwable toException(XydraElement element) {
		
		if(element == null || !ELEMENT_XERROR.equals(element.getType())) {
			return null;
		}
		
		String type = element.getAttribute(ATTRIBUTE_TYPE).toString();
		Object messageObj = element.getContent(NAME_MESSAGE);
		String message = messageObj == null ? "" : messageObj.toString();
		
		if(TYPE_ACCESS.equals(type)) {
			return new AccessException(message);
		} else if(TYPE_AUTHORIZATION.equals(type)) {
			return new AuthorisationException(message);
		} else if(TYPE_TIMEOUT.equals(type)) {
			return new TimeoutException(message, true);
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
	
	public static void serializeAuthenticationResult(boolean result, XydraOut out) {
		out.element(ELEMENT_AUTHENTICATED, NAME_AUTHENTICATED, result);
	}
	
	public static boolean toAuthenticationResult(XydraElement element) {
		
		SerializingUtils.checkElementType(element, ELEMENT_AUTHENTICATED);
		
		return SerializingUtils.toBoolean(element.getContent(NAME_AUTHENTICATED));
	}
	
	public static class EventsRequest {
		
		public final StoreException[] exceptions;
		public final GetEventsRequest[] requests;
		
		public EventsRequest(StoreException[] exceptions, GetEventsRequest[] requests) {
			this.exceptions = exceptions;
			this.requests = requests;
		}
		
	}
	
	public static void serializeCommandResults(BatchedResult<Long>[] commandRes, EventsRequest ger,
	        BatchedResult<XEvent[]>[] eventsRes, XydraOut out) {
		
		out.open(ELEMENT_RESULTS);
		
		out.child(NAME_COMMANDRESULTS, ELEMENT_COMMANDRESULTS);
		out.beginArray();
		setRevisionListContents(commandRes, out);
		out.endArray();
		
		if(eventsRes != null) {
			out.child(NAME_EVENTRESULTS, ELEMENT_EVENTRESULTS);
			out.beginArray();
			serialize(ger, eventsRes, out);
			out.endArray();
		}
		
		out.close(ELEMENT_RESULTS);
		
	}
	
	public static void toCommandResults(XydraElement element, GetEventsRequest[] context,
	        BatchedResult<Long>[] commandResults, BatchedResult<XEvent[]>[] eventResults) {
		
		SerializingUtils.checkElementType(element, ELEMENT_RESULTS);
		
		XydraElement commandsEle = element.getChild(NAME_COMMANDRESULTS, ELEMENT_COMMANDRESULTS);
		if(commandsEle == null) {
			throw new IllegalArgumentException("missing command results");
		}
		
		getRevisionListContents(commandsEle, commandResults);
		
		XydraElement eventsEle = element.getChild(NAME_EVENTRESULTS, ELEMENT_EVENTRESULTS);
		if(eventResults != null && eventsEle != null) {
			toEventResultLists(eventsEle, context, eventResults);
		}
		
	}
	
	public static void serializeEventsResults(EventsRequest ger, BatchedResult<XEvent[]>[] results,
	        XydraOut out) {
		
		out.open(ELEMENT_EVENTRESULTS);
		
		out.child(NAME_EVENTS);
		out.beginArray();
		serialize(ger, results, out);
		out.endArray();
		
		out.close(ELEMENT_EVENTRESULTS);
		
	}
	
	private static void serialize(EventsRequest ger, BatchedResult<XEvent[]>[] results, XydraOut out) {
		
		assert results.length == ger.requests.length;
		for(int i = 0; i < results.length; i++) {
			BatchedResult<XEvent[]> result = results[i];
			
			if(ger.requests[i] == null) {
				serializeException(ger.exceptions[i], out);
				continue;
			}
			
			if(result.getException() != null) {
				serializeException(result.getException(), out);
				continue;
			}
			
			XEvent[] events = result.getResult();
			if(events == null) {
				out.nullElement();
			} else {
				SerializedEvent.serialize(Arrays.asList(events).iterator(), out,
				        ger.requests[i].address);
			}
			
		}
	}
	
	public static void toEventResults(XydraElement element, GetEventsRequest[] context,
	        BatchedResult<XEvent[]>[] results) {
		
		SerializingUtils.checkElementType(element, ELEMENT_EVENTRESULTS);
		
		toEventResultLists(element.getChild(NAME_EVENTS), context, results);
	}
	
	private static void toEventResultLists(XydraElement element, GetEventsRequest[] context,
	        BatchedResult<XEvent[]>[] results) {
		
		assert context.length == results.length;
		
		int i = 0;
		
		Iterator<XydraElement> it = element.getChildren();
		while(it.hasNext()) {
			XydraElement result = it.next();
			
			while(results[i] != null) {
				i++;
			}
			assert i < results.length;
			
			Throwable t = toException(result);
			if(t != null) {
				results[i] = new BatchedResult<XEvent[]>(t);
				continue;
			} else if(result == null) {
				results[i] = new BatchedResult<XEvent[]>((XEvent[])null);
				continue;
			}
			
			try {
				
				List<XEvent> events = SerializedEvent.toEventList(result, context[i].address);
				
				results[i] = new BatchedResult<XEvent[]>(events.toArray(new XEvent[events.size()]));
				
			} catch(Throwable th) {
				results[i] = new BatchedResult<XEvent[]>(th);
			}
			
		}
		
		for(; i < results.length; i++) {
			assert results[i] != null;
		}
	}
	
	public static void serializeModelRevisions(BatchedResult<Long>[] result, XydraOut out) {
		
		out.open(ELEMENT_MODEL_REVISIONS);
		out.child(NAME_REVISIONS);
		out.beginArray();
		setRevisionListContents(result, out);
		out.endArray();
		out.close(ELEMENT_MODEL_REVISIONS);
		
	}
	
	public static void toModelRevisions(XydraElement element, BatchedResult<Long>[] res) {
		
		SerializingUtils.checkElementType(element, ELEMENT_MODEL_REVISIONS);
		
		getRevisionListContents(element.getChild(NAME_REVISIONS), res);
	}
	
	private static void setRevisionListContents(BatchedResult<Long>[] results, XydraOut out) {
		
		for(BatchedResult<Long> result : results) {
			if(result.getException() != null) {
				serializeException(result.getException(), out);
			} else {
				assert result.getResult() != null;
				out.element(ELEMENT_XREVISION, NAME_REVISION, result.getResult());
			}
		}
	}
	
	private static void getRevisionListContents(XydraElement element, BatchedResult<Long>[] results) {
		
		int i = 0;
		
		Iterator<XydraElement> it = element.getChildren();
		while(it.hasNext()) {
			XydraElement result = it.next();
			
			while(results[i] != null) {
				i++;
			}
			assert i < results.length;
			
			Throwable t = toException(result);
			if(t != null) {
				results[i] = new BatchedResult<Long>(t);
				continue;
			}
			
			try {
				
				SerializingUtils.checkElementType(result, ELEMENT_XREVISION);
				
				long rev = SerializingUtils.toLong(result.getContent(NAME_REVISION));
				
				results[i] = new BatchedResult<Long>(rev);
				
			} catch(Throwable th) {
				results[i] = new BatchedResult<Long>(th);
			}
			
		}
		
		for(; i < results.length; i++) {
			assert results[i] != null;
		}
	}
	
	public static void serializeSnapshots(StoreException[] ex, boolean[] isModel,
	        BatchedResult<XReadableModel>[] mr, BatchedResult<XReadableObject>[] or, XydraOut out) {
		
		assert ex.length == isModel.length;
		
		out.open(ELEMENT_SNAPSHOTS);
		
		int mi = 0, oi = 0;
		
		out.child(NAME_SNAPSHOTS);
		out.beginArray();
		for(int i = 0; i < isModel.length; i++) {
			
			if(ex[i] != null) {
				serializeException(ex[i], out);
				continue;
			}
			
			Throwable t = isModel[i] ? mr[mi++].getException() : or[oi++].getException();
			if(t != null) {
				serializeException(t, out);
				continue;
			}
			
			if(isModel[i]) {
				XReadableModel model = mr[mi - 1].getResult();
				if(model == null) {
					out.nullElement();
				} else {
					SerializedModel.serialize(model, out);
				}
			} else {
				XReadableObject object = or[oi - 1].getResult();
				if(object == null) {
					out.nullElement();
				} else {
					SerializedModel.serialize(object, out);
				}
			}
			
		}
		out.endArray();
		
		out.close(ELEMENT_SNAPSHOTS);
		
	}
	
	public static List<Object> toSnapshots(XydraElement element, XAddress[] context) {
		
		SerializingUtils.checkElementType(element, ELEMENT_SNAPSHOTS);
		
		List<Object> results = new ArrayList<Object>();
		
		int i = 0 - 1;
		
		Iterator<XydraElement> it = element.getChildrenByName(NAME_SNAPSHOTS);
		while(it.hasNext()) {
			XydraElement result = it.next();
			i++;
			
			try {
				
				Throwable t = toException(result);
				if(t != null) {
					results.add(t);
					continue;
				}
				
				if(result == null) {
					results.add(null);
				} else if(SerializedModel.isModel(result)) {
					results.add(SerializedModel.toModelState(result, context[i]));
				} else {
					results.add(SerializedModel.toObjectState(result, context[i]));
				}
				
			} catch(Throwable th) {
				results.add(null);
			}
			
		}
		
		return results;
	}
	
	public static void serializeModelIds(Set<XID> result, XydraOut out) {
		
		out.open(ELEMENT_MODEL_IDS);
		SerializedValue.setIdListContents(result, out);
		out.close(ELEMENT_MODEL_IDS);
		
	}
	
	public static Set<XID> toModelIds(XydraElement element) {
		
		SerializingUtils.checkElementType(element, ELEMENT_MODEL_IDS);
		
		return new HashSet<XID>(SerializedValue.getIdListContents(element));
	}
	
	public static void serializeRepositoryId(XID result, XydraOut out) {
		out.element(ELEMENT_REPOSITORY_ID, NAME_XID, result);
	}
	
	public static XID toRepositoryId(XydraElement element) {
		
		SerializingUtils.checkElementType(element, ELEMENT_REPOSITORY_ID);
		
		return SerializingUtils.toId(element.getContent(NAME_XID));
	}
	
}
