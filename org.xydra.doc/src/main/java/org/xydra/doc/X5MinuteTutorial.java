package org.xydra.doc;

import org.xydra.base.X;
import org.xydra.base.XId;
import org.xydra.base.XIdProvider;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class X5MinuteTutorial {
	
	public static void main(String[] args) {
		// getting an XIdProvider will save us the work of creating XIds
		// ourselves
		XIdProvider idProvider = X.getIDProvider();
		// instead of creating a random XId, we will create one from a String
		XId actorID = idProvider.fromString("exampleActor");
		
		XRepository exampleRepo = X.createMemoryRepository(actorID);
		
		// creating a random and unique XId for our phonebook XModel
		XId phonebookID = idProvider.createUniqueId();
		
		XModel phonebook = exampleRepo.createModel(phonebookID);
		
		// adding an object representing an entry in the phonebook for a person
		// named "John"
		XId johnID = idProvider.fromString("john");
		XObject johnEntry = phonebook.createObject(johnID);
		
		// adding fields to the john-XObject which will the name and phonenumber
		// of "John"
		XId nameID = idProvider.fromString("name");
		XField nameField = johnEntry.createField(nameID);
		
		XId phonenrID = idProvider.fromString("phonenr");
		XField phonenrField = johnEntry.createField(phonenrID);
		
		// getting an XValueFactory using X
		XValueFactory valueFactory = X.getValueFactory();
		
		// names can be represented by Strings -> create an XStringValue
		XStringValue nameValue = valueFactory.createStringValue("John");
		
		// phone numbers can be represented by Integers -> create an
		// XIntegerValue
		XIntegerValue phonenrValue = valueFactory.createIntegerValue(1234567);
		
		// lets add these values to our XFields
		nameField.setValue(nameValue);
		phonenrField.setValue(phonenrValue);
	}
	
}
