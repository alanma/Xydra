package org.xydra.store;

import org.xydra.base.change.XCommand;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.sync.XSynchronizer;


/**
 * <h2>Documentation for the sync-process</h2>
 * 
 * The server provides
 * {@link XydraStore#executeCommands(org.xydra.base.XId, String, org.xydra.base.change.XCommand[], Callback)}
 * and
 * {@link XydraStore#getEvents(org.xydra.base.XId, String, GetEventsRequest[], Callback)}
 * - the REST interface allows to do both in one batch command.
 * 
 * <h3>Server-side</h3> Let's take a closer look what happens here. Firs we look
 * at it from the server's perspective:
 * 
 * <ol>
 * <li>The client submits a bunch of commands,</li>
 * <li>Authentication or server errors are already reported as
 * {@link XydraStore}-exceptions.</li>
 * <li>For each command: The server looks if the client is authorised to do
 * this, and if so, executes the command. For each such command, the resulting
 * revision number is remembered. Error codes are also encoded as revisions
 * numbers, such as {@link XCommand#FAILED} and {@link XCommand#NOCHANGE}.</li>
 * <li>Next, the server loads the requested events and returns them to the
 * client. Only successful commands result in events. If there is no change,
 * nothing happens and there is no event in the world.</li>
 * </ol>
 * Challenges on the server-side result mainly from guaranteeing transaction
 * semantics while being used concurrently and while using a distributed simple
 * key-value data store.
 * 
 * <h3>Client-side</h3> On the client-side, things are a little more
 * complicated.
 * 
 * There are two main components involved in syncing: The {@link XSynchronizer}
 * (aka "syncer") and application logic ("app" for short").
 * 
 * First start of the app: App loads a current snapshot from server, uses this
 * state as local model and runs the app from there. User makes local changes,
 * they are applied immediately on the model and the {@link MemoryModel}
 * remembers which are new and which are synced.
 * 
 * Each run of the app: Each command that is executed against the model has a
 * callback, which will be called later once the change is on the server. If the
 * browser dies right now, those callbacks should be remembered by the app. The
 * changed model (of which the local changes contain the locally applied but
 * not-yet-commited commands) need to be persisted after each action as well.
 * 
 * n-th start of the app: Last state of the app is loaded, from localstorage,
 * cookies or whatever. If callbacks for pending commands had been registered,
 * they need to be recreated now as well.
 * 
 * <h4>Syncing</h4> one can register add the {@link XSynchronizesChanges}
 * -enabled entities (= client-side model, object and field) and gets events
 * when an entity is currently in a fully persisted state.
 * 
 * During the whole sync-process the model and hence the GUI need to be locked
 * to avoid race conditions and maintain consistency. This are the steps. Lets
 * assume the last time we sync'ed from the server was 33 and now we are locally
 * at revision 52. Thus we have 19 uncommitted changes. Some of these might be
 * transactions, containing several commands each. This process happens in
 * several phase:
 * <ol>
 * <li>Syncer calls executeCommandsAndGetEvents on server with all locally
 * pending commands</li>
 * <li>For each command, the corresponding callback is called: onSuccess or
 * onFaillure. This is an opportunity for the client to deal with failed
 * commands. Changes to the model are still allowed.</li>
 * <li>Now the model is locked. Syncer does a roll-back for the locally pending
 * changes. This is simply executing the pending changes in reverse. More
 * precisely, its executing commands which have the opposite effect of the
 * events that happended already. I.e. if an object was added locally, there is
 * an add-object-command in the local changes. Additionally, the local model
 * contains an add-object-event. By executing a remove-object-event the effect
 * of the add-object-command can be neutralized. Of course, if the local
 * sequence of uncommited events is A, B, C, then the syncer executes anti-C,
 * anti-B and anti-A in this order. During the whole rollback sending events is
 * stopped. The whole purpose is a memory efficient way to get a complete
 * picture of the state of the model as it was at revision 33 (the
 * sync-revisions).</li>
 * <li>Syncer applies the events from the server. These include the events that
 * result from executing successful commands, but contain additionally the
 * events caused by other users.</li>
 * <li>Syncer remembers which events had happened locally already and suppresses
 * sending them again. All other new events are called at the respective change
 * listeners.</li>
 * <li>For each entity that was changed, the syncer observes if the entities
 * local revision is currently the same as the server-side revisions, indicating
 * that there are no pending changes on the client. If this is the case, the
 * persistence change listener is called. The app can now remove the
 * "in progress" spinners or other such GUI elements and indicate to the user
 * that her changes have in fact been persisted successfully on the server.</li>
 * </ol>
 * 
 * 
 * <ol>
 * <li></li>
 * </ol>
 * 
 * TODO Role of relative revision numbers
 * 
 * 
 * @author xamde
 * 
 */
public interface SynDocu {
    
}
