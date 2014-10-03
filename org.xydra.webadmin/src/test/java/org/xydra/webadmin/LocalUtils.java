package org.xydra.webadmin;

import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.xgae.XGae;
import org.xydra.xgae.gaeutils.GaeTestfixer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LocalUtils {

	public static void main(String[] args) throws IOException {
		loadFromLocalDisk("/xxx/file.zip");
	}

	public static void loadFromLocalDisk(String zipFilePath) throws IOException {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();

		// was "deleteOneByOne", I wonder why
		XGae.get().datastore().sync().clear();
		assert XGae.get().datastore().sync().getAllKinds().size() == 0;
		XGae.get().memcache().clear();
		InstanceContext.clear();

		File zipFile = new File(zipFilePath);
		FileInputStream fis = new FileInputStream(zipFile);
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		RepositoryResource.updateFromZippedInputStream(fis, XX.toId("gae-data"), osw, true);
		// now the data is loaded
		XydraPersistence repo = XydraRuntime.getPersistence(XX.toId("gae-data"));
		for (XId modelId : repo.getManagedModelIds()) {
			System.out.println("Managed modelId: " + modelId);
		}
		osw.flush();
		osw.close();
	}

}
