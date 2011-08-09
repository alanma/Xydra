package org.xydra.testgae;

/**
 * A simple interface to capsule operation which an {@link OperationWorker}
 * -Thread can execute (makes configuring one single thread class to do
 * different things easier).
 * 
 * @author Kaidel
 * 
 */
public interface Operation {
	public void doOperation();
}
