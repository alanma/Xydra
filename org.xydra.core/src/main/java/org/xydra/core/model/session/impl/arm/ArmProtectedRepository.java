package org.xydra.core.model.session.impl.arm;

import org.xydra.core.access.XAccessManagerWithListeners;
import org.xydra.core.model.XID;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedRepository;


/**
 * An abstract {@link XProtectedRepository} that wraps an {@link XRepository}
 * for a specific actor and checks all access against an {@link XAccessManagerWithListeners}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedRepository extends AbstractArmProtectedRepository {
	
	public ArmProtectedRepository(XRepository repo, XAccessManagerWithListeners arm, XID actor) {
		super(repo, arm, actor);
	}
	
	@Override
	protected XAccessManagerWithListeners getArmForModel(XID modelId) {
		return getArm();
	}
	
}
