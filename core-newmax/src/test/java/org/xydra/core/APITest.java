package org.xydra.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.base.value.impl.memory.MemoryIdListValue;
import org.xydra.core.model.MissingPieceException;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryField;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.model.impl.memory.MemoryObject;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraElement;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.serialize.xml.XmlParser;


/**
 * @author voelkel
 */
public class APITest {
    
    // A wrapper for the books - A book can be modeled as an XObject
    static class Book {
        public static final XId authorID = XX.toId("author");
        public static final XId copiesID = XX.toId("copies");
        // we'll use XFields and XValues to store the informations about the
        // book, so we need some XIds for that
        public static final XId titleID = XX.toId("title");
        
        private XObject book;
        
        public Book(XObject book) {
            this.book = book;
        }
        
        // a method for adding a copy
        public void addCopy(XId actorID, XId copyID) {
            XField copies = this.book.getField(copiesID);
            XIdListValue copiesList = (XIdListValue)copies.getValue();
            copiesList = copiesList.add(copyID);
            copies.setValue(copiesList);
        }
        
        // the following methods will assume that the book-XObject is correctly
        // set up
        
        // a method for getting the author
        public String getAuthor() {
            XValue value = this.book.getField(authorID).getValue();
            
            if(value instanceof XStringValue) {
                return ((XStringValue)value).contents();
            } else {
                return null; // the authorField should hold a String
            }
        }
        
        // a method for getting the XIds of the copies of this book
        public List<XId> getCopies() {
            return XV.asList((XIdListValue)this.book.getField(copiesID).getValue());
        }
        
        // a method for getting the title
        public String getTitle() {
            XValue value = this.book.getField(titleID).getValue();
            
            if(value instanceof XStringValue) {
                return ((XStringValue)value).contents();
            } else {
                return null; // the titleField should hold a String
            }
        }
        
        // a method for setting the author
        public void setAuthor(XId actorID, String author) {
            this.book.getField(authorID).setValue(XV.toValue(author));
        }
        
        // a method for setting the title
        public void setTitle(XId actorID, String name) {
            this.book.getField(titleID).setValue(XV.toValue(name));
        }
        
        // a little method to set up the object structure
        public void setUp(XId actorID) {
            this.book.createField(titleID); // create a field for the
            // name
            this.book.createField(authorID); // create a field for the
            // author
            XField copiesField = this.book.createField(copiesID); // create
            // a
            // field
            // for
            // the
            // copies
            
            // we'll set up the value here too, because wed take it for granted
            // that setUp will only be called on "new" objects
            // otherwise it might overwrite the already existing value
            copiesField.setValue(XV.toValue(new XId[] {})); // the
            // copies
            // have
            // an
            // own
            // XId,
            // so
            // we'll
            // store those
        }
    }
    
    // A wrapper for a book copy - a book copy can be modeled as an XObject
    static class BookCopy {
        // we'll use XFields and XValues to store the informations about the
        // book copy, so we need some XIds for that
        public static final XId copyOfID = XX.toId("copyOf");
        public static final XId isBorrowedID = XX.toId("isBorrowed");
        
        private XObject bookCopy;
        
        public BookCopy(XObject bookCopy) {
            this.bookCopy = bookCopy;
        }
        
        // a method for getting the XId this bookCopy is a copy of
        public XId getCopyOf() {
            XValue value = this.bookCopy.getField(copyOfID).getValue();
            
            if(value instanceof XId) {
                return ((XId)value);
            } else {
                return null; // the copyOf-Field should hold an XId
            }
        }
        
        // the following methods will assume that the bookCopy-XObject is
        // correctly set up
        
        // a method for checking whether this bookCopy is borrowed or not
        public boolean isBorrowed() {
            XValue value = this.bookCopy.getField(isBorrowedID).getValue();
            
            if(value instanceof XBooleanValue) {
                return ((XBooleanValue)value).contents();
            } else {
                return true; // the copyOf-Field should hold a boolean
            }
        }
        
        // a method for setting the XId this bookCopy is a copy of
        public void setCopyOf(XId actorID, XId bookID) {
            this.bookCopy.getField(copyOfID).setValue(bookID);
        }
        
        // a method for setting whether this bookCopy is borrowed or not
        public void setIsBorrowed(XId actorID, boolean isBorrowed) {
            this.bookCopy.getField(isBorrowedID).setValue(XV.toValue(isBorrowed));
        }
        
        // a little method to set up the object structure
        public void setUp(XId actorID) {
            this.bookCopy.createField(copyOfID); // create a field for
            // the ID of the book
            // this object is a
            // copy of
            this.bookCopy.createField(isBorrowedID); // create a field
            // for the
            // isBorrowed-attribute
        }
    }
    
    // A wrapper for the library - a library can be modeled as an XRepository
    static class Library {
        public static final XId bookCopiesID = XX.toId("bookCopies");
        // we'll use 2 models for books and bookCopies, so we'll need 2 unique
        // IDs:
        public static final XId booksID = XX.toId("books");
        
        // we'll interpret the library itself as an XRepository
        private XRepository library;
        
        public Library(XRepository library) {
            this.library = library;
        }
        
        // a method to add a new book to the library
        public XId addBook(XId actorID, String title, String author) {
            XId bookID = XX.createUniqueId();
            XObject book = this.library.getModel(booksID).createObject(bookID);
            
            Book bookWrapper = new Book(book); // we'll use the book wrapper to
            // set the title & author
            bookWrapper.setUp(actorID);
            bookWrapper.setTitle(actorID, title);
            bookWrapper.setAuthor(actorID, author);
            
            return bookID;
        }
        
        // the following methods will assume that the library-XRepository is
        // correctly set up
        
        // a method to add a copy of a specified book to the library
        public XId addBookyCopy(XId actorID, XId bookID) {
            XObject book = this.library.getModel(booksID).getObject(bookID);
            if(book != null) {
                Book bookWrapper = new Book(book);
                XId bookCopyID = XX.createUniqueId();
                XObject bookCopy = this.library.getModel(bookCopiesID).createObject(bookCopyID);
                
                BookCopy bookCopyWrapper = new BookCopy(bookCopy); // we'll use
                // the
                // bookCopy-Wrapper
                // to set the
                // fields
                bookCopyWrapper.setUp(actorID);
                bookCopyWrapper.setCopyOf(actorID, bookID);
                bookCopyWrapper.setIsBorrowed(actorID, false);
                
                // add a reference to this copy to the book
                bookWrapper.addCopy(actorID, bookCopyID);
                
                return bookCopyID;
            } else {
                return null; // return null, if there is no book with the ID
                // bookID
            }
        }
        
        // a method for borrowing a book (will return null if no copy of this
        // book is available)
        public XId borrow(XId actorID, XId bookID) {
            XObject book = this.library.getModel(booksID).getObject(bookID);
            
            if(book != null) {
                Book bookWrapper = new Book(book);
                
                // find an unborrowed copy
                for(XId copyID : bookWrapper.getCopies()) {
                    XObject unborrowedCopy = this.library.getModel(bookCopiesID).getObject(copyID);
                    
                    if(unborrowedCopy != null) {
                        BookCopy copyWrapper = new BookCopy(unborrowedCopy);
                        
                        // check whether this copy is borrowed or not
                        if(!copyWrapper.isBorrowed()) {
                            // found an unborrowed copy
                            copyWrapper.setIsBorrowed(actorID, true); // borrow
                            // it
                            
                            return unborrowedCopy.getId();
                        }
                    }
                }
                
                return null; // no unborrowed copy found
            } else {
                return null;
            }
        }
        
        // a method for checking whether a book has unborrowed copies
        public boolean hasUnborrowedCopies(XId bookID) {
            XObject book = this.library.getModel(booksID).getObject(bookID);
            
            if(book != null) {
                Book bookWrapper = new Book(book);
                
                // find an unborrowed copy
                for(XId copyID : bookWrapper.getCopies()) {
                    XObject unborrowedCopy = this.library.getModel(bookCopiesID).getObject(copyID);
                    
                    if(unborrowedCopy != null) {
                        BookCopy copyWrapper = new BookCopy(unborrowedCopy);
                        
                        // check whether this copy is borrowed or not
                        if(!copyWrapper.isBorrowed()) {
                            return true;
                        }
                    }
                }
                
                return false; // no unborrowed copy found
            } else {
                return false;
            }
        }
        
        // a method for returning a copy of a book
        public void returnCopy(XId actorID, XId copyID) {
            XObject copy = this.library.getModel(bookCopiesID).getObject(copyID);
            
            if(copy != null) {
                BookCopy copyWrapper = new BookCopy(copy);
                copyWrapper.setIsBorrowed(actorID, false);
            }
        }
        
        // a little method to set up the model structure
        public void setUp(XId actorID) {
            this.library.createModel(booksID); // create model for the
            // books
            this.library.createModel(bookCopiesID); // create model for
            // the book copies
        }
    }
    
    /**
     * Sets the {@link XField} specified by 'fieldId' of the given
     * {@link XObject} to given stringValue on behalf of the actor with
     * {@link XId} 'actorID'
     * 
     * @param object The {@link XObject} containing the {@link XField} specified
     *            by'fieldId'.
     * @param fieldId The {@link XId} of the {@link XField} which value is to be
     *            set. {@link XField} will be created if it doesn't exist.
     * @param stringValue The new String, which will be set as the value of the
     *            specified {@link XField}.
     */
    public static void safeSetStringValue(XObject object, XId fieldId, String stringValue) {
        if(object != null) {
            CoreUtils.setValue(object, fieldId, XV.toValue(stringValue));
        }
    }
    
    private XId actorId = XX.toId("AbstractTestAPI");
    
    private String password = null; // TODO auth: where to get this?
    
    {
        LoggerTestHelper.init();
    }
    
    @Test
    public void testField() {
        // create a field
        XField field = new MemoryField(this.actorId, XX.createUniqueId());
        
        // check that the value isn't set
        assertNull(field.getValue());
        
        // add a value to the object
        XValue testValue1 = XV.toValue("Test");
        field.setValue(testValue1);
        
        // check whether it was really added
        assertEquals(testValue1, field.getValue());
        
        // change the value
        XValue testValue2 = XV.toValue("Another test");
        field.setValue(testValue2);
        
        // check whether it was really changed
        assertEquals(testValue2, field.getValue());
        
        // remove the value
        field.setValue(null);
        
        // check whether it was really removed
        assertNull(field.getValue());
        
        // - do the same with a field that was created by an object -
        XRepository repo = X.createMemoryRepository(this.actorId);
        XId modelId = XX.createUniqueId();
        XModel model = repo.createModel(modelId);
        XObject object = model.createObject(XX.createUniqueId());
        field = object.createField(XX.createUniqueId());
        
        // check that the value isn't set
        assertNull(field.getValue());
        
        // add a value to the object
        XValue testValue3 = XV.toValue("Testing again");
        field.setValue(testValue3);
        
        // check whether it was really added
        assertEquals(testValue3, field.getValue());
        
        // change the value
        XValue testValue4 = XV.toValue("AND AGAIN!");
        field.setValue(testValue4);
        
        // check whether it was really changed
        assertEquals(testValue4, field.getValue());
        
        // remove the value
        field.setValue(null);
        
        // check whether it was really removed
        assertNull(field.getValue());
    }
    
    /*
     * The following is a big test illustrating how to use XModel to build a
     * model and how to actually use the model we built. The sample model will
     * model a library in the following style:
     * 
     * - A Library has: Books, BookCopies (the actual books that can be
     * borrowed), Users - A Book has: An ID, A title, an author and copies - A
     * BookCopy has: An ID, A field telling whether its borrowed or not, a field
     * telling which Book it's a copy of, a field telling who borrowed it Book
     * copies can be borrowed and returned
     * 
     * The first thing we'll do is building some wrappers that'll make it easier
     * to use XModel
     * 
     * Attention: We will not catch errors in using the models in the wrappers
     * here. For example if the user tries to set the titleField on an XObject
     * which has no such field, the JVM will throw an exception, because we're
     * not paying attention to that here. It's generally a good idea to use the
     * wrappers to catch possible errors like that
     */
    
    @Test
    public void testLibrary() {
        // Use the omnipotent X to get a repository
        XRepository libraryRepo = X.createMemoryRepository(this.actorId);
        XId actorID = XX.createUniqueId();
        
        // Wrap it with the library class and set it up
        Library library = new Library(libraryRepo);
        library.setUp(actorID); // set the repository up so that it can be used
        // as a library
        
        // add some books
        XId hitchhikerID = library.addBook(actorID, "The Hitchhiker's Guide To The Galaxy",
                "Douglas Adams");
        XId guardsguardsID = library.addBook(actorID, "Guards! Guards!", "Terry Pratchett");
        XId daVinciID = library.addBook(actorID, "The Da Vinci Code", "Dan Brown");
        
        // add some copies for the books
        List<XId> hitchhikerCopies = new ArrayList<XId>();
        List<XId> guardsguardsCopies = new ArrayList<XId>();
        List<XId> daVinciCopies = new ArrayList<XId>();
        
        // Add 5 copies for the Hitchhiker's Guide
        for(int i = 0; i < 5; i++) {
            hitchhikerCopies.add(library.addBookyCopy(actorID, hitchhikerID));
        }
        
        // Add 2 copies for Guards! Guards!
        for(int i = 0; i < 2; i++) {
            guardsguardsCopies.add(library.addBookyCopy(actorID, guardsguardsID));
        }
        
        // Add 1 copy for The Da Vinci Code
        daVinciCopies.add(library.addBookyCopy(actorID, daVinciID));
        
        // Create some userIDs
        XId user1 = XX.createUniqueId();
        XId user2 = XX.createUniqueId();
        
        // borrow some books
        assertTrue(library.hasUnborrowedCopies(hitchhikerID));
        assertTrue(library.hasUnborrowedCopies(guardsguardsID));
        assertTrue(library.hasUnborrowedCopies(daVinciID));
        
        // borrow all copies of The Hitchhiker's Guide To The Galaxy
        for(int i = 0; i < 5; i++) {
            library.borrow(user1, hitchhikerID);
        }
        // try to borrow another copy of The Hitchhiker's Guide To The Galaxy
        // (should'nt work, all 5 copies should be borrowed)
        assertNull(library.borrow(user1, hitchhikerID));
        
        // borrow the only copy of The Da Vinci Code
        XId borrowedDaVinciID = library.borrow(user1, daVinciID);
        // check that all copies of The Da Vinci Code are now borrowed
        assertFalse(library.hasUnborrowedCopies(daVinciID));
        
        // return the borrowed copy of The Da Vinci Code
        library.returnCopy(user1, borrowedDaVinciID);
        assertTrue(library.hasUnborrowedCopies(daVinciID));
        
        // borrow 1 copy of the Guards! Guards!
        XId borrowedGuardsID1 = library.borrow(user1, guardsguardsID);
        assertTrue(library.hasUnborrowedCopies(guardsguardsID)); // ...there's
        // still
        // another copy
        XId borrowedGuardsID2 = library.borrow(user2, guardsguardsID); // borrow
        // that
        // too
        assertFalse(library.hasUnborrowedCopies(guardsguardsID));
        
        // return both copies
        library.returnCopy(user1, borrowedGuardsID1);
        library.returnCopy(user2, borrowedGuardsID2);
        
        assertTrue(library.hasUnborrowedCopies(guardsguardsID));
    }
    
    @Test
    public void testModel() {
        // create a model
        XModel model = X.createMemoryRepository(this.actorId).createModel(XX.createUniqueId());
        assertTrue(model.getRevisionNumber() >= 0);
        
        // add an object to the model
        XId objectId = XX.createUniqueId(); // create an ID for
        // the object
        XObject object = model.createObject(objectId);
        
        // check whether it was really added
        assertEquals(object, model.getObject(objectId));
        assertTrue(model.hasObject(objectId));
        
        // remove object again
        model.removeObject(object.getId());
        
        // check whether it was really removed
        assertNull(model.getObject(objectId));
        assertFalse(model.hasObject(objectId));
        
        // - do the same with a model that was created by a repository -
        
        XRepository repo = new MemoryRepository(this.actorId, this.password, XX.createUniqueId());
        XId modelId = XX.createUniqueId();
        model = repo.createModel(modelId);
        
        // add an object to the model
        XId object2ID = XX.createUniqueId(); // create an ID for
        // the object
        XObject object2 = model.createObject(object2ID);
        
        // check whether it was really added
        assertEquals(object2, model.getObject(object2ID));
        assertTrue(model.hasObject(object2ID));
        
        // remove object again
        model.removeObject(object2.getId());
        
        // check whether it was really removed
        assertNull(model.getObject(object2ID));
        assertFalse(model.hasObject(object2ID));
        
    }
    
    @Test
    public void testObject() {
        // create an object
        XObject object = new MemoryObject(this.actorId, this.password, XX.createUniqueId());
        
        // add a field to the object
        XId fieldId = XX.createUniqueId(); // create an ID for
        // the field
        XField field = object.createField(fieldId);
        
        // check whether it was really added
        assertEquals(field, object.getField(fieldId));
        assertTrue(object.hasField(fieldId));
        
        // remove field again
        object.removeField(field.getId());
        
        // check whether it was really removed
        XField fieldAgain = object.getField(fieldId);
        assertNull(fieldAgain);
        assertFalse(object.hasField(fieldId));
        
        // - do the same with an object that was created by a model -
        XRepository repo = X.createMemoryRepository(this.actorId);
        XId modelId = XX.createUniqueId();
        XModel model = repo.createModel(modelId);
        object = model.createObject(XX.createUniqueId());
        
        // add a field to the object
        XId field2ID = XX.createUniqueId(); // create an ID for
        // the field
        XField field2 = object.createField(field2ID);
        
        // check whether it was really added
        assertEquals(field2, object.getField(field2ID));
        assertTrue(object.hasField(field2ID));
        
        // remove field again
        object.removeField(field2.getId());
        
        // check whether it was really removed
        assertNull(object.getField(field2ID));
        assertFalse(object.hasField(field2ID));
    }
    
    /*
     * Here is a little test showing a normal user would use the XModel API +
     * our wrappers to model a library
     */
    
    @Test
    public void testRepository() {
        
        // create a repository
        XRepository repo = new MemoryRepository(this.actorId, this.password, XX.createUniqueId());
        
        // add a model to the repository; create an ID for the model
        XId modelId = XX.createUniqueId();
        XModel model = repo.createModel(modelId);
        
        // check whether it was really added
        assertEquals(model, repo.getModel(modelId));
        assertTrue(repo.hasModel(modelId));
        
        // remove model again
        repo.removeModel(model.getId());
        
        // check whether it was really removed
        assertNull(repo.getModel(modelId));
        assertFalse(repo.hasModel(modelId));
    }
    
    @Test
    public void testSaveAndLoadModel() {
        XModel model = new MemoryModel(this.actorId, this.password, XX.createUniqueId());
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue(true));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Test!"));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue(false));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue(42));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue(0L));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue(3.14159265));
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Another Test!"));
        
        XydraOut out = new XmlOut();
        SerializedModel.serialize(model, out);
        
        // try to load it
        XydraElement e = new XmlParser().parse(out.getData());
        XModel loadedModel = SerializedModel.toModel(this.actorId, this.password, e);
        assertTrue(loadedModel != null);
        assertTrue(XCompareUtils.equalState(loadedModel, model));
        assertNotNull(loadedModel);
        assert loadedModel != null;
        assertTrue(loadedModel.equals(model));
        assertEquals(loadedModel, model);
    }
    
    @Test
    public void testSaveAndLoadRepository() {
        // we'll use our library classes to create a repository with content
        XRepository repo = X.createMemoryRepository(this.actorId);
        XId actorID = XX.createUniqueId();
        
        // add some books and copies
        Library library = new Library(repo);
        library.setUp(actorID);
        XId hitchhikerID = library.addBook(actorID, "The Hitchhiker's Guide To The Galaxy",
                "Douglas Adams");
        XId guardsguardsID = library.addBook(actorID, "Guards! Guards!", "Terry Pratchett");
        XId daVinciID = library.addBook(actorID, "The Da Vinci Code", "Dan Brown");
        
        library.borrow(actorID, hitchhikerID);
        library.borrow(actorID, hitchhikerID);
        library.borrow(actorID, hitchhikerID);
        library.borrow(actorID, guardsguardsID);
        library.borrow(actorID, guardsguardsID);
        library.borrow(actorID, daVinciID);
        
        // Add 5 copies for the Hitchhiker's Guide
        for(int i = 0; i < 5; i++) {
            library.addBookyCopy(actorID, hitchhikerID);
        }
        
        // Add 2 copies for Guards! Guards!
        for(int i = 0; i < 2; i++) {
            library.addBookyCopy(actorID, guardsguardsID);
        }
        
        // Add 1 copy for The Da Vinci Code
        library.addBookyCopy(actorID, daVinciID);
        
        // We now created a little repository with some content, so saving makes
        // sense
        XydraOut out = new XmlOut();
        SerializedModel.serialize(repo, out);
        
        // try to load it
        XydraElement e = new XmlParser().parse(out.getData());
        XRepository loadedRepo = SerializedModel.toRepository(this.actorId, this.password, e);
        // if loadedRepo == null, saving wasn't successful
        assertNotNull(loadedRepo);
        // assert that the saving process really saved our repo
        assertEquals(loadedRepo, repo);
        
        assertTrue(XCompareUtils.equalState(loadedRepo, repo));
        
    }
    
    @Test
    public void testXX() {
        // This test shows how XX can be used
        
        // - - Method: XField setValue(XId actorID, XObject object, XId fieldId,
        // XValue value) - -
        XRepository repo = X.createMemoryRepository(this.actorId);
        XModel model = repo.createModel(XX.createUniqueId());
        XObject object = model.createObject(XX.createUniqueId());
        XField field1 = object.createField(XX.createUniqueId());
        XValue value1 = XV.toValue("Test value");
        
        // Add value to an existing field
        XField field2 = CoreUtils.setValue(object, field1.getId(), value1);
        
        // check if the method works correctly
        assertEquals("the value of field should be set", field1, field2);
        assertTrue("the method should return the field1-object, and not create a new one",
                field1 == field2); //
        
        // Change value of the existing field
        XValue value2 = XV.toValue("Another test value");
        field2 = CoreUtils.setValue(object, field1.getId(), value2);
        assertEquals(field1, field2);
        assertTrue(field1 == field2);
        
        // Remove value of the existing field
        field2 = CoreUtils.setValue(object, field1.getId(), null);
        assertEquals(field1, field2);
        assertTrue(field1 == field2);
        assertNull(field1.getValue());
        
        // Add value to a not existing field (should create a fitting field)
        XId newID = XX.createUniqueId();
        field2 = CoreUtils.setValue(object, newID, value1);
        assertTrue(object.hasField(newID)); // did it create a new field?
        assertEquals(object.getField(newID), field2);
        assertTrue(object.getField(newID) == field2);
        assertEquals(value1, object.getField(newID).getValue());
        
        // Remove a value from a not existing field (should create a new field
        // which value isn't set)
        newID = XX.createUniqueId();
        field2 = CoreUtils.setValue(object, newID, null);
        assertTrue(object.hasField(newID));
        assertEquals(object.getField(newID), field2);
        assertTrue(object.getField(newID) == field2);
        assertNull(object.getField(newID).getValue());
        
        // - - Method: void copy(XId actorID, XModel sourceModel, XModel
        // targetModel) - -
        model = repo.createModel(XX.createUniqueId());
        // add some content
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Test"));
        
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Test2"));
        
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Test3"));
        
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId())
                .setValue(XV.toValue("Test4"));
        
        model.createObject(XX.createUniqueId()).createField(XX.createUniqueId());
        
        model.createObject(XX.createUniqueId());
        
        // copy it!
        XModel copyModel = new MemoryModel(this.actorId, this.password, model.getId());
        
        XCopyUtils.copyData(model, copyModel);
        
        // do both models have the same content? (revision numbers may differ)
        assertTrue(XCompareUtils.equalTree(model, copyModel));
        
        // - - Method: void copy(XId actorID, XObject sourceObject, XObject
        // targetObject) - -
        model = repo.createModel(XX.createUniqueId());
        object = model.createObject(XX.createUniqueId());
        
        // add some content
        object.createField(XX.createUniqueId()).setValue(XV.toValue("Test"));
        
        object.createField(XX.createUniqueId()).setValue(XV.toValue("Test 2"));
        
        object.createField(XX.createUniqueId()).setValue(XV.toValue("Test 3"));
        
        object.createField(XX.createUniqueId()).setValue(XV.toValue("Test 4"));
        
        object.createField(XX.createUniqueId());
        
        // copy it!
        XObject copyObject = new MemoryObject(this.actorId, this.password, object.getId());
        
        XCopyUtils.copyData(object, copyObject);
        
        // do both objects have the same content? (revision numbers may differ)
        assertTrue(XCompareUtils.equalTree(object, copyObject));
        
        // - - Method: XValue safeGetValue(XObject object, XId fieldId) - -
        object = model.createObject(XX.createUniqueId());
        XId fieldId = XX.createUniqueId();
        field1 = object.createField(fieldId);
        field1.setValue(XV.toValue("Test"));
        XValue value = field1.getValue();
        
        // get the value of an existing field
        assertEquals(value, CoreUtils.safeGetValue(object, fieldId));
        assertTrue(value == CoreUtils.safeGetValue(object, fieldId));
        
        // remove value and try to get it (should throw an exception)
        field1.setValue(null);
        try {
            CoreUtils.safeGetValue(object, fieldId); // safeGetValue should
            // throw a
            // MissingPieceException
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get the value of a not existing field
        try {
            // safeGetValue should throw a MissingPieceException
            CoreUtils.safeGetValue(object, XX.createUniqueId());
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: XValue safeGetValue(XModel model, XId objectId, XId
        // fieldId - -
        model = repo.createModel(XX.createUniqueId());
        XId objectId = XX.createUniqueId();
        object = model.createObject(objectId);
        fieldId = XX.createUniqueId();
        field1 = object.createField(fieldId);
        field1.setValue(XV.toValue("Test"));
        value = field1.getValue();
        
        // get the value of an existing field
        assertEquals(value, CoreUtils.safeGetValue(model, objectId, fieldId));
        assertTrue(value == CoreUtils.safeGetValue(model, objectId, fieldId));
        
        // remove value and try to get it (should throw an exception)
        field1.setValue(null);
        try {
            CoreUtils.safeGetValue(model, objectId, fieldId); // safeGetValue
            // should
            // throw a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get the value of a not existing field
        try {
            // safeGetValue should throw a MissingPieceException here
            CoreUtils.safeGetValue(model, objectId, XX.createUniqueId());
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get the value of a field in an not existing object
        try {
            // safeGetValue should throw a MissingPieceException here
            CoreUtils.safeGetValue(model, XX.createUniqueId(), XX.createUniqueId());
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: XField safeGetField(XObject object, XId fieldId) - -
        model = repo.createModel(XX.createUniqueId());
        object = model.createObject(XX.createUniqueId());
        fieldId = XX.createUniqueId();
        field1 = object.createField(fieldId);
        
        // get an existing field of an existing object
        assertEquals(field1, CoreUtils.safeGetField(object, fieldId));
        assertTrue(field1 == CoreUtils.safeGetField(object, fieldId));
        
        // remove the field and try to get it (should throw an exception)
        object.removeField(field1.getId());
        
        try {
            CoreUtils.safeGetField(object, fieldId); // safeGetField should
            // throw a
            // MissingPieceException here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get a not existing field
        try {
            CoreUtils.safeGetField(object, XX.createUniqueId()); // safeGetField
            // should
            // throw
            // a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: XModel safeGetModel(XRepository repository, XId modelId)
        // - -
        XId modelId = XX.createUniqueId();
        model = repo.createModel(modelId);
        
        // get an existing model of an existing object
        assertEquals(model, CoreUtils.safeGetModel(repo, modelId));
        assertTrue(model == CoreUtils.safeGetModel(repo, modelId));
        
        // remove model and try to get it (should throw an exception)
        repo.removeModel(model.getId());
        
        try {
            CoreUtils.safeGetModel(repo, modelId); // safeGetModel should throw
            // a
            // MissingPieceException here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get a not existing model
        try {
            CoreUtils.safeGetModel(repo, XX.createUniqueId()); // safeGetModel
            // should
            // throw a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: XObject safeGetObject(XModel model, XId objectId) - -
        model = repo.createModel(XX.createUniqueId());
        objectId = XX.createUniqueId();
        object = model.createObject(objectId);
        
        // get existing object
        assertEquals(object, CoreUtils.safeGetObject(model, objectId));
        assertTrue(object == CoreUtils.safeGetObject(model, objectId));
        
        // remove object and try to get it (should throw an exception)
        model.removeObject(object.getId());
        try {
            CoreUtils.safeGetObject(model, objectId); // safeGetObject should
            // throw a
            // MissingPieceException here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get a not existing object
        try {
            CoreUtils.safeGetObject(model, XX.createUniqueId()); // safeGetObject
            // should
            // throw
            // a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: XObject safeGetObject(XRepository repository, XId
        // modelId, XId objectId) - -
        repo = X.createMemoryRepository(this.actorId);
        modelId = XX.createUniqueId();
        model = repo.createModel(modelId);
        objectId = XX.createUniqueId();
        object = model.createObject(objectId);
        
        // get existing object
        assertEquals(object, CoreUtils.safeGetObject(repo, modelId, objectId));
        assertTrue(object == CoreUtils.safeGetObject(repo, modelId, objectId));
        
        // remove object and try to get it (should throw an exception)
        model.removeObject(object.getId());
        try {
            CoreUtils.safeGetObject(repo, modelId, objectId); // safeGetObject
            // should
            // throw a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get a not existing object
        try {
            CoreUtils.safeGetObject(repo, modelId, XX.createUniqueId()); // safeGetObject
            // should
            // throw
            // a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // try to get a not existing object of a not existing model
        try {
            CoreUtils.safeGetObject(repo, XX.createUniqueId(), XX.createUniqueId()); // safeGetObject
            // should
            // throw a
            // MissingPieceException
            // here
            assertTrue(false);
        } catch(MissingPieceException mpe) {
            assertTrue(true);
        }
        
        // - - Method: void safeSetStringValue(XId actorID, XObject object, XId
        // fieldId, String stringValue) - -
        object = new MemoryObject(this.actorId, this.password, XX.createUniqueId());
        fieldId = XX.createUniqueId();
        field1 = object.createField(fieldId);
        
        // set the value of an existing field
        APITest.safeSetStringValue(object, fieldId, "Test");
        assertTrue(field1.getValue() instanceof XStringValue);
        assertEquals("Test", ((XStringValue)field1.getValue()).contents());
        
        // set the value of a not existing field
        newID = XX.createUniqueId();
        assertFalse(object.hasField(newID));
        APITest.safeSetStringValue(object, newID, "Test");
        assertTrue(object.hasField(newID));
        assertTrue(object.getField(newID).getValue() instanceof XStringValue);
        assertEquals("Test", ((XStringValue)object.getField(newID).getValue()).contents());
        
        // - - Method: XIdListValue addIDToList(XId actorID, XField field, XId
        // id) - -
        // - - Method: XIdListValue removeIDFromList(XId actorID, XField field,
        // XId id) - -
        field1 = new MemoryField(this.actorId, XX.createUniqueId());
        field1.setValue(new MemoryIdListValue(new XId[] { XX.createUniqueId() }));
        newID = XX.createUniqueId();
        
        XIdListValue listValue = (XIdListValue)field1.getValue();
        assertFalse(listValue.contains(newID));
        // add the new id
        listValue = listValue.add(newID);
        field1.setValue(listValue);
        
        // check that the id was added
        listValue = (XIdListValue)field1.getValue();
        assertEquals(listValue.size(), 2);
        assertTrue(listValue.contains(newID));
        
        // remove it
        listValue = listValue.remove(newID);
        field1.setValue(listValue);
        
        // check that it was removed
        listValue = (XIdListValue)field1.getValue();
        assertEquals(listValue.size(), 1);
        assertFalse(listValue.contains(newID));
        
        // Some tests for the getXXfromURI Methods
        XRepository testRepository = X.createMemoryRepository(this.actorId);
        String modelIdString = "TestModel";
        XModel testModel = testRepository.createModel(XX.toId(modelIdString));
        String objectIdString = "Object";
        XObject testObject = testModel.createObject(XX.toId(objectIdString));
        String fieldIdString = "Field";
        XField testField = testObject.createField(XX.toId(fieldIdString));
        XValue testValue = XV.toValue("TestValue");
        testField.setValue(testValue);
    }
    
}