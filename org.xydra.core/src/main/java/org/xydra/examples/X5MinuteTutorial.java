package org.xydra.examples;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XV;


public class X5MinuteTutorial {
        public static void main(String[] args) {
                XRepository exampleRepo = X.createMemoryRepository();
                
                // getting an XIDProvider will save us the work of creating XIDs
                // ourselves
                XIDProvider idProvider = X.getIDProvider();
                // creating a random and unique XID for our phonebook XModel
                XID phonebookID = idProvider.createUniqueID();
                
                // instead of creating a random XID, we will create one from a String
                XID actorID = idProvider.fromString("exampleActor");
                
                XModel phonebook = exampleRepo.createModel(actorID, phonebookID);
                
                // adding an object representing an entry in the phonebook for a person
                // named "John"
                XID johnID = idProvider.fromString("john");
                XObject johnEntry = phonebook.createObject(actorID, johnID);
                
                // adding fields to the john-XObject which will the name and phonenumber
                // of "John"
                XID nameID = idProvider.fromString("name");
                XField nameField = johnEntry.createField(actorID, nameID);
                
                XID phonenrID = idProvider.fromString("phonenr");
                XField phonenrField = johnEntry.createField(actorID, phonenrID);
                
                // names can be represented by Strings -> create an XStringValue
                XStringValue nameValue = XV.toValue("John");
                
                // phone numbers can be represented by Integers -> create an
                // XIntegerValue
                XIntegerValue phonenrValue = XV.toValue(1234567);
                
                // lets add these values to our XFields
                nameField.setValue(actorID, nameValue);
                phonenrField.setValue(actorID, phonenrValue);
        }
}