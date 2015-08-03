package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XId;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

public class ModelConfiguration {

	public XyAdmin adminObject;
	public XId repoId;
	public XId modelId;
	public long revisionNumber;

	public ModelConfiguration(final XyAdmin adminObject, final XId repoId, final XId modelId) {

		this.adminObject = adminObject;
		this.repoId = repoId;
		this.modelId = modelId;
	}

}
