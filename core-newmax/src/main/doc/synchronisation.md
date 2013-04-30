# Xydra Synchronisation

## The Sync Process

### Server
The server provides `XydraStore.executeCommands(..)` to execute a number of commands,
and `XydraStore.getEvents(..)` to get a part of the changeLog of model. 
The REST interface allows to do both in one batch command.

Background: Challenges on the server-side result mainly from guaranteeing transaction
 semantics while being used concurrently and while using a distributed simple
 key-value data store.

### Client
Booting is the process of starting the client before connecting to a server.
This is possible thanks to HTML5 AppCache and LocalStorage.

If this is the first start of the app: 
App loads a current snapshot from server, uses this
state as local model and runs the app from there. User makes local changes,
they are applied immediately on the model and the Client remembers which are new
and which are synced.

On subsequent starts of the app: 
Last state of the app is loaded, from localstorage, cookies or whatever. 

Technically, the client maintains for each model:

* the **CurrentState** of models, objects, fields and values, including revision numbers.
This state is used to answer queries such as 
"Does this object exist?", 
"What is the value of this field?", or
"What is the current revision number of this model?"
* a **ChangeLog** which is a list of all events so far.
* a **SyncRev**, the highest known revision for whichthe client can be sure, that
the local change log matches the server'S view.
* a list of **LocalChanges** that is a list of commands executed locally, 
but not yet committed to the server.
* currently registered **EventListenrs**

To allow working in offline-mode across browser restarts, this local state must be 
persisted, i.e. it must be serializable. All mentioned objects are serializable,
except the EventListeners, which need to be re-created on de-serialisation.

Invariants maintained by client:

* The model in CurrentState has the same current revision number (CurrentRev) 
  as the last event in the ChangeLog.
* SyncRev is always less than or equal to CurrentRev.
* All events in ChangeLog with a revision greater than SyncRev are also contained
  in the LocalChanges.


### Synchronisation from the Client Apps Perspective

* Certain GUI widgets currently show an indicator, e.g. a spinning icon, 
to signal that the currently visible state is not yet synchronized on a server.
* GUI freezes, synchronizer runs
* Synchronizer fires SyncEvents on MOFs (model, objects, fields) to indicate 
which local commands succeeded and which not. Success is here defined as "the requested
effect has materialised on the server while respecting your transaction semantics and
command modes". The CurrentState still looks like before the sync.
* Synchronizer changes CurrentState and ChangeLog.
* Synchronizer fires change events. The CurrentState looks already like it will look
  after all change events. Nevertheless, the change events fired are not in the order
  they happened, instead, they are in an order ideal for GUIS:
  * remove value events, if any
  * remove field events, if any
  * remove object events, if any
  * remove model events, if any
  * add model events, if any
  * add object events, if any
  * add field events, if any
  * add value events, if any
  * change value events, if any
  This order minimised the amount of widgets simultanously present in the GUI.
  It also puts potentially more visible changes (like adding entities) before more subtle
  changes (like changing values) , so the overall GUI should feel like updating quicker
  and doing re-renderings early.
* GUI becomes responsive again.

### Inside the Client-side Synchronizer
The core algorithm:

1. Lock the App for changes.
1. Prepare list of commands from LocalChanges
1. Call executeCommandsAndGetEvents with a callback
1. Wait for server response.
   IMPROVE Do first steps in the background and let app continue working until we 
   receive a callback from the server.
1. Callback fires
   * TODO Deal with Authentication and Authorisation  Errors
   * TODO Deal with Network Error
1. Find the longest mapping from LocalChanges and ServerEvents.
   If multiple such mappings exist, prefer the one that starts the earliest
   in the ServerChanges.
1. Sent all events from ServerEvents that were not mapped to EventDelta.
1. For each event in LocalChanges that was not mapped, send the inverse
   event also to EventDelta. Now EventDelta contains exactly the delta
   (represented as events) that needs to be sent to the local client state
   to change it so that it equals the state the server has after all
   ServerChanges are applied.
1. Fire sync-events.
   * If a sync-success event is fired, this implies the current revision of 
     the entity equals its syncRevision. 
   * If a sync-failed event is fired, this implies the desired event never happened
     on the server. This is an opportunity for the client to deal with failed
     commands. TODO Changes to the model are still allowed.  
1. Copy ServerChanges into local ChangeLog
1. Let EventDelta update the state of CurrentState
1. Update the revision numbers of CurrentState using all events in ServerChanges
1. Fire change events from EventDelta   
1. Release App-lock.    

## Event mapping
The core part of a sync algorithm is the event mapping, that is, given the set of locally
executed events and the set of remotely executed events, which local events should be
considered as 'happened' and which as 'failed'. In other words the task is:
Find a mapping from localChanges (pairs of command and event) to serverEvents to determine
which events should be considered "happened on server" and which "failed".

A serverEvent can be: 

* atomicEvent, 
* txnEvent caused from REMOVE (n implied events, followed by 1 non-implied),
* txnEvent caused from txnCommand.
 
Define: A **ChangeOperation** has a **Condition** and an **Effect**.
Every command can be transformed into a ChangeOperation. 
A ChangeOperation by the nature of its constructions eliminates redundant parts.
E.g. add(x), remove(x), add(x) has a combined effect of add(x).
Another example: A txn with two SAFE change commands change(x->y) and change(y->z) 
result in a change operation with condition value=x and effect value=z.

* The **Condition** is a set of AtomicConditions; each AtomicCondition can:

  * a certain entity must be present or absent.
  * a certain entity must have a certain revision, i.e. the one to be REMOVEd or CHANGEd.
  
  A condition matches if all entities that are bound by the condition 
  have the desired revision number.
  
  **Note:** *The algorithm described below does not use the conditions.*  The server 
  respects the clients command semantics and conditions under all circumstances. If, however,
  the client requested a SAFE remove, it might match a FORCED removed issued by another client.
  The end result is: The clients condition was honored, and the entity is removed.


* The **Effect** consists of AtomicEffects, each AtomicEffect is:

  * add an entity
  * remove an entity
  * set the value of a field
  
  Effects have no version number.
  
  An effect matches an entity state if
   
  * all child entities that should have been ADDed are present,
  * all child entities that should be REMOVEd are absent, and
  * all fields have the value they should have (as stated by the effect).
  
A **ChangeOperation** contains 1-n **AtomicChangeOperations**.
An AtomicChangeOperation has

* 0-1 AtomicCondition
* 1-n AtomicEffects
 
FORCEd commands result in an empty condition, as they have no case in which they fail
due to an unmatched pre-condition.

### Desired Properties of Sync Algorithms
There are several, sometimes mutually exclusive properties of sync algorithms that are
desirable:

* All local commands (atomic and transactions) should happen on the server
  * All or none
  * In the same sequence
  * Without any other commands coming in its way
  * No other events ever occurring in the change log except 'my own'
  
These strict properties can only work if a remote repository is used exclusively for
one user with one device. As soon as multiple clients are involved, the fulfilled properties
can at best be:

* All local commands (atomic and transactions) should happen on the server
  * All or none
  * In the same sequence
  * Without any other commands coming in its way

In order to minimize sync conflicts we can demand from the client that all commands that
must be executed together must be in the same transaction. Then the relaxed properties
of the sync algorithm itself are:

* All local commands (atomic and transactions) should happen on the server
  * All, some, or none
  * In the same sequence

Let's call this variant **sequence-stable + partial**. Partial, because we now allow
some local commands to succeed and others to fail.
This version guarantees that of all states that were experienced on the client, those that
were executed successfully on the server too, happened in the same order in the change log.
Hence, later executed undo commands can travel back in time in the same order as experienced
by the user that executed the commands initially.

To implement this algorithm one can use variants of minimum-edit-distance.

If we demand only that all states and values can be found in the change log - but ignoring 
the order in which they happen - we can further relax the constraints. This would guarantee
that a long document stored in a value of a field can always be retrieved from the change log,
but the overall state of an object probably not. The relaxed constraints are:

* All local commands (atomic and transactions) should happen on the server
  * All, some, or none

Let's call this version the **unordered + partial** version. It's easy to implement.



### Algorithm Common Data Structures

The algorithm uses these classes:

ChangeOperation

* Set<AtomicEffect> = new HashSet()
* Long latestTimeWhereEffectFulfilled = null
* Set<XEvent> takenEvents = new HashSet()
* Boolean success = null;

AtomicEffect

* XAddress entity (that was added, removed or changed; must be a field for CHANGE effect)
* ChangeType (ADD, REMOVE, CHANGE) -- no TRANSACTION here 
* XValue (the new value for a CHANGE effect, or null for other effects)

Set<XEvent> allTakenEvents = new HashSe();

Algorithm:

* Start with the sync-state, i.e. the state with the revision = syncRevision.
* for each state(t) resulting from applying a serverEvent(t) (starting with none):
  * for each ChangeOperation co
    * if (co.effect.matches(state(t))
      * co.latestTimeWhereEffectFulfilled = t;

* for each ChangeOperation:
  * if (co.latestTimeWhereEffectFulfilled)
    * // cool
  * else: mark as failed; remove from list; cannot have happened.

### Algorithm for Unordered + Partial

// greedy:

* sort ChangeOperations by number of atomicEffects (= atomicEvents required), largest first
* for each cool ChangeOperation:
  * for each atomicEffect : co.atomicOperations:
    * for t = 0; t <= co.latestTimeWhereEffectFulfilled; t++:
      * for serverEvent in state(t).events:  (atomic events)
        * if atomicEffect.matches(serverEvent):
          * co.takenEvents.add(serverEvent)
          * allTakenEvents.add(serverEvents)
      * if all effects matched: // keep events marked and success
        * co.success = True
      * else: mark events as not-taken and fail
        * allTakenEvents.removeAll( co.takenEvents );
        * co.takenEvents.clear();
        * co.success = False

define: effect.matches(event):

* if the desired entity was ADDed or REMOVEd or a field has now the desired value => match


## The Old Synchronizer
To compare this with the old way, here we describe the old Synchronizer code:
 
During the whole sync-process the model and hence the GUI need to be locked
to avoid race conditions and maintain consistency. This are the steps. Lets
assume the last time we sync'ed from the server was 33 and now we are locally
at revision 52. Thus we have 19 uncommitted changes. Some of these might be
transactions, containing several commands each. This process happens in
several phases:

* Syncer calls executeCommandsAndGetEvents on server with all locally
  pending commands
* For each command, the corresponding callback is called: onSuccess or
  onFaillure. This is an opportunity for the client to deal with failed
  commands. Changes to the model are still allowed.
* Now the model is locked. 
* Syncer does a roll-back for the locally pending
  changes. This is simply executing the pending changes in reverse. More
  precisely, its executing commands which have the opposite effect of the
  events that happended already. I.e. if an object was added locally, there is
  an add-object-command in the local changes. Additionally, the local model
  contains an add-object-event. By executing a remove-object-event the effect
  of the add-object-command can be neutralized. Of course, if the local
  sequence of uncommited events is A, B, C, then the syncer executes anti-C,
  anti-B and anti-A in this order. During the whole rollback, sending events is
  stopped. 
  The whole purpose is a memory efficient way to get a complete
  picture of the state of the model as it was at revision 33 (the
  sync-revisions).
* Syncer applies the events from the server. These include the events that
  result from executing successful commands, but contain additionally the
  events caused by other users.
* Syncer remembers which events had happened locally already and suppresses
  sending them again. All other new events are called at the respective change
  listeners.
* For each entity that was changed, the syncer observes if the entities
  local revision is currently the same as the server-side revisions, indicating
  that there are no pending changes on the client. If this is the case, the
  persistence change listener is called. The app can now remove the
  "in progress" spinners or other such GUI elements and indicate to the user
  that her changes have in fact been persisted successfully on the server.
 

TODO Role of relative revision numbers

