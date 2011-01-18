package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XID;
import org.xydra.core.access.XAccessManager;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedRepository;


/**
 * An abstract {@link XProtectedRepository} that wraps an {@link XRepository}
 * for a specific actor and checks all access against an {@link XAccessManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedRepository extends AbstractArmProtectedRepository {
	
	public ArmProtectedRepository(XRepository repo, XAccessManager arm, XID actor) {
		super(repo, arm, actor);
	}
	
	@Override
	protected XAccessManager getArmForModel(XID modelId) {
		return getArm();
	}
	
}
