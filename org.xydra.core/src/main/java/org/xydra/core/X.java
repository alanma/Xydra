package org.xydra.core;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XCommandFactory;
import org.xydra.core.change.impl.memory.MemoryCommandFactory;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XIDProvider;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.core.model.impl.memory.MemoryStringIDProvider;
import org.xydra.core.model.state.XRepositoryState;
import org.xydra.core.model.state.XSPI;
import org.xydra.core.value.XValueFactory;
import org.xydra.core.value.impl.memory.MemoryValueFactory;


/**
 * A utility class that provides helpful methods concerning the set-up of
 * XModels.
 * 
 * @author Voelkel
 * @author Kaidel
 * 
 */
public class X {
	
	private static XIDProvider idProvider;
	
	private static XValueFactory valueFactory;
	
	private static XCommandFactory commandFactory;
	
	/**
	 * Returns the XIDProvider of the XModel that is currently being used. XIDs
	 * should only be created using the XIDProvider to ensure, that only unique
	 * IDs are being used.
	 * 
	 * @return Returns the XIDProvider of the XModel that is currently being
	 *         used.
	 */
	
	public static XIDProvider getIDProvider() {
		if(idProvider == null) {
			idProvider = new MemoryStringIDProvider();
		}
		return idProvider;
	}
	
	/**
	 * Returns the XValueFactory of the XModel that is currently being used. The
	 * XValueFacotry provides methods for creating XValues of all types.
	 * 
	 * @return Returns the XValueFactory of the XModel that is currently being
	 *         used.
	 */
	
	public static XValueFactory getValueFactory() {
		if(valueFactory == null) {
			valueFactory = new MemoryValueFactory();
		}
		
		return valueFactory;
	}
	
	/**
	 * Returns the {@link XCommandFactory} of the XModel that is currently being
	 * used. The {@link XCommandFactory} provides methods for creating
	 * {@link XCommand}s of all types.
	 * 
	 * @return Returns the {@link XCommandFactory} of the XModel that is
	 *         currently being used.
	 */
	
	public static XCommandFactory getCommandFactory() {
		if(commandFactory == null) {
			commandFactory = new MemoryCommandFactory();
		}
		
		return commandFactory;
	}
	
	public static final String DEFAULT_REPOSITORY_ID = "repo";
	
	/**
	 * Creates an XRepository implementation that lives in-memory. The
	 * underlying {@link XSPI} layer determines where persistence ultimately
	 * happens.
	 * 
	 * @return the new repository with ID = {@link X}#DEFAULT_REPOSITORY_ID.
	 */
	
	public static XRepository createMemoryRepository() {
		XID repoId = getIDProvider().fromString(DEFAULT_REPOSITORY_ID);
		XAddress repoAddr = getIDProvider().fromComponents(repoId, null, null, null);
		XRepositoryState repoState = XSPI.getStateStore().createRepositoryState(repoAddr);
		return new MemoryRepository(repoState);
	}
}
