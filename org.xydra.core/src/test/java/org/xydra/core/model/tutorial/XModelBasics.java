package org.xydra.core.model.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xydra.core.X;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.change.ChangeType;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XModelCommand;
import org.xydra.core.change.XModelEvent;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectCommand;
import org.xydra.core.change.XObjectEvent;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryCommand;
import org.xydra.core.change.XRepositoryEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.model.state.impl.memory.TemporaryStateStore;
import org.xydra.core.value.XValue;


/**
 * <h2>Welcome to XModel</h2> Before we can actually use XModel to build a
 * little sample application, we first have to understand the basic concepts and
 * ideas behind XModel and how to use them. This class tries to give a little
 * overview about XModel and its components.
 * 
 * This class uses JUnit to show that the demonstrated functions actually work.
 * 
 * <h2>Tutorial</h2>
 * 
 * XModel consists of a basic nested structure with 5 elements, these are:
 * <dl>
 * <dt>XRepository</dt>
 * <dd>An {@link XRepository} basically is a container for different XModels.
 * Its purpose is to organize different XModels</dd>
 * 
 * <dt>XModel</dt>
 * <dd>An {@link XModel} is the basic element which represents a specific
 * concept of the application, for example it could be used to organize the
 * different entries in an address book application.</dd>
 * 
 * <dt>XObject</dt>
 * <dd>An {@link XObject} is an "entry" in an XModel, for example it could be
 * used to represent a person in an address book application.</dd>
 * 
 * <dt>XField</dt>
 * <dd>An {@link XField} represents an attribute of its XObject, for example the
 * name-field of a person in an address book application.</dd>
 * 
 * <dt>XValue</dt>
 * <dd>An {@link XValue} holds the value of a given XField, for example the
 * actual string of the name-field of a person in an address book application.</dd>
 * </dl>
 * 
 * @author Kaidel
 */
public class XModelBasics {
	
	@BeforeClass
	public static void init() {
		XSPI.setStateStore(new TemporaryStateStore());
	}
	
	/**
	 * To create an XModel you first have to get an XRepository to hold it, this
	 * can be done by using the class "X", which basically holds the most basic
	 * functions that are needed to build an XModel application.
	 */
	@Test
	public void testBasicStructure() {
		
		// creating a new repository
		XRepository repository = X.createMemoryRepository();
		
		/*
		 * Every XModel needs a specific XID. You can either create an XID from
		 * a given string or use the XIDProvider to create a random and unique
		 * XID.
		 */

		// getting the XIDProvider
		XIDProvider xidProvider = X.getIDProvider();
		
		// creating an ID from a string
		XID stringID = xidProvider.fromString("ExampleModel");
		
		// creating a random & unique ID
		xidProvider.createUniqueID();
		
		/*
		 * If you want to create a new XModel you also have to specify who or
		 * what creates the XModel by passing a so called actor ID, which also
		 * is an XID
		 */
		XID actorID = xidProvider.fromString("ExampleActor");
		
		// actually creating the model
		XModel model = repository.createModel(actorID, stringID);
		
		/*
		 * Now that we have an XModel we can start to create XObjects. Just like
		 * XModels an XObject needs an XID. In fact, every part of the XModel
		 * structure, except the XValues, needs an XID. Even the repository has
		 * an XID, but we didn't have to set in manually since we used X to
		 * create it.
		 * 
		 * Furthermore every entity also has an XAddress. An XAddress is just a
		 * list of XIDs that basically specifies the path in which the entity
		 * lies. For example, consider the following situation:
		 * 
		 * An XRepository with ID "repo" that holds an XModel with ID "model",
		 * which holds an XObject with ID "object". The XAddress of that XObject
		 * would be "/repo/model/object/-" (represented as a string), the last
		 * field is a "-" because were referring to an object and the last field
		 * always specifies an XField.
		 */

		// creating the XObject
		XID objectID = xidProvider.fromString("ExampleObject");
		model.createObject(actorID, objectID);
		
		/*
		 * We forgot to save the XObject into a variable! So how can we get a
		 * hold of it? Simple, just use its XID!
		 */

		XObject object = model.getObject(objectID);
		
		/*
		 * Now that we have an object we can add fields to it, which basically
		 * works the same as creating an XObject.
		 */

		XID fieldID = xidProvider.fromString("ExampleField");
		XField field = object.createField(actorID, fieldID);
		
		/*
		 * Setting the value of a field is just as simple, but first we have to
		 * create a new XValue using the XValueFactory. The are many different
		 * XValue types, as an example we'll create a String value and pass it
		 * to the field.
		 */

		XValue stringValue = X.getValueFactory().createStringValue("StringValue");
		field.setValue(actorID, stringValue);
		
		/*
		 * Please note that the value-type of an XField is not fixed. We could
		 * change the value-type of our XField to an Integer, if we wanted to.
		 */

		XValue integerValue = X.getValueFactory().createIntegerValue(42);
		field.setValue(actorID, integerValue);
		
		/*
		 * Removing any part of our XModel structure is just as easy.
		 */

		// removing our field from our object
		object.removeField(actorID, fieldID);
	}
	
	/**
	 * Instead of manually manipulating the XModel structure we can encapsulate
	 * our commands in so called XCommands, send them to our XModel and let them
	 * be executed.
	 * 
	 * That essentially enables us to send commands over a network or build
	 * atomic transactions.
	 */
	@Test
	public void testUsingCommands() {
		
		// setting up an actor ID
		XID actorID = X.getIDProvider().fromString("TutorialActor");
		
		// getting an XRepository
		XRepository repository = X.createMemoryRepository();
		XID repositoryID = repository.getID();
		
		/*
		 * We want to add a model to our repository. We could either achieve
		 * this by manually creating the XModel, as seen in the previous method
		 * basicStructure() or by creating an XCommand and pass it to the
		 * repository.
		 * 
		 * To create a new XCommand we can use the XCommandFactory.
		 */
		XCommandFactory commandFactory = X.getCommandFactory();
		
		/*
		 * An XCommand can be executed in a <em>safe</em> manner, i.e. it
		 * considers the state of the entity you want to change and will only be
		 * executed if it is okay, or you can mark it as a forced Command which
		 * means, that it will be executed no matter what. Every XCommand
		 * creation method of our XCommandFactory allows us to specify whether
		 * the command is forced or not by passing a boolean variable called
		 * isForced.
		 * 
		 * Lets suppose we'd want to create a new XModel, add 2 XObjects, remove
		 * one of the XObejcts again, add an XField to the remaining and set its
		 * value to a Double value.
		 * 
		 * Here's how we'd to the above using XCommands:
		 */

		XID modelID = X.getIDProvider().createUniqueID();
		XRepositoryCommand repositoryCommand = commandFactory.createAddModelCommand(repositoryID,
		        modelID, false);
		
		// execute the command, therefore adding an XModel
		repository.executeCommand(actorID, repositoryCommand);
		
		XModel model = repository.getModel(modelID);
		assertNotNull(model);
		
		// FIXME max: consider using adapter pattern for transactions
		// ITransactionAdapter t = model.getAdapter(ITransactionAdapter.class);
		
		// adding the objects
		XID object1ID = X.getIDProvider().createUniqueID();
		XID object2ID = X.getIDProvider().createUniqueID();
		
		XModelCommand modelCommand1 = commandFactory.createAddObjectCommand(repositoryID, modelID,
		        object1ID, false);
		XModelCommand modelCommand2 = commandFactory.createAddObjectCommand(repositoryID, modelID,
		        object2ID, false);
		
		model.executeCommand(actorID, modelCommand1);
		model.executeCommand(actorID, modelCommand2);
		
		// lets see if the objects were actually added :)
		XObject object1 = model.getObject(object1ID);
		XObject object2 = model.getObject(object2ID);
		
		assertNotNull(object1);
		assertNotNull(object2);
		
		/*
		 * To get a better understanding of how XCommands work we need to now
		 * about revision numbers. Every entity of XModel has a revision number
		 * that increases if someone changes its properties, for example the
		 * revision number of an XModel increases if someone adds or deletes one
		 * of its XObjects. This can be used for versioning. An safe XCommand
		 * can only be executed if it "fits" to the given entity.
		 * 
		 * Safe Add Commands: Add Commands can only be executed if the entity
		 * that you want to add doesn't already exist.
		 * 
		 * Safe Remove and Safe Change Commands: These commands can only be
		 * executed if the command you send refers to the right version of the
		 * entity you want to change, this is because the entity might have
		 * changed significantly since you last checked it and the change you
		 * want to execute might not be okay or wanted any more. This is why you
		 * have to specify the revision number of the entity you want to modify
		 * in these commands.
		 */

		// lets remove object2
		XModelCommand removeObject2Command = commandFactory.createRemoveObjectCommand(repositoryID,
		        modelID, object2ID, object2.getRevisionNumber(), false);
		
		model.executeCommand(actorID, removeObject2Command);
		
		// lets see if object2 was actually removed
		assertEquals(model.getObject(object2ID), null);
		
		// lets add a field to object1
		XID fieldID = X.getIDProvider().createUniqueID();
		XObjectCommand objectCommand = commandFactory.createAddFieldCommand(repositoryID, modelID,
		        object1ID, fieldID, false);
		object1.executeCommand(actorID, objectCommand);
		
		// lets see if the field was actually added
		XField field = object1.getField(fieldID);
		
		assertNotNull(field);
		
		// and, finally, lets set the value of the field
		XValue doubleValue = X.getValueFactory().createDoubleValue(3.14159);
		XFieldCommand fieldCommand = commandFactory.createAddValueCommand(repositoryID, modelID,
		        object1ID, fieldID, field.getRevisionNumber(), doubleValue, false);
		
		field.executeFieldCommand(actorID, fieldCommand);
		
		// lets see if the value was set
		assertEquals(doubleValue, field.getValue());
	}
	
	/**
	 * XCommands can be combined to a transaction and then be executed
	 * atomically. For example we could do everything we did in
	 * testUsingCommands() in one transactions. Transactions can be executed by
	 * XModels and XObjects.
	 * 
	 * Here's how, first we'll create the repository and the XModel:
	 */
	@Test
	public void testUsingTransactions() {
		
		// setting up an actor ID
		XID actorID = X.getIDProvider().fromString("TutorialActor");
		
		// getting an XRepository
		XRepository repository = X.createMemoryRepository();
		XID repositoryID = repository.getID();
		
		// creating the XModel
		XCommandFactory commandFactory = X.getCommandFactory();
		
		XID modelID = X.getIDProvider().createUniqueID();
		XRepositoryCommand repositoryCommand = commandFactory.createAddModelCommand(repositoryID,
		        modelID, false);
		
		// execute the command, therefore adding an XModel
		repository.executeCommand(actorID, repositoryCommand);
		
		XModel model = repository.getModel(modelID);
		assertNotNull(model);
		
		// building the commands
		XID objectID = X.getIDProvider().createUniqueID();
		XID fieldID = X.getIDProvider().createUniqueID();
		XValue doubleValue = X.getValueFactory().createDoubleValue(3.14159);
		
		XModelCommand addObjectCommandCommand = commandFactory.createAddObjectCommand(repositoryID,
		        modelID, objectID, false);
		XObjectCommand addFieldCommand = commandFactory.createAddFieldCommand(repositoryID,
		        modelID, objectID, fieldID, false);
		/*
		 * we know that the XField which value we want to set has just been
		 * added and since the transaction will be executed atomically we can be
		 * sure that the revision number of the XField will be 0
		 */
		XFieldCommand addValueCommand = commandFactory.createAddValueCommand(repositoryID, modelID,
		        objectID, fieldID, 0, doubleValue, false);
		
		/*
		 * After we created the commands it's time to add them to a transaction.
		 * For that purpose we'll use a XTransactionBuilder.
		 * 
		 * We have to specify on what we want to execute the transaction, since
		 * we want to execute it on the model, we'll pass the XAddress of the
		 * model.
		 */

		XTransactionBuilder transBuilder = new XTransactionBuilder(model.getAddress());
		transBuilder.addCommand(addObjectCommandCommand);
		transBuilder.addCommand(addFieldCommand);
		transBuilder.addCommand(addValueCommand);
		
		// getting the transaction
		XTransaction transaction = transBuilder.build();
		// FIXME
		// transaction.execute(model)
		
		// executing the transaction on the model
		model.executeTransaction(actorID, transaction);
		
		// checking whether the transaction was actually executed or not
		XObject object = model.getObject(objectID);
		assertNotNull(object);
		
		XField field = object.getField(fieldID);
		assertNotNull(field);
		
		assertEquals(field.getValue(), doubleValue);
	}
	
	/**
	 * Every {@link XCommand} (or manually executed change) on any entity of
	 * XModel results in creating an {@link XEvent}. XEvents tell the users and
	 * the system what happened, and can be used for logging purposes.
	 * 
	 * Every XEvent has a ChangeType, which can be ADD, REMOVE, CHANGE (only for
	 * XFieldEvents) or TRANSACTION (if the event actually was a transaction)
	 * that specifies what happened.
	 * 
	 * {@link XRepositoryEvent} tell whether a model was added to or removed
	 * from the repository.
	 * 
	 * {@link XModelEvent} tell whether a object was added to or removed from
	 * the model.
	 * 
	 * {@link XObjectEvent} tell whether a field was added to or removed from
	 * the object.
	 * 
	 * {@link XFieldEvent} tell whether the value of the field was added,
	 * removed or changed.
	 * 
	 * To be notified about any events that occurred on a specific entity, we'll
	 * have to register a listener of the right type on the entity we want to
	 * watch.
	 */
	@Test
	public void testUsingEventsAndListeners() {
		
		XID actorID = X.getIDProvider().fromString("ExampleActor");
		XRepository repository = X.createMemoryRepository();
		XModel model = repository.createModel(actorID, X.getIDProvider().createUniqueID());
		
		/*
		 * For example, if we want to be notified when objects are added to or
		 * removed from our XModel, we would write an XModelEventListener and
		 * add it to the model.
		 * 
		 * We'll implement a simple listener that counts how many objects our
		 * model holds.
		 */

		XModelEventListener modelListener = new XModelEventListener() {
			int objectCount;
			
			public void onChangeEvent(XModelEvent event) {
				if(event.getChangeType() == ChangeType.ADD) {
					this.objectCount++;
					System.out.println("Our model now holds " + this.objectCount + " XObjects!");
				} else if(event.getChangeType() == ChangeType.REMOVE) {
					this.objectCount--;
					System.out.println("Our model now holds " + this.objectCount + " XObjects!");
				}
				
			}
		};
		
		model.addListenerForModelEvents(modelListener);
		
		// lets add some objects to see if our listener works
		XID object1ID = X.getIDProvider().fromString("Object1");
		XID object2ID = X.getIDProvider().fromString("Object2");
		
		model.createObject(actorID, object1ID);
		model.createObject(actorID, object2ID);
		
		model.removeObject(actorID, object2ID);
		
		/*
		 * XEvents are also propagated to the higher entities in the XModel
		 * structure.
		 * 
		 * For example, you can register an XObjectListener on an XModel to be
		 * notified of changes to the XObjects that the XModel holds.
		 */

		XObjectEventListener objectListener = new XObjectEventListener() {
			
			public void onChangeEvent(XObjectEvent event) {
				if(event.getChangeType() == ChangeType.ADD) {
					System.out.println("An XField with ID " + event.getFieldID()
					        + " was added to the XObject with ID " + event.getObjectID() + "!");
				} else if(event.getChangeType() == ChangeType.REMOVE) {
					System.out.println("An XField with ID " + event.getFieldID()
					        + " was removed from the XObject with ID " + event.getObjectID() + "!");
				}
			}
			
		};
		
		// add it to the model
		// TODO as adapter?
		model.addListenerForObjectEvents(objectListener);
		
		// lets add and remove an XField to the object that the model still
		// holds
		XObject object = model.getObject(object1ID);
		
		object.createField(actorID, X.getIDProvider().fromString("IWillBeRemoved"));
		object.removeField(actorID, X.getIDProvider().fromString("IWillBeRemoved"));
		
		/*
		 * Note: XEvents are also logged by the XModel which can be used for
		 * versioning and undo/redo purposes. These features are not fully
		 * implemented at the moment.
		 */
	}
	
	/**
	 * Since XModel was developed to be used in a client-server environment with
	 * many different users operating on the same set of data, we also need some
	 * kind of access right management.
	 * 
	 * XModel allows to specify access rights on the user level and it allows to
	 * group users and grant access rights to a whole group.
	 * 
	 * There are 2 main interfaces for this purpose: {@link XAccessManager} and
	 * {@link XGroupDatabase}.
	 * 
	 * The {@link XAccessManager} actually manages the access rights.
	 * 
	 * The XGroupDatabase allows to group users, which enables the
	 * XAccessManager to grant access rights to whole user groups. Every
	 * XAccessManager uses an XGroupDatabase.
	 * 
	 * At the moment an XModel itself does not use an XAccessManager and the
	 * application has to make sure that the access rights that are defined in
	 * the XAccessManager are actually enforced. Note: This may change in the
	 * future.
	 */
	@Test
	public void testUsingAccessRights() {
		/* Here's a little example: */
		XID actorID = X.getIDProvider().fromString("ExampleActor");
		XID user1ID = X.getIDProvider().fromString("ExampleUser1");
		XID user2ID = X.getIDProvider().fromString("ExampleUser2");
		XID user3ID = X.getIDProvider().fromString("ExampleUser3");
		
		XRepository repo = X.createMemoryRepository();
		XModel model = repo.createModel(actorID, X.getIDProvider().createUniqueID());
		
		// creating an XAccessManager and an XGroupDatabase
		XGroupDatabase groups = new MemoryGroupDatabase();
		XAccessManager arm = new MemoryAccessManager(groups);
		
		// granting write access to the user with the XID user1ID on model
		
		// FIXME use small local ID and Address interface
		
		// arm.setAccess(user1ID, XA.ACCESS_WRITE, true, XID...path);
		
		arm.setAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE, true);
		assertTrue(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE));
		
		// depriving user1ID from his read-access again
		arm.resetAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE);
		assertEquals(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE), null);
		
		/*
		 * Note that the hasAccess method does not return false, but null now,
		 * because we reset the access rights for user1ID which means that his
		 * rights are not specified. It would return false if we had used
		 * arm.setAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE, false);
		 * and therefore explicitly stated that user1ID should not have write
		 * access
		 */

		// grouping user1ID, user2ID and user3ID and granting read-access to the
		// group
		XID groupID = X.getIDProvider().fromString("ExampleGroup");
		groups.addToGroup(user1ID, groupID);
		groups.addToGroup(user2ID, groupID);
		groups.addToGroup(user3ID, groupID);
		
		arm.setAccess(groupID, model.getAddress(), XA.ACCESS_WRITE, true);
		assertTrue(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE));
		assertTrue(arm.hasAccess(user2ID, model.getAddress(), XA.ACCESS_WRITE));
		assertTrue(arm.hasAccess(user3ID, model.getAddress(), XA.ACCESS_WRITE));
		
		/*
		 * Note that individual access right definitions take precedence before
		 * the access rights defined on groups. For example if we specify that
		 * user1ID is not allowed to read the model, the group definition will
		 * be ignored.
		 */

		arm.setAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE, false);
		assertFalse(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE));
	}
	
}
