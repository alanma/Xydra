package org.xydra.core.model.tutorial;

import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.access.XA;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.access.XGroupDatabase;
import org.xydra.core.access.impl.memory.MemoryAccessManager;
import org.xydra.core.access.impl.memory.MemoryGroupDatabase;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XField;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XV;


/**
 * To demonstrate how we could use XModel we'll write a little program for
 * managing calendars. Every user has it's own calendar that he may or may not
 * share with other users. A user can add events to its calendar and change them
 * later.
 * 
 * Since this is an introduction the functions will only be rudimentary and we
 * won't bother with complex exception handling and things like that.
 * 
 * @author Kaidel
 * 
 */

public class CalendarManager {
	
	/*
	 * Before we can begin we need to think about how we want to organize the
	 * data of our calendar manager using XModel.
	 * 
	 * Each XModel will represent the calendar of a user.
	 * 
	 * Each XObject represents an event that the user added to the calendar
	 * 
	 * Each XField will represent a specific attribute of the event, for example
	 * the name, date and description.
	 * 
	 * We'll use some pre-defined XIDs for the XFields, so that every
	 * event-XObject has the same structure.
	 */

	private static XID nameFieldID = XX.toId("name");
	private static XID placeFieldID = XX.toId("place");
	private static XID dayFieldID = XX.toId("day");
	private static XID monthFieldID = XX.toId("month");
	private static XID yearFieldID = XX.toId("year");
	private static XID beginFieldID = XX.toId("begin");
	private static XID endFieldID = XX.toId("end");
	private static XID descFieldID = XX.toId("description");
	
	/*
	 * We'll also use a simple user account management. Were going to use a
	 * special XModel for that, where every XObject represents a user and only
	 * holds one XField for saving its password.
	 */
	private static XID pwdFieldID = XX.toId("pwd");
	
	private XModel accountModel;
	
	/*
	 * We'll use a repository to store all calendar-XModels. Every XModel will
	 * have the same ID as the user that it belongs to.
	 */
	private XRepository calendarRepo;
	private XID calendarRepoID;
	
	/*
	 * Sometimes users may or may not want to share their calendar with other
	 * users, so we'll need access right management too.
	 */
	private XAccessManager arm;
	private XGroupDatabase groups;
	
	/*
	 * We'll need an ID for our program to distinguish between changes made by
	 * the user and changes made by the program.
	 */
	private static XID managerID = XX.toId("calendarManager");
	
	public CalendarManager() {
		this.accountModel = new MemoryModel(XX.createUniqueID());
		
		this.calendarRepo = X.createMemoryRepository();
		this.calendarRepoID = this.calendarRepo.getID();
		
		this.groups = new MemoryGroupDatabase();
		this.arm = new MemoryAccessManager(this.groups);
	}
	
	/**
	 * Registers a new user on this Calendar Manager and creates a calendar for
	 * the user.
	 * 
	 * @param userName The username.
	 * @param pwd The password that the user wants to use.
	 * @return true, if registration was successful, false otherwise
	 */
	public boolean registerNewUser(String userName, String pwd) {
		/*
		 * check whether the user name is already taken or not by checking
		 * whether there already exists an XObject in our accountModel with an
		 * XID that equals the name of the user who wants to register.
		 */
		XID userID = XX.toId(userName);
		
		if(this.accountModel.getObject(userID) != null) {
			return false;
		}
		
		// create the entry for the user in the accountModel and save the
		// password
		XObject userObject = this.accountModel.createObject(managerID, userID);
		XField pwdField = userObject.createField(managerID, pwdFieldID);
		
		XStringValue pwdValue = XV.toValue(pwd);
		
		pwdField.setValue(managerID, pwdValue);
		
		// create the calendar for the user
		XModel usrCalendar = this.calendarRepo.createModel(managerID, userID);
		
		// set access rights for the user
		this.arm.setAccess(userID, usrCalendar.getAddress(), XA.ACCESS_READ, true);
		this.arm.setAccess(userID, usrCalendar.getAddress(), XA.ACCESS_WRITE, true);
		
		return true;
	}
	
	/**
	 * This method adds a new event to the calendar of the user.
	 * 
	 * @param userName The name of the user who wants to add an event to his
	 *            calendar.
	 * @param pwd The password of the user.
	 * @param name The name of the event
	 * @param place The place this event takes place at.
	 * @param year The year in which this event takes place.
	 * @param month The month in which this event takes place.
	 * @param day The day on which this event takes place.
	 * @param description A description of the event.
	 * @param begin The time the event starts.
	 * @param end The time the event ends.
	 * @return true, if adding the event was successful, false otherwise
	 */
	public boolean addEvent(String userName, String pwd, String name, String place, int year,
	        int month, int day, String description, int begin, int end) {
		if(!checkUserNameAndPassword(userName, pwd)) {
			return false;
		}
		
		// check if the date is plausible (to keeps things simple we won't check
		// if the day actually exists, for example
		// 31.02.2010 would be okay, too)
		if(day < 0 || day > 31 || year < 0 || month < 1 || month > 12) {
			return false;
		}
		
		// check if begin and end are valid time points
		if(begin < 0 || begin > 2400 || end < 0 || end > 2400 || begin > end) {
			return false;
		}
		
		XID userID = XX.toId(userName);
		
		XModel userCalendar = this.calendarRepo.getModel(userID);
		
		// we'll use commands to execute the changes
		XCommandFactory cmdFactory = X.getCommandFactory();
		
		XID eventID = XX.createUniqueID();
		XCommand addEvent = cmdFactory.createAddObjectCommand(this.calendarRepoID, userID, eventID,
		        false);
		
		XCommand addName = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        nameFieldID, false);
		XCommand setName = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        nameFieldID, 0, XV.toValue(name), false);
		
		XCommand addPlace = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        placeFieldID, false);
		XCommand setPlace = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        placeFieldID, 0, XV.toValue(place), false);
		
		XCommand addYear = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        yearFieldID, false);
		XCommand setYear = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        yearFieldID, 0, XV.toValue(year), false);
		
		XCommand addMonth = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        monthFieldID, false);
		XCommand setMonth = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        monthFieldID, 0, XV.toValue(month), false);
		
		XCommand addDay = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        dayFieldID, false);
		XCommand setDay = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        dayFieldID, 0, XV.toValue(day), false);
		
		XCommand addDescription = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID,
		        eventID, descFieldID, false);
		XCommand setDescription = cmdFactory.createAddValueCommand(this.calendarRepoID, userID,
		        eventID, descFieldID, 0, XV.toValue(description), false);
		
		XCommand addBegin = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        beginFieldID, false);
		XCommand setBegin = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        beginFieldID, 0, XV.toValue(begin), false);
		
		XCommand addEnd = cmdFactory.createAddFieldCommand(this.calendarRepoID, userID, eventID,
		        endFieldID, false);
		XCommand setEnd = cmdFactory.createAddValueCommand(this.calendarRepoID, userID, eventID,
		        endFieldID, 0, XV.toValue(end), false);
		
		// put the commands into a transaction
		XTransactionBuilder transBuilder = new XTransactionBuilder(userCalendar.getAddress());
		transBuilder.addCommand(addEvent);
		transBuilder.addCommand(addName);
		transBuilder.addCommand(setName);
		transBuilder.addCommand(addPlace);
		transBuilder.addCommand(setPlace);
		transBuilder.addCommand(addYear);
		transBuilder.addCommand(setYear);
		transBuilder.addCommand(addMonth);
		transBuilder.addCommand(setMonth);
		transBuilder.addCommand(addDay);
		transBuilder.addCommand(setDay);
		transBuilder.addCommand(addDescription);
		transBuilder.addCommand(setDescription);
		transBuilder.addCommand(addBegin);
		transBuilder.addCommand(setBegin);
		transBuilder.addCommand(addEnd);
		transBuilder.addCommand(setEnd);
		
		// execute the transaction
		long result = userCalendar.executeTransaction(userID, transBuilder.build());
		
		return result != XCommand.FAILED;
	}
	
	/**
	 * Gets a string representation of all events in the given time frame in the
	 * specified calendar, if the given user has read access to the calendar.
	 * 
	 * @param userName The name of the user who wants to get the events.
	 * @param pwd The password of the user.
	 * @param calendar The name of the user whose calendar the events will be
	 *            extracted from
	 * @param beginYear the year of the date on which the time frame begins
	 * @param beginMonth the month of the date on which the time frame begins
	 * @param beginDay the day of the date on which the time frame begins
	 * @param endYear the year of the date on which the time frame ends
	 * @param endMonth the month of the date on which the time frame ends
	 * @param endDay the day of the date on which the time frame ends
	 * @return a String representation of the events in the given time frame or
	 *         "Error" if the operation was not successful, for example if the
	 *         given user is not allowed to read the specified calendar
	 */
	public String getEvents(String userName, String pwd, String calendar, int beginYear,
	        int beginMonth, int beginDay, int endYear, int endMonth, int endDay) {
		if(!checkUserNameAndPassword(userName, pwd)) {
			return "Error";
		}
		
		// check if the dates are plausible (to keeps things simple we won't
		// check
		// if the day actually exists, for example
		// 31.02.2010 would be okay, too)
		if(beginDay < 0 || beginDay > 31 || beginYear < 0 || beginMonth < 1 || beginMonth > 12) {
			return "Error";
		}
		if(endDay < 0 || endDay > 31 || endYear < 0 || endMonth < 1 || endMonth > 12) {
			return "Error";
		}
		
		XID userID = XX.toId(userName);
		XID calendarID = XX.toId(calendar);
		
		XModel userCalendar = this.calendarRepo.getModel(calendarID);
		
		// check whether the user is allowed to read the specified calendar or
		// not
		if(!this.arm.canRead(userID, userCalendar.getAddress())) {
			// user has no right access!
			return "Error!";
		}
		
		// build the string representation
		String result = "";
		
		for(XID eventID : userCalendar) {
			XObject event = userCalendar.getObject(eventID);
			
			int eventYear = ((XIntegerValue)event.getField(yearFieldID).getValue()).contents();
			int eventMonth = ((XIntegerValue)event.getField(yearFieldID).getValue()).contents();
			int eventDay = ((XIntegerValue)event.getField(yearFieldID).getValue()).contents();
			
			if(eventYear >= beginYear && eventYear <= endYear) {
				if(eventMonth >= beginMonth && eventMonth <= endMonth) {
					if(eventDay >= beginDay && eventDay <= endDay) {
						// the date of the event is within the given bounds!
						String name = ((XStringValue)event.getField(nameFieldID).getValue())
						        .contents();
						String place = ((XStringValue)event.getField(placeFieldID).getValue())
						        .contents();
						String description = ((XStringValue)event.getField(descFieldID).getValue())
						        .contents();
						
						int begin = ((XIntegerValue)event.getField(beginFieldID).getValue())
						        .contents();
						int end = ((XIntegerValue)event.getField(endFieldID).getValue()).contents();
						
						result += "Event: " + name + '\n' + "At: " + place + ", from " + begin
						        + " to " + end + '\n' + "Description: " + description + '\n' + '\n';
						
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * A method which allows a user to share is calendar with another user (this
	 * grants read-access to the user with the username specified in userName2)
	 * 
	 * @param userName The user who wants to grant someone access to his
	 *            calendar
	 * @param pwd The password of the user.
	 * @param userName2 The name of the user who will get read-access
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean shareCalendarWith(String userName, String pwd, String userName2) {
		if(!checkUserNameAndPassword(userName, pwd)) {
			return false;
		}
		
		XID user1ID = XX.toId(userName);
		
		// check whether the second specified user actualy exists
		XID user2ID = XX.toId(userName2);
		if(this.accountModel.getObject(user2ID) == null) {
			return false;
		}
		
		// grant read access
		XModel user1Calendar = this.calendarRepo.getModel(user1ID);
		
		this.arm.setAccess(user2ID, user1Calendar.getAddress(), XA.ACCESS_READ, true);
		
		return true;
	}
	
	/**
	 * This method allows to deprive a user of its read-access to the calendar
	 * of another user.
	 * 
	 * @param userName The name of the user who wants to deprive someone of his
	 *            read-access to his calendar.
	 * @param pwd The password of the user.
	 * @param userName2 The user who will loose his read-access
	 * @return true, if operation was successful, false otherwise
	 */
	public boolean unshareCalendarWith(String userName, String pwd, String userName2) {
		if(!checkUserNameAndPassword(userName, pwd)) {
			return false;
		}
		
		XID user1ID = XX.toId(userName);
		XID user2ID = XX.toId(userName2);
		if(this.accountModel.getObject(user2ID) == null) {
			return false;
		}
		
		XModel user1Calendar = this.calendarRepo.getModel(user1ID);
		
		this.arm.setAccess(user2ID, user1Calendar.getAddress(), XA.ACCESS_READ, false);
		
		return true;
	}
	
	/**
	 * A method for checking if the given user name ist actualy taken and if it
	 * is, to check if the given password is correct
	 * 
	 * @param userName The name of the user
	 * @param pwd The password of the user
	 * @return true, if the user exists and the given password is correct
	 */
	private boolean checkUserNameAndPassword(String userName, String pwd) {
		XID userID = XX.toId(userName);
		
		// check if the user exists
		XObject usrAccount = this.accountModel.getObject(userID);
		if(usrAccount == null) {
			return false;
		}
		
		// check if the given password is right
		if(!pwd.equals(usrAccount.getField(pwdFieldID).getValue().toString())) {
			return false;
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		CalendarManager cm = new CalendarManager();
		cm.registerNewUser("john", "superman");
		// FIXME it seems the event is not added properly
		cm.addEvent("john", "superman", "Brush Teeth", "at home", 2010, 4, 14,
		        "Brush teeth for 2 minutes", 2200, 2203);
		String events = cm.getEvents("john", "superman", "john", 2000, 1, 1, 2020, 12, 31);
		System.out.println(events);
		
		// alice registers
		cm.registerNewUser("alice", "wonderwomen");
		
		// john shares with alice
		cm.shareCalendarWith("john", "superman", "alice");
		
		// alice looks up johns events
		String aliceEvents = cm.getEvents("alice", "wonderwomen", "john", 2000, 1, 1, 2020, 12, 31);
		System.out.println(aliceEvents);
	}
}
