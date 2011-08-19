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
	
	// TODO Comment
	
	public void doOperation();
	
	public int getOperationExceptions();
	
	public int getOtherExceptions();
	
	public long getTimesSum();
}
