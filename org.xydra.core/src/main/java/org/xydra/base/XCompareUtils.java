package org.xydra.base;

import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XReadableRepository;
import org.xydra.base.rmof.XStateReadableField;
import org.xydra.base.rmof.XStateReadableModel;
import org.xydra.base.rmof.XStateReadableObject;
import org.xydra.base.rmof.XStateReadableRepository;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.index.XI;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


/**
 * A helper class containing methods for comparing Xydra entities in different
 * ways.
 *
 * @author kaidel
 *
 */
public class XCompareUtils {

    private static final Logger log = LoggerFactory.getLogger(XCompareUtils.class);

    /**
     * @param fieldA the bigger field
     * @param fieldB the smaller field
     * @return true if all data from fieldB is also contained in fieldA
     */
    public static boolean containsTree(final XWritableField fieldA, final XWritableField fieldB) {
        return fieldB.isEmpty() || !fieldA.isEmpty();
    }

    /**
     * @param modelA the bigger model
     * @param modelB the smaller model
     * @return true if all tree-state information of modelB is also contained in
     *         modelA. In other words: modelA must be a super-set of modelB.
     */
    public static boolean containsTree(final XWritableModel modelA, final XWritableModel modelB) {
        if(modelA == null) {
            log.warn("ModelA is null");
            return modelB == null;
        }
        if(modelB == null) {
			return true;
		}
        // for every object: assert it is in A
        for(final XId objectId : modelB) {
            if(!modelA.hasObject(objectId)) {
                return false;
            }
            final XWritableObject objectA = modelA.getObject(objectId);
            if(!containsTree(objectA, modelB.getObject(objectId))) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsTree(final XWritableObject objectA, final XWritableObject objectB) {
        // for every field: assert it is in A
        for(final XId fieldId : objectB) {
            if(!objectA.hasField(fieldId)) {
                return false;
            }
            final XWritableField fieldA = objectA.getField(fieldId);
            if(!containsTree(fieldA, objectB.getField(fieldId))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if two {@link XReadableField}s have the same ID, the same revision
     * and the same {@link XValue}.
     *
     * This is similar to
     * {@link XCompareUtils#equalTree(XStateReadableField, XStateReadableField)}
     * but also checks the revision number.
     *
     * Parent-{@link XReadableObject}s, if they exist, are not compared
     *
     * @param fieldA
     * @param fieldB
     *
     * @return true if the two {@link XReadableField}s have the same state.
     */
    // 2010-10-27: used here + in several different functionality tests
    public static boolean equalState(final XReadableField fieldA, final XReadableField fieldB) {
        if(!equalTree(fieldA, fieldB)) {
            return false;
        }

        if(fieldA.getRevisionNumber() != fieldB.getRevisionNumber()) {
            if(log.isDebugEnabled()) {
				log.debug("revNr differs");
			}
            return false;
        }

        return true;
    }

    /**
     * Check if two {@link XReadableModel}s have the same {@link XId}, the same
     * revision and the same {@link XReadableObject}s as defined by
     * {@link XCompareUtils#equalState(XReadableObject, XReadableObject)}.
     *
     * This is similar to
     * {@link XCompareUtils#equalTree(XStateReadableModel, XStateReadableModel)}
     * but also checks the revision number.
     *
     * Parent-repositories, if they exist, are not compared.
     *
     * @param modelA
     * @param modelB
     *
     * @return true if the two {@link XReadableModel}s have the same state.
     */
    public static boolean equalState(final XReadableModel modelA, final XReadableModel modelB) {
        if(!equalId(modelA, modelB)) {
            if(log.isDebugEnabled()) {
				log.debug("model id differs");
			}
            return false;
        }

        if(modelA.getRevisionNumber() != modelB.getRevisionNumber()) {
            if(log.isDebugEnabled()) {
				log.debug("model revNr differs A=" + modelA.getRevisionNumber() + " vs. B="
				        + modelB.getRevisionNumber());
			}
            return false;
        }

        for(final XId objectId : modelA) {

            final XReadableObject objectA = modelA.getObject(objectId);
            final XReadableObject objectB = modelB.getObject(objectId);

            if(objectB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no object " + objectId);
				}
                return false;
            }

            if(!XCompareUtils.equalState(objectA, objectB)) {
                if(log.isDebugEnabled()) {
					log.debug("object " + objectId + " differs");
				}
                return false;
            }

        }

        for(final XId objectId : modelB) {

            if(modelA.getObject(objectId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no object " + objectId);
				}
                return false;
            }

        }

        return true;
    }

    /**
     * Check if two {@link XReadableObject}s have the same {@link XId}, the same
     * revision and the same {@link XReadableField}s as defined by
     * {@link XCompareUtils#equalState(XReadableField, XReadableField)}.
     *
     * This is similar to
     * {@link XCompareUtils#equalTree(XStateReadableObject, XStateReadableObject)}
     * but also checks the revision number.
     *
     * Parent-{@link XReadableModel}s, if they exist, are not compared
     *
     * @param objectA
     * @param objectB
     *
     * @return true if the two {@link XReadableObject}s have the same state.
     */
    public static boolean equalState(final XReadableObject objectA, final XReadableObject objectB) {
        if(!equalId(objectA, objectB)) {
            if(log.isDebugEnabled()) {
				log.debug("object id differs");
			}
            return false;
        }

        if(objectA.getRevisionNumber() != objectB.getRevisionNumber()) {
            if(log.isDebugEnabled()) {
				log.debug("revNr differs; a=" + objectA.getRevisionNumber() + " ("
				        + objectA.getAddress() + ") b=" + objectB.getRevisionNumber() + " ("
				        + objectB.getAddress() + ") equalTree?" + equalTree(objectA, objectB));
			}
            return false;
        }

        for(final XId fieldId : objectA) {

            final XReadableField fieldA = objectA.getField(fieldId);
            final XReadableField fieldB = objectB.getField(fieldId);

            if(fieldB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no field " + fieldId);
				}
                return false;
            }

            if(!XCompareUtils.equalState(fieldA, fieldB)) {
                if(log.isDebugEnabled()) {
					log.debug("field " + fieldId + " differs");
				}
                return false;
            }

        }

        for(final XId fieldId : objectB) {
            if(objectA.getField(fieldId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no field " + fieldId);
				}
                return false;
            }
        }

        return true;
    }

    /**
     * Check if two {@link XReadableRepository}s have the same {@link XId}, the
     * same revision and the same {@link XReadableModel}s as defined by
     * {@link XCompareUtils#equalState(XReadableModel, XReadableModel)}.
     *
     * This is similar to
     * {@link XCompareUtils#equalTree(XStateReadableRepository, XStateReadableRepository)}
     * but also checks the revision number.
     *
     * @param repoA
     * @param repoB
     *
     * @return true if the two {@link XReadableRepository}s have the same state.
     */
    public static boolean equalState(final XReadableRepository repoA, final XReadableRepository repoB) {
        if(!equalId(repoA, repoB)) {
            if(log.isDebugEnabled()) {
				log.debug("repo id differs");
			}
            return false;
        }

        for(final XId modelId : repoA) {

            final XReadableModel modelA = repoA.getModel(modelId);
            final XReadableModel modelB = repoB.getModel(modelId);

            if(modelB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no model " + modelId);
				}
                return false;
            }

            if(!XCompareUtils.equalState(modelA, modelB)) {
                if(log.isDebugEnabled()) {
					log.debug("Model " + modelId + " differs");
				}
                return false;
            }

        }

        for(final XId modelId : repoB) {

            if(repoA.getModel(modelId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no model " + modelId);
				}
                return false;
            }

        }

        return true;
    }

    /**
     * Check if two {@link XStateReadableField}s have the same {@link XId} and
     * the same {@link XValue}.
     *
     * This is similar to {@link #equalState(XReadableField, XReadableField)}
     * but ignores the revision number.
     *
     * Parent-{@link XReadableObject}s, if they exist, are not compared.
     *
     * @param fieldA
     * @param fieldB
     *
     * @return true if the two {@link XStateReadableField}s represent the same
     *         subtree.
     */
    public static boolean equalTree(final XStateReadableField fieldA, final XStateReadableField fieldB) {
        if(fieldA == null && fieldB == null) {
            return true;
        }

        if(fieldA == null || fieldB == null) {
            if(log.isDebugEnabled()) {
				log.debug("one of them is null, the other isn't");
			}
            return false;
        }

        if(!XI.equals(fieldA.getValue(), fieldB.getValue())) {
            if(log.isDebugEnabled()) {
				log.debug("values differ A=" + fieldA.getValue() + " B=" + fieldB.getValue());
			}
            return false;
        }

        if(!fieldA.getId().equals(fieldB.getId())) {
            if(log.isDebugEnabled()) {
				log.debug("field Ids differ");
			}
            return false;
        }

        return true;
    }

    /**
     * Check if two {@link XReadableModel}s have the same {@link XId} and the
     * same {@link XReadableObject}s as defined by
     * {@link XCompareUtils#equalTree(XStateReadableObject, XStateReadableObject)}
     * .
     *
     * This is similar to {@link #equalState(XReadableModel, XReadableModel)}
     * ignores the revision number.
     *
     * Parent-{@link XReadableRepository}s, if they exist, are not compared.
     *
     * @param modelA
     * @param modelB
     *
     * @return true if the two {@link XReadableModel}s represent the same tree.
     */
    public static boolean equalTree(final XStateReadableModel modelA, final XStateReadableModel modelB) {
        if(!equalId(modelA, modelB)) {
            if(log.isDebugEnabled()) {
				log.debug("model ids differ");
			}
            return false;
        }

        for(final XId objectId : modelA) {

            final XStateReadableObject objectA = modelA.getObject(objectId);
            final XStateReadableObject objectB = modelB.getObject(objectId);

            if(objectB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no object " + objectId);
				}
                return false;
            }

            if(!XCompareUtils.equalTree(objectA, objectB)) {
                if(log.isDebugEnabled()) {
					log.debug("object " + objectId + " differs");
				}
                return false;
            }

        }

        for(final XId objectId : modelB) {

            if(modelA.getObject(objectId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no object " + objectId);
				}
                return false;
            }

        }

        return true;
    }

    /**
     * Check if two {@link XReadableObject}s have the same {@link XId} and the
     * same {@link XReadableField}s as defined by
     * {@link XCompareUtils#equalTree(XStateReadableField, XStateReadableField)}
     * .
     *
     * This is similar to {@link #equalState(XReadableObject, XReadableObject)}
     * but ignores the revision number.
     *
     * Parent-{@link XReadableModel}s, if they exist, are not compared.
     *
     * @param objectA
     * @param objectB
     *
     * @return true if the two {@link XReadableObject}s represent the same
     *         subtree.
     */
    public static boolean equalTree(final XStateReadableObject objectA, final XStateReadableObject objectB) {
        if(!equalId(objectA, objectB)) {
            if(log.isDebugEnabled()) {
				log.debug("object ids differ");
			}
            return false;
        }

        for(final XId fieldId : objectA) {

            final XStateReadableField fieldA = objectA.getField(fieldId);
            final XStateReadableField fieldB = objectB.getField(fieldId);

            if(fieldB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no field " + fieldId);
				}
                return false;
            }

            if(!XCompareUtils.equalTree(fieldA, fieldB)) {
                if(log.isDebugEnabled()) {
					log.debug("field " + fieldId + " differs");
				}
                return false;
            }

        }

        for(final XId fieldId : objectB) {
            if(objectA.getField(fieldId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no field " + fieldId);
				}
                return false;
            }
        }

        return true;
    }

    /**
     * Check if two {@link XReadableRepository}s have the same {@link XId} and
     * the same {@link XReadableModel}s as defined by
     * {@link XCompareUtils#equalTree(XStateReadableModel, XStateReadableModel)}
     * .
     *
     * This is similar to
     * {@link #equalState(XReadableRepository, XReadableRepository)} but ignores
     * the revision number.
     *
     * Parent-{@link XReadableModel}s, if they exist, are not compared.
     *
     * @param repoA
     * @param repoB
     *
     * @return true if the two {@link XReadableRepository}s represent the same
     *         tree.
     */
    public static boolean equalTree(final XStateReadableRepository repoA, final XStateReadableRepository repoB) {
        if(!equalId(repoA, repoB)) {
            if(log.isDebugEnabled()) {
				log.debug("repo id differs");
			}
            return false;
        }

        for(final XId modelId : repoA) {
            final XStateReadableModel modelA = repoA.getModel(modelId);
            final XStateReadableModel modelB = repoB.getModel(modelId);

            if(modelB == null) {
                if(log.isDebugEnabled()) {
					log.debug("B has no model " + modelId);
				}
                return false;
            }

            if(!XCompareUtils.equalTree(modelA, modelB)) {
                if(log.isDebugEnabled()) {
					log.debug("Model " + modelId + " differs");
				}
                return false;
            }

        }

        for(final XId modelId : repoB) {

            if(repoA.getModel(modelId) == null) {
                if(log.isDebugEnabled()) {
					log.debug("A has no model " + modelId);
				}
                return false;
            }

        }

        return true;
    }

    /**
     * @param a
     * @param b
     * @return true if both objects are null or both are not-null and have the
     *         same id.
     */
    public static boolean equalId(final IHasXId a, final IHasXId b) {
        if(a == null && b == null) {
            return true;
        }

        if(a == null || b == null) {
            if(log.isDebugEnabled()) {
				log.debug("one of them is null, the other isn't");
			}
            return false;
        }

        return a.getId().equals(b.getId());
    }

}
