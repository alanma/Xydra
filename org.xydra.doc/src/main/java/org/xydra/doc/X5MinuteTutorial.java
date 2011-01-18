package org.xydra.doc;

import org.xydra.base.XID;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class X5MinuteTutorial {
	
	public static void main(String[] args) {
		// getting an XIDProvider will save us the work of creating XIDs
		// ourselves
		XIDProvider idProvider = X.getIDProvider();
		// instead of creating a random XID, we will create one from a String
		XID actorID = idProvider.fromString("exampleActor");
		
		XRepository exampleRepo = X.createMemoryRepository(actorID);
		
		// creating a random and unique XID for our phonebook XModel
		XID phonebookID = idProvider.createUniqueID();
		
		XModel phonebook = exampleRepo.createModel(phonebookID);
		
		// adding an object representing an entry in the phonebook for a person
		// named "John"
		XID johnID = idProvider.fromString("john");
		XObject johnEntry = phonebook.createObject(johnID);
		
		// adding fields to the john-XObject which will the name and phonenumber
		// of "John"
		XID nameID = idProvider.fromString("name");
		XField nameField = johnEntry.createField(nameID);
		
		XID phonenrID = idProvider.fromString("phonenr");
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
