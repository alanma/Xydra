package org.xydra.client.gwt.service;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.client.Callback;
import org.xydra.client.XDataService;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.serialize.MiniElement;
import org.xydra.core.serialize.MiniParser;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;


/**
 * A GWT implementation of the {@link XDataService} API that uses a
 * {@link RequestBuilder} to perform the HTTP requests.
 * 
 * The delete method is emulated using the "X-HTTP-Method-Override" header.
 */
public class GWTDataService extends AbstractGWTHttpService implements XDataService {
	
	static private final Logger log = LoggerFactory.getLogger(GWTDataService.class);
	
	static private final XID actorId = XX.toId(GWTDataService.class.getName());
	
	public GWTDataService(String baseUrl, MiniParser parser) {
		super(baseUrl, parser);
	}
	
	public void getModel(XID modelId, final Callback<XModel> callback) {
		
		getXml(modelId.toString(), new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XModel model;
				try {
					model = SerializedModel.toModel(actorId, null, xml);
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(model);
			}
			
		});
		
	}
	
	public void getObject(XID modelId, XID objectId, final Callback<XObject> callback) {
		
		String addr = modelId.toString() + "/" + objectId.toString();
		getXml(addr, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XObject object;
				try {
					object = SerializedModel.toObject(actorId, null, xml);
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(object);
			}
			
		});
		
	}
	
	public void getField(XID modelId, XID objectId, XID fieldId, final Callback<XField> callback) {
		
		String addr = modelId.toString() + "/" + objectId.toString() + "/" + fieldId.toString();
		getXml(addr, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XField field;
				try {
					field = SerializedModel.toField(actorId, xml);
				} catch(Exception e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(field);
			}
			
		});
		
	}
	
	private static class VoidCallback implements RequestCallback {
		
		private final Callback<Void> callback;
		
		public VoidCallback(Callback<Void> callback) {
			this.callback = callback;
		}
		
		public void onResponseReceived(Request req, Response resp) {
			if(handleError(resp, this.callback)) {
				return;
			}
			log.info("data service: deleted");
			this.callback.onSuccess(null);
		}
		
		public void onError(Request req, Throwable t) {
			handleError(t, this.callback);
		}
		
	}
	
	static class ChangedCallback implements RequestCallback {
		
		private final Callback<Boolean> callback;
		
		public ChangedCallback(Callback<Boolean> callback) {
			this.callback = callback;
		}
		
		public void onResponseReceived(Request req, Response resp) {
			if(handleError(resp, this.callback)) {
				return;
			}
			this.callback.onSuccess(resp.getStatusCode() == Response.SC_CREATED);
		}
		
		public void onError(Request req, Throwable t) {
			handleError(t, this.callback);
		}
		
	}
	
	public void send(String address, String data, Callback<Boolean> callback) {
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, this.baseUrl + "/" + address);
		
		if(callback == null)
			rb.setCallback(NoCallback.getInstance());
		else
			rb.setCallback(new ChangedCallback(callback));
		
		rb.setRequestData(data);
		
		rb.setHeader("Content-Type", "application/xml");
		
		try {
			rb.send();
		} catch(RequestException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void setModel(XReadableModel model, Callback<Boolean> callback) {
		
		XydraOut xo = new XmlOut();
		SerializedModel.toXml(model, xo, false, false, false);
		
		send("", xo.getData(), callback);
		
	}
	
	public void setObject(XID modelId, XReadableObject object, Callback<Boolean> callback) {
		
		XydraOut xo = new XmlOut();
		SerializedModel.toXml(object, xo, false, false, false);
		
		send(modelId.toString(), xo.getData(), callback);
		
	}
	
	public void setField(XID modelId, XID objectId, XReadableField field, Callback<Boolean> callback) {
		
		XydraOut xo = new XmlOut();
		SerializedModel.toXml(field, xo, false);
		
		send(modelId.toString() + "/" + objectId.toString(), xo.getData(), callback);
		
	}
	
	public void delete(String address, Callback<Void> callback) {
		
		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, this.baseUrl + "/" + address);
		rb.setHeader("X-HTTP-Method-Override", "DELETE");
		
		if(callback == null)
			rb.setCallback(NoCallback.getInstance());
		else
			rb.setCallback(new VoidCallback(callback));
		
		try {
			rb.send();
		} catch(RequestException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void deleteModel(XID modelId, Callback<Void> callback) {
		delete(modelId.toString(), callback);
	}
	
	public void deleteObject(XID modelId, XID objectId, Callback<Void> callback) {
		delete(modelId.toString() + "/" + objectId.toString(), callback);
	}
	
	public void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback) {
		delete(modelId.toString() + "/" + objectId.toString() + "/" + fieldId.toString(), callback);
	}
	
}
