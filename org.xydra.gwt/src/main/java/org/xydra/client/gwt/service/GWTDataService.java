package org.xydra.client.gwt.service;

import org.xydra.client.Callback;
import org.xydra.client.XDataService;
import org.xydra.core.XX;
import org.xydra.core.model.XBaseField;
import org.xydra.core.model.XBaseModel;
import org.xydra.core.model.XBaseObject;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.xml.MiniElement;
import org.xydra.core.xml.MiniXMLParser;
import org.xydra.core.xml.XmlModel;
import org.xydra.core.xml.impl.XmlOutStringBuffer;
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
	
	public GWTDataService(String baseUrl, MiniXMLParser parser) {
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
					model = XmlModel.toModel(actorId, null, xml);
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
					object = XmlModel.toObject(actorId, null, xml);
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
					field = XmlModel.toField(actorId, xml);
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
	
	public void setModel(XBaseModel model, Callback<Boolean> callback) {
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(model, xo, false, false, false);
		
		send("", xo.getXml(), callback);
		
	}
	
	public void setObject(XID modelId, XBaseObject object, Callback<Boolean> callback) {
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(object, xo, false, false, false);
		
		send(modelId.toString(), xo.getXml(), callback);
		
	}
	
	public void setField(XID modelId, XID objectId, XBaseField field, Callback<Boolean> callback) {
		
		XmlOutStringBuffer xo = new XmlOutStringBuffer();
		XmlModel.toXml(field, xo, false);
		
		send(modelId.toString() + "/" + objectId.toString(), xo.getXml(), callback);
		
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
