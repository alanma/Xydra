package org.xydra.core.model.tutorial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


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
	
	private static final Logger log = getLogger();
	
	private static Logger getLogger() {
		LoggerTestHelper.init();
		return LoggerFactory.getLogger(XModelBasics.class);
	}
	
	/**
	 * To create an XModel you first have to get an XRepository to hold it, this
	 * can be done by using the class "X", which basically holds the most basic
	 * functions that are needed to build an XModel application.
	 */
	@Test
	public void testBasicStructure() {
		/*
		 * If you want to do anything in Xydra you also have to specify who or
		 * what does the action by passing a so called actor ID, which also is
		 * an XId
		 */
		XId actorID = XX.toId("ExampleActor");
		
		// creating a new repository
		XRepository repository = X.createMemoryRepository(actorID);
		
		/*
		 * Every XModel needs a specific XId. You can either create an XId from
		 * a given string or use the XIdProvider to create a random and unique
		 * XId.
		 */
		
		// creating an ID from a string
		XId stringID = XX.toId("ExampleModel");
		
		// creating a random & unique ID
		XX.createUniqueId();
		
		// actually creating the model
		XModel model = repository.createModel(stringID);
		assertEquals(0, model.getRevisionNumber());
		
		/*
		 * Now that we have an XModel we can start to create XObjects. Just like
		 * XModels an XObject needs an XId. In fact, every part of the XModel
		 * structure, except the XValues, needs an XId. Even the repository has
		 * an XId, but we didn't have to set in manually since we used X to
		 * create it.
		 * 
		 * Furthermore every entity also has an XAddress. An XAddress is just a
		 * list of XIds that basically specifies the path in which the entity
		 * lies. For example, consider the following situation:
		 * 
		 * An XRepository with ID "repo" that holds an XModel with ID "model",
		 * which holds an XObject with ID "object". The XAddress of that XObject
		 * would be "/repo/model/object/-" (represented as a string), the last
		 * field is a "-" because were referring to an object and the last field
		 * always specifies an XField.
		 */
		// creating the XObject
		XId objectId = XX.toId("ExampleObject");
		model.createObject(objectId);
		assertEquals(1, model.getRevisionNumber());
		
		/*
		 * We forgot to save the XObject into a variable! So how can we get a
		 * hold of it? Simple, just use its XId!
		 */
		XObject object = model.getObject(objectId);
		assertEquals(1, object.getRevisionNumber());
		
		/*
		 * Now that we have an object we can add fields to it, which basically
		 * works the same as creating an XObject.
		 */
		XId fieldId = XX.toId("ExampleField");
		XField field = object.createField(fieldId);
		assert field != null;
		
		/*
		 * Setting the value of a field is just as simple, but first we have to
		 * create a new XValue. This can be done by using the XValueFactory or
		 * the convenience functions in the XV class. The are many different
		 * XValue types, as an example we'll create a String value and pass it
		 * to the field.
		 */
		XValue stringValue = XV.toValue("StringValue");
		field.setValue(stringValue);
		
		/*
		 * Please note that the value-type of an XField is not fixed. We could
		 * change the value-type of our XField to an Integer, if we wanted to.
		 */
		XValue integerValue = XV.toValue(42);
		field.setValue(integerValue);
		
		/*
		 * Removing any part of our XModel structure is just as easy.
		 */
		// removing our field from our object
		object.removeField(fieldId);
	}
	
	/**
	 * Since XModel was developed to be used in a client-server environment with
	 * many different users operating on the same set of data, we also need some
	 * kind of access right management.
	 * 
	 * XModel allows to specify access rights on the user level and it allows to
	 * group users and grant access rights to a whole group.
	 * 
	 * There are 2 main interfaces for this purpose:
	 * {@link XAuthorisationManager} and {@link XGroupDatabaseWithListeners}.
	 * 
	 * The {@link XAuthorisationManager} actually manages the access rights.
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
		XId actorID = XX.toId("ExampleActor");
		XId user1ID = XX.toId("ExampleUser1");
		XId user2ID = XX.toId("ExampleUser2");
		XId user3ID = XX.toId("ExampleUser3");
		
		XRepository repo = X.createMemoryRepository(actorID);
		XModel model = repo.createModel(XX.createUniqueId());
		
		// creating an XAccessManager and an XGroupDatabase
		XGroupDatabaseWithListeners groups = new MemoryGroupDatabase();
		XAuthorisationManager arm = new MemoryAuthorisationManager(groups);
		
		// granting write access to the user with the XId user1ID on model
		
		// arm.setAccess(user1ID, XA.ACCESS_WRITE, true, XId...path);
		
		arm.getAuthorisationDatabase()
		        .setAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE, true);
		assertTrue(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE).isAllowed());
		
		// depriving user1ID from his read-access again
		arm.getAuthorisationDatabase().resetAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE);
		assertTrue(!arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE).isDefined());
		
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
		XId groupID = XX.toId("ExampleGroup");
		groups.addToGroup(user1ID, groupID);
		groups.addToGroup(user2ID, groupID);
		groups.addToGroup(user3ID, groupID);
		
		arm.getAuthorisationDatabase()
		        .setAccess(groupID, model.getAddress(), XA.ACCESS_WRITE, true);
		assertTrue(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE).isAllowed());
		assertTrue(arm.hasAccess(user2ID, model.getAddress(), XA.ACCESS_WRITE).isAllowed());
		assertTrue(arm.hasAccess(user3ID, model.getAddress(), XA.ACCESS_WRITE).isAllowed());
		
		/*
		 * Note that individual access right definitions take precedence before
		 * the access rights defined on groups. For example if we specify that
		 * user1ID is not allowed to read the model, the group definition will
		 * be ignored.
		 */
		
		arm.getAuthorisationDatabase().setAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE,
		        false);
		assertTrue(arm.hasAccess(user1ID, model.getAddress(), XA.ACCESS_WRITE).isDenied());
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
		XId actorID = XX.toId("TutorialActor");
		
		// getting an XRepository
		XRepository repository = X.createMemoryRepository(actorID);
		XId repositoryId = repository.getId();
		
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
		
		XId modelId = XX.createUniqueId();
		XRepositoryCommand repositoryCommand = commandFactory.createAddModelCommand(repositoryId,
		        modelId, false);
		
		// execute the command, therefore adding an XModel
		repository.executeCommand(repositoryCommand);
		
		XModel model = repository.getModel(modelId);
		assertNotNull(model);
		
		// adding the objects
		XId object1ID = XX.createUniqueId();
		XId object2ID = XX.createUniqueId();
		
		XModelCommand modelCommand1 = commandFactory.createAddObjectCommand(
		        XX.resolveModel(repositoryId, modelId), object1ID, false);
		XModelCommand modelCommand2 = commandFactory.createAddObjectCommand(
		        XX.resolveModel(repositoryId, modelId), object2ID, false);
		
		model.executeCommand(modelCommand1);
		model.executeCommand(modelCommand2);
		
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
		XModelCommand removeObject2Command = commandFactory.createRemoveObjectCommand(repositoryId,
		        modelId, object2ID, object2.getRevisionNumber(), false);
		
		model.executeCommand(removeObject2Command);
		
		// lets see if object2 was actually removed
		assertEquals(model.getObject(object2ID), null);
		
		// lets add a field to object1
		XId fieldId = XX.createUniqueId();
		XObjectCommand objectCommand = commandFactory.createAddFieldCommand(
		        XX.resolveObject(repositoryId, modelId, object1ID), fieldId, false);
		object1.executeCommand(objectCommand);
		
		// lets see if the field was actually added
		XField field = object1.getField(fieldId);
		
		assertNotNull("field should not be null now", field);
		
		// and, finally, lets set the value of the field
		XValue doubleValue = XV.toValue(3.14159);
		XFieldCommand fieldCommand = commandFactory.createAddValueCommand(
		        XX.resolveField(repositoryId, modelId, object1ID, fieldId),
		        field.getRevisionNumber(), doubleValue, false);
		
		field.executeFieldCommand(fieldCommand);
		
		// lets see if the value was set
		assertEquals(doubleValue, field.getValue());
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
		
		XId actorID = XX.toId("ExampleActor");
		XRepository repository = X.createMemoryRepository(actorID);
		XModel model = repository.createModel(XX.createUniqueId());
		
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
			
			@Override
			public void onChangeEvent(XModelEvent event) {
				if(event.getChangeType() == ChangeType.ADD) {
					this.objectCount++;
					log.info("Our model now holds " + this.objectCount + " XObjects!");
				} else if(event.getChangeType() == ChangeType.REMOVE) {
					this.objectCount--;
					log.info("Our model now holds " + this.objectCount + " XObjects!");
				}
				
			}
		};
		
		model.addListenerForModelEvents(modelListener);
		
		// lets add some objects to see if our listener works
		XId object1ID = XX.toId("Object1");
		XId object2ID = XX.toId("Object2");
		
		model.createObject(object1ID);
		model.createObject(object2ID);
		
		model.removeObject(object2ID);
		
		/*
		 * XEvents are also propagated to the higher entities in the XModel
		 * structure.
		 * 
		 * For example, you can register an XObjectListener on an XModel to be
		 * notified of changes to the XObjects that the XModel holds.
		 */
		
		XObjectEventListener objectListener = new XObjectEventListener() {
			
			@Override
			public void onChangeEvent(XObjectEvent event) {
				if(event.getChangeType() == ChangeType.ADD) {
					log.info("An XField with ID " + event.getFieldId()
					        + " was added to the XObject with ID " + event.getObjectId() + "!");
				} else if(event.getChangeType() == ChangeType.REMOVE) {
					log.info("An XField with ID " + event.getFieldId()
					        + " was removed from the XObject with ID " + event.getObjectId() + "!");
				}
			}
			
		};
		
		// add it to the model
		model.addListenerForObjectEvents(objectListener);
		
		// lets add and remove an XField to the object that the model still
		// holds
		XObject object = model.getObject(object1ID);
		
		object.createField(XX.toId("IWillBeRemoved"));
		object.removeField(XX.toId("IWillBeRemoved"));
		
		/*
		 * Note: XEvents are also logged by the XModel which can be used for
		 * versioning and undo/redo purposes. These features are not fully
		 * implemented at the moment.
		 */
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
		XId actorID = XX.toId("TutorialActor");
		
		// getting an XRepository
		XRepository repository = X.createMemoryRepository(actorID);
		XId repositoryId = repository.getId();
		
		// creating the XModel
		XCommandFactory commandFactory = X.getCommandFactory();
		
		XId modelId = XX.createUniqueId();
		XRepositoryCommand repositoryCommand = commandFactory.createAddModelCommand(repositoryId,
		        modelId, false);
		
		// execute the command, therefore adding an XModel
		repository.executeCommand(repositoryCommand);
		
		XModel model = repository.getModel(modelId);
		assertNotNull(model);
		
		// building the commands
		XId objectId = XX.createUniqueId();
		XId fieldId = XX.createUniqueId();
		XValue doubleValue = XV.toValue(3.14159);
		
		XModelCommand addObjectCommand = commandFactory.createAddObjectCommand(
		        XX.resolveModel(repositoryId, modelId), objectId, false);
		
		XObjectCommand addFieldCommand = commandFactory.createAddFieldCommand(
		        XX.resolveObject(repositoryId, modelId, objectId), fieldId, false);
		/*
		 * we know that the XField which value we want to set has just been
		 * added and since the transaction will be executed atomically we can be
		 * sure that the revision number of the XField will be 0
		 */
		XFieldCommand addValueCommand = commandFactory.createAddValueCommand(
		        XX.resolveField(repositoryId, modelId, objectId, fieldId), 0, doubleValue, false);
		
		/*
		 * After we created the commands it's time to add them to a transaction.
		 * For that purpose we'll use a XTransactionBuilder.
		 * 
		 * We have to specify on what we want to execute the transaction, since
		 * we want to execute it on the model, we'll pass the XAddress of the
		 * model.
		 */
		
		XTransactionBuilder transBuilder = new XTransactionBuilder(model.getAddress());
		transBuilder.addCommand(addObjectCommand);
		transBuilder.addCommand(addFieldCommand);
		transBuilder.addCommand(addValueCommand);
		
		// getting the transaction
		XTransaction transaction = transBuilder.build();
		
		// executing the transaction on the model
		model.executeCommand(transaction);
		
		// checking whether the transaction was actually executed or not
		XObject object = model.getObject(objectId);
		assertNotNull(object);
		
		XField field = object.getField(fieldId);
		assertNotNull(field);
		
		assertEquals(field.getValue(), doubleValue);
	}
	
}
