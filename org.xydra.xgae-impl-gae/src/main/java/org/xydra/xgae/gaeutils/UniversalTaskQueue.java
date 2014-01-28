package org.xydra.xgae.gaeutils;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


/**
 * TODO try to get this to run on gwt
 * 
 * @author xamde
 * 
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class UniversalTaskQueue {
	
	public static void enqueueTask(DeferredTask task) {
		if(AboutAppEngine.onAppEngine()) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder.withDefaults().payload(task));
		} else {
			// run immediately
			task.run();
		}
	}
	
	public static void enqueueTask(NamedDeferredTask task) {
		if(AboutAppEngine.onAppEngine()) {
			Queue queue = QueueFactory.getDefaultQueue();
			queue.add(TaskOptions.Builder.withDefaults().header("id", task.getId()).payload(task));
		} else {
			// run immediately
			task.run();
		}
	}
	
	public static interface NamedDeferredTask extends DeferredTask {
		/**
		 * Some name to be used within url-query strings to identify the task
		 * 
		 * @return the name used to debug this task
		 */
		String getId();
	}
}
