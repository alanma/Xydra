package org.xydra.oo.testspecs;

import java.util.List;
import java.util.Set;

import org.xydra.oo.generator.Comment;

public class TasksSpec {

	abstract class Home {
		BaseList inbox, starred, today, week;
	}

	abstract class User {
		String name;
		String email;
		String password;
		String picture;
		Settings settings;
	}

	// static enum TimeFormat {
	// AMPM, TWENTYFOUR
	// }

	abstract class Settings {
		boolean newItemsAtTop;
		int startOfWeek;
		int timeFormat;
		String language;
		boolean notifyByEmail;
		boolean notifyByDesktop;
		boolean notifyByPush;
	}

	abstract class BaseList {

		String name;

		String icon;

		@Comment("a number to be displayed in the 'badge' of the list")
		abstract int counter();

		User owner;

		Set<User> members;

		boolean isShared() {
			return !isPrivate();
		}

		@Comment("true if the list has no members")
		abstract boolean isPrivate();
	}

	abstract class SmartList extends BaseList {
		String querySpec;
	}

	abstract class TaskList extends BaseList {
		List<Task> tasks;
	}

	abstract class Task {

		private static final long RECENTLY_COMPLETED = 8 * 60 * 60 * 1000;

		boolean checked;

		long completionDate;

		boolean isRecentlyCompleted() {
			return System.currentTimeMillis() - this.completionDate < RECENTLY_COMPLETED;
		}

		String title;

		long remindDate;

		long dueDate;

		boolean starred;

		String note;

		List<Task> subTasks;

	}

}
