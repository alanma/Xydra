package org.xydra.client;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.store.AccessException;


/**
 * An interface for interacting with the "/data" API on remote CXM servers.
 * 
 * Errors while executing the operation (except those caused by illegal
 * arguments) are passed to the callback's {@link Callback#onFailure(Throwable)}
 * method. With the exception of {@link AccessException}, foreseeable modes of
 * failure should be mapped to a subclass of {@link ServiceException}.
 * 
 * Any unauthorized operation will result in a {@link AccessException} being
 * passed to the callback's {@link Callback#onFailure(Throwable)}.
 * 
 * The callback may or may not be called before the method returns.
 * 
 * TODO The server should implement this interface, too (and interface belongs
 * to server project)
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
		 * command.
		 * 
		 * FIXME XCommand.CHANGED does not exist. Should it?
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
	 * @param entity Address of the model or object to get changes from, must
	 *            not be null. If the modelId is not set, no events can be
	 *            retrieved, since must be {@link #NONE} and command must be a
	 *            {@link XRepositoryCommand}. If modelId is set, the commands
	 *            target must match that modelId. If objectId is set, the
	 *            commands target must match objectId.
	 * @param command The command to execute.
	 * @param since Revision of the first event to get. {@link #NONE} if only
	 *            the event caused by the given command is wanted.
	 * @param callback The callback to receive the result and events. If the
	 *            command changes something (result >= 0), the resulting event
	 *            will always be the last in the list.
	 */
	void executeCommand(XAddress entity, XCommand command, long since,
	        Callback<CommandResult> callback);
	
	/**
	 * Get events from a model.
	 * 
	 * @param entity Address of the model or object to get changes from, must
	 *            not be null.
	 * @param since Revision of the first event to get. NONE for no lower limit.
	 * @param until Revision of the last event to get. NONE for no upper limit.
	 * @param callback The callback to receive the events.
	 * @param context The address prefix used for the constructing the received
	 *            events.
	 */
	void getEvents(XAddress entity, long since, long until, Callback<List<XEvent>> callback,
	        XAddress context);
	
}
