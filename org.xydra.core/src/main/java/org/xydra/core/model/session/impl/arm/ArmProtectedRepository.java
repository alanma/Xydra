package org.xydra.core.model.session.impl.arm;

import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.session.XProtectedRepository;
import org.xydra.store.access.XAuthorisationManager;


/**
 * An abstract {@link XProtectedRepository} that wraps an {@link XRepository}
 * for a specific actor and checks all access against an
 * {@link XAuthorisationManager}.
 * 
 * @author dscharrer
 * 
 */
public class ArmProtectedRepository extends AbstractArmProtectedRepository {
	
	public ArmProtectedRepository(XRepository repo, XAuthorisationManager arm, XID actor) {
		super(repo, arm, actor);
	}
	
	@Override
	protected XAuthorisationManager getArmForModel(XID modelId) {
		return getArm();
	}
	
	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}
	
}
