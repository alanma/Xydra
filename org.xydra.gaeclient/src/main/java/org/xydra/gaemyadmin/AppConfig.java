package org.xydra.gaemyadmin;

import java.io.File;

import com.google.appengine.tools.admin.AppCfg;


public class AppConfig {
	
	static abstract class Command {
	}
	
	static class download_data extends Command {
		public download_data(String appId, String appName, String filename) {
			super();
			this.appId = appId;
			this.appName = appName;
			this.filename = filename;
		}
		
		String appId;
		String appName;
		String filename;
		
		@Override
		public String toString() {
			return "download_data --application=" + this.appId + " --url=http://" + this.appName
			        + ".appspot.com/remote_api --filename=" + this.filename;
		}
	}
	
	static class update extends Command {
		public update(String warDir) {
			super();
			this.warDir = warDir;
		}
		
		String warDir;
		
		@Override
		public String toString() {
			return "update " + this.warDir;
		}
	}
	
	public static void main(String[] mainArgs) {
		// AppAdminFactory appAdminFactory = new AppAdminFactory();
		//	    
		// ConnectOptions options = new ConnectOptions();
		// Application app = Application.readApplication("");
		// PrintWriter errorWriter = new PrintWriter(System.out);
		// appAdminFactory.createAppAdmin(options, app, errorWriter);
		
		String setSdkRootInVM = "-Dappengine.sdk.root=C:/app/dev/appengine-java-sdk-131";
		
		String warDir = "P:\\2010\\org.xydra.gaemyadmin\\target\\gaemyadmin-0.0.1-SNAPSHOT";
		String logResultFilename = "mylogs.txt";
		
		String sdk = System.getProperty("appengine.sdk.root");
		if(sdk == null) {
			sdk = "C:/app/dev/appengine-java-sdk-131";
			// throw new RuntimeException("sdk not set");
		}
		String sdkDir = sdk;
		
		// hack for getting appengine-tools-api.jar on a runtime classpath
		// (KickStart checks java.class.path system property for classpath
		// entries)
		final String classpath = System.getProperty("java.class.path");
		final String toolsJar = sdkDir + "/lib/appengine-tools-api.jar";
		if(!classpath.contains(toolsJar)) {
			System.setProperty("java.class.path", classpath + File.pathSeparator + toolsJar);
		}
		
		Command command;
		
		String appId = "gae-experiments";
		String appName = "gae-experiments";
		String filename = "./target/backup.txt";
		command = new download_data(appId, appName, filename);
		command = new update(warDir);
		
		String commandLine = command.toString();
		System.out.println("Launching\n" + commandLine);
		
		String[] args = commandLine.split(" ");
		AppCfg.main(args);
	}
}
