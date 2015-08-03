package org.xydra.webadmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.xydra.base.Base;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.persistence.XydraPersistence;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.xgae.XGae;
import org.xydra.xgae.gaeutils.GaeTestfixer;

public class LocalUtils {

	public static void main(final String[] args) throws IOException {
		loadFromLocalDisk("/xxx/file.zip");
	}

	public static void loadFromLocalDisk(final String zipFilePath) throws IOException {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();

		// was "deleteOneByOne", I wonder why
		XGae.get().datastore().sync().clear();
		assert XGae.get().datastore().sync().getAllKinds().size() == 0;
		XGae.get().memcache().clear();
		InstanceContext.clear();

		final File zipFile = new File(zipFilePath);
		final FileInputStream fis = new FileInputStream(zipFile);
		final OutputStreamWriter osw = new OutputStreamWriter(System.out);
		RepositoryResource.updateFromZippedInputStream(fis, Base.toId("gae-data"), osw, true);
		// now the data is loaded
		final XydraPersistence repo = XydraRuntime.getPersistence(Base.toId("gae-data"));
		for (final XId modelId : repo.getManagedModelIds()) {
			System.out.println("Managed modelId: " + modelId);
		}
		osw.flush();
		osw.close();
	}

}
