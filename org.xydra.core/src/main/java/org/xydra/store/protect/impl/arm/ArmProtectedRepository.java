package org.xydra.store.protect.impl.arm;

import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.core.model.XRepository;
import org.xydra.store.access.XAuthorisationManager;
import org.xydra.store.protect.XProtectedRepository;



/**
 * An abstract {@link XProtectedRepository} that wraps an {@link XRepository}
 * for a specific actor and checks all access against an
 * {@link XAuthorisationManager}.
 *
 * @author dscharrer
 *
 */
public class ArmProtectedRepository extends AbstractArmProtectedRepository {

	public ArmProtectedRepository(final XRepository repo, final XAuthorisationManager arm, final XId actor) {
		super(repo, arm, actor);
	}

	@Override
	protected XAuthorisationManager getArmForModel(final XId modelId) {
		return getArm();
	}

	@Override
	public XType getType() {
		return XType.XREPOSITORY;
	}

}
