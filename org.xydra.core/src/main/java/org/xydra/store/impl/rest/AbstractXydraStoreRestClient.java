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
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.XydraParser;
import org.xydra.core.serialize.XydraSerializer;
import org.xydra.index.query.Pair;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.ModelRevision;
import org.xydra.sharedutils.URLUtils;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.ConnectionException;
import org.xydra.store.InternalStoreException;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;
import org.xydra.store.XydraStoreAdmin;
import org.xydra.store.serialize.SerializedStore;


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

    public AbstractXydraStoreRestClient(final XydraSerializer serializer, final XydraParser parser) {
        this.serializer = serializer;
        this.parser = parser;
    }

    protected abstract class Request<T> {

        final public XId actor;
        final public String password;
        final private Callback<T> callback;

        protected Request(final XId actor, final String password, final Callback<T> callback) {

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

        public void onFailure(final Throwable t) {
            if(this.callback != null) {
                this.callback.onFailure(new ConnectionException(t.getMessage(), t));
            }
        }

        protected void onSuccess(final T result) {
            if(this.callback != null) {
                this.callback.onSuccess(result);
            }
        }

        public void onResponse(final String content, final int code, final String message) {

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
            } catch(final Throwable th) {
                this.callback.onFailure(new InternalStoreException("error parsing response", th));
                return;
            }

            final Throwable t = SerializedStore.toException(element);
            if(t != null) {
                this.callback.onFailure(t);
                return;
            }

            T result;
            try {
                result = parse(element);
            } catch(final Throwable th) {
                this.callback.onFailure(new InternalStoreException("error parsing response", th));
                return;
            }

            this.callback.onSuccess(result);
        }

        protected abstract T parse(XydraElement element);

        /**
         * @param uri
         */
        protected void get(final String uri) {

            if(this.callback == null) {
                throw new IllegalArgumentException("callback may not be null");
            }

            AbstractXydraStoreRestClient.this.get(uri, this);
        }

        protected void post(final String uri, final XydraOut data) {
            AbstractXydraStoreRestClient.this.post(uri, data, this);
        }

    }

    /**
     * @param uri format: no slashes
     * @param req
     */
    protected abstract void get(String uri, Request<?> req);

    /**
     * @param uri format: no slashes
     * @param data
     * @param req
     */
    protected abstract void post(String uri, XydraOut data, Request<?> req);

    private String encodeEventsRequests(final GetEventsRequest[] getEventsRequests,
            final BatchedResult<XEvent[]>[] res) {

        if(getEventsRequests == null) {
            throw new IllegalArgumentException("getEventsRequests array must not be null");
        }

        XyAssert.xyAssert(getEventsRequests.length == res.length);

        final StringBuilder sb = new StringBuilder();

        boolean first = true;

        for(int i = 0; i < getEventsRequests.length; i++) {
            final GetEventsRequest ger = getEventsRequests[i];

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
                sb.append(ger.endRevision);
            }

        }

        if(first) {
            return null;
        }

        return sb.toString();
    }

    private <T> String encodeAddresses(final GetWithAddressRequest[] getModelRevisionRequests,
            final BatchedResult<T>[] res, final XType type) {

        XyAssert.xyAssert(res.length == getModelRevisionRequests.length);

        final StringBuilder sb = new StringBuilder();

        boolean first = true;

        for(int i = 0; i < getModelRevisionRequests.length; i++) {
            final GetWithAddressRequest getModelRevisionRequest = getModelRevisionRequests[i];
            final XAddress address = getModelRevisionRequest.address;

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
                // FIXME REST: ! make constant + add in parser + add in docu
                sb.append("+tentative");
            }
        }

        if(first) {
            return null;
        }

        return sb.toString();
    }

    protected String urlencode(final String string) {
        return URLUtils.encode(string);
    }

    private static <T> void toBatchedResults(final List<Object> snapshots, final BatchedResult<T>[] result,
            final XType type) {

        int i = 0;
        for(final Object o : snapshots) {

            while(result[i] != null) {
                i++;
            }

            XyAssert.xyAssert(i < result.length);

            if(o == null) {
                result[i] = new BatchedResult<T>((T)null);
            } else if(o instanceof XEntity && ((XEntity)o).getType() == type) {
                @SuppressWarnings("unchecked")
				final
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

    protected String encodeLoginCookie(final XId actorId, final String passwordHash) {
        return XydraStoreRestInterface.ARG_ACTOR_ID + "=" + urlencode(actorId.toString()) + "; "
                + XydraStoreRestInterface.ARG_PASSWORD_HASH + "="
                + urlencode(passwordHash.toString());
    }

    protected String encodeLoginQuery(final XId actorId, final String passwordHash) {
        return XydraStoreRestInterface.ARG_ACTOR_ID + "=" + urlencode(actorId.toString()) + "&"
                + XydraStoreRestInterface.ARG_PASSWORD_HASH + "="
                + urlencode(passwordHash.toString());
    }

    private class LoginRequest extends Request<Boolean> {

        protected LoginRequest(final XId actor, final String password, final Callback<Boolean> callback) {
            super(actor, password, callback);
        }

        protected void run() {
            get(XydraStoreRestInterface.URL_LOGIN);
        }

        @Override
        protected Boolean parse(final XydraElement element) {
            return SerializedStore.toAuthenticationResult(element);
        }

    }

    @Override
    public void checkLogin(final XId actor, final String password, final Callback<Boolean> callback) {
        new LoginRequest(actor, password, callback).run();
    }

    protected XydraOut prepareExecuteRequest(final XCommand[] commands) {

        if(commands == null) {
            throw new IllegalArgumentException("commands array must not be null");
        }

        final XydraOut out = this.serializer.create();
        SerializedCommand.serialize(Arrays.asList(commands).iterator(), out, null);

        return out;
    }

    private class ExecuteRequest extends Request<BatchedResult<Long>[]> {

        private final XCommand[] commands;

        protected ExecuteRequest(final XId actor, final String password, final XCommand[] commands,
                final Callback<BatchedResult<Long>[]> callback) {
            super(actor, password, callback);
            this.commands = commands;
        }

        protected void run() {
            final XydraOut out = prepareExecuteRequest(this.commands);
            post(XydraStoreRestInterface.URL_EXECUTE, out);
        }

        @Override
        protected BatchedResult<Long>[] parse(final XydraElement element) {

            @SuppressWarnings("unchecked")
			final
            BatchedResult<Long>[] res = new BatchedResult[this.commands.length];

            SerializedStore.toCommandResults(element, null, res, null);

            return res;
        }

    }

    @Override
    public void executeCommands(final XId actor, final String password, final XCommand[] commands,
            final Callback<BatchedResult<Long>[]> callback) {
        new ExecuteRequest(actor, password, commands, callback).run();
    }

    @SuppressWarnings("unchecked")
    private static BatchedResult<XEvent[]>[] prepareEventsResultsArray(
            final GetEventsRequest[] getEventsRequests) {
        if(getEventsRequests == null) {
            throw new IllegalArgumentException("getEventsRequests array must not be null");
        }
        return new BatchedResult[getEventsRequests.length];
    }

    private class EventsRequest extends Request<BatchedResult<XEvent[]>[]> {

        private final GetEventsRequest[] getEventsRequests;
        private final BatchedResult<XEvent[]>[] res;

        protected EventsRequest(final XId actor, final String password, final GetEventsRequest[] getEventsRequests,
                final Callback<BatchedResult<XEvent[]>[]> callback) {
            super(actor, password, callback);
            this.getEventsRequests = getEventsRequests;
            this.res = prepareEventsResultsArray(getEventsRequests);
        }

        protected void run() {

            final String req = encodeEventsRequests(this.getEventsRequests, this.res);
            if(req == null) {
                onSuccess(this.res);
                return;
            }

            get(XydraStoreRestInterface.URL_EVENTS + "?" + req);
        }

        @Override
        protected BatchedResult<XEvent[]>[] parse(final XydraElement element) {

            SerializedStore.toEventResults(element, this.getEventsRequests, this.res);

            return this.res;
        }

    }

    @Override
    public void getEvents(final XId actor, final String password, final GetEventsRequest[] getEventsRequests,
            final Callback<BatchedResult<XEvent[]>[]> callback) {
        new EventsRequest(actor, password, getEventsRequests, callback).run();
    }

    private class ExecuteAndEventsRequest extends
            Request<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> {

        private final XCommand[] commands;
        private final GetEventsRequest[] getEventsRequests;
        private final BatchedResult<XEvent[]>[] eventsRes;

        protected ExecuteAndEventsRequest(final XId actor, final String password, final XCommand[] commands,
                final GetEventsRequest[] getEventsRequests,
                final Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
            super(actor, password, callback);
            this.commands = commands;
            this.getEventsRequests = getEventsRequests;
            this.eventsRes = prepareEventsResultsArray(getEventsRequests);
        }

        protected void run() {

            final XydraOut out = prepareExecuteRequest(this.commands);

            final String req = encodeEventsRequests(this.getEventsRequests, this.eventsRes);

            String uri = XydraStoreRestInterface.URL_EXECUTE;
            if(req != null) {
                uri += "?" + req;
            }

            post(uri, out);
        }

        @Override
        protected Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> parse(final XydraElement element) {

            @SuppressWarnings("unchecked")
			final
            BatchedResult<Long>[] commandsRes = new BatchedResult[this.commands.length];

            SerializedStore.toCommandResults(element, this.getEventsRequests, commandsRes,
                    this.eventsRes);

            return new Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>(commandsRes,
                    this.eventsRes);
        }

    }

    @Override
    public void executeCommandsAndGetEvents(final XId actor, final String password, final XCommand[] commands,
            final GetEventsRequest[] getEventsRequests,
            final Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback) {
        new ExecuteAndEventsRequest(actor, password, commands, getEventsRequests, callback).run();
    }

    private class ModelIdsRequest extends Request<Set<XId>> {

        protected ModelIdsRequest(final XId actor, final String password, final Callback<Set<XId>> callback) {
            super(actor, password, callback);
        }

        protected void run() {
            get(XydraStoreRestInterface.URL_MODEL_IDS);
        }

        @Override
        protected Set<XId> parse(final XydraElement element) {
            return SerializedStore.toModelIds(element);
        }

    }

    @Override
    public void getModelIds(final XId actor, final String password, final Callback<Set<XId>> callback) {
        new ModelIdsRequest(actor, password, callback).run();
    }

    private class RevisionsRequest extends Request<BatchedResult<ModelRevision>[]> {

        private final GetWithAddressRequest[] modelAddresses;
        private final BatchedResult<ModelRevision>[] res;

        @SuppressWarnings("unchecked")
        protected RevisionsRequest(final XId actor, final String password,
                final GetWithAddressRequest[] modelAddresses,
                final Callback<BatchedResult<ModelRevision>[]> callback) {
            super(actor, password, callback);
            this.modelAddresses = modelAddresses;
            if(this.modelAddresses == null) {
                throw new IllegalArgumentException("modelAddresses array must not be null");
            }
            this.res = new BatchedResult[this.modelAddresses.length];
        }

        protected void run() {

            final String req = encodeAddresses(this.modelAddresses, this.res, XType.XMODEL);
            if(req == null) {
                onSuccess(this.res);
                return;
            }

            get(XydraStoreRestInterface.URL_REVISIONS + "?" + req);
        }

        @Override
        protected BatchedResult<ModelRevision>[] parse(final XydraElement element) {

            SerializedStore.toModelRevisions(element, this.res);

            return this.res;
        }

    }

    @Override
    public void getModelRevisions(final XId actor, final String password,
            final GetWithAddressRequest[] modelRevisionRequests,
            final Callback<BatchedResult<ModelRevision>[]> callback) {
        new RevisionsRequest(actor, password, modelRevisionRequests, callback).run();
    }

    private class SnapshotsRequest<T> extends Request<BatchedResult<T>[]> {

        private final GetWithAddressRequest[] addressRequests;
        private final BatchedResult<T>[] res;
        private final XType type;

        @SuppressWarnings("unchecked")
        protected SnapshotsRequest(final XId actor, final String password,
                final GetWithAddressRequest[] modelAddressRequests,
                final Callback<BatchedResult<T>[]> callback, final XType type) {
            super(actor, password, callback);
            this.addressRequests = modelAddressRequests;
            if(this.addressRequests == null) {
                throw new IllegalArgumentException("addresses array must not be null");
            }
            this.res = new BatchedResult[this.addressRequests.length];
            this.type = type;
        }

        protected void run() {

            final String req = encodeAddresses(this.addressRequests, this.res, this.type);
            if(req == null) {
                onSuccess(this.res);
                return;
            }

            get(XydraStoreRestInterface.URL_SNAPSHOTS + "?" + req);
        }

        @Override
        protected BatchedResult<T>[] parse(final XydraElement element) {

            final XAddress[] addresses = new XAddress[this.addressRequests.length];
            for(int i = 0; i < addresses.length; i++) {
                addresses[i] = this.addressRequests[i].address;
            }
            final List<Object> snapshots = SerializedStore.toSnapshots(element, addresses);

            toBatchedResults(snapshots, this.res, this.type);

            return this.res;
        }

    }

    @Override
    public void getModelSnapshots(final XId actor, final String password,
            final GetWithAddressRequest[] modelAddressRequests,
            final Callback<BatchedResult<XReadableModel>[]> callback) {
        new SnapshotsRequest<XReadableModel>(actor, password, modelAddressRequests, callback,
                XType.XMODEL).run();
    }

    @Override
    public void getObjectSnapshots(final XId actor, final String password,
            final GetWithAddressRequest[] objectAddressRequests,
            final Callback<BatchedResult<XReadableObject>[]> callback) throws IllegalArgumentException {
        new SnapshotsRequest<XReadableObject>(actor, password, objectAddressRequests, callback,
                XType.XOBJECT).run();
    }

    private class RepositoryIdRequest extends Request<XId> {

        protected RepositoryIdRequest(final XId actor, final String password, final Callback<XId> callback) {
            super(actor, password, callback);
        }

        protected void run() {
            get(XydraStoreRestInterface.URL_REPOSITORY_ID);
        }

        @Override
        protected XId parse(final XydraElement element) {
            return SerializedStore.toRepositoryId(element);
        }

    }

    @Override
    public void getRepositoryId(final XId actor, final String password, final Callback<XId> callback) {
        new RepositoryIdRequest(actor, password, callback).run();
    }

}
