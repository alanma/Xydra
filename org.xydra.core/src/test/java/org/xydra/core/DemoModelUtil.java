package org.xydra.core;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XCommand;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XByteListValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
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


/**
 * This demo model is supposed to contain each XModel data-type and construct at
 * least once.
 * 
 * @author voelkel
 * 
 */
public class DemoModelUtil {
	
	public static final XID ACTOR_ID = XX.toId("DemoModel");
	
	private static final XID ADDRESS_ID = XX.toId("address");
	private static final XAddress ADDRESS_VALUE = XX.toAddress("/hello/world/-/-");
	private static final XID ADDRESSLIST_ID = XX.toId("addressList");
	
	private static final XAddressListValue ADDRESSLIST_VALUE = XV.toValue(new XAddress[] {
	        ADDRESS_VALUE, null });
	private static final XID ADDRESSSET_ID = XX.toId("addressSet");
	
	private static final XAddressSetValue ADDRESSSET_VALUE = XV.toAddressSetValue(new XAddress[] {
	        ADDRESS_VALUE, null });
	private static final XID ADDRESSSORTEDSET_ID = XX.toId("addressSortedSet");
	
	private static final XAddressSortedSetValue ADDRESSSORTEDSET_VALUE = XV
	        .toAddressSortedSetValue(new XAddress[] { XX.toAddress("/hello/b/-/-"),
	                XX.toAddress("/hello/a/-/-") });
	public static final XID AGE_ID = XX.toId("age");
	
	public static final XIntegerValue AGE_VALUE = XV.toValue(42);
	public static final XID ALIASES_ID = XX.toId("aliases");
	
	public static final XStringListValue ALIASES_VALUE = XV.toValue(new String[] { "Johnny",
	        "John the Man", "Cookie Monster" });
	
	public static final XID CLAUDIA_ID = XX.toId("claudia");
	public static final XID COOKIE1_ID = XX.toId("cookie1");
	
	public static final XID COOKIE2_ID = XX.toId("cookie2");
	public static final XID COOKIENAMES_ID = XX.toId("cookienames");
	
	public static final XStringSetValue COOKIENAMES_VALUE = XV.toStringSetValue(new String[] {
	        "Chocolate Chip", "Almond" });
	public static final XID COOKIES_ID = XX.toId("cookies");
	
	public static final XIDSetValue COOKIES_VALUE = XV.toIDSetValue(new XID[] { COOKIE1_ID,
	        COOKIE2_ID });
	public static final XID COORDINATES_ID = XX.toId("coordinates");
	
	public static final XDoubleListValue COORDINATES_VALUE = XV.toValue(new double[] { 32.465,
	        19.34 });
	public static final XID EMPTYFIELD_ID = XX.toId("emptyfield");
	
	public static final XID FLAGS_ID = XX.toId("flags");
	public static final XBooleanListValue FLAGS_VALUE = XV.toValue(new boolean[] { true, false,
	        true, true, false });
	
	public static final XID FRIENDS_ID = XX.toId("friends");
	public static final XID PETER_ID = XX.toId("peter");
	
	public static final XIDListValue FRIENDS_VALUE = XV.toValue(new XID[] { CLAUDIA_ID, PETER_ID });
	
	public static final XID HEIGHT_ID = XX.toId("height");
	public static final XDoubleValue HEIGHT_VALUE = XV.toValue(121.3);
	
	public static final XID HIDDEN_ID = XX.toId("hidden");
	
	public static final XBooleanValue HIDDEN_VALUE = XV.toValue(false);
	private static final XID IDSORTEDSET_ID = XX.toId("idSortedSet");
	
	private static final XIDSortedSetValue IDSORTEDSET_VALUE = XV.toIDSortedSetValue(new XID[] {
	        XX.toId("b"), XX.toId("a") });
	public static final XID JOHN_ID = XX.toId("john");
	public static final XID LASTCALLTIMES_ID = XX.toId("lastCallTime");
	public static final XLongListValue LASTCALLTIMES_VALUE = XV.toValue(new long[] { 32456L, 7664L,
	        56L });
	
	public static final XID MAXCALLTIME_ID = XX.toId("maxCallTime");
	public static final XLongValue MAXCALLTIME_VALUE = XV.toValue(675874678478467L);
	
	public static final XID PHONE_ID = XX.toId("phone");
	
	public static final XStringValue PHONE_VALUE = XV.toValue("3463-2346");
	public static final XID PHONEBOOK_ID = XX.toId("phonebook");
	
	public static final XID SCORES_ID = XX.toId("scores");
	public static final XIntegerListValue SCORES_VALUE = XV.toValue(new int[] { 34, 234, 34 });
	
	public static final XID SIGNATURE_ID = XX.toId("signature");
	public static final XByteListValue SIGNATURE_VALUE = XV.toValue(new byte[] { 0, 1, 2, 3, 4, 5,
	        6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, (byte)253, (byte)254, (byte)255 });
	
	public static final XID SPOUSE_ID = XX.toId("spouse");
	public static final XID TITLE_ID = XX.toId("title");
	
	public static final XStringValue TITLE_VALUE = XV.toValue("Dr. John Doe");
	
	/**
	 * Add the phonebook-demo model with ID "phonebook" to the given repository.
	 * 
	 * @param repository The repository to add the phonebook to.
	 */
	public static void addPhonebookModel(XRepository repository) {
		
		// don't add twice
		if(repository.hasModel(PHONEBOOK_ID)) {
			return;
		}
		
		XModel model = repository.createModel(PHONEBOOK_ID);
		
		setupPhonebook(model);
	}
	
	public static void setupJohn(XAddress objectAddr, XTransactionBuilder tb) {
		
		tb.addField(objectAddr, XCommand.SAFE, TITLE_ID);
		XAddress titleAddr = XX.resolveField(objectAddr, TITLE_ID);
		tb.addValue(titleAddr, XCommand.FORCED, TITLE_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, PHONE_ID);
		XAddress phoneAddr = XX.resolveField(objectAddr, PHONE_ID);
		tb.addValue(phoneAddr, XCommand.FORCED, PHONE_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, ALIASES_ID);
		XAddress aliasesAddr = XX.resolveField(objectAddr, ALIASES_ID);
		tb.addValue(aliasesAddr, XCommand.FORCED, ALIASES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, FRIENDS_ID);
		XAddress friendsAddr = XX.resolveField(objectAddr, FRIENDS_ID);
		tb.addValue(friendsAddr, XCommand.FORCED, FRIENDS_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, SPOUSE_ID);
		XAddress spouseAddr = XX.resolveField(objectAddr, SPOUSE_ID);
		tb.addValue(spouseAddr, XCommand.FORCED, CLAUDIA_ID);
		
		tb.addField(objectAddr, XCommand.SAFE, HIDDEN_ID);
		XAddress hiddenAddr = XX.resolveField(objectAddr, HIDDEN_ID);
		tb.addValue(hiddenAddr, XCommand.FORCED, HIDDEN_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, FLAGS_ID);
		XAddress flagsAddr = XX.resolveField(objectAddr, FLAGS_ID);
		tb.addValue(flagsAddr, XCommand.FORCED, FLAGS_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, HEIGHT_ID);
		XAddress heightAddr = XX.resolveField(objectAddr, HEIGHT_ID);
		tb.addValue(heightAddr, XCommand.FORCED, HEIGHT_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, COORDINATES_ID);
		XAddress coordinatesAddr = XX.resolveField(objectAddr, COORDINATES_ID);
		tb.addValue(coordinatesAddr, XCommand.FORCED, COORDINATES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, AGE_ID);
		XAddress ageAddr = XX.resolveField(objectAddr, AGE_ID);
		tb.addValue(ageAddr, XCommand.FORCED, AGE_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, SCORES_ID);
		XAddress scoresAddr = XX.resolveField(objectAddr, SCORES_ID);
		tb.addValue(scoresAddr, XCommand.FORCED, SCORES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, MAXCALLTIME_ID);
		XAddress maxcalltimeAddr = XX.resolveField(objectAddr, MAXCALLTIME_ID);
		tb.addValue(maxcalltimeAddr, XCommand.FORCED, MAXCALLTIME_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, LASTCALLTIMES_ID);
		XAddress lastcalltimesAddr = XX.resolveField(objectAddr, LASTCALLTIMES_ID);
		tb.addValue(lastcalltimesAddr, XCommand.FORCED, LASTCALLTIMES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, EMPTYFIELD_ID);
		
		tb.addField(objectAddr, XCommand.SAFE, SIGNATURE_ID);
		XAddress signatureAddr = XX.resolveField(objectAddr, SIGNATURE_ID);
		tb.addValue(signatureAddr, XCommand.FORCED, SIGNATURE_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, COOKIES_ID);
		XAddress cookiesAddr = XX.resolveField(objectAddr, COOKIES_ID);
		tb.addValue(cookiesAddr, XCommand.FORCED, COOKIES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, COOKIENAMES_ID);
		XAddress cookienamesAddr = XX.resolveField(objectAddr, COOKIENAMES_ID);
		tb.addValue(cookienamesAddr, XCommand.FORCED, COOKIENAMES_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, ADDRESS_ID);
		XAddress addressAddr = XX.resolveField(objectAddr, ADDRESS_ID);
		tb.addValue(addressAddr, XCommand.FORCED, ADDRESS_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, ADDRESSLIST_ID);
		XAddress addressListAddr = XX.resolveField(objectAddr, ADDRESSLIST_ID);
		tb.addValue(addressListAddr, XCommand.FORCED, ADDRESSLIST_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, ADDRESSSET_ID);
		XAddress addressSetAddr = XX.resolveField(objectAddr, ADDRESSSET_ID);
		tb.addValue(addressSetAddr, XCommand.FORCED, ADDRESSSET_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, IDSORTEDSET_ID);
		XAddress idSortedSetAddr = XX.resolveField(objectAddr, IDSORTEDSET_ID);
		tb.addValue(idSortedSetAddr, XCommand.FORCED, IDSORTEDSET_VALUE);
		
		tb.addField(objectAddr, XCommand.SAFE, ADDRESSSORTEDSET_ID);
		XAddress addressSortedSetAddr = XX.resolveField(objectAddr, ADDRESSSORTEDSET_ID);
		tb.addValue(addressSortedSetAddr, XCommand.FORCED, ADDRESSSORTEDSET_VALUE);
		
	}
	
	public static void setupJohn(XObject john) {
		
		XTransactionBuilder tb = new XTransactionBuilder(john.getAddress());
		setupJohn(john.getAddress(), tb);
		
		// Execute commands individually.
		for(int i = 0; i < tb.size(); i++) {
			john.executeCommand(tb.getCommand(i));
		}
		
	}
	
	public static void setupPhonebook(XAddress modelAddr, XTransactionBuilder tb) {
		
		tb.addObject(modelAddr, XCommand.SAFE, JOHN_ID);
		tb.addObject(modelAddr, XCommand.SAFE, CLAUDIA_ID);
		tb.addObject(modelAddr, XCommand.SAFE, PETER_ID);
		
		XAddress johnAddr = XX.resolveObject(modelAddr, JOHN_ID);
		
		setupJohn(johnAddr, tb);
		
	}
	
	public static void setupPhonebook(XModel model) {
		
		XTransactionBuilder tb = new XTransactionBuilder(model.getAddress());
		setupPhonebook(model.getAddress(), tb);
		
		// Execute commands individually.
		for(int i = 0; i < tb.size(); i++) {
			model.executeCommand(tb.getCommand(i));
		}
		
	}
}
