package org.xydra.testgae;

/**
 * A simple thread for executing a specific operation multiple times.
 * 
 * @author Kaidel
 */

public class OperationWorker extends Thread {
	private Operation operation;
	private int times;
	
	public OperationWorker(int times, Operation operation) {
		this.times = times;
		this.operation = operation;
	}
	
	@Override
	public void run() {
		for(int i = 0; i < this.times; i++) {
			this.operation.doOperation();
		}
	}
	
	public Operation getOperation() {
		return this.operation;
	}
}
