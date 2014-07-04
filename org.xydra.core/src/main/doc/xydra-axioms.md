# Xydra Core Documentation - The Axioms
See also XydraPersistence and XydraStore.

## Xydra Axioms 

### The Four Entities
**Axiom: Repository, Model, and Object can contain 0-n child entities.**

**Axiom: Model, Object, Field and Value entities can be serialised.**

That means:

* Repository: contains Models; can not be serialized
* Model: contains Objects; can be serialised
* Object: contains Fields; can be serialised
* Field: contains 0 or 1 Value; can be serialised
* Value: cannot be changed; can be serialised

### Revision Numbers
**Axiom: Whenever a Xydra entity changes, it revision number increases.**
Sometimes by more than exactly 1. 
Revision numbers never decrease.

**Axiom: Whenever an entity changes, its new revision number is also** 
**propagated to its father-entity (recursively).**

**Axiom: A revision number of -1 signals a non-existing entity.**
**An entity that does exist has at least the revision number 0.**

As a corollary from first and second axiom, the revision number of a model is equal to the highest
revision number of any of its objects. Which in turn is equal to the highest revision
number of its fields.

As a corollary from first and third axiom, an entity that gets deleted and re-created has a revision
number > 0.

### Addressing
Models, Objects, and Fields are addressed by Ids, which when chained together form
Addresses. Ids are strings that are also legal XML element names. Names starting with
an underscore are reserved for internal purposes.

### ChangeLog
Each Model, Object and Field has a ChangeLog from which previous versions can be
reconstructed. A ChangeLog is a list of events that happened.

### Commands and Events
Repositories, Models, Objects and Fields can be watched, i.e. one can add event
listeners to them. 

**Axiom: On Repositories, Models, Objects and Fields one can execute Commands.**

**A command can be executed in three modes:**

* **FORCED**: Cares about the post-condition
* **SAFE state-bound**: Cares about the pre-condition state
* **SAFE revision-bound**: Cares about the pre-condition revision number. 
This is the most careful mode.

**The result of a command can be:**

* **FAILED**: Nothing changed and this is considered a problem.
* **NOCHANGE**: Nothing changed, and this is considered OK.
* **Success** = resulting new revision number, and this is considered OK.

The effect of the modes is best given by examples. 
Consider the following ADD-Object commands.
The revision number -1 denotes the entity does not exist.

<table border="1" style="border: 1px solid black;">
<tr>
<th>Mode</th>
<th>ChangeType</th>
<th>BEFORE: Revision of model / object</th>
<th>Returned Command result</th>
<th>AFTER: Revision of model / object</th>
</tr>

<tr>
<td>FORCED</td>
<td>ADD</td>
<td>42/-1</td>
<td>SUCCESS</td>
<td>43/43</td>
</tr>

<tr>
<td>FORCED</td>
<td>ADD</td>
<td>42/23</td>
<td>SUCCESS</td>
<td>42/23</td>
</tr>

<tr>
<td>SAFE state-bound</td>
<td>ADD</td>
<td>42/-1</td>
<td>SUCCESS</td>
<td>43/0</td>
</tr>

<tr>
<td>SAFE state-bound</td>
<td>ADD</td>
<td>42/23</td>
<td>FAILED</td>
<td>42/23</td>
</tr>

<tr>
<td>SAFE revision-bound to rev 23</td>
<td>ADD</td>
<td>42/-1</td>
<td>FAILED</td>
<td>42/-1</td>
</tr>

<tr>
<td>SAFE revision-bound to rev 23</td>
<td>ADD</td>
<td>42/17</td>
<td>FAILED</td>
<td>43/17</td>
</tr>

<tr>
<td>SAFE revision-bound to rev 23</td>
<td>ADD</td>
<td>42/23</td>
<td>SUCCESS</td>
<td>43/43</td>
</tr>

<tr>
<td>SAFE revision-bound to rev 23</td>
<td>ADD</td>
<td>42/39</td>
<td>FAILED</td>
<td>43/39</td>
</tr>

</table>

Possible commands are:

* XRepositoryCommand, can be executed on a Repository to ADD or REMOVE a Model.
* XModelCommand, can be executed on a Model to ADD or REMOVE an Object.
* XObjectCommand, can be executed on an Object to ADD or REMOVE a Field.
* XFieldCommand, can be executed on a Field to ADD, REMOVE or CHANGE a Value.
* XTransaction, a list of the commands listed above.

### Transactions
**Axiom: A transaction is a list of Commands. It contains at least 1 Command.**

**Axiom: A transaction either completely succeeds or completely fails (atomicity).**

**Axiom: Xydra allows executing transactions on Models and Objects.**

**Axiom: A transaction is scoped. 
A transaction may only change entities in the same address space as the entity
on which the transaction is executed.**
In other words, a transaction executed on an Object can only change that object, 
its fields and values of fields within the object. 
It cannot created, remove or change other objects. 
For that, use a transaction on a model.

Transactions are not required to be minimal, i.e. it is allowed to have a 
transaction that just adds and removes a field.

Executing an XTransaction (which contains 1-n XCommands) results in an 
XTransactionEvent (which contains 1-n XEvents).

### Rules for Commands
**Axiom: After a REMOVE Command Xydra asserts that all child entities are also REMOVED.**

If an entity has no children, the result of executing a single, e.g., REMOVE object command,
is a single XObjectEvent.

However, if an entity has children when being removed, the result is an 
XTransactionEvent. Inside, this event has a list of REMOVE events which are reported
in an order so that the ends of the tree are removed first. I.e. first all values are
removed, then the fields, then the object and if requested finally the model itself.
All events except the last one are IMPLIED events, and they are marked as such.


Fin.
