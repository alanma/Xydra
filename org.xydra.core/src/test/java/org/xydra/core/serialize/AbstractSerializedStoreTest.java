package org.xydra.core.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.MemoryFieldEvent;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XV;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XCompareUtils;
import org.xydra.core.serialize.SerializedStore.EventsRequest;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.AccessException;
import org.xydra.store.AuthorisationException;
import org.xydra.store.BatchedResult;
import org.xydra.store.ConnectionException;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.InternalStoreException;
import org.xydra.store.QuotaException;
import org.xydra.store.RequestException;
import org.xydra.store.RevisionState;
import org.xydra.store.StoreException;
import org.xydra.store.TimeoutException;


abstract public class AbstractSerializedStoreTest extends AbstractSerializingTest {
	
	private static final Logger log = getLogger();
	
	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(AbstractSerializedStoreTest.class);
	}
	
	private static final XAddress address = XX.toAddress("/a/b/c/d");
	private static final XEvent event = MemoryFieldEvent.createAddEvent(XX.toId("actor"), address,
	        XV.toValue("value"), 2, 1, false);
	
	@Test
	public void testExceptionAccess() {
		testException(new AccessException("test"));
	}
	
	@Test
	public void testExceptionAuthorisation() {
		testException(new AuthorisationException("test"));
	}
	
	@Test
	public void testExceptionConnection() {
		testException(new ConnectionException("test"));
	}
	
	@Test
	public void testExceptionTimeout() {
		testException(new TimeoutException("test"));
	}
	
	@Test
	public void testExceptionInternal() {
		testException(new InternalStoreException("test"));
	}
	
	@Test
	public void testExceptionQuota() {
		testException(new QuotaException("test"));
	}
	
	@Test
	public void testExceptionStore() {
		testException(new StoreException("test"));
	}
	
	@Test
	public void testExceptionRequest() {
		testException(new RequestException("test"));
	}
	
	private void testException(Throwable t) {
		
		XydraOut out = create();
		SerializedStore.serializeException(t, out);
		
		XydraElement e = parse(out.getData());
		Throwable t2 = SerializedStore.toException(e);
		assertNotNull(t2);
		
		checkException(t, t2);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandResultsEmpty() {
		testCommandResults(new BatchedResult[] {}, null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandResultsStoreError() {
		testCommandResults(new BatchedResult[] { storeError() }, null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandResultsParseError() {
		testCommandResults(new BatchedResult[] { preError() }, null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandResultsSuccess() {
		testCommandResults(new BatchedResult[] { result(42l) }, null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandResultsMixed() {
		testCommandResults(new BatchedResult[] { result(42l), preError(), result(-1l),
		        storeError(), result(-2l) }, null);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultEmpty() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] {});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultStoreError() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] { storeError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultPreError() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] { preError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultParseError() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] { parseError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultSuccess() {
		testCommandResults(new BatchedResult[] {},
		        new BatchedResult[] { result(new XEvent[] { event }) });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultNull() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] { result(null) });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testCommandEventsResultMixed() {
		testCommandResults(new BatchedResult[] {}, new BatchedResult[] {
		        result(new XEvent[] { event }), preError(), result(null), parseError(),
		        storeError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultEmpty() {
		testEventsResults(new BatchedResult[] {});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultStoreError() {
		testEventsResults(new BatchedResult[] { storeError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultPreError() {
		testEventsResults(new BatchedResult[] { preError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultParseError() {
		testEventsResults(new BatchedResult[] { parseError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultSuccess() {
		testEventsResults(new BatchedResult[] { result(new XEvent[] { event }) });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultNull() {
		testEventsResults(new BatchedResult[] { result(null) });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEventsResultMixed() {
		testEventsResults(new BatchedResult[] { result(new XEvent[] { event }), preError(),
		        result(null), parseError(), storeError() });
	}
	
	private void testEventsResults(BatchedResult<XEvent[]>[] eventRes) {
		
		assert eventRes != null;
		
		EventsRequest er = preparePreRequests(eventRes);
		BatchedResult<XEvent[]>[] events = preparePreResults(eventRes);
		
		XydraOut out = create();
		SerializedStore.serializeEventsResults(er, events, out);
		
		GetEventsRequest[] req = preparePostRequests(eventRes);
		BatchedResult<XEvent[]>[] res2 = preparePost(eventRes);
		
		String data = out.getData();
		log.info(data);
		
		XydraElement element = parse(data);
		assertNotNull(element);
		SerializedStore.toEventResults(element, req, res2);
		
		checkBatchedResult(eventRes, res2);
	}
	
	private void testCommandResults(BatchedResult<Long>[] commandRes,
	        BatchedResult<XEvent[]>[] eventRes) {
		
		assert commandRes != null;
		
		BatchedResult<Long>[] commands = preparePre(commandRes);
		
		EventsRequest er = preparePreRequests(eventRes);
		BatchedResult<XEvent[]>[] events = preparePreResults(eventRes);
		
		XydraOut out = create();
		SerializedStore.serializeCommandResults(commands, er, events, out);
		
		BatchedResult<Long>[] res = preparePost(commandRes);
		
		GetEventsRequest[] req = preparePostRequests(eventRes);
		BatchedResult<XEvent[]>[] res2 = preparePost(eventRes);
		
		String data = out.getData();
		
		log.info(data);
		
		XydraElement element = parse(data);
		assertNotNull(element);
		SerializedStore.toCommandResults(element, req, res, res2);
		
		checkBatchedResult(commandRes, res);
		if(eventRes != null) {
			checkBatchedResult(eventRes, res2);
		}
	}
	
	private GetEventsRequest[] preparePostRequests(BatchedResult<XEvent[]>[] eventRes) {
		
		if(eventRes == null) {
			return null;
		}
		
		GetEventsRequest[] res = new GetEventsRequest[eventRes.length];
		for(int i = 0; i < eventRes.length; i++) {
			res[i] = makeRequest(eventRes[i]);
		}
		
		return res;
	}
	
	private static final GetEventsRequest dummyReq = new GetEventsRequest(XX
	        .toAddress("/hello/world"), 0, Long.MAX_VALUE);
	
	@SuppressWarnings("unchecked")
	private BatchedResult<XEvent[]>[] preparePreResults(BatchedResult<XEvent[]>[] eventRes) {
		
		if(eventRes == null) {
			return null;
		}
		
		List<BatchedResult<XEvent[]>> events = new ArrayList<BatchedResult<XEvent[]>>();
		for(BatchedResult<XEvent[]> res : eventRes) {
			assert res != null;
			if(!(res.getException() instanceof PreException)) {
				if(res.getException() instanceof RequestException) {
					events.add((BatchedResult<XEvent[]>)(Object)storeError());
				} else {
					events.add(res);
				}
			}
		}
		
		return events.toArray(new BatchedResult[events.size()]);
	}
	
	private EventsRequest preparePreRequests(BatchedResult<XEvent[]>[] eventRes) {
		
		if(eventRes == null) {
			return null;
		}
		
		List<GetEventsRequest> ger = new ArrayList<GetEventsRequest>();
		List<StoreException> except = new ArrayList<StoreException>();
		
		for(BatchedResult<XEvent[]> res : eventRes) {
			assert res != null;
			
			if(res.getException() instanceof PreException) {
				continue;
			}
			
			if(res.getException() instanceof RequestException) {
				ger.add(null);
				except.add((RequestException)res.getException());
			} else {
				ger.add(makeRequest(res));
				except.add(null);
			}
			
		}
		
		return new EventsRequest(except.toArray(new StoreException[except.size()]), ger
		        .toArray(new GetEventsRequest[ger.size()]));
	}
	
	private GetEventsRequest makeRequest(BatchedResult<XEvent[]> res) {
		
		if(res.getResult() == null) {
			return dummyReq;
		}
		
		XEvent[] events = res.getResult();
		
		XAddress addr = null;
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		
		for(XEvent event : events) {
			
			if(event == null) {
				continue;
			}
			
			if(addr == null) {
				addr = event.getTarget();
			} else {
				while(addr != null && !addr.equalsOrContains(event.getTarget())) {
					addr = addr.getParent();
				}
				assert addr != null;
			}
			
			if(event.getRevisionNumber() < min) {
				min = event.getRevisionNumber();
			}
			if(event.getRevisionNumber() > max) {
				max = event.getRevisionNumber();
			}
			
		}
		
		return addr == null ? dummyReq : new GetEventsRequest(addr, min, max);
	}
	
	private <T> void checkBatchedResult(BatchedResult<T>[] expected, BatchedResult<T>[] actual) {
		assert expected.length == actual.length;
		for(int i = 0; i < expected.length; i++) {
			checkBatchedResult(expected[i], actual[i]);
		}
	}
	
	private <T> void checkBatchedResult(BatchedResult<T> expected, BatchedResult<T> actual) {
		
		assert expected != null;
		assertNotNull(actual);
		
		if(expected.getException() != null) {
			assertNotNull(actual.getException());
			checkException(expected.getException(), actual.getException());
		} else {
			assertNull(actual.getException());
		}
		
		if(expected.getResult() != null) {
			assertNotNull(actual.getResult());
			if(expected.getResult().getClass().isArray()) {
				assertTrue(expected.getResult().getClass().isArray());
				assertTrue(Arrays.equals((Object[])actual.getResult(), (Object[])expected
				        .getResult()));
			} else {
				assertFalse(expected.getResult().getClass().isArray());
				assertEquals(expected.getResult(), actual.getResult());
			}
		} else {
			assertNull(actual.getResult());
		}
		
	}
	
	private void checkException(Throwable expected, Throwable actual) {
		
		if(expected instanceof AccessException) {
			assertTrue(actual instanceof AccessException);
		} else {
			assertFalse(actual instanceof AccessException);
		}
		if(expected instanceof AuthorisationException) {
			assertTrue(actual instanceof AuthorisationException);
		} else {
			assertFalse(actual instanceof AuthorisationException);
		}
		if(expected instanceof TimeoutException) {
			assertTrue(actual instanceof TimeoutException);
		} else {
			assertFalse(actual instanceof TimeoutException);
		}
		if(expected instanceof ConnectionException) {
			assertTrue(actual instanceof ConnectionException);
		} else {
			assertFalse(actual instanceof ConnectionException);
		}
		if(expected instanceof AccessException) {
			assertTrue(actual instanceof AccessException);
		} else {
			assertFalse(actual instanceof AccessException);
		}
		if(expected instanceof InternalStoreException) {
			assertTrue(actual instanceof InternalStoreException);
		} else {
			assertFalse(actual instanceof InternalStoreException);
		}
		if(expected instanceof QuotaException) {
			assertTrue(actual instanceof QuotaException);
		} else {
			assertFalse(actual instanceof QuotaException);
		}
		if(expected instanceof RequestException) {
			assertTrue(actual instanceof RequestException);
		} else {
			assertFalse(actual instanceof RequestException);
		}
		if(expected instanceof StoreException) {
			assertTrue(actual instanceof StoreException);
		} else {
			assertFalse(actual instanceof StoreException);
		}
		if(expected instanceof PreException) {
			assertTrue(actual instanceof PreException);
		} else {
			assertFalse(actual instanceof PreException);
		}
		
		assertEquals(expected.getMessage(), actual.getMessage());
		
	}
	
	private static class PreException extends Throwable {
		
		public PreException() {
			super("test");
		}
		
		private static final long serialVersionUID = -6088414262013276208L;
		
	}
	
	private static <T> BatchedResult<T> preError() {
		return error(new PreException());
	}
	
	private static <T> BatchedResult<T> parseError() {
		return error(new RequestException("test2"));
	}
	
	private static <T> BatchedResult<T> storeError() {
		return error(new StoreException("hello world"));
	}
	
	private static <T> BatchedResult<T> error(Throwable t) {
		return new BatchedResult<T>(t);
	}
	
	private static <T> BatchedResult<T> result(T result) {
		return new BatchedResult<T>(result);
	}
	
	@Test
	public void testAuthenticationResultTrue() {
		testAuthenticationResult(true);
	}
	
	@Test
	public void testAuthenticationResultFalse() {
		testAuthenticationResult(false);
	}
	
	public void testAuthenticationResult(boolean b) {
		
		XydraOut out = create();
		SerializedStore.serializeAuthenticationResult(b, out);
		
		XydraElement e = parse(out.getData());
		boolean b2 = SerializedStore.toAuthenticationResult(e);
		
		assertEquals(b, b2);
	}
	
	private void testModelRevisions(BatchedResult<RevisionState>[] revs) {
		
		assert revs != null;
		
		BatchedResult<RevisionState>[] results = preparePre(revs);
		
		XydraOut out = create();
		SerializedStore.serializeModelRevisions(results, out);
		
		BatchedResult<RevisionState>[] res = preparePost(revs);
		
		XydraElement element = parse(out.getData());
		assertNotNull(element);
		SerializedStore.toModelRevisions(element, res);
		
		checkBatchedResult(revs, res);
	}
	
	@SuppressWarnings("unchecked")
	private <T> BatchedResult<T>[] preparePost(BatchedResult<T>[] revs) {
		
		if(revs == null) {
			return null;
		}
		
		BatchedResult<T>[] res = new BatchedResult[revs.length];
		for(int i = 0; i < revs.length; i++) {
			assert revs[i] != null;
			if(revs[i].getException() instanceof PreException) {
				res[i] = revs[i];
			}
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private <T> BatchedResult<T>[] preparePre(BatchedResult<T>[] revs) {
		
		if(revs == null) {
			return null;
		}
		
		List<BatchedResult<T>> results = new ArrayList<BatchedResult<T>>();
		for(BatchedResult<T> res : revs) {
			assert res != null;
			if(!(res.getException() instanceof PreException)) {
				results.add(res);
			}
		}
		return results.toArray(new BatchedResult[results.size()]);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testModelRevisionsEmpty() {
		testModelRevisions(new BatchedResult[] {});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testModelRevisionsStoreError() {
		testModelRevisions(new BatchedResult[] { storeError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testModelRevisionsPreError() {
		testModelRevisions(new BatchedResult[] { preError() });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testModelRevisionsSuccess() {
		testModelRevisions(new BatchedResult[] { result(new RevisionState(42, true)) });
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testModelRevisionsMixed() {
		testModelRevisions(new BatchedResult[] { result(new RevisionState(42, true)), preError(),
		        result(new RevisionState(-1, true)), storeError(),
		        result(new RevisionState(10, true)) });
	}
	
	private static Object modelError() {
		return new AccessException("model");
	}
	
	private static Object objectError() {
		return new QuotaException("object");
	}
	
	private static Object nullModel() {
		return XX.toAddress("/repo/model");
	}
	
	private static Object nullObject() {
		return XX.toAddress("/repo/model/object");
	}
	
	@SuppressWarnings("unchecked")
	private void testSnapshots(Object[] data) {
		
		assert data != null;
		
		StoreException[] parseErrors = new StoreException[data.length];
		boolean[] isModel = new boolean[data.length];
		List<BatchedResult<XReadableModel>> models = new ArrayList<BatchedResult<XReadableModel>>();
		List<BatchedResult<XReadableObject>> objects = new ArrayList<BatchedResult<XReadableObject>>();
		XAddress[] context = new XAddress[data.length];
		
		for(int i = 0; i < data.length; i++) {
			Object item = data[i];
			
			if(item instanceof RequestException) {
				parseErrors[i] = (RequestException)item;
				context[i] = address;
			} else if(item instanceof XReadableModel) {
				models.add(result((XReadableModel)item));
				isModel[i] = true;
				context[i] = ((XReadableModel)item).getAddress();
			} else if(item instanceof XReadableObject) {
				objects.add(result((XReadableObject)item));
				isModel[i] = false;
				context[i] = ((XReadableObject)item).getAddress();
			} else if(item instanceof AccessException) {
				models.add((BatchedResult<XReadableModel>)(Object)error((AccessException)item));
				isModel[i] = true;
				context[i] = address;
			} else if(item instanceof QuotaException) {
				objects.add((BatchedResult<XReadableObject>)(Object)error((QuotaException)item));
				isModel[i] = false;
				context[i] = address;
			} else if(item instanceof XAddress) {
				XAddress addr = (XAddress)item;
				if(addr.getAddressedType() == XType.XMODEL) {
					models.add(new BatchedResult<XReadableModel>((XReadableModel)null));
					isModel[i] = true;
				} else {
					objects.add(new BatchedResult<XReadableObject>((XReadableObject)null));
					isModel[i] = false;
				}
				context[i] = addr;
			} else {
				assert false;
			}
			
		}
		
		XydraOut out = create();
		SerializedStore.serializeSnapshots(parseErrors, isModel, models
		        .toArray(new BatchedResult[1]), objects.toArray(new BatchedResult[1]), out);
		
		XydraElement element = parse(out.getData());
		assertNotNull(element);
		List<Object> results = SerializedStore.toSnapshots(element, context);
		
		assertEquals(data.length, results.size());
		
		for(int i = 0; i < data.length; i++) {
			Object expected = data[i];
			Object actual = results.get(i);
			
			if(expected instanceof Throwable) {
				assertTrue(actual instanceof Throwable);
				checkException((Throwable)expected, (Throwable)actual);
			} else if(expected instanceof XReadableModel) {
				assertTrue(actual instanceof XReadableModel);
				assertTrue(XCompareUtils.equalState((XReadableModel)expected,
				        (XReadableModel)actual));
			} else if(expected instanceof XReadableObject) {
				assertTrue(actual instanceof XReadableObject);
				assertTrue(XCompareUtils.equalState((XReadableObject)expected,
				        (XReadableObject)actual));
			} else if(expected instanceof XAddress) {
				assertNull(actual);
			} else {
				assert false;
			}
		}
	}
	
	@Test
	public void testSnapshotsEmpty() {
		testSnapshots(new Object[] {});
	}
	
	@Test
	public void testSnapshotsError() {
		testSnapshots(new Object[] { new RequestException("parse error") });
	}
	
	@Test
	public void testSnapshotsSuccessModel() {
		testSnapshots(new Object[] { new SimpleModel(XX.toAddress("/a/b"), 23l) });
	}
	
	@Test
	public void testSnapshotsSuccessObject() {
		testSnapshots(new Object[] { new SimpleObject(XX.toAddress("/a/b/c"), 23l) });
	}
	
	@Test
	public void testSnapshotsModelError() {
		testSnapshots(new Object[] { modelError() });
	}
	
	@Test
	public void testSnapshotsObjectError() {
		testSnapshots(new Object[] { objectError() });
	}
	
	@Test
	public void testSnapshotsNullModel() {
		testSnapshots(new Object[] { nullModel() });
	}
	
	@Test
	public void testSnapshotsNullObject() {
		testSnapshots(new Object[] { nullObject() });
	}
	
	@Test
	public void testSnapshotsMixed() {
		testSnapshots(new Object[] { new SimpleModel(XX.toAddress("/a/b"), 23l),
		        new RequestException("parse error"), new SimpleObject(XX.toAddress("/a/b/c"), 23l),
		        nullModel(), modelError(), nullObject(), objectError() });
	}
	
	@Test
	public void testModelIdsEmpty() {
		testModelIds(new XID[] {});
	}
	
	@Test
	public void testModelIds() {
		testModelIds(new XID[] { XX.toId("test") });
	}
	
	public void testModelIds(XID[] mids) {
		
		Set<XID> ids = new HashSet<XID>();
		ids.addAll(Arrays.asList(mids));
		
		XydraOut out = create();
		SerializedStore.serializeModelIds(ids, out);
		
		XydraElement e = parse(out.getData());
		Set<XID> ids2 = SerializedStore.toModelIds(e);
		
		assertEquals(ids, ids2);
	}
	
	@Test
	public void testRepositoryId() {
		
		XID repoId = XX.toId("repoId");
		
		XydraOut out = create();
		SerializedStore.serializeRepositoryId(repoId, out);
		
		XydraElement e = parse(out.getData());
		XID repoId2 = SerializedStore.toRepositoryId(e);
		
		assertEquals(repoId, repoId2);
	}
	
}
