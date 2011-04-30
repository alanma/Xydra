package org.xydra.core.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.index.query.Pair;
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
	
	private static final String ELEMENT_REPOSITORY_ID = "repositoryId";
	private static final String ELEMENT_MODEL_IDS = "modelIds";
	private static final String ELEMENT_AUTHENTICATED = "authenticated";
	private static final String ELEMENT_EVENTRESULTS = "eventResults";
	private static final String ELEMENT_COMMANDRESULTS = "commandResults";
	private static final String ELEMENT_MODEL_REVISIONS = "revisions";
	private static final String ELEMENT_XREVISION = "xrevision";
	private static final String ELEMENT_RESULTS = "results";
	private static final String ELEMENT_SNAPSHOTS = "snapshots";
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
	
	public static void toXml(Throwable t, XmlOut out) {
		
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
			out.content(t.getMessage());
		}
		
		out.close(ELEMENT_XERROR);
		
	}
	
	public static Throwable toException(MiniElement xml) {
		
		if(!ELEMENT_XERROR.equals(xml.getName())) {
			return null;
		}
		
		String type = xml.getAttribute(ATTRIBUTE_TYPE);
		String message = xml.getData();
		
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
	
	public static void toAuthenticationResult(boolean result, XmlOut out) {
		
		out.open(ELEMENT_AUTHENTICATED);
		out.content(Boolean.toString(result));
		out.close(ELEMENT_AUTHENTICATED);
		
	}
	
	public static boolean toAuthenticationResult(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_AUTHENTICATED);
		
		return Boolean.parseBoolean(xml.getData());
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
	        BatchedResult<XEvent[]>[] eventsRes, XmlOut out) {
		
		out.open(ELEMENT_RESULTS);
		
		out.open(ELEMENT_COMMANDRESULTS);
		setRevisionListContents(commandRes, out);
		out.close(ELEMENT_COMMANDRESULTS);
		
		if(eventsRes != null) {
			toXml(ger, eventsRes, out);
		}
		
		out.close(ELEMENT_RESULTS);
		
	}
	
	public static Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> toCommandResults(
	        MiniElement xml, GetEventsRequest[] context) {
		
		XmlUtils.checkElementName(xml, ELEMENT_RESULTS);
		
		Iterator<MiniElement> it = xml.getElements();
		if(!it.hasNext()) {
			throw new IllegalArgumentException("missing command results");
		}
		MiniElement commandsEle = it.next();
		
		XmlUtils.checkElementName(commandsEle, ELEMENT_COMMANDRESULTS);
		
		BatchedResult<Long>[] commandResults = getRevisionListContents(commandsEle);
		
		BatchedResult<XEvent[]>[] eventResults = null;
		if(it.hasNext()) {
			eventResults = toEventResults(it.next(), context);
		}
		
		return new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(commandResults,
		        eventResults);
	}
	
	public static void toXml(EventsRequest ger, BatchedResult<XEvent[]>[] results, XmlOut out) {
		
		out.open(ELEMENT_EVENTRESULTS);
		
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
	
	public static BatchedResult<XEvent[]>[] toEventResults(MiniElement xml,
	        GetEventsRequest[] context) {
		
		XmlUtils.checkElementName(xml, ELEMENT_EVENTRESULTS);
		
		List<BatchedResult<XEvent[]>> results = new ArrayList<BatchedResult<XEvent[]>>();
		
		int i = 0 - 1;
		
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement ele = it.next();
			i++;
			
			Throwable t = toException(xml);
			if(t != null) {
				results.add(new BatchedResult<XEvent[]>(t));
				continue;
			}
			
			if(XmlValue.isNullElement(ele)) {
				results.add(new BatchedResult<XEvent[]>((XEvent[])null));
				continue;
			}
			
			try {
				
				List<XEvent> events = XmlEvent.toEventList(xml,
				        context != null ? i < context.length ? context[i].address : null : null);
				
				results.add(new BatchedResult<XEvent[]>(events.toArray(new XEvent[events.size()])));
				
			} catch(Throwable th) {
				results.add(new BatchedResult<XEvent[]>(th));
			}
			
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<XEvent[]>[] arr = new BatchedResult[results.size()];
		return results.toArray(arr);
	}
	
	public static void toModelRevisions(BatchedResult<Long>[] result, XmlOut out) {
		
		out.open(ELEMENT_MODEL_REVISIONS);
		setRevisionListContents(result, out);
		out.close(ELEMENT_MODEL_REVISIONS);
		
	}
	
	public static BatchedResult<Long>[] toModelRevisions(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_MODEL_REVISIONS);
		
		return getRevisionListContents(xml);
	}
	
	private static void setRevisionListContents(BatchedResult<Long>[] results, XmlOut out) {
		
		for(BatchedResult<Long> result : results) {
			
			if(result.getException() != null) {
				toXml(result.getException(), out);
				continue;
			}
			
			out.open(ELEMENT_XREVISION);
			assert result.getResult() != null;
			out.content(Long.toString(result.getResult()));
			out.close(ELEMENT_XREVISION);
			
		}
		
	}
	
	private static BatchedResult<Long>[] getRevisionListContents(MiniElement xml) {
		
		List<BatchedResult<Long>> results = new ArrayList<BatchedResult<Long>>();
		
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement ele = it.next();
			
			Throwable t = toException(xml);
			if(t != null) {
				results.add(new BatchedResult<Long>(t));
				continue;
			}
			
			try {
				
				XmlUtils.checkElementName(ele, ELEMENT_XREVISION);
				
				long rev = Long.parseLong(ele.getData());
				
				results.add(new BatchedResult<Long>(rev));
				
			} catch(Throwable th) {
				results.add(new BatchedResult<Long>(th));
			}
			
		}
		
		@SuppressWarnings("unchecked")
		BatchedResult<Long>[] arr = new BatchedResult[results.size()];
		return results.toArray(arr);
	}
	
	public static void toXml(StoreException[] ex, boolean[] isModel,
	        BatchedResult<XReadableModel>[] mr, BatchedResult<XReadableObject>[] or, XmlOut out) {
		
		out.open(ELEMENT_SNAPSHOTS);
		
		int mi = 0, oi = 0;
		
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
	
	public static class Snapshots {
		
		public XReadableModel[] models;
		public XReadableObject[] objects;
		public Throwable[] exceptions;
		
	}
	
	public static Snapshots toSnapshots(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_SNAPSHOTS);
		
		List<XReadableModel> models = new ArrayList<XReadableModel>();
		List<XReadableObject> objects = new ArrayList<XReadableObject>();
		List<Throwable> exceptions = new ArrayList<Throwable>();
		
		Iterator<MiniElement> it = xml.getElements();
		while(it.hasNext()) {
			MiniElement ele = it.next();
			
			Throwable t = toException(xml);
			exceptions.add(t);
			if(t != null) {
				continue;
			}
			
			try {
				
				if(XmlValue.isNullElement(ele)) {
					models.add(null);
					objects.add(null);
				} else if(XmlModel.isModel(xml)) {
					models.add(XmlModel.toModelState(xml, null));
					objects.add(null);
				} else {
					objects.add(XmlModel.toObjectState(xml, null));
					models.add(null);
				}
				
			} catch(Throwable th) {
				exceptions.set(exceptions.size() - 1, th);
			}
			
		}
		
		Snapshots s = new Snapshots();
		
		s.models = models.toArray(new XReadableModel[models.size()]);
		s.objects = objects.toArray(new XReadableObject[objects.size()]);
		s.exceptions = exceptions.toArray(new Throwable[exceptions.size()]);
		
		return s;
	}
	
	public static void toModelIds(Set<XID> result, XmlOut out) {
		
		out.open(ELEMENT_MODEL_IDS);
		XmlValue.setIdListContents(result, out);
		out.close(ELEMENT_MODEL_IDS);
		
	}
	
	public static Set<XID> toModelIds(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_MODEL_IDS);
		
		List<XID> ids = XmlValue.getIdListContents(xml);
		
		return new HashSet<XID>(ids);
	}
	
	public static void toRepositoryId(XID result, XmlOut out) {
		
		out.open(ELEMENT_REPOSITORY_ID);
		XmlValue.toXml(result, out);
		out.close(ELEMENT_REPOSITORY_ID);
		
	}
	
	public static XID toRepositoryId(MiniElement xml) {
		
		XmlUtils.checkElementName(xml, ELEMENT_REPOSITORY_ID);
		
		Iterator<MiniElement> it = xml.getElements();
		
		if(!it.hasNext()) {
			throw new IllegalArgumentException("missing repository id");
		}
		
		return XmlValue.toId(it.next());
		
	}
	
}
