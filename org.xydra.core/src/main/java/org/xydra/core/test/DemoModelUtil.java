package org.xydra.core.test;

import org.xydra.core.XX;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.value.XV;


/**
 * This demo model is supposed to contain each XModel data-type and construct at
 * least once.
 * 
 * @author voelkel
 * 
 */
public class DemoModelUtil {
	
	public static final XID PHONEBOOK_ID = XX.toId("phonebook");
	public static final XID JOHN_ID = XX.toId("john");
	public static final XID TITLE_ID = XX.toId("title");
	public static final XID PHONE_ID = XX.toId("phone");
	public static final XID CLAUDIA_ID = XX.toId("claudia");
	public static final XID ALIASES_ID = XX.toId("aliases");
	public static final XID FRIENDS_ID = XX.toId("friends");
	public static final XID SPOUSE_ID = XX.toId("spouse");
	public static final XID HIDDEN_ID = XX.toId("hidden");
	public static final XID PETER_ID = XX.toId("peter");
	public static final XID FLAGS_ID = XX.toId("flags");
	public static final XID AGE_ID = XX.toId("age");
	public static final XID HEIGHT_ID = XX.toId("height");
	public static final XID COORDIANTES_ID = XX.toId("coordinates");
	public static final XID SCORES_ID = XX.toId("scores");
	public static final XID MAXCALLTIME_ID = XX.toId("maxCallTime");
	public static final XID LASTCALLTIMES_ID = XX.toId("lastCallTime");
	public static final XID EMPTYFIELD_ID = XX.toId("emptyfield");
	public static final XID SIGNATURE_ID = XX.toId("signature");
	public static final XID COOKIES_ID = XX.toId("cookies");
	public static final XID COOKIENAMES_ID = XX.toId("cookienames");
	public static final XID COOKIE1_ID = XX.toId("cookie1");
	public static final XID COOKIE2_ID = XX.toId("cookie2");
	
	public static final XID ACTOR_ID = XX.toId("DemoModel");
	
	/**
	 * Add the phonebook-demo model with ID "phonebook" to the given repository.
	 * 
	 * @param repository
	 */
	public static void addPhonebookModel(XRepository repository) {
		
		// don't add twice
		if(repository.hasModel(PHONEBOOK_ID)) {
			return;
		}
		
		XModel model = repository.createModel(ACTOR_ID, PHONEBOOK_ID);
		
		setupPhonebook(model);
	}
	
	public static void setupPhonebook(XModel model) {
		
		XObject john = model.createObject(ACTOR_ID, JOHN_ID);
		
		model.createObject(ACTOR_ID, CLAUDIA_ID);
		model.createObject(ACTOR_ID, PETER_ID);
		
		setupJohn(john);
		
	}
	
	public static void setupJohn(XObject john) {
		
		XField name = john.createField(ACTOR_ID, TITLE_ID);
		name.setValue(ACTOR_ID, XV.toValue("Dr. John Doe"));
		XField phoneNumber = john.createField(ACTOR_ID, PHONE_ID);
		phoneNumber.setValue(ACTOR_ID, XV.toValue("3463-2346"));
		
		XField aliases = john.createField(ACTOR_ID, ALIASES_ID);
		aliases.setValue(ACTOR_ID, XV.toValue(new String[] { "Johnny", "John the Man",
		        "Cookie Monster" }));
		
		XField friends = john.createField(ACTOR_ID, FRIENDS_ID);
		friends.setValue(ACTOR_ID, XV.toValue(new XID[] { CLAUDIA_ID, PETER_ID }));
		
		XField spouse = john.createField(ACTOR_ID, SPOUSE_ID);
		spouse.setValue(ACTOR_ID, XV.toValue(CLAUDIA_ID));
		
		XField hidden = john.createField(ACTOR_ID, HIDDEN_ID);
		hidden.setValue(ACTOR_ID, XV.toValue(false));
		
		XField flags = john.createField(ACTOR_ID, FLAGS_ID);
		flags.setValue(ACTOR_ID, XV.toValue(new boolean[] { true, false, true, true, false }));
		
		XField height = john.createField(ACTOR_ID, HEIGHT_ID);
		height.setValue(ACTOR_ID, XV.toValue(121.3));
		
		XField coordinates = john.createField(ACTOR_ID, COORDIANTES_ID);
		coordinates.setValue(ACTOR_ID, XV.toValue(new double[] { 32.465, 19.34 }));
		
		XField age = john.createField(ACTOR_ID, AGE_ID);
		age.setValue(ACTOR_ID, XV.toValue(42));
		
		XField scores = john.createField(ACTOR_ID, SCORES_ID);
		scores.setValue(ACTOR_ID, XV.toValue(new int[] { 34, 234, 34 }));
		
		XField maxCallTime = john.createField(ACTOR_ID, MAXCALLTIME_ID);
		maxCallTime.setValue(ACTOR_ID, XV.toValue(675874678478467L));
		
		XField lastCallTimes = john.createField(ACTOR_ID, LASTCALLTIMES_ID);
		lastCallTimes.setValue(ACTOR_ID, XV.toValue(new long[] { 32456L, 7664L, 56L }));
		
		john.createField(ACTOR_ID, EMPTYFIELD_ID);
		
		XField signature = john.createField(ACTOR_ID, SIGNATURE_ID);
		signature.setValue(ACTOR_ID, XV.toValue(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
		        12, 13, 14, 15, 16, (byte)253, (byte)254, (byte)255 }));
		
		XField cookies = john.createField(ACTOR_ID, COOKIES_ID);
		cookies.setValue(ACTOR_ID, XV.toIDSetValue(new XID[] { COOKIE1_ID, COOKIE2_ID }));
		
		XField cookieNames = john.createField(ACTOR_ID, COOKIENAMES_ID);
		cookieNames.setValue(ACTOR_ID, XV.toStringSetValue(new String[] { "Chocolate Chip",
		        "Almond" }));
		
	}
}
