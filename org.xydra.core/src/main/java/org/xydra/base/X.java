package org.xydra.base;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandFactory;
import org.xydra.base.change.impl.memory.MemoryCommandFactory;
import org.xydra.base.value.XValue;
import org.xydra.base.value.XValueFactory;
import org.xydra.base.value.impl.memory.MemoryValueFactory;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.MemoryStringIDProvider;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XSPI;


/**
 * A utility class that provides helpful methods concerning the set-up of Xydra.
 * 
 * @author Voelkel
 * @author Kaidel
 * 
 */
public class X {
	
	private static XCommandFactory commandFactory;
	
	public static final String DEFAULT_REPOSITORY_ID = "repo";
	
	private static XIDProvider idProvider;
	
	private static XValueFactory valueFactory;
	
	/**
	 * Creates an {@link XRepository} implementation that lives in-memory. The
	 * underlying {@link XSPI} layer determines where persistence ultimately
	 * happens.
	 * 
	 * @param actorId TODO
	 * 
	 * @return the new repository with ID = {@link X}#DEFAULT_REPOSITORY_ID.
	 */
	public static XRepository createMemoryRepository(XID actorId) {
		XID repoId = getIDProvider().fromString(DEFAULT_REPOSITORY_ID);
		XAddress repoAddr = getIDProvider().fromComponents(repoId, null, null, null);
		XRepositoryState repoState = XSPI.getStateStore().createRepositoryState(repoAddr);
		// TODO where to get the passwordHash?
		return new MemoryRepository(actorId, null, repoState);
	}
	
	/**
	 * Returns the {@link XCommandFactory} instance of the Xydra Instance that
	 * is currently being used. An {@link XCommandFactory} provides methods for
	 * creating {@link XCommand}s of all types.
	 * 
	 * @return Returns the {@link XCommandFactory} of the Xydra Instance that is
	 *         currently being used.
	 */
	public static XCommandFactory getCommandFactory() {
		if(commandFactory == null) {
			commandFactory = new MemoryCommandFactory();
		}
		
		return commandFactory;
	}
	
	/**
	 * Returns the {@link XIDProvider} instance of the Xydra Instance that is
	 * currently being used. {@link XID}s should only be created using this
	 * {@link XIDProvider} instance to ensure, that only unique {@link XID}s are
	 * being used.
	 * 
	 * TODO Maybe we should think about a way to persist the XIDProvider in the
	 * future to really ensure unique random ids
	 * 
	 * @return Returns the {@link XIDProvider} instance of the Xydra Instance
	 *         that is currently being used.
	 */
	public static XIDProvider getIDProvider() {
		if(idProvider == null) {
			idProvider = new MemoryStringIDProvider();
		}
		return idProvider;
	}
	
	/**
	 * Returns the {@link XValueFactory} instance of the Xydra Instance that is
	 * currently being used. An {@link XValueFactory} provides methods for
	 * creating {@link XValue XValues} of all types.
	 * 
	 * @return Returns the {@link XValueFactory} of the Xydra Instance that is
	 *         currently being used.
	 */
	public static XValueFactory getValueFactory() {
		if(valueFactory == null) {
			valueFactory = new MemoryValueFactory();
		}
		
		return valueFactory;
	}
	
	// TODO Maybe we should add a method for creating Repositories with
	// arbitrary IDs?
}
