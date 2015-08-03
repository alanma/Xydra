package org.xydra.restless.gaedemo;

import org.xydra.restless.IRestlessContext;
import org.xydra.restless.Restless;
import org.xydra.restless.RestlessExceptionHandler;

public class LiveDemoApp {

	public void restless(final Restless r, final String path) {
		TimeResource.restless(r);

		/** a rather useless exception handler */
		r.addExceptionHandler(new RestlessExceptionHandler() {

			@Override
			public boolean handleException(final Throwable t, final IRestlessContext restlessContext) {
				System.err.println("Restless error");
				throw new RuntimeException("Something went wrong for URI: "
						+ restlessContext.getRequest().getRequestURI(), t);
			}

		});

	}

}
