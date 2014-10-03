package org.xydra.testgae;

import org.xydra.conf.IConfig;
import org.xydra.env.Env;
import org.xydra.jetty.ConfParamsJetty;
import org.xydra.jetty.Jetty;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.Restless;
import org.xydra.xgae.gaeutils.GaeTestfixer;

import java.net.URI;

/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from src/main/webapp. This class is not
 * required to run the webapp.
 * 
 * @author voelkel
 */
public class RunXmasJetty {

	private static final Logger log = LoggerFactory.getLogger(RunXmasJetty.class);

	public static void main(String[] args) throws Exception {
		/*
		 * Enable tests with GAE (especially mail)
		 */
		GaeTestfixer.enable();
		/* Make this thread GAE-test-ready */
		GaeTestfixer.initialiseHelperAndAttachToCurrentThread();

		Restless.DELEGATE_UNHANDLED_TO_DEFAULT = true;

		CopyGwt.copyGwt();

		// start jetty
		Jetty jetty = new Jetty();

		IConfig conf = Env.get().conf();
		new ConfParamsJetty().configure(conf);

		conf.setLong(ConfParamsJetty.PORT, 8787);
		conf.set(ConfParamsJetty.DOC_ROOT, "src/main/webapp");
		URI uri = jetty.startServer();

		log.info("Started embedded Jetty server. User interface is at " + uri.toString());
	}
}
