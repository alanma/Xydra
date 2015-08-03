package org.xydra.webadmin.gwt.client.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;

public class EntityTree {

	HashSet<XId> openRepos = new HashSet<XId>();
	HashMap<XAddress, Set<XAddress>> modelObjectMap = new HashMap<XAddress, Set<XAddress>>();

	public void add(final XAddress entityAddress) {

		/* assert repository is opened */
		final XId entityRepoId = entityAddress.getRepository();
		this.openRepos.add(entityRepoId);

		if (entityAddress.getModel() != null) {
			/* find out if model is already opened */
			final XAddress entityModelAddress = Base.resolveModel(
					Base.toAddress(entityRepoId, null, null, null), entityAddress.getModel());

			final Set<XAddress> openModels = this.modelObjectMap.keySet();
			if (openModels.contains(entityModelAddress)) {
				// nothing: model is already opened
			} else {
				this.modelObjectMap.put(entityModelAddress, new HashSet<XAddress>());
			}

			if (entityAddress.getObject() != null) {

				/* find out if object is already opened */
				final XAddress objectAddress = Base.resolveObject(
						Base.toAddress(entityRepoId, null, null, null), entityAddress.getModel(),
						entityAddress.getObject());

				final Set<XAddress> openObjectSet = this.modelObjectMap.get(entityModelAddress);
				openObjectSet.add(objectAddress);
			}
		}
	}

	public void remove(final XAddress entityAddress) {

		final XId entityRepoId = entityAddress.getRepository();
		final XAddress entityModelAddress = Base.resolveModel(Base.toAddress(entityRepoId, null, null, null),
				entityAddress.getModel());

		if (entityAddress.getObject() != null) {
			final XAddress objectAddress = Base.resolveObject(Base.toAddress(entityRepoId, null, null, null),
					entityAddress.getModel(), entityAddress.getObject());
			final Set<XAddress> openObjectSet = this.modelObjectMap.get(entityModelAddress);
			openObjectSet.remove(objectAddress);
		} else {
			if (entityAddress.getModel() != null) {
				this.modelObjectMap.remove(entityModelAddress);
			} else {
				final XId repoId = entityAddress.getRepository();
				this.openRepos.remove(repoId);

				/* remove all models, which contain that repo */
				final Set<Entry<XAddress, Set<XAddress>>> modelObjectEntries = this.modelObjectMap
						.entrySet();

				final Set<XAddress> modelToBeDeleted = new HashSet<XAddress>();
				for (final Entry<XAddress, Set<XAddress>> entry : modelObjectEntries) {
					final XAddress modelKey = entry.getKey();
					if (modelKey.getRepository().equals(repoId)) {
						modelToBeDeleted.add(modelKey);
					}
				}
				for (final XAddress xAddress : modelToBeDeleted) {
					this.modelObjectMap.remove(xAddress);
				}
			}
		}
	}

	@Override
	public String toString() {
		String resultString = "";

		resultString += "open Repos: \n";
		for (final XId repoId : this.openRepos) {
			resultString += repoId.toString() + ", ";
		}

		resultString += "\n Models: \n";
		for (final XAddress modelAddress : this.modelObjectMap.keySet()) {
			resultString += modelAddress.toString() + ", ";

			final Set<XAddress> objectSet = this.modelObjectMap.get(modelAddress);
			resultString += "objects in this model: \n";
			for (final XAddress xAddress : objectSet) {
				resultString += xAddress.toString() + ", ";
			}
			resultString += "\n";
		}

		return resultString;
	}

	public Set<XId> getOpenRepos() {
		return this.openRepos;
	}

}
