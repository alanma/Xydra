package org.xydra.store;

import org.xydra.base.XHalfWritableModel;
import org.xydra.base.XHalfWritableRepository;
import org.xydra.base.XID;
import org.xydra.core.access.impl.memory.AccountModelWrapper;
import org.xydra.store.access.XAccountDatabase;
import org.xydra.store.base.Credentials;
import org.xydra.store.base.HalfWritableRepositoryOnStore;


@SuppressWarnings("deprecation")
public class StoreUtils {
	
	/**
	 * @param actorId never null
	 * @param passwordHash never null
	 * @param store
	 * @return an {@link XAccountDatabase} backed by a model in a store. Each
	 *         change operation is immediately executed against changes in the
	 *         account management model in the store.
	 * 
	 * @deprecated not really deprecated, but not working..
	 */
	@Deprecated
	public static XAccountDatabase getAccountDatabase(XID actorId, String passwordHash,
	        XydraStore store) {
		if(actorId == null) {
			throw new IllegalArgumentException("actorId is null");
		}
		if(passwordHash == null) {
			throw new IllegalArgumentException("passwordHash is null");
		}
		Credentials credentials = new Credentials(actorId, passwordHash);
		XHalfWritableRepository repository = new HalfWritableRepositoryOnStore(credentials, store);
		XHalfWritableModel accountModel = repository.getModel(NamingUtils.ID_ACCOUNT_MODEL);
		if(accountModel == null) {
			throw new IllegalStateException("Store is missing the account model '"
			        + NamingUtils.ID_ACCOUNT_MODEL + "'");
		}
		AccountModelWrapper accountModelWrapper = new AccountModelWrapper(accountModel);
		return accountModelWrapper;
	}
}
