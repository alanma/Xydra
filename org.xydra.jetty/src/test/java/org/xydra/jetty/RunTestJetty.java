package org.xydra.jetty;

import java.io.File;
import java.net.URI;

import org.xydra.conf.IConfig;
import org.xydra.env.Env;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

/**
 * This class is starts a Jetty server configured to allow testing of the
 * webapp, loading static files directly from source code. This class is not
 * required to run the webapp.
 *
 * <p>
 * If only static files have been modified, no call is neccesary as this Jetty
 * is configured to load resources directly from src/test/resources
 *
 * @author xamde
 *
 */
public class RunTestJetty {

	private static final Logger log = LoggerFactory.getLogger(RunTestJetty.class);

	public static void main(final String[] args) throws Exception {
		final Jetty jetty = new Jetty();

		final IConfig conf = Env.get().conf();
		new ConfParamsJetty().configureDefaults(conf);
		conf.set(ConfParamsJetty.DOC_ROOT, new File("src/test/resources").toURI().toURL()
				.toExternalForm());
		jetty.configureFromConf(conf);

		final URI uri = jetty.startServer();
		log.info("Started embedded Jetty server. User interface is at " + uri.toString());

	}
}
