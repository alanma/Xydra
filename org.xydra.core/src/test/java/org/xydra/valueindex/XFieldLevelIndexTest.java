package org.xydra.valueindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.X;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.index.query.Pair;


/**
 * An abstract test for testing {@link XFieldLevelIndex}. The phonebook model
 * from {@link DemoModelUtil} is used as the basic source of data.
 *
 * @author kaidel
 *
 */

public abstract class XFieldLevelIndexTest {

    private enum TestType {
        XOBJECT, XFIELD, XEVENT;
    }

    /**
     * The basic model. Most updateIndex methods need an old instance of a model
     * or object and the newest instance to compute what exactly needs to be
     * updated. This "oldModel" can be used to get the "old" objects etc. to
     * test updateIndex methods.
     *
     * Both oldModel and newModel will contain the same data during the tests,
     * but newModel will be used to change values etc., whereas oldModel will
     * remain in the old state, so that it can be used in the
     * updateIndex-methods as described above.
     */
    public XModel oldModel;

    /**
     * see description of oldModel
     */
    public XModel newModel;

    /**
     * Has almost the same role as oldModel, but will be used to test an Index
     * which excludes almost all fields from indexing. See description of
     * oldModel for more information.
     *
     * newExcludeAllModel has the same role for oldExcludeAllModel as newModel
     * for oldModel.
     */
    public XModel oldExcludeAllModel;

    /**
     * see description of oldExcludeAllModel.
     */
    public XModel newExcludeAllModel;

    /**
     * oldIndex is the index used for indexing the oldModel, newIndex the index
     * used for indexing the newModel.
     *
     * defaultIncludeAll needs to be set to true and at least one ID in
     * excludedFieldIds must be provided. includeFieldIds should be an empty
     * set.
     */
    public XFieldLevelIndex oldIndex;
    public XFieldLevelIndex newIndex;

    /**
     * oldExcludeallIndex is the index used for indexing the oldExcludeAllModel,
     * newExcludeAllIndex the index used for indexing the newExcludeAllModel.
     *
     * defaultIncludeAll needs to be set to true and at least one ID in
     * excludedFieldIds must be provided. includeFieldIds should be an empty
     * set.
     */
    public XFieldLevelIndex oldExcludeAllIndex;
    public XFieldLevelIndex newExcludeAllIndex;

    /**
     * oldIndexer is used by oldIndex, newIndexer by newIndex
     */
    public XValueIndexer oldIndexer;
    public XValueIndexer newIndexer;

    /**
     * oldExcludeAllIndexer is used by oldExcludeAllIndex, newExcludeAllIndexer
     * by newExcludeAllIndex
     */
    public XValueIndexer oldExcludeAllIndexer;
    public XValueIndexer newExcludeAllIndexer;

    /**
     * a set of Ids which are excluded from being indexed by oldIndex/newIndex
     */
    public Set<XId> excludedIds;

    /**
     * Id of an object holding fields with ids in excludedIds.
     */
    public XId excludedObjectId;

    /**
     * String which only exists as a value in fields which Id is in excludedIds.
     */
    public String excludedValueString;

    /**
     * a set of Ids which will be indexed by
     * oldExcludedAllIndex/newExcludeAllIndex. Only fields which Id are in this
     * set will be indexed by theses indexes.
     */
    public Set<XId> includedIds;

    /**
     * the id of an object holding fields with ids in includedIds.
     */
    public XId includedObjectId;

    /**
     * String which only exists as a value in fields which Id is in includedIds.
     */
    public String includedValueString;

    /**
     * Method which initializes the indexes in the setup-method.
     *
     * You need to adhere to the following guidelines if you want to overwrite
     * this method, otherwise the tests won't work correctly.
     *
     * {@link XFieldLevelIndexTest#oldIndex} needs to be parameterized with
     * {@link XFieldLevelIndexTest#oldModel} (as the model which it indexes,
     * index(oldModel) needs to be executed!) and
     * {@link XFieldLevelIndexTest#oldIndexer} (as the used indexer).
     * {@link XFieldLevelIndexTest#newIndex} needs to be parameterized with
     * {@link XFieldLevelIndexTest#newModel} (as the model which it indexes,
     * index(newModel) needs to be executed!) and with
     * {@link XFieldLevelIndexTest#newIndexer} (as the used indexer).
     * defaultIncludeAll needs to be set to true, includeFieldIds should be an
     * empty set. excludeFieldIds has to only include {@link XId XIds} which are
     * NOT used in the phonebook model of {@link DemoModelUtil} (if you include
     * such {@link XId XIds}, the test will not work correctly), it must not be
     * empty, at least one such Id must be provided. Furthermore,
     * {@link XFieldLevelIndexTest#excludedIds} needs to include the same
     * {@link XId XIds} as excludeFieldIds.
     *
     * {@link XFieldLevelIndexTest#oldExcludeAllIndex} needs to be parameterized
     * with {@link XFieldLevelIndexTest#oldExcludeAllModel} (as the model which
     * it indexes, index(oldExcludeAllModel) needs to be executed!) and
     * {@link XFieldLevelIndexTest#oldExcludeAllIndexer} (as the used indexer).
     * {@link XFieldLevelIndexTest#newExcludeAllIndex} needs to be parameterized
     * with {@link XFieldLevelIndexTest#newExcludeAllModel} (as the model which
     * it indexes, index(newExcludeAllModel) needs to be executed!) and with
     * {@link XFieldLevelIndexTest#newIndexer} (as the used indexer).
     * defaultIncludeAll needs to be set to false, excludeFieldIds should be an
     * empty set. includeFieldIds has to only include {@link XId XIds} which are
     * NOT used in the phonebook model of {@link DemoModelUtil} (if you include
     * such {@link XId XIds}, the test will not work correctly), it must not be
     * empty, at least two such Ids must be provided. Furthermore,
     * {@link XFieldLevelIndexTest#includedIds} needs to include the same
     * {@link XId XIds} as includeFieldIds.
     */
    public void initializeIndexes() {
        // oldModel, oldIndexer, newModel, newIndexer, excludedIds and
        // includedIds need to be set before calling all this!
        this.oldIndex = new XFieldLevelIndex(this.oldModel, this.oldIndexer, true, null,
                this.excludedIds);
        this.newIndex = new XFieldLevelIndex(this.newModel, this.newIndexer, true, null,
                this.excludedIds);

        this.oldExcludeAllIndex = new XFieldLevelIndex(this.oldExcludeAllModel,
                this.oldExcludeAllIndexer, false, this.includedIds, null);
        this.newExcludeAllIndex = new XFieldLevelIndex(this.newExcludeAllModel,
                this.newExcludeAllIndexer, false, this.includedIds, null);
    }

    /**
     * Abstract method which initializes the indexers in the setup-method.
     *
     * Needs to be overwritten, otherwise the test won't work correctly.
     */
    public abstract void initializeIndexers();

    public void initializeExcludedAndIncludedIds() {
        this.excludedIds = new HashSet<XId>();
        this.includedIds = new HashSet<XId>();
        for(int i = 0; i < 13; i++) {
            final XId excludedId = Base.toId("ToBeExcluded" + i);
            final XId includedId = Base.toId("ToBeIncluded" + i);

            this.excludedIds.add(excludedId);
            this.includedIds.add(includedId);
        }
    }

    @Before
    public void setup() {
        final XId actorId = Base.createUniqueId();

        final XRepository repo1 = X.createMemoryRepository(actorId);
        DemoModelUtil.addPhonebookModel(repo1);

        this.oldModel = repo1.getModel(DemoModelUtil.PHONEBOOK_ID);

        final XRepository repo2 = X.createMemoryRepository(actorId);
        DemoModelUtil.addPhonebookModel(repo2);

        this.newModel = repo2.getModel(DemoModelUtil.PHONEBOOK_ID);

        final XRepository repo3 = X.createMemoryRepository(actorId);
        DemoModelUtil.addPhonebookModel(repo3);

        this.oldExcludeAllModel = repo3.getModel(DemoModelUtil.PHONEBOOK_ID);

        final XRepository repo4 = X.createMemoryRepository(actorId);
        DemoModelUtil.addPhonebookModel(repo4);

        this.newExcludeAllModel = repo4.getModel(DemoModelUtil.PHONEBOOK_ID);

        initializeExcludedAndIncludedIds();

        this.excludedObjectId = Base.createUniqueId();
        final XObject oldExcludedObject = this.oldModel.createObject(this.excludedObjectId);
        final XObject newExcludedObject = this.newModel.createObject(this.excludedObjectId);

        this.excludedValueString = "thisvalueisexcludedfromindexingbecauseitonlyexistsinexcludedfields";

        assertNotNull("excludedIds must not be null!", this.excludedIds);
        assertFalse("excludedIds needs to contain at least one Id!", this.excludedIds.isEmpty());

        for(final XId id : this.excludedIds) {
            final XField oldField = oldExcludedObject.createField(id);
            final XField newField = newExcludedObject.createField(id);

            final XValue value = BaseRuntime.getValueFactory().createStringValue(this.excludedValueString);

            oldField.setValue(value);
            newField.setValue(value);
        }

        this.includedObjectId = Base.createUniqueId();
        final XObject oldIncludedObject = this.oldExcludeAllModel.createObject(this.includedObjectId);
        final XObject newIncludedObject = this.newExcludeAllModel.createObject(this.includedObjectId);

        this.includedValueString = "thisvalueisincludedeventhoughweexcludealmosteverything";

        assertNotNull("includedIds must not be null", this.includedIds);
        assertFalse("includedIds needs to contain at least two Ids!", this.includedIds.size() < 2);

        for(final XId id : this.includedIds) {
            final XField oldField = oldIncludedObject.createField(id);
            final XField newField = newIncludedObject.createField(id);

            final XValue value = BaseRuntime.getValueFactory().createStringValue(this.includedValueString);

            oldField.setValue(value);
            newField.setValue(value);
        }

        initializeIndexers();

        initializeIndexes();

    }

    /**
     * Returns the set of {@link XAddress XAddresses} implicitly stored in the
     * given set of {@link Pair Pairs} of {@link XAddress} and {@link XValue}
     *
     * @param pairs
     * @return the set of {@link XAddress XAddresses} implicitly stored in the
     *         given set of {@link Pair Pairs} of {@link XAddress} and
     *         {@link XValue}
     */
    private static Set<XAddress> getAddressesFromSetOfPairs(final Set<ValueIndexEntry> pairs) {
        final HashSet<XAddress> addresses = new HashSet<XAddress>();

        for(final ValueIndexEntry pair : pairs) {
            final XAddress address = pair.getAddress();
            assertEquals(XType.XFIELD, address.getAddressedType());
            addresses.add(address);
        }

        return addresses;
    }

    /**
     * Tests whether indexing an XModel works correctly when defaultIncludeAll
     * is set to true.
     *
     * Note: index(oldModel) and index(newModel) were already called in the
     * setup() by initializing the indexes.
     */
    @Test
    public void testIndexingXModel() {

        for(final XId objectId : this.oldModel) {
            final XObject object = this.oldModel.getObject(objectId);

            for(final XId fieldId : object) {
                final XField field = object.getField(fieldId);
                final XValue value = field.getValue();

                final List<String> list = this.oldIndexer.getIndexStrings(value);

                for(final String s : list) {
                    final Set<ValueIndexEntry> entries = this.oldIndex.search(s);

                    if(this.excludedIds.contains(fieldId)) {
                        /**
                         * the value of the current field was excluded from
                         * indexing. Since all theses fields have a the same
                         * value which should not exist anywhere else, the
                         * returned set of pairs should be empty.
                         */
                        assertTrue(entries.isEmpty());
                    } else {
                        final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
                        assertTrue(addresses.contains(field.getAddress()));
                    }
                }
            }
        }
    }

    /**
     * Tests whether indexing an XModel works correctly when defaultIncludeAll
     * is set to false.
     *
     * Note: index(oldExcludeAllModel) and index(newExcludeAllModel) were
     * already called in the setup() by initializing the indexes.
     */
    @Test
    public void testIndexingExcludeAllModel() {

        for(final XId objectId : this.oldExcludeAllModel) {
            final XObject object = this.oldExcludeAllModel.getObject(objectId);

            for(final XId fieldId : object) {
                final XField field = object.getField(fieldId);
                final XValue value = field.getValue();

                final List<String> list = this.oldExcludeAllIndexer.getIndexStrings(value);

                for(final String s : list) {
                    final Set<ValueIndexEntry> entries = this.oldExcludeAllIndex.search(s);

                    if(this.includedIds.contains(fieldId)) {
                        final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
                        assertTrue(addresses.contains(field.getAddress()));
                    } else {
                        /**
                         * almost all fields were excluded from indexing.
                         */
                        assertTrue(entries.isEmpty());
                    }
                }
            }
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new field with a new value gets added to the
     * object.
     */
    @Test
    public void testUpdateIndexObjectAddNewFieldWithValue() {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);

        // check that no entry for valueString exists in the old index
        final Set<ValueIndexEntry> oldSet = this.oldIndex.search(valueString);
        assertTrue(oldSet.isEmpty());

        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        // add the new value, update index and check whether an entry exists or
        // not
        final XId id = Base.createUniqueId();
        final XField newField = newJohn.createField(id);
        newField.setValue(value);

        this.newIndex.updateIndex(oldJohn, newJohn);

        final Set<ValueIndexEntry> set = this.newIndex.search(valueString);
        assertEquals(1, set.size());
        assertEquals(newField.getAddress(), set.iterator().next().getAddress());
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new field with a new value and gets added to the
     * object, where the {@link XId} of the field is one of the Ids of fields
     * the index will not index (i.e. the field is excluded from being indexed).
     */
    @Test
    public void testUpdateIndexObjectAddNewFieldWithValueAndExcludedId() {
        // Tests adding a field with an ID in excludedIds (should not be
        // indexed)
        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        final XId id = this.excludedIds.iterator().next();
        final XField newField = newJohn.createField(id);
        final XValue value = BaseRuntime.getValueFactory().createStringValue(this.excludedValueString);
        newField.setValue(value);

        this.newIndex.updateIndex(oldJohn, newJohn);

        final Set<ValueIndexEntry> set = this.newIndex.search(this.excludedValueString);
        assertTrue(set.isEmpty());
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new field with a new value gets added to the
     * object, where the index excludes almost all fields from being indexed
     * (i.e. defaultIncludeAll is set to false).
     */
    @Test
    public void testUpdateIndexObjectAddNewFieldWithValueInExcludeAllModel() {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);

        // check that no entry for valueString exists in the old index
        final Set<ValueIndexEntry> oldSet = this.oldExcludeAllIndex.search(valueString);
        assertTrue(oldSet.isEmpty());

        final XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);

        // add the new value, update index and check whether an entry exists or
        // not
        XId fieldId;
        do {
            fieldId = Base.createUniqueId();
        } while(this.includedIds.contains(fieldId));

        XField newField = newJohn.createField(fieldId);
        newField.setValue(value);

        this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);

        Set<ValueIndexEntry> set = this.newExcludeAllIndex.search(valueString);
        assertTrue(set.isEmpty());

        // Add a value to a field with ID in includedIds ( = should be indexed)

        final XId objectId = Base.createUniqueId();
        final XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
        final XObject newObject = this.newExcludeAllModel.createObject(objectId);

        fieldId = this.includedIds.iterator().next();
        newField = newObject.createField(fieldId);
        newField.setValue(value);

        this.newExcludeAllIndex.updateIndex(oldObject, newObject);

        set = this.newExcludeAllIndex.search(valueString);
        assertEquals(1, set.size());
        assertEquals(newField.getAddress(), set.iterator().next().getAddress());
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexObjectAddValue() {
        testUpdateIndexAddValue(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexFieldAddValue() {
        testUpdateIndexAddValue(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a new value gets added to a field of an object, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexEventAddValue() {
        testUpdateIndexAddValue(TestType.XEVENT);
    }

    private void testUpdateIndexAddValue(final TestType type) {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);

        // check that no entry for valueString exists in the old index
        final Set<ValueIndexEntry> oldSet = this.oldIndex.search(valueString);
        assertTrue(oldSet.isEmpty());

        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        // add the new value, update index and check whether an entry exists or
        // not
        final XId id = Base.createUniqueId();

        final XField oldField = oldJohn.createField(id);
        final XField newField = newJohn.createField(id);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newJohn.addListenerForFieldEvents(listener);

        newField.setValue(value);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertNotNull(event);
            assertTrue(event.getClass().toString(), event instanceof XFieldEvent);
            assertEquals(ChangeType.ADD, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, null);
            break;
        }

        final Set<ValueIndexEntry> newSet = this.newIndex.search(valueString);
        assertEquals(1, newSet.size());
        assertEquals(newField.getAddress(), newSet.iterator().next().getAddress());
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state and which is excluded from being
     * indexed.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * has some fields which are excluded from being indexed.
     */
    @Test
    public void testUpdateIndexObjectAddValueToExcludedField() {
        testUpdateIndexAddValueToExcludedField(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state and which is excluded from being
     * indexed.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * has some fields which are excluded from being indexed.
     */
    @Test
    public void testUpdateIndexFieldAddValueToExcludedField() {
        testUpdateIndexAddValueToExcludedField(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a new value gets added to a field of an object, which
     * already existed in the old state and which is excluded from being
     * indexed.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * has some fields which are excluded from being indexed.
     */
    @Test
    public void testUpdateIndexEventAddValueToExcludedField() {
        testUpdateIndexAddValueToExcludedField(TestType.XEVENT);
    }

    private void testUpdateIndexAddValueToExcludedField(final TestType type) {
        final XId id = Base.createUniqueId();
        final XId fieldId = this.excludedIds.iterator().next();

        final XObject oldExcludedObject = this.oldModel.createObject(id);
        final XField oldExcludedField = oldExcludedObject.createField(fieldId);

        final XObject newExcludedObject = this.newModel.createObject(id);
        final XField newExcludedField = newExcludedObject.createField(fieldId);

        final XValue value = BaseRuntime.getValueFactory().createStringValue(this.excludedValueString);
        final List<String> newIndexStrings = this.newIndexer.getIndexStrings(value);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newExcludedObject.addListenerForFieldEvents(listener);

        newExcludedField.setValue(value);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldExcludedObject, newExcludedObject);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldExcludedField, newExcludedField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.ADD, event.getChangeType());
            assertEquals(newExcludedField.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, null);
        }

        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }

    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is added and when a value of field
     * which IS being index is added.
     */
    @Test
    public void testUpdateIndexObjectAddValueToFieldOfExcludeAllModel() {
        testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a new value gets added to a field of an object,
     * which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is added and when a value of field
     * which IS being index is added.
     */
    @Test
    public void testUpdateIndexFieldAddValueToFieldOfExcludeAllModel() {
        testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a new value gets added to a field of an object, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is added and when a value of field
     * which IS being index is added.
     */
    @Test
    public void testUpdateIndexEventAddValueToFieldOfExcludeAllModel() {
        testUpdateIndexAddValueToFieldOfExcludeAllModel(TestType.XEVENT);
    }

    private void testUpdateIndexAddValueToFieldOfExcludeAllModel(final TestType type) {
        final XId id = Base.createUniqueId();
        final XId fieldId = Base.createUniqueId();

        final XObject oldExcludedObject = this.oldExcludeAllModel.createObject(id);
        final XField oldExcludedField = oldExcludedObject.createField(fieldId);

        final XObject newExcludedObject = this.newExcludeAllModel.createObject(id);
        final XField newExcludedField = newExcludedObject.createField(fieldId);

        final XValue value = BaseRuntime.getValueFactory().createStringValue(this.excludedValueString);
        final List<String> newIndexStrings = this.newIndexer.getIndexStrings(value);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newExcludedObject.addListenerForFieldEvents(listener);

        newExcludedField.setValue(value);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldExcludedObject, newExcludedObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldExcludedField, newExcludedField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.ADD, event.getChangeType());
            assertEquals(newExcludedField.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, null);
        }

        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertTrue(found.isEmpty());
        }

        // add value to field which is not exlude (i.e. which id is in
        // includedIds)
        final XId includedId = this.includedIds.iterator().next();

        final XField oldIncludedField = oldExcludedObject.createField(includedId);
        final XField newIncludedField = newExcludedObject.createField(includedId);

        final DummyFieldEventListener listener2 = new DummyFieldEventListener();
        newExcludedObject.addListenerForFieldEvents(listener2);

        newIncludedField.setValue(value);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldExcludedObject, newExcludedObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldIncludedField, newIncludedField);
            break;
        case XEVENT:
            final XEvent event = listener2.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.ADD, event.getChangeType());
            assertEquals(newIncludedField.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, null);
        }

        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertFalse(found.isEmpty());
            assertEquals(1, found.size());
            assertEquals(newIncludedField.getAddress(), found.iterator().next().getAddress());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value, which only existed once, gets removed from
     * a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexObjectDeleteValue() {
        testUpdateIndexDeleteValue(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value, which only existed once, gets removed from
     * a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexFieldDeleteValue() {
        testUpdateIndexDeleteValue(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value, which only existed once, gets removed from a
     * field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexEventDeleteValue() {
        testUpdateIndexDeleteValue(TestType.XEVENT);
    }

    private void testUpdateIndexDeleteValue(final TestType type) {
        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        // Add not yet existing value, update index, remove it again, update
        // index and check whether no entry for this value exists anymore or
        // not

        final XId testId = BaseRuntime.getIDProvider().createUniqueId();
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newIndexer.getIndexStrings(value);

        final XField oldField = oldJohn.createField(testId);
        final XField newField = newJohn.createField(testId);

        newField.setValue(value);

        this.newIndex.updateIndex(oldJohn, newJohn);

        oldField.setValue(value);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newJohn.addListenerForFieldEvents(listener);

        newField.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;

            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, value);
            break;
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value, which only existed once, gets removed from
     * a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexObjectDeleteValueFromExcludeAllModel() {
        testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value, which only existed once, gets removed from
     * a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexFieldDeleteValueFromExcludeAllModel() {
        testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value, which only existed once, gets removed from a
     * field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexEventDeleteValueFromExcludeAllModel() {
        testUpdateIndexDeleteValueFromExcludeAllModel(TestType.XEVENT);
    }

    private void testUpdateIndexDeleteValueFromExcludeAllModel(final TestType type) {
        final XId objectId = Base.createUniqueId();
        final XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
        final XObject newObject = this.newExcludeAllModel.createObject(objectId);

        // Add not yet existing value, update index, remove it again, update
        // index and check whether no entry for this value exists anymore or
        // not

        final XId includedId = this.includedIds.iterator().next();
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newIndexer.getIndexStrings(value);

        final XField oldField = oldObject.createField(includedId);
        final XField newField = newObject.createField(includedId);

        newField.setValue(value);

        this.newExcludeAllIndex.updateIndex(oldObject, newObject);

        oldField.setValue(value);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newObject.addListenerForFieldEvents(listener);

        newField.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldObject, newObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;

            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
            break;
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value, which existed multiple times, gets removed
     * from a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexObjectDeleteMultipleExistingValue() {
        testUpdateIndexDeleteMultipleExistingValue(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value, which existed multiple times, gets removed
     * from a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexFieldDeleteMultipleExistingValue() {
        testUpdateIndexDeleteMultipleExistingValue(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value, which existed multiple times, gets removed from a
     * field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexEventDeleteMultiplyExistingValue() {
        testUpdateIndexDeleteMultipleExistingValue(TestType.XEVENT);
    }

    private void testUpdateIndexDeleteMultipleExistingValue(final TestType type) {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newIndexer.getIndexStrings(value);

        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        final XId id1 = Base.createUniqueId();
        final XId id2 = Base.createUniqueId();

        final XField newField1 = newJohn.createField(id1);
        final XField newField2 = newJohn.createField(id2);

        newField1.setValue(value);
        newField2.setValue(value);

        // update index and update the old model, so that it can be used again
        this.newIndex.updateIndex(oldJohn, newJohn);

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> entries = this.newIndex.search(s);

            final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);

            /*
             * Only two fields should hold this value, newField1 & newField2
             */
            assertEquals(2, addresses.size());
            assertTrue(addresses.contains(newField1.getAddress()));
            assertTrue(addresses.contains(newField2.getAddress()));
        }

        final XField oldField1 = oldJohn.createField(id1);
        final XField oldField2 = oldJohn.createField(id2);

        oldField1.setValue(value);
        oldField2.setValue(value);

        // Remove value once, update, and check again
        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newJohn.addListenerForFieldEvents(listener);

        newField1.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldField1, newField1);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField1.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, value);
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> entries = this.newIndex.search(s);
            final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
            /*
             * The value should still be indexed, since it existed multiple
             * times
             */

            assertEquals(1, addresses.size());
            assertTrue(addresses.contains(newField2.getAddress()));
        }

        oldField1.setValue(null);

        // Remove value completely

        newField2.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldField2, newField2);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField2.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, value);
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> entries = this.newIndex.search(s);
            final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
            /*
             * The value should be completely deindexed now
             */

            assertTrue(addresses.isEmpty());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value, which existed multiple times, gets removed
     * from a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexObjectDeleteMultipleExistingValueFromExcludeAllModel() {
        testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value, which existed multiple times, gets removed
     * from a field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexFieldDeleteMultipleExistingValueFromExcludeAllModel() {
        testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value, which existed multiple times, gets removed from a
     * field of an object, which already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexEventDeleteMultipleExistingValueFromExcludeAllModel() {
        testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(TestType.XEVENT);
    }

    private void testUpdateIndexDeleteMultipleExistingValueFromExcludeAllModel(final TestType type) {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newIndexer.getIndexStrings(value);

        final XId objectId = Base.createUniqueId();
        final XObject oldObject = this.oldModel.createObject(objectId);
        final XObject newObject = this.newModel.createObject(objectId);

        final Iterator<XId> iterator = this.includedIds.iterator();
        final XId id1 = iterator.next();
        final XId id2 = iterator.next();

        final XField newField1 = newObject.createField(id1);
        final XField newField2 = newObject.createField(id2);

        newField1.setValue(value);
        newField2.setValue(value);

        // update index and update the old model, so that it can be used again
        this.newExcludeAllIndex.updateIndex(oldObject, newObject);

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> pairs = this.newExcludeAllIndex.search(s);

            final Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);

            /*
             * Only two fields should hold this value, newField1 & newField2
             */
            assertEquals(2, addresses.size());
            assertTrue(addresses.contains(newField2.getAddress()));
        }

        final XField oldField1 = oldObject.createField(id1);
        final XField oldField2 = oldObject.createField(id2);

        oldField1.setValue(value);
        oldField2.setValue(value);

        // Remove value once, update, and check again
        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newObject.addListenerForFieldEvents(listener);

        newField1.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldObject, newObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldField1, newField1);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField1.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> entries = this.newExcludeAllIndex.search(s);
            final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
            /*
             * The value should still be indexed, since it existed multiple
             * times
             */

            assertEquals(1, addresses.size());
            assertTrue(addresses.contains(newField2.getAddress()));
        }

        oldField1.setValue(null);

        // Remove value completely

        newField2.setValue(null);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldObject, newObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldField2, newField2);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.REMOVE, event.getChangeType());
            assertEquals(newField2.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, value);
        }

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> entries = this.newExcludeAllIndex.search(s);
            final Set<XAddress> addresses = getAddressesFromSetOfPairs(entries);
            /*
             * The value should be completely deindexed now
             */

            assertTrue(addresses.isEmpty());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexObjectChangeValue() {
        testUpdateIndexChangeValue(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexFieldChangeValue() {
        testUpdateIndexChangeValue(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexEventChangeValue() {
        testUpdateIndexChangeValue(TestType.XEVENT);
    }

    private void testUpdateIndexChangeValue(final TestType type) {
        final String valueString1 = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue oldValue = BaseRuntime.getValueFactory().createStringValue(valueString1);
        final List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);

        final String valueString2 = "Anothervaluestringwhichshouldntexistanywhere";
        final XValue newValue = BaseRuntime.getValueFactory().createStringValue(valueString2);
        final List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);

        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        final XId id = BaseRuntime.getIDProvider().createUniqueId();
        final XField oldField = oldJohn.createField(id);
        final XField newField = newJohn.createField(id);

        newField.setValue(oldValue);
        this.newIndex.updateIndex(oldJohn, newJohn);

        oldField.setValue(oldValue);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newJohn.addListenerForFieldEvents(listener);

        newField.setValue(newValue);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.CHANGE, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, oldValue);
        }

        // make sure the old value was deindexed
        for(final String s : oldIndexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }

        // make sure the new value was indexed
        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertEquals(1, found.size());
            assertEquals(newField.getAddress(), found.iterator().next().getAddress());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * contains fields which are being excluded from indexing. This tests
     * whether if the method behaves correctly if the value of such a field is
     * changed.
     */
    @Test
    public void testUpdateIndexObjectChangeValueOfExcludedField() {
        testUpdateIndexChangeValueOfExcludedField(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * contains fields which are being excluded from indexing. This tests
     * whether if the method behaves correctly if the value of such a field is
     * changed.
     */
    @Test
    public void testUpdateIndexFieldChangeValueOfExcludedField() {
        testUpdateIndexChangeValueOfExcludedField(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true, but
     * contains fields which are being excluded from indexing. This tests
     * whether if the method behaves correctly if the value of such a field is
     * changed.
     */
    @Test
    public void testUpdateIndexEventChangeValueOfExcludedField() {
        testUpdateIndexChangeValueOfExcludedField(TestType.XEVENT);
    }

    private void testUpdateIndexChangeValueOfExcludedField(final TestType type) {
        final XId fieldId = this.excludedIds.iterator().next();

        final XObject oldExcludedObject = this.oldModel.getObject(this.excludedObjectId);
        final XField oldExcludedField = oldExcludedObject.getField(fieldId);

        final XObject newExcludedObject = this.newModel.getObject(this.excludedObjectId);
        final XField newExcludedField = newExcludedObject.getField(fieldId);

        final XValue oldValue = BaseRuntime.getValueFactory().createStringValue(this.excludedValueString);
        final XValue newValue = BaseRuntime.getValueFactory().createStringValue(
                this.excludedValueString + "addingsomethingnew");
        final List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);

        final DummyFieldEventListener listener = new DummyFieldEventListener();
        newExcludedObject.addListenerForFieldEvents(listener);

        newExcludedField.setValue(newValue);

        switch(type) {
        case XOBJECT:
            this.newIndex.updateIndex(oldExcludedObject, newExcludedObject);
            break;
        case XFIELD:
            this.newIndex.updateIndex(oldExcludedField, newExcludedField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.CHANGE, event.getChangeType());
            assertEquals(newExcludedField.getAddress(), event.getChangedEntity());

            this.newIndex.updateIndex((XFieldEvent)event, oldValue);
        }

        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }

    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is changed and when a value of field
     * which IS being index is changed.
     */
    @Test
    public void testUpdateIndexObjectChangeValueInExcludeAllModel() {
        testUpdateIndexChangeValueInExcludeAllModel(TestType.XOBJECT);
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableField, XReadableField)}
     * works correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is changed and when a value of field
     * which IS being index is changed.
     */
    @Test
    public void testUpdateIndexFieldChangeValueInExcludeAllModel() {
        testUpdateIndexChangeValueInExcludeAllModel(TestType.XFIELD);
    }

    /**
     * Tests if {@link XFieldLevelIndex#updateIndex(XFieldEvent, XValue)} works
     * correctly when a value of a field of an object gets changed, which
     * already existed in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false,
     * but has some fields which are still being indexed (i.e. the
     * includedFieldIds set is not empty). Tests both the cases when a value of
     * a field, which is not being indexed, is changed and when a value of field
     * which IS being index is changed.
     */
    @Test
    public void testUpdateIndexEventChangeValueInExcludeAllModel() {
        testUpdateIndexChangeValueInExcludeAllModel(TestType.XEVENT);
    }

    private void testUpdateIndexChangeValueInExcludeAllModel(final TestType type) {
        // change value of field which is excluded from indexing

        final String valueString1 = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue oldValue = BaseRuntime.getValueFactory().createStringValue(valueString1);
        final List<String> oldIndexStrings = this.newIndexer.getIndexStrings(oldValue);

        final String valueString2 = "Anothervaluestringwhichshouldntexistanywhere";
        final XValue newValue = BaseRuntime.getValueFactory().createStringValue(valueString2);
        final List<String> newIndexStrings = this.newIndexer.getIndexStrings(newValue);

        final XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);

        XId id = BaseRuntime.getIDProvider().createUniqueId();
        XField oldField = oldJohn.createField(id);
        XField newField = newJohn.createField(id);

        newField.setValue(oldValue);
        this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);

        oldField.setValue(oldValue);

        DummyFieldEventListener listener = new DummyFieldEventListener();
        newJohn.addListenerForFieldEvents(listener);

        newField.setValue(newValue);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.CHANGE, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, oldValue);
        }

        // make sure that nothing happened
        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertTrue(found.isEmpty());
        }

        // change value of field which is indexed

        final XId objectId = Base.createUniqueId();
        final XObject oldObject = this.oldExcludeAllModel.createObject(objectId);
        final XObject newObject = this.newExcludeAllModel.createObject(objectId);

        id = this.includedIds.iterator().next();
        oldField = oldObject.createField(id);
        newField = newObject.createField(id);

        newField.setValue(oldValue);
        this.newExcludeAllIndex.updateIndex(oldObject, newObject);

        oldField.setValue(oldValue);

        listener = new DummyFieldEventListener();
        newObject.addListenerForFieldEvents(listener);

        newField.setValue(newValue);

        switch(type) {
        case XOBJECT:
            this.newExcludeAllIndex.updateIndex(oldObject, newObject);
            break;
        case XFIELD:
            this.newExcludeAllIndex.updateIndex(oldField, newField);
            break;
        case XEVENT:
            final XEvent event = listener.event;
            assertTrue(event instanceof XFieldEvent);
            assertEquals(ChangeType.CHANGE, event.getChangeType());
            assertEquals(newField.getAddress(), event.getChangedEntity());

            this.newExcludeAllIndex.updateIndex((XFieldEvent)event, oldValue);
        }

        // make sure the old value was deindexed
        for(final String s : oldIndexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertTrue(found.isEmpty());
        }

        // make sure the new value was indexed
        for(final String s : newIndexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertEquals(1, found.size());
            assertEquals(newField.getAddress(), found.iterator().next().getAddress());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a field an object gets remove, which already existed
     * in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testUpdateIndexObjectRemoveField() {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newIndexer.getIndexStrings(value);

        final XObject oldJohn = this.oldModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        final XId id = BaseRuntime.getIDProvider().createUniqueId();
        newJohn.createField(id).setValue(value);

        this.newIndex.updateIndex(oldJohn, newJohn);
        oldJohn.createField(id).setValue(value);

        newJohn.removeField(id);
        this.newIndex.updateIndex(oldJohn, newJohn);

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> found = this.newIndex.search(s);
            assertTrue(found.isEmpty());
        }
    }

    /**
     * Tests if
     * {@link XFieldLevelIndex#updateIndex(XReadableObject, XReadableObject)}
     * works correctly when a field an object gets remove, which already existed
     * in the old state.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testUpdateIndexObjectRemoveFieldOfExcludeAllModel() {
        final String valueString = "Firstvaluestringwhichshoudlntexistinbothmodels";
        final XValue value = BaseRuntime.getValueFactory().createStringValue(valueString);
        final List<String> indexStrings = this.newExcludeAllIndexer.getIndexStrings(value);

        final XObject oldJohn = this.oldExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);
        final XObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);

        final XId id = this.includedIds.iterator().next();
        newJohn.createField(id).setValue(value);

        this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);
        oldJohn.createField(id).setValue(value);

        newJohn.removeField(id);
        this.newExcludeAllIndex.updateIndex(oldJohn, newJohn);

        for(final String s : indexStrings) {
            final Set<ValueIndexEntry> found = this.newExcludeAllIndex.search(s);
            assertTrue(found.isEmpty());
        }
    }

    /**
     * Tests if {@link XFieldLevelIndex#deIndex(XReadableObject)} works
     * correctly.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testDeIndexObject() {
        final XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);
        this.newIndex.deIndex(newJohn);

        testIfObjectWasDeIndexed(newJohn, this.newIndex, this.newIndexer);
    }

    /**
     * Tests if {@link XFieldLevelIndex#deIndex(XReadableObject)} works
     * correctly.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testDeIndexObjectOfExcludeAllModel() {
        final XReadableObject includedObject = this.newExcludeAllModel.getObject(this.includedObjectId);
        this.newExcludeAllIndex.deIndex(includedObject);

        testIfObjectWasDeIndexed(includedObject, this.newExcludeAllIndex, this.newExcludeAllIndexer);
    }

    /**
     * Tests if {@link XFieldLevelIndex#deIndex(XReadableField)} works
     * correctly.
     *
     * This tests an index which defaultIncludeAll parameter is set to true.
     */
    @Test
    public void testDeIndexField() {
        final XReadableObject newJohn = this.newModel.getObject(DemoModelUtil.JOHN_ID);

        for(final XId fieldId : newJohn) {
            final XReadableField field = newJohn.getField(fieldId);
            this.newIndex.deIndex(field);
        }

        // deindexing all fields equals deindexing the whole object
        testIfObjectWasDeIndexed(newJohn, this.newIndex, this.newIndexer);
    }

    /**
     * Tests if {@link XFieldLevelIndex#deIndex(XReadableField)} works
     * correctly.
     *
     * This tests an index which defaultIncludeAll parameter is set to false.
     */
    @Test
    public void testDeIndexFieldOfExcludeAllModel() {
        final XReadableObject newJohn = this.newExcludeAllModel.getObject(DemoModelUtil.JOHN_ID);

        for(final XId fieldId : newJohn) {
            final XReadableField field = newJohn.getField(fieldId);
            this.newExcludeAllIndex.deIndex(field);
        }

        // deindexing all fields equals deindexing the whole object
        testIfObjectWasDeIndexed(newJohn, this.newExcludeAllIndex, this.newExcludeAllIndexer);
    }

    private static void testIfObjectWasDeIndexed(final XReadableObject object, final XFieldLevelIndex index,
            final XValueIndexer indexer) {

        for(final XId fieldId : object) {
            final XReadableField field = object.getField(fieldId);
            final XAddress fieldAddress = field.getAddress();

            final XValue value = field.getValue();
            final List<String> indexStrings = indexer.getIndexStrings(value);

            for(final String s : indexStrings) {
                final Set<ValueIndexEntry> pairs = index.search(s);
                final Set<XAddress> addresses = getAddressesFromSetOfPairs(pairs);

                assertFalse(addresses.contains(fieldAddress));
            }
        }
    }
}
