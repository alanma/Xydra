package org.xydra.xgae.datastore.impl.gae;

import org.xydra.xgae.datastore.api.SText;

import com.google.appengine.api.datastore.Text;

public class GText extends RawWrapper<Text, GText> implements SText {

	private GText(Text raw) {
		super(raw);
	}

	protected static Text unwrap(SText in) {
		if (in == null)
			return null;

		return (Text) in.raw();
	}

	public static GText wrap(Text raw) {
		if (raw == null)
			return null;

		return new GText(raw);
	}

	@Override
	public String getValue() {
		return raw().getValue();
	}

}
