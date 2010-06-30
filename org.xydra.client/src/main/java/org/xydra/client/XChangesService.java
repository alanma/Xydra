package org.xydra.client;

import java.util.List;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XID;



/**
 * An interface for interacting with the "/data" API on remote CXM servers.
 */
public interface XChangesService {
	
	public static final long NONE = -1;
	
	public class CommandResult {
		
		private final long result;
		private final List<XEvent> events;
		
		public CommandResult(long result, List<XEvent> events) {
			this.result = result;
			this.events = events;
		}
		
		/**
		 * The same as would have been returned if executing the command
		 * directly at a local repository, model, object or field via
		 * XExecutesCommands#executeCommand(): {@link XCommand#FAILED} if the
		 * command failed, {@link XCommand#NOCHANGE} if the command didn't
		 * change anything or the revision number of the event caused by the
		 * command (or {@link XCommand#CHANGED} for repository commands).
		 */
		public long getResult() {
			return this.result;
		}
		
		public List<XEvent> getEvents() {
			return this.events;
		}
		
	}
	
	/**
	 * Execute a command and get events since a specific revision.
	 * 
	 * @param command The command to execute.
	 * @param since Revision of the first event to get. NONE if only the event
	 *            caused by the given command is wanted.
	 * @param callback The callback to receive the result and events. If the
	 *            command changes something (result >= 0), the resulting event
	 *            will always be the last in the list.
	 */
	void executeCommand(XCommand command, long since, Callback<CommandResult> callback);
	
	/**
	 * Get events from a model.
	 * 
	 * @param repoId RepoId to fill in when de-serializing the commands.
	 * @param modelId ID of the model to get changes from, must not be null.
	 * @param since Revision of the first event to get. NONE for no lower limit.
	 * @param until Revision of the last event to get. NONE for no upper limit.
	 * @param callback
	 */
	void getEvents(XID repoId, XID modelId, long since, long until, Callback<List<XEvent>> callback);
	
}
