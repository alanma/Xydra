package org.xydra.core;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XIdSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;


/**
 * This demo model is supposed to contain each XModel data-type and construct at
 * least once.
 *
 * @author xamde
 *
 */
public class DemoModelUtil {

    public static final XId ACTOR_ID = XX.toId("DemoModel");

    private static final XId ADDRESS_ID = XX.toId("address");
    private static final XAddress ADDRESS_VALUE = XX.toAddress("/hello/world/-/-");
    private static final XId ADDRESSLIST_ID = XX.toId("addressList");

    private static final XAddressListValue ADDRESSLIST_VALUE = XV.toValue(new XAddress[] {
            ADDRESS_VALUE, null });
    private static final XId ADDRESSSET_ID = XX.toId("addressSet");

    private static final XAddressSetValue ADDRESSSET_VALUE = XV.toAddressSetValue(new XAddress[] {
            ADDRESS_VALUE, null });
    public static final XId ADDRESSSORTEDSET_ID = XX.toId("addressSortedSet");

    private static final XAddressSortedSetValue ADDRESSSORTEDSET_VALUE = XV
            .toAddressSortedSetValue(new XAddress[] { XX.toAddress("/hello/b/-/-"),
                    XX.toAddress("/hello/a/-/-") });
    public static final XId AGE_ID = XX.toId("age");

    public static final XIntegerValue AGE_VALUE = XV.toValue(42);
    public static final XId ALIASES_ID = XX.toId("aliases");

    public static final XStringListValue ALIASES_VALUE = XV.toValue(new String[] { "Johnny",
            "John the Man", "Cookie Monster" });

    public static final XId CLAUDIA_ID = XX.toId("claudia");
    public static final XId COOKIE1_ID = XX.toId("cookie1");

    public static final XId COOKIE2_ID = XX.toId("cookie2");
    public static final XId COOKIENAMES_ID = XX.toId("cookienames");

    public static final XStringSetValue COOKIENAMES_VALUE = XV.toStringSetValue(new String[] {
            "Chocolate Chip", "Almond" });
    public static final XId COOKIES_ID = XX.toId("cookies");

    public static final XIdSetValue COOKIES_VALUE = XV.toIdSetValue(new XId[] { COOKIE1_ID,
            COOKIE2_ID });
    public static final XId COORDINATES_ID = XX.toId("coordinates");

    public static final XDoubleListValue COORDINATES_VALUE = XV.toValue(new double[] { 32.465,
            19.34 });
    public static final XId EMPTYFIELD_ID = XX.toId("emptyfield");

    public static final XId FLAGS_ID = XX.toId("flags");
    public static final XBooleanListValue FLAGS_VALUE = XV.toValue(new boolean[] { true, false,
            true, true, false });

    public static final XId FRIENDS_ID = XX.toId("friends");
    public static final XId PETER_ID = XX.toId("peter");

    public static final XIdListValue FRIENDS_VALUE = XV.toValue(new XId[] { CLAUDIA_ID, PETER_ID });

    public static final XId HEIGHT_ID = XX.toId("height");
    public static final XDoubleValue HEIGHT_VALUE = XV.toValue(121.3);

    public static final XId HIDDEN_ID = XX.toId("hidden");

    public static final XBooleanValue HIDDEN_VALUE = XV.toValue(false);
    private static final XId IDSORTEDSET_ID = XX.toId("idSortedSet");

    private static final XIdSortedSetValue IDSORTEDSET_VALUE = XV.toIdSortedSetValue(new XId[] {
            XX.toId("b"), XX.toId("a") });
    public static final XId JOHN_ID = XX.toId("john");
    public static final XId LASTCALLTIMES_ID = XX.toId("lastCallTime");
    public static final XLongListValue LASTCALLTIMES_VALUE = XV.toValue(new long[] { 32456L, 7664L,
            56L });

    public static final XId MAXCALLTIME_ID = XX.toId("maxCallTime");
    public static final XLongValue MAXCALLTIME_VALUE = XV.toValue(675874678478467L);

    public static final XId PHONE_ID = XX.toId("phone");

    public static final XStringValue PHONE_VALUE = XV.toValue("3463-2346");
    public static final XId PHONEBOOK_ID = XX.toId("phonebook");

    public static final XId SCORES_ID = XX.toId("scores");
    public static final XIntegerListValue SCORES_VALUE = XV.toValue(new int[] { 34, 234, 34 });

    public static final XId SIGNATURE_ID = XX.toId("signature");
    public static final XBinaryValue SIGNATURE_VALUE = XV.toValue(new byte[] { 0, 1, 2, 3, 4, 5, 6,
            7, 8, 9, 10, 11, 12, 13, 14, 15, 16, (byte)253, (byte)254, (byte)255 });

    public static final XId SPOUSE_ID = XX.toId("spouse");
    public static final XId TITLE_ID = XX.toId("title");

    public static final XStringValue TITLE_VALUE = XV.toValue("Dr. John Doe");

    public static final long REVISION_AFTER_ADDING_INCLUDING_MODEL_ITSELF = 46;

    /**
     * Add the phonebook-demo model with ID "phonebook" to the given repository.
     *
     * @param repository The repository to add the phonebook to.
     */
    public static void addPhonebookModel(final XRepository repository) {

        // don't add twice
        if(repository.hasModel(PHONEBOOK_ID)) {
            return;
        }

        final XModel model = repository.createModel(PHONEBOOK_ID);
        setupPhonebook(model);
    }

    public static void setupJohn(final XAddress objectAddr, final XTransactionBuilder tb) {

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, TITLE_ID);
        final XAddress titleAddr = Base.resolveField(objectAddr, TITLE_ID);
        tb.addValue(titleAddr, XCommand.FORCED, TITLE_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, PHONE_ID);
        final XAddress phoneAddr = Base.resolveField(objectAddr, PHONE_ID);
        tb.addValue(phoneAddr, XCommand.FORCED, PHONE_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, ALIASES_ID);
        final XAddress aliasesAddr = Base.resolveField(objectAddr, ALIASES_ID);
        tb.addValue(aliasesAddr, XCommand.FORCED, ALIASES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, FRIENDS_ID);
        final XAddress friendsAddr = Base.resolveField(objectAddr, FRIENDS_ID);
        tb.addValue(friendsAddr, XCommand.FORCED, FRIENDS_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, SPOUSE_ID);
        final XAddress spouseAddr = Base.resolveField(objectAddr, SPOUSE_ID);
        tb.addValue(spouseAddr, XCommand.FORCED, CLAUDIA_ID);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, HIDDEN_ID);
        final XAddress hiddenAddr = Base.resolveField(objectAddr, HIDDEN_ID);
        tb.addValue(hiddenAddr, XCommand.FORCED, HIDDEN_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, FLAGS_ID);
        final XAddress flagsAddr = Base.resolveField(objectAddr, FLAGS_ID);
        tb.addValue(flagsAddr, XCommand.FORCED, FLAGS_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, HEIGHT_ID);
        final XAddress heightAddr = Base.resolveField(objectAddr, HEIGHT_ID);
        tb.addValue(heightAddr, XCommand.FORCED, HEIGHT_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, COORDINATES_ID);
        final XAddress coordinatesAddr = Base.resolveField(objectAddr, COORDINATES_ID);
        tb.addValue(coordinatesAddr, XCommand.FORCED, COORDINATES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, AGE_ID);
        final XAddress ageAddr = Base.resolveField(objectAddr, AGE_ID);
        tb.addValue(ageAddr, XCommand.FORCED, AGE_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, SCORES_ID);
        final XAddress scoresAddr = Base.resolveField(objectAddr, SCORES_ID);
        tb.addValue(scoresAddr, XCommand.FORCED, SCORES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, MAXCALLTIME_ID);
        final XAddress maxcalltimeAddr = Base.resolveField(objectAddr, MAXCALLTIME_ID);
        tb.addValue(maxcalltimeAddr, XCommand.FORCED, MAXCALLTIME_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, LASTCALLTIMES_ID);
        final XAddress lastcalltimesAddr = Base.resolveField(objectAddr, LASTCALLTIMES_ID);
        tb.addValue(lastcalltimesAddr, XCommand.FORCED, LASTCALLTIMES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, EMPTYFIELD_ID);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, SIGNATURE_ID);
        final XAddress signatureAddr = Base.resolveField(objectAddr, SIGNATURE_ID);
        tb.addValue(signatureAddr, XCommand.FORCED, SIGNATURE_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, COOKIES_ID);
        final XAddress cookiesAddr = Base.resolveField(objectAddr, COOKIES_ID);
        tb.addValue(cookiesAddr, XCommand.FORCED, COOKIES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, COOKIENAMES_ID);
        final XAddress cookienamesAddr = Base.resolveField(objectAddr, COOKIENAMES_ID);
        tb.addValue(cookienamesAddr, XCommand.FORCED, COOKIENAMES_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, ADDRESS_ID);
        final XAddress addressAddr = Base.resolveField(objectAddr, ADDRESS_ID);
        tb.addValue(addressAddr, XCommand.FORCED, ADDRESS_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, ADDRESSLIST_ID);
        final XAddress addressListAddr = Base.resolveField(objectAddr, ADDRESSLIST_ID);
        tb.addValue(addressListAddr, XCommand.FORCED, ADDRESSLIST_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, ADDRESSSET_ID);
        final XAddress addressSetAddr = Base.resolveField(objectAddr, ADDRESSSET_ID);
        tb.addValue(addressSetAddr, XCommand.FORCED, ADDRESSSET_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, IDSORTEDSET_ID);
        final XAddress idSortedSetAddr = Base.resolveField(objectAddr, IDSORTEDSET_ID);
        tb.addValue(idSortedSetAddr, XCommand.FORCED, IDSORTEDSET_VALUE);

        tb.addField(objectAddr, XCommand.SAFE_STATE_BOUND, ADDRESSSORTEDSET_ID);
        final XAddress addressSortedSetAddr = Base.resolveField(objectAddr, ADDRESSSORTEDSET_ID);
        tb.addValue(addressSortedSetAddr, XCommand.FORCED, ADDRESSSORTEDSET_VALUE);

    }

    public static void setupJohn(final XObject john) {

        final XTransactionBuilder tb = new XTransactionBuilder(john.getAddress());
        setupJohn(john.getAddress(), tb);

        // Execute commands individually.
        for(int i = 0; i < tb.size(); i++) {
            john.executeCommand(tb.getCommand(i));
        }

    }

    public static void setupPhonebook(final XAddress modelAddr, final XTransactionBuilder tb,
            final boolean createModel) {
        if(createModel) {
            assert modelAddr.getParent() != null;
            tb.addModel(modelAddr.getParent(), XCommand.SAFE_STATE_BOUND, modelAddr.getModel());
        }

        tb.addObject(modelAddr, XCommand.SAFE_STATE_BOUND, JOHN_ID);
        tb.addObject(modelAddr, XCommand.SAFE_STATE_BOUND, CLAUDIA_ID);
        tb.addObject(modelAddr, XCommand.SAFE_STATE_BOUND, PETER_ID);

        final XAddress johnAddr = Base.resolveObject(modelAddr, JOHN_ID);

        setupJohn(johnAddr, tb);

        // revision is now REVISION_AFTER_ADDING_INCLUDING_MODEL_ITSELF
    }

    /**
     * @param model within a repository that has been created
     */
    public static void setupPhonebook(final XModel model) {
        assert model.getRevisionNumber() >= 0;
        assert ((IMemoryModel)model).exists();

        final XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
        setupPhonebook(model.getAddress(), tb, false);

        // Execute commands individually.
        for(int i = 0; i < tb.size(); i++) {
            model.executeCommand(tb.getCommand(i));
        }

    }

    public static void main(final String[] args) {
        final XRepository repo = new MemoryRepository(Base.toId("actor"), "secret", Base.toId("repo"));
        addPhonebookModel(repo);
        assert repo.getModel(PHONEBOOK_ID).getRevisionNumber() == REVISION_AFTER_ADDING_INCLUDING_MODEL_ITSELF;
        final XydraOut out = new XmlOut();
        SerializedModel.serialize(repo.getModel(PHONEBOOK_ID), out, true, false, true);
        System.out.println(out.getData());
    }
}
