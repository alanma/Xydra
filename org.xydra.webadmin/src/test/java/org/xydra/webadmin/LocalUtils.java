package org.xydra.webadmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.store.XydraRuntime;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.gae.GaeTestfixer;
import org.xydra.store.impl.gae.InstanceContext;
import org.xydra.store.impl.gae.SyncDatastore;


public class LocalUtils {
	
	public static void main(String[] args) throws IOException {
		loadFromLocalDisk("/xxx/file.zip");
	}
	
	public static void loadFromLocalDisk(String zipFilePath) throws IOException {
		GaeTestfixer.enable();
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();
		
		SyncDatastore.deleteAllEntitiesOneByOne();
		assert SyncDatastore.getAllKinds().size() == 0;
		XydraRuntime.getMemcache().clear();
		InstanceContext.clearInstanceContext();
		
		File zipFile = new File(zipFilePath);
		FileInputStream fis = new FileInputStream(zipFile);
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		RepositoryResource.updateFromZippedInputStream(fis, XX.toId("gae-data"), osw, true);
		// now the data is loaded
		XydraPersistence repo = XydraRuntime.getPersistence(XX.toId("gae-data"));
		for(XID modelId : repo.getManagedModelIds()) {
			System.out.println("Managed modelId: " + modelId);
		}
		osw.flush();
		osw.close();
	}
	
}
