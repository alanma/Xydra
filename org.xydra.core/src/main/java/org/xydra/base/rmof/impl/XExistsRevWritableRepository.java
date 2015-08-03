package org.xydra.base.rmof.impl;

import org.xydra.annotations.ModificationOperation;
import org.xydra.base.XId;
import org.xydra.base.rmof.XRevWritableRepository;


/**
 * This repository always exists if not null. But it can create models that
 * follow he {@link XExists} idea.
 *
 * @author xamde
 */
public interface XExistsRevWritableRepository extends XRevWritableRepository {

    @Override
	XExistsRevWritableModel createModel(XId modelId);

    @Override
	XExistsRevWritableModel getModel(XId modelId);

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
    void addModel(XExistsRevWritableModel model);

}
