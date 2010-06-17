package org.xydra.client.gwt.service;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.XDataService;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;

import com.allen_sauer.gwt.log.client.Log;
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
	
	public GWTDataService(String baseUrl, MiniXMLParser parser) {
		super(baseUrl, parser);
	}
	
	public void getModel(XID modelId, final Callback<XModel> callback) {
		Log.info("data service: loading model " + modelId);
		getXml(modelId.toString(), new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				Log.info("data service: loading model failed", error);
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XModel model;
				try {
					model = XmlModel.toModel(xml);
				} catch(Exception e) {
					Log.info("data service: loading model failed", e);
					callback.onFailure(e);
					return;
				}
				Log.info("model loaded");
				callback.onSuccess(model);
			}
		});
	}
	
	public void getObject(XID modelId, XID objectId, final Callback<XObject> callback) {
		String addr = modelId.toString() + "/" + objectId.toString();
		Log.info("data service: loading object " + addr);
		getXml(addr, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				Log.info("data service: loading object failed", error);
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XObject object;
				try {
					object = XmlModel.toObject(xml);
				} catch(Exception e) {
					Log.info("data service: loading object failed", e);
					callback.onFailure(e);
					return;
				}
				Log.info("object loaded");
				callback.onSuccess(object);
			}
		});
	}
	
	public void getField(XID modelId, XID objectId, XID fieldId, final Callback<XField> callback) {
		String addr = modelId.toString() + "/" + objectId.toString() + "/" + fieldId.toString();
		Log.info("data service: loading field " + addr);
		getXml(addr, new Callback<MiniElement>() {
			
			public void onFailure(Throwable error) {
				Log.info("data service: loading field failed", error);
				callback.onFailure(error);
			}
			
			public void onSuccess(MiniElement xml) {
				XField field;
				try {
					field = XmlModel.toField(xml);
				} catch(Exception e) {
					Log.info("data service: loading field failed", e);
					callback.onFailure(e);
					return;
				}
				Log.info("field loaded");
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
			Log.info("data service: deleted");
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
			Log.info("data service: saved");
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
	
	public void setModel(XModel model, Callback<Boolean> callback) {
		Log.info("data service: setting model " + model.getID().toString());
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, false, false);
		
		send("", xo.getXml(), callback);
	}
	
	public void setObject(XID modelId, XObject object, Callback<Boolean> callback) {
		Log.info("data service: setting object " + modelId.toString() + "/"
		        + object.getID().toString());
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(object, xo, false, false);
		
		send(modelId.toURI(), xo.getXml(), callback);
	}
	
	public void setField(XID modelId, XID objectId, XField field, Callback<Boolean> callback) {
		Log.info("data service: setting object " + modelId.toString() + "/" + objectId.toString()
		        + "/" + field.getID().toString());
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(field, xo, false);
		
		send(modelId.toURI() + "/" + objectId.toURI(), xo.getXml(), callback);
	}
	
	public void delete(String address, Callback<Void> callback) {
		Log.info("data service: deleting " + address);
		
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
		delete(modelId.toURI(), callback);
	}
	
	public void deleteObject(XID modelId, XID objectId, Callback<Void> callback) {
		delete(modelId.toURI() + "/" + objectId.toURI(), callback);
	}
	
	public void deleteField(XID modelId, XID objectId, XID fieldId, Callback<Void> callback) {
		delete(modelId.toURI() + "/" + objectId.toURI() + "/" + fieldId.toURI(), callback);
	}
	
}
