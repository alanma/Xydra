package org.xydra.restless;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.restless.IMultipartFormDataHandler.IProgressReporter;

/**
 * Handles distribution of progress information while uploads are in progress.
 *
 * ProgressTokens should be unique and unguessable. There is a public REST end-point to retrieve progress messages.
 *
 * See also {@link IProgressReporter}
 *
 * @author xamde
 */
public class ProgressManager {

	private static final IProgressReporter LOG_PROGRESS_REPORTER = new IProgressReporter() {

		@Override
		public void reportProgress(final String progressMessage) {
			log.debug("PROGRESS: " + progressMessage);
		}
	};

	/**
	 * Create an {@link IProgressReporter} which sends progress to the {@link ProgressManager#DEFAULT_PROGRESS_BROKER}
	 *
	 * @param progressToken @CanBeNull and results in a default progress reporter on the log of {@link ProgressManager}
	 *        at debug level
	 * @return a new {@link IProgressReporter} @CanBeNull if progressToken is null
	 */
	public static IProgressReporter createDefaultProgressReporter(final String progressToken) {
		if (progressToken == null) {
			return LOG_PROGRESS_REPORTER;
		}

		return new IProgressReporter() {

			@Override
			public void reportProgress(final String progressMessage) {
				ProgressManager.DEFAULT_PROGRESS_BROKER.appendProgress(progressToken, progressMessage);
			}
		};
	}

	public static interface IProgressBroker {

		/**
		 * Append new message, keep existing log, separated by CR-LF
		 *
		 * @param progessToken
		 * @param progressMessage
		 */
		void appendProgress(String progessToken, String progressMessage);

		/**
		 * Replace existing message
		 *
		 * @param progressToken
		 * @param progressMessage
		 */
		void setProgress(String progressToken, String progressMessage);

		/**
		 * @param progessToken
		 * @return @CanBeNull if progressToken is wrong or no progress message has been set yet
		 */
		String getProgress(String progessToken);

	}

	/**
	 * Cloud users should use some distributed progress broker here
	 *
	 * FIXME implement auto-purge after some time-out
	 */
	public static IProgressBroker DEFAULT_PROGRESS_BROKER = new SingleMachineProgressBroker();

	private static final Logger log = LoggerFactory.getLogger(ProgressManager.class);

	public static final class SingleMachineProgressBroker implements IProgressBroker {

		private final Map<String, String> map = new HashMap<String, String>();

		@Override
		public void appendProgress(final String progressToken, final String progressMessage) {
			if (log.isDebugEnabled()) {
				log.debug("PROGRESS(" + progressToken + "): " + progressMessage);
			}

			final String recordedProgress = this.map.get(progressToken);
			if (recordedProgress == null) {
				this.map.put(progressToken, progressMessage);
			} else {
				final String combinedProgress = recordedProgress + "\n\r" + progressMessage;
				this.map.put(progressToken, combinedProgress);
			}
		}

		@Override
		public void setProgress(final String progressToken, final String progressMessage) {
			if (log.isDebugEnabled()) {
				log.debug("PROGRESS(" + progressToken + "): " + progressMessage);
			}

			this.map.put(progressToken, progressMessage);
		}

		@Override
		public String getProgress(final String progessToken) {
			return this.map.get(progessToken);
		}

	}

	public static synchronized void restless(final Restless restless) {
		restless.addMethod("/_uploadProgress", "GET", ProgressManager.class, "getProgress", false,

				new RestlessParameter("progressToken")

				);
	}

	public static final String NO_MESSAGE = "NO_MESSAGE";

	public static final String SUCCESS = "SUCCESS";

	public static final String ERROR = "ERROR";

	/**
	 * REST-exposed via Restless.
	 *
	 * Sends "NO_MESSAGE" if there is no message.
	 *
	 * @param progressToken
	 * @throws IOException
	 */
	public static void getProgress(final HttpServletResponse res, final String progressToken) throws IOException {
		String progressMessage = DEFAULT_PROGRESS_BROKER.getProgress(progressToken);

		if (progressMessage == null) {
			progressMessage = NO_MESSAGE;
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				throw new RuntimeException("Error", e);
			}
		}

		res.setContentType("text/plain; charset=utf8");
		final Writer w = res.getWriter();
		w.write("" + progressMessage);
		w.close();
	}

	/**
	 * Utility method
	 *
	 * @param progressReporter @CanBeNull in which case no progress is reported anywhere
	 * @param string
	 */
	public static void reportProgress(final IProgressReporter progressReporter, final String progressMessage) {
		if (progressReporter != null) {
			progressReporter.reportProgress(progressMessage);
		}
	}

	/**
	 * Utility method
	 *
	 * @param progressReporter @CanBeNull in which case no progress is reported anywhere
	 * @param success iff true, reports {@link #SUCCESS} else {@link #ERROR}
	 */
	public static void reportProgressDone(final IProgressReporter progressReporter, final boolean success) {
		reportProgress(progressReporter, success ? ProgressManager.SUCCESS : ProgressManager.ERROR);
	}

	/**
	 * Includes reporting progressDone=FALSE
	 *
	 * @param pr
	 * @param e
	 */
	public static void reportException(final IProgressReporter pr, final Throwable e) {
		if (pr == null) {
			return;
		}

		pr.reportProgress("Exception " + e);

		log.warn("Exception: ", e);

		reportProgressDone(pr, false);
	}

}
