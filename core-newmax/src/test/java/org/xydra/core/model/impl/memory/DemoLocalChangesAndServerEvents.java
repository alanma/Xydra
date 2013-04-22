package org.xydra.core.model.impl.memory;

import java.util.Iterator;

import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;


public class DemoLocalChangesAndServerEvents {
	
	private XId actorId = XX.toId("DEMOCHANGES");
	
	private String password = null; // TODO auth: where to get this?
	
	XId repo = XX.toId("remoteRepo");
	
	// Local Values
	static XId PETER_ID = DemoModelUtil.PETER_ID;
	static XId JOHN_ID = DemoModelUtil.JOHN_ID;
	static XId CLAUDIA_ID = DemoModelUtil.CLAUDIA_ID;
	static XId JENNY_ID = XX.toId("Jenny");
	
	static XId PHONE_ID = DemoModelUtil.PHONE_ID;
	static XId SCORES_ID = DemoModelUtil.SCORES_ID;
	static XId CAR_ID = XX.toId("car");
	static XValue JOHN_CAR = XV.toValue("T");
	static XId BDAY_ID = XX.toId("bDay");
	static XValue JOHN_BDAY = XV.toValue("01.02.03");
	static XValue CLAUDIA_PHONE = XV.toValue(456);
	static XValue CLAUDIA_CAR = XV.toValue("911");
	static XValue JENNY_PHONE = XV.toValue(8575309);
	
	// Server Values
	static XId KERSTIN_ID = XX.toId("Kerstin");
	
	static XValue JOHN_PHONE = XV.toValue(56789);
	static XId BIRTHDAY_ID = XX.toId("birthday");
	static XValue JOHN_BIRTHDAY = XV.toValue("01.02.03");
	static XId CUPS_ID = XX.toId("cups");
	static XValue JOHN_CUPS = XV.toValue("fishing");
	static XId FLAGS_ID = DemoModelUtil.FLAGS_ID;
	
	static XValue CLAUDIA_CAR_TRUE = XV.toValue("911S");
	static XValue KERSTIN_PHONE = XV.toValue("Canada");
	
	static long SYNCREVISION;
	
	/**
	 * <ul>
	 * 
	 * <li>model events:
	 * <ul>
	 * <li>remove "peter"
	 * <li>add "jenny"
	 * </ul>
	 * 
	 * <li>object events: "john"
	 * <ul>
	 * <li>remove "phone"
	 * <li>remove "scores"
	 * <li>add "car" - "T"
	 * <li>add "bDay" - "01.02.03"
	 * </ul>
	 * <li>object events: "claudia"
	 * <ul>
	 * <li>add "phone" - "456"
	 * <li>add "car" - "911"
	 * </ul>
	 * 
	 * <li>object events: "jenny"
	 * <ul>
	 * <li>add "phone" - "8675309"
	 * </ul>
	 * </ul>
	 * 
	 * @param repo a repository
	 * 
	 * @return all events from local Change Log
	 */
	public static XChangeLog getLocalChanges(XRepository repo) {
		
		DemoModelUtil.addPhonebookModel(repo);
		
		XModel phonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		SYNCREVISION = phonebook.getRevisionNumber() + 1;
		
		// apply local changes:
		phonebook.removeObject(PETER_ID);
		phonebook.createObject(JENNY_ID);
		
		XObject objectJohn = phonebook.getObject(JOHN_ID);
		objectJohn.removeField(PHONE_ID);
		objectJohn.removeField(SCORES_ID);
		objectJohn.createField(CAR_ID);
		objectJohn.getField(CAR_ID).setValue(JOHN_CAR);
		objectJohn.createField(BDAY_ID);
		objectJohn.getField(BDAY_ID).setValue(JOHN_BDAY);
		
		XObject objectClaudia = phonebook.getObject(CLAUDIA_ID);
		objectClaudia.createField(PHONE_ID);
		objectClaudia.getField(PHONE_ID).setValue(CLAUDIA_PHONE);
		objectClaudia.createField(CAR_ID).setValue(CLAUDIA_CAR);
		
		XObject objectJenny = phonebook.getObject(JENNY_ID);
		objectJenny.createField(PHONE_ID).setValue(JENNY_PHONE);
		
		return phonebook.getChangeLog();
	}
	
	/**
	 * Server response to synchronizing request expressed with events
	 * 
	 * <ul>
	 * <li>model events:
	 * <ul>
	 * <li>remove "peter" <font color=RED>failed</font><br>
	 * </i>
	 * <li>add "jenny" <font color=GREEN>&#10003</font>
	 * <li>add "kerstin" <font color=BLUE>extern</font><br>
	 * </ul>
	 * 
	 * <li>object events: "john"
	 * <ul>
	 * <li>"phone" - "56789" <font color=RED>adverse</font><br>
	 * <li>remove "scores" <font color=GREEN>&#10003</font>
	 * <li>add "car" - "T" <font color=GREEN>&#10003</font>
	 * <li>add "bDay" - "01.02.03" <font color=RED>set value failed</font><br>
	 * <li>add "birthday" - "01.02.03" <font color=BLUE>extern</font><br>
	 * <li>add "cups" - "fishing" <font color=BLUE>extern</font><br>
	 * <li>change "flags" - null <font color=BLUE>extern</font><br>
	 * </ul>
	 * <li>object events: "claudia"
	 * <ul>
	 * <li>add "phone" - "456" <font color=GREEN>&#10003</font>
	 * <li>add "car" - "911S" <font color=RED>adverse</font><br>
	 * </ul>
	 * 
	 * <li>object events: "jenny"
	 * <ul>
	 * <li>add "phone" - "8675309" <font color=GREEN>&#10003</font>
	 * </ul>
	 * 
	 * <li>object events: "kerstin"
	 * <ul>
	 * <li>add "phone" - "Canada" <font color=BLUE>extern</font><br>
	 * </ul>
	 * </ul>
	 * 
	 * @param repo
	 * 
	 * @return all event - response from server
	 */
	public static Iterator<XEvent> getServerChanges(XRepository repo) {
		
		XModel phonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		SYNCREVISION = phonebook.getRevisionNumber() + 1;
		
		// apply server changes:
		phonebook.createObject(JENNY_ID);
		phonebook.createObject(KERSTIN_ID);
		
		XObject objectJohn = phonebook.getObject(JOHN_ID);
		objectJohn.getField(PHONE_ID).setValue(JOHN_PHONE);
		objectJohn.removeField(SCORES_ID);
		objectJohn.createField(CAR_ID);
		objectJohn.getField(CAR_ID).setValue(JOHN_CAR);
		objectJohn.createField(BDAY_ID);
		objectJohn.getField(BDAY_ID);
		objectJohn.createField(BIRTHDAY_ID).setValue(JOHN_BIRTHDAY);
		objectJohn.createField(CUPS_ID).setValue(JOHN_CUPS);
		objectJohn.getField(FLAGS_ID).setValue(null);
		
		XObject objectClaudia = phonebook.getObject(CLAUDIA_ID);
		objectClaudia.createField(PHONE_ID);
		objectClaudia.getField(PHONE_ID).setValue(CLAUDIA_PHONE);
		objectClaudia.createField(CAR_ID).setValue(CLAUDIA_CAR_TRUE);
		
		XObject objectJenny = phonebook.getObject(JENNY_ID);
		objectJenny.createField(PHONE_ID).setValue(JENNY_PHONE);
		
		XObject objectKerstin = phonebook.createObject(KERSTIN_ID);
		objectKerstin.createField(PHONE_ID).setValue(KERSTIN_PHONE);
		
		Iterator<XEvent> changeEvents = phonebook.getChangeLog().getEventsSince(SYNCREVISION);
		return changeEvents;
	}
	
	/**
	 * <ul>
	 * 
	 * <li>model events:
	 * <ul>
	 * <li>add "kerstin"
	 * </ul>
	 * 
	 * <li>object events: "peter"
	 * <ul>
	 * <li>add "phone"
	 * <li>remove "phone"
	 * </ul>
	 * 
	 * <li>object events: "john"
	 * <ul>
	 * <li>change "phone" - 56789
	 * <li>add "bDay" - "01.02.03"
	 * <li>change "bDay" - null
	 * <li>add "birthday" - "01.02.03"
	 * <li>add "cups" - "fishing"
	 * <li>change "flags" - null
	 * </ul>
	 * <li>object events: "claudia"
	 * <ul>
	 * <li>add "car" - "911S"
	 * </ul>
	 * 
	 * <li>object events: "kerstin"
	 * <ul>
	 * <li>add "phone" - "Canada"
	 * </ul>
	 * </ul>
	 * 
	 * @param repo a repository
	 * 
	 * @return all events from local Change Log
	 */
	public static Iterator<XEvent> getOtherClientsChanges(XRepository repo) {
		XModel phonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		SYNCREVISION = phonebook.getRevisionNumber() + 1;
		
		phonebook.createObject(KERSTIN_ID);
		
		// to change peter's revision number */
		XObject objectPeter = phonebook.getObject(PETER_ID);
		objectPeter.createField(PHONE_ID);
		objectPeter.removeField(PHONE_ID);
		
		XObject objectJohn = phonebook.getObject(JOHN_ID);
		objectJohn.getField(PHONE_ID).setValue(JOHN_PHONE);
		objectJohn.createField(BDAY_ID).setValue(BDAY_ID);
		objectJohn.getField(BDAY_ID).setValue(null);
		objectJohn.createField(BIRTHDAY_ID).setValue(JOHN_BIRTHDAY);
		objectJohn.createField(CUPS_ID).setValue(JOHN_CUPS);
		objectJohn.getField(FLAGS_ID).setValue(null);
		
		XObject objectClaudia = phonebook.getObject(CLAUDIA_ID);
		objectClaudia.createField(CAR_ID).setValue(CLAUDIA_CAR_TRUE);
		
		XObject objectKerstin = phonebook.createObject(KERSTIN_ID);
		objectKerstin.createField(PHONE_ID).setValue(KERSTIN_PHONE);
		
		Iterator<XEvent> changeEvents = phonebook.getChangeLog().getEventsSince(SYNCREVISION);
		return changeEvents;
	}
	
	/**
	 * The state which the client should have after the synchronization
	 * 
	 * model revision of phonebook: r65
	 * 
	 * <ul>
	 * 
	 * <li>objects:
	 * <ul>
	 * <li>"peter"-r2
	 * <li>"john" -r57
	 * <li>"claudia" -r61
	 * <li>"jenny" -r63
	 * <li>"kerstin" -r65
	 * </ul>
	 * 
	 * 
	 * 
	 * <li>fields of "peter"
	 * 
	 * <ul>
	 * <li>none
	 * </ul>
	 * 
	 * <li>fields of "john"
	 * <ul>
	 * <li>"phone" - "56789" -r48
	 * <li>"car" - "T" -r51
	 * <li>"bDay" - null -r52
	 * <li>"birthday" - "01.02.03" -r54
	 * <li>"cups" - "fishing" -r56
	 * <li>"flags" - null -r57
	 * <li>some others that you can look up at the {@link DemoModelUtil}
	 * </ul>
	 * 
	 * <li>fields of "claudia"
	 * <ul>
	 * <li>"phone" - "456" -r59
	 * <li>"car" - "911S" -r61
	 * </ul>
	 * 
	 * <li>fields of "jenny"
	 * <ul>
	 * <li>"phone" - "8675309" -r63
	 * </ul>
	 * 
	 * <li>fields of "kerstin"
	 * <ul>
	 * <li>"phone" - "Canada" -r65
	 * </ul>
	 * </ul>
	 * 
	 * @param repo
	 * 
	 * @return a model with the current state
	 * 
	 */
	public static XRevWritableModel getResultingClientState(XRepository repo) {
		XModel otherPhonebook = repo.getModel(DemoModelUtil.PHONEBOOK_ID);
		XRevWritableModel phonebook = XCopyUtils.createSnapshot(otherPhonebook);
		SYNCREVISION = phonebook.getRevisionNumber() + 1;
		
		// apply server changes:
		phonebook.createObject(JENNY_ID);
		phonebook.createObject(KERSTIN_ID);
		
		XRevWritableObject objectPeter = phonebook.getObject(PETER_ID);
		objectPeter.setRevisionNumber(2);
		
		XRevWritableObject objectJohn = phonebook.getObject(JOHN_ID);
		objectJohn.getField(PHONE_ID).setValue(JOHN_PHONE);
		objectJohn.getField(PHONE_ID).setRevisionNumber(48);
		objectJohn.removeField(SCORES_ID);
		objectJohn.createField(CAR_ID).setValue(JOHN_CAR);
		objectJohn.getField(CAR_ID).setRevisionNumber(51);
		objectJohn.createField(BDAY_ID).setRevisionNumber(52);
		objectJohn.createField(BIRTHDAY_ID).setValue(JOHN_BIRTHDAY);
		objectJohn.getField(BIRTHDAY_ID).setRevisionNumber(54);
		objectJohn.createField(CUPS_ID).setValue(JOHN_CUPS);
		objectJohn.getField(CUPS_ID).setRevisionNumber(56);
		objectJohn.getField(FLAGS_ID).setValue(null);
		objectJohn.getField(FLAGS_ID).setRevisionNumber(57);
		objectJohn.setRevisionNumber(57);
		
		XRevWritableObject objectClaudia = phonebook.getObject(CLAUDIA_ID);
		objectClaudia.createField(PHONE_ID);
		objectClaudia.getField(PHONE_ID).setValue(CLAUDIA_PHONE);
		objectClaudia.getField(PHONE_ID).setRevisionNumber(59);
		objectClaudia.createField(CAR_ID).setValue(CLAUDIA_CAR_TRUE);
		objectClaudia.getField(CAR_ID).setRevisionNumber(61);
		objectClaudia.setRevisionNumber(61);
		
		XRevWritableObject objectJenny = phonebook.getObject(JENNY_ID);
		objectJenny.createField(PHONE_ID).setValue(JENNY_PHONE);
		objectJenny.getField(PHONE_ID).setRevisionNumber(63);
		objectJenny.setRevisionNumber(63);
		
		XRevWritableObject objectKerstin = phonebook.createObject(KERSTIN_ID);
		objectKerstin.createField(PHONE_ID).setValue(KERSTIN_PHONE);
		objectKerstin.getField(PHONE_ID).setRevisionNumber(65);
		objectKerstin.setRevisionNumber(65);
		
		phonebook.setRevisionNumber(65);
		
		System.out.println("phonebook: " + phonebook.toString() + ", rev: "
		        + phonebook.getRevisionNumber());
		
		return phonebook;
	}
	
}
