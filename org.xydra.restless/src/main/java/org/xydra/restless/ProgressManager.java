package org.xydra.restless;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Handles distribution of progress information while uploads are in progress.
 * 
 * ProgressTokens should be unique and unguessable. There is a public REST
 * end-point to retrieve progress messages.
 * 
 * @author xamde
 */
public class ProgressManager {

	public static interface IProgressBroker {

		/**
		 * @param progessToken
		 * @param progressMessage
		 */
		void putProgress(String progessToken, String progressMessage);

		/**
		 * @param progessToken
		 * @return @CanBeNull if progressToken is wrong or no progress message
		 *         has been set yet
		 */
		String getProgress(String progessToken);

	}

	/** Cloud users should use some distributed progress broker here */
	public static IProgressBroker PROGRESS_BROKER = new SingleMachineProgressBroker();

	public static final class SingleMachineProgressBroker implements IProgressBroker {

		private Map<String, String> map = new HashMap<String, String>();

		@Override
		public void putProgress(String progessToken, String progressMessage) {
			this.map.put(progessToken, progressMessage);
		}

		@Override
		public String getProgress(String progessToken) {
			return this.map.get(progessToken);
		}

	}

	public static synchronized void restless(Restless restless) {
		restless.addMethod("/_uploadProgress", "GET", ProgressManager.class, "getProgress", false,

		new RestlessParameter("progressToken")

		);
	}

	/**
	 * REST-exposed via Restless
	 * 
	 * @param progressToken
	 * @throws IOException
	 */
	public static void getProgress(HttpServletResponse res, String progressToken)
			throws IOException {
		String progressMessage = PROGRESS_BROKER.getProgress(progressToken);
		res.setContentType("text/plain; charset=utf8");
		Writer w = res.getWriter();
		w.write("" + progressMessage);
		w.close();
	}

}
