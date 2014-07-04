package org.xydra.base.rmof;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;


/**
 * An {@link XWritableRepository} to which existing {@link XRevWritableModel
 * XRevWritableModels} can be added.
 */
public interface XRevWritableRepository extends XWritableRepository {
	
	/* More specific return type */
	@Override
	@ModificationOperation
	XRevWritableModel createModel(XId modelId);
	
	/* More specific return type */
	@Override
	@ReadOperation
	XRevWritableModel getModel(XId modelId);
	
	/**
	 * Add an existing model to this repository. Models created using
	 * {@link #createModel(XId)} are automatically added.
	 * 
	 * This overwrites any existing model in this repository with the same
	 * {@link XId}.
	 * 
	 * @param model
	 */
	@ModificationOperation
	void addModel(XRevWritableModel model);
	
}
