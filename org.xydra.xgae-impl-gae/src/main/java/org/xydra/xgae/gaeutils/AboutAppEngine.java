package org.xydra.xgae.gaeutils;

import org.xydra.store.XydraRuntime;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;

/**
 * Information about AppEngine
 *
 *
 * TODO expose os.environ['INSTANCE_ID']
 *
 * @author xamde
 */
public class AboutAppEngine {

	/**
	 * @return true if app is running on a real remote GAE server
	 */
	public static boolean inProduction() {
		return SystemProperty.environment.get() != null
				&& SystemProperty.environment.value().equals(
						SystemProperty.Environment.Value.Production);
	}

	/**
	 * @return only if on AppEngine development mode (i.e. locally)
	 */
	public static boolean inDevelopment() {
		return SystemProperty.environment.get() != null
				&& SystemProperty.environment.value().equals(
						SystemProperty.Environment.Value.Development);
	}

	/**
	 * @return true if on AppEngine (regardless whether in production or in
	 *         development mode)
	 */
	public static boolean onAppEngine() {
		return inProduction() || inDevelopment();
	}

	/**
	 * @return true if running without GAE mode; technically: !onAppEngine()
	 */
	public static boolean notOnAppengine() {
		return !onAppEngine();
	}

	/**
	 * @return The major version number for the currently running version of the
	 *         application plus a timestamp at which it was deployed. This is
	 *         not just the version identifier string you specify in
	 *         appengine-web.xml.
	 *
	 *         Format: user-chosen-versionId-from-appengine-xml '.' timestamp
	 */
	public static String getVersion() {
		String version = SystemProperty.applicationVersion.get();
		if (version == null) {
			version = "devmode";
		}
		return version;
	}

	@SuppressWarnings("deprecation")
	public static String getInstanceId() {
		return "GAE:" + SystemProperty.instanceReplicaId.get() + ";Xydra:"
				+ XydraRuntime.getInstanceId();
	}

	public static String getThreadInfo() {
		final Thread ct = Thread.currentThread();
		return ct.getId() + "-'" + ct.getName() + "'";
	}

	public static String getApplicationId() {
		String id = SystemProperty.applicationId.get();
		if (id == null) {
			id = "devmode";
		}
		return id;
	}

	/**
	 * @return true if GAE self-reports that the datastore works fine.
	 */
	public static boolean canWriteDataStore() {
		final CapabilitiesService service = CapabilitiesServiceFactory.getCapabilitiesService();
		final CapabilityStatus status = service.getStatus(Capability.DATASTORE_WRITE).getStatus();

		if (status == CapabilityStatus.ENABLED) {
			return true;
		} else {
			// at least, we dont know for sure if it works
			return false;
		}
	}

	public static String inModeAsString() {
		return (inProduction() ? "inProduction" : "inDevelopment") + "-"
				+ (onAppEngine() ? "onAppEngine" : "notOnAppEngine") + "-"
				+ "module:" + ApiProxy.getCurrentEnvironment().getModuleId();
	}

	// TODO get os.environ REQUEST HASH

}
