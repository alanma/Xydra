package org.xydra.core.model.tutorial;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XTransactionBuilder;
import org.xydra.core.model.XField;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.store.access.XA;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.access.XGroupDatabaseWithListeners;
import org.xydra.store.access.impl.memory.MemoryAuthorisationManager;
import org.xydra.store.access.impl.memory.MemoryGroupDatabase;


/**
 * To demonstrate how we could use XModel we'll write a little program for
 * managing calendars. Every user has it's own calendar that he may or may not
 * share with other users. A user can add events to its calendar and change them
 * later.
 *
 * Since this is an introduction the functions will only be rudimentary and we
 * won't bother with complex exception handling and things like that.
 *
 * @author kaidel
 *
 */
public class CalendarManager {

    private static XId beginFieldId = XX.toId("begin");

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
     * We'll use some pre-defined XIds for the XFields, so that every
     * event-XObject has the same structure.
     */

    private static XId dayFieldId = XX.toId("day");
    private static XId descFieldId = XX.toId("description");
    private static XId endFieldId = XX.toId("end");
    private static final Logger log = LoggerFactory.getLogger(CalendarManager.class);
    /*
     * We'll need an ID for our program to distinguish between changes made by
     * the user and changes made by the program.
     */
    private static XId managerID = XX.toId("calendarManager");
    private static XId monthFieldId = XX.toId("month");
    private static XId nameFieldId = XX.toId("name");
    private static XId placeFieldId = XX.toId("place");

    /*
     * We'll also use a simple user account management. Were going to use a
     * special XModel for that, where every XObject represents a user and only
     * holds one XField for saving its password.
     */
    private static XId pwdFieldId = XX.toId("pwd");

    private static XId yearFieldId = XX.toId("year");

    public static void main(final String[] args) {

        final CalendarManager cm = new CalendarManager();
        cm.registerNewUser("john", "superman");
        cm.addEvent("john", "superman", "Brush Teeth", "at home", 2010, 4, 14,
                "Brush teeth for 2 minutes", 2200, 2203);
        final String events = cm.getEvents("john", "superman", "john", 2000, 1, 1, 2020, 12, 31);
        log.info(events);

        // alice registers
        cm.registerNewUser("alice", "wonderwomen");

        // john shares with alice
        cm.shareCalendarWith("john", "superman", "alice");

        // alice looks up johns events
        final String aliceEvents = cm.getEvents("alice", "wonderwomen", "john", 2000, 1, 1, 2020, 12, 31);
        log.info("alice: " + aliceEvents);

    }

    private final XModel accountModel;

    /*
     * Sometimes users may or may not want to share their calendar with other
     * users, so we'll need access right management too.
     */
    private final XAuthorisationManager arm;
    /*
     * We'll use a repository to store all calendar-XModels. Every XModel will
     * have the same ID as the user that it belongs to.
     */
    private final XRepository calendarRepo;

    private final XId calendarRepoID;

    private final XGroupDatabaseWithListeners groups;

    public CalendarManager() {
        this.accountModel = new MemoryModel(managerID, null, Base.createUniqueId());

        this.calendarRepo = X.createMemoryRepository(managerID);
        this.calendarRepoID = this.calendarRepo.getId();

        this.groups = new MemoryGroupDatabase();
        this.arm = new MemoryAuthorisationManager(this.groups);
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
    public boolean addEvent(final String userName, final String pwd, final String name, final String place, final int year,
            final int month, final int day, final String description, final int begin, final int end) {
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

        final XId userID = Base.toId(userName);

        final XModel userCalendar = this.calendarRepo.getModel(userID);

        // we'll use commands to execute the changes
        final XCommandFactory cmdFactory = BaseRuntime.getCommandFactory();

        final XId eventID = Base.createUniqueId();
        final XCommand addEvent = cmdFactory.createAddObjectCommand(
                Base.resolveModel(this.calendarRepoID, userID), eventID, false);

        final XCommand addName = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), nameFieldId, false);
        final XCommand setName = cmdFactory.createAddValueCommand(
                Base.resolveField(this.calendarRepoID, userID, eventID, nameFieldId), 0,
                XV.toValue(name), false);

        final XCommand addPlace = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), placeFieldId, false);
        final XCommand setPlace = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                placeFieldId), 0, XV.toValue(place), false);

        final XCommand addYear = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), yearFieldId, false);
        final XCommand setYear = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                yearFieldId), 0, XV.toValue(year), false);

        final XCommand addMonth = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), monthFieldId, false);
        final XCommand setMonth = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                monthFieldId), 0, XV.toValue(month), false);

        final XCommand addDay = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), dayFieldId, false);
        final XCommand setDay = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                dayFieldId), 0, XV.toValue(day), false);

        final XCommand addDescription = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), descFieldId, false);
        final XCommand setDescription = cmdFactory.createAddValueCommand(
                Base.resolveField(this.calendarRepoID, userID, eventID, descFieldId), 0,
                XV.toValue(description), false);

        final XCommand addBegin = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), beginFieldId, false);
        final XCommand setBegin = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                beginFieldId), 0, XV.toValue(begin), false);

        final XCommand addEnd = cmdFactory.createAddFieldCommand(
                Base.resolveObject(this.calendarRepoID, userID, eventID), endFieldId, false);
        final XCommand setEnd = cmdFactory.createAddValueCommand(Base.resolveField(this.calendarRepoID, userID, eventID,
                endFieldId), 0, XV.toValue(end), false);

        // put the commands into a transaction
        final XTransactionBuilder transBuilder = new XTransactionBuilder(userCalendar.getAddress());
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
        final long result = userCalendar.executeCommand(transBuilder.build());

        return result != XCommand.FAILED;
    }

    /**
     * A method for checking if the given user name is actually taken and if it
     * is, to check if the given password is correct
     *
     * @param userName The name of the user
     * @param pwd The password of the user
     * @return true, if the user exists and the given password is correct
     */
    private boolean checkUserNameAndPassword(final String userName, final String pwd) {
        final XId userID = Base.toId(userName);

        // check if the user exists
        final XObject usrAccount = this.accountModel.getObject(userID);
        if(usrAccount == null) {
            return false;
        }

        // check if the given password is right
        if(!pwd.equals(usrAccount.getField(pwdFieldId).getValue().toString())) {
            return false;
        }

        return true;
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
    public String getEvents(final String userName, final String pwd, final String calendar, final int beginYear,
            final int beginMonth, final int beginDay, final int endYear, final int endMonth, final int endDay) {
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

        final XId userID = Base.toId(userName);
        final XId calendarID = Base.toId(calendar);

        final XModel userCalendar = this.calendarRepo.getModel(calendarID);

        // check whether the user is allowed to read the specified calendar or
        // not
        if(!this.arm.canRead(userID, userCalendar.getAddress())) {
            // user has no right access!
            return "Error!";
        }

        // build the string representation
        String result = "{";

        for(final XId eventID : userCalendar) {
            final XObject event = userCalendar.getObject(eventID);

            final int eventYear = ((XIntegerValue)event.getField(yearFieldId).getValue()).contents();
            final int eventMonth = ((XIntegerValue)event.getField(monthFieldId).getValue()).contents();
            final int eventDay = ((XIntegerValue)event.getField(dayFieldId).getValue()).contents();

            if(eventYear >= beginYear && eventYear <= endYear) {
                if(eventMonth >= beginMonth && eventMonth <= endMonth) {
                    if(eventDay >= beginDay && eventDay <= endDay) {
                        // the date of the event is within the given bounds!
                        final String name = ((XStringValue)event.getField(nameFieldId).getValue())
                                .contents();
                        final String place = ((XStringValue)event.getField(placeFieldId).getValue())
                                .contents();
                        final String description = ((XStringValue)event.getField(descFieldId).getValue())
                                .contents();

                        final int begin = ((XIntegerValue)event.getField(beginFieldId).getValue())
                                .contents();
                        final int end = ((XIntegerValue)event.getField(endFieldId).getValue()).contents();

                        result += "Event: " + name + '\n' + "At: " + place + ", from " + begin
                                + " to " + end + '\n' + "Description: " + description + '\n' + '\n';

                    }
                }
            }
        }
        result += "}";

        return result;
    }

    /**
     * Registers a new user on this Calendar Manager and creates a calendar for
     * the user.
     *
     * @param userName The username.
     * @param pwd The password that the user wants to use.
     * @return true, if registration was successful, false otherwise
     */
    public boolean registerNewUser(final String userName, final String pwd) {
        /*
         * check whether the user name is already taken or not by checking
         * whether there already exists an XObject in our accountModel with an
         * XId that equals the name of the user who wants to register.
         */
        final XId userID = Base.toId(userName);

        if(this.accountModel.getObject(userID) != null) {
            return false;
        }

        // create the entry for the user in the accountModel and save the
        // password
        final XObject userObject = this.accountModel.createObject(userID);
        final XField pwdField = userObject.createField(pwdFieldId);

        final XStringValue pwdValue = XV.toValue(pwd);

        pwdField.setValue(pwdValue);

        // create the calendar for the user
        final XModel usrCalendar = this.calendarRepo.createModel(userID);

        // set access rights for the user
        this.arm.getAuthorisationDatabase().setAccess(userID, usrCalendar.getAddress(),
                XA.ACCESS_READ, true);
        this.arm.getAuthorisationDatabase().setAccess(userID, usrCalendar.getAddress(),
                XA.ACCESS_WRITE, true);

        return true;
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
    public boolean shareCalendarWith(final String userName, final String pwd, final String userName2) {
        if(!checkUserNameAndPassword(userName, pwd)) {
            return false;
        }

        final XId user1ID = Base.toId(userName);

        // check whether the second specified user actualy exists
        final XId user2ID = Base.toId(userName2);
        if(this.accountModel.getObject(user2ID) == null) {
            return false;
        }

        // grant read access
        final XModel user1Calendar = this.calendarRepo.getModel(user1ID);

        this.arm.getAuthorisationDatabase().setAccess(user2ID, user1Calendar.getAddress(),
                XA.ACCESS_READ, true);

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
    public boolean unshareCalendarWith(final String userName, final String pwd, final String userName2) {
        if(!checkUserNameAndPassword(userName, pwd)) {
            return false;
        }

        final XId user1ID = Base.toId(userName);
        final XId user2ID = Base.toId(userName2);
        if(this.accountModel.getObject(user2ID) == null) {
            return false;
        }

        final XModel user1Calendar = this.calendarRepo.getModel(user1ID);

        this.arm.getAuthorisationDatabase().setAccess(user2ID, user1Calendar.getAddress(),
                XA.ACCESS_READ, false);

        return true;
    }
}
