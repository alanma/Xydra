package org.xydra.serverapi;

import java.io.Serializable;


/**
 * Contains conditions under which the command should fail.
 * 
 * A command can also be a transaction.
 * 
 * Immutable.
 * 
 * @author voelkel
 * 
 */
public interface ICommand extends Serializable {
	
}
