package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.xgae.XGae;
import org.xydra.xgae.datastore.api.SKey;


/**
 * Translates {@link XAddress} to GAE {@link SKey Keys}.
 * 
 * Changes are persisted as {@link #KIND_XCHANGE} and {@link #KIND_XVALUE}
 * types.
 * 
 * @author scharrer
 * 
 */
public class KeyStructure {
    
    public static final String KIND_XVALUE = "XVALUE";
    public static final String KIND_XCHANGE = "XCHANGE";
    
    /**
     * Note: Only used by InternalGae(MOF)Entity
     * 
     * @param address The {@link XAddress} of the entity that the key should
     *            point to.
     * @return a GAE {@link SKey} addressing an internal Xydra entity (not a
     *         snapshot entity)
     */
    public static SKey createEntityKey(XAddress address) {
        String kind = address.getAddressedType().name();
        return XGae.get().datastore().createKey(kind, address.toURI());
    }
    
    /**
     * @param entityKey A key as returned by {@link #createEntityKey(XAddress)}
     * @return a Xydra {@link XAddress} from a entity (via
     *         {@link #createEntityKey(XAddress)}) GAE {@link SKey}
     */
    public static XAddress toAddress(SKey entityKey) {
        String combinedKeyString = entityKey.getName();
        XAddress address = XX.toAddress(combinedKeyString);
        assert address.getAddressedType().toString().equals(entityKey.getKind());
        return address;
    }
    
    /**
     * @param modelAddr The address of the model containing the entity modified
     *            by the change.
     * @param revision The revision number of the change.
     * @return a GAE {@link SKey} representing a Xydra change entity
     */
    public static SKey createChangeKey(XAddress modelAddr, long revision) {
        assert modelAddr.getAddressedType() == XType.XMODEL;
        return XGae.get().datastore().createKey(KIND_XCHANGE, revision + modelAddr.toURI());
    }
    
    /**
     * @param key must be a change key created via
     *            {@link #createChangeKey(XAddress, long)}
     * @return the XAddress part of the change key
     */
    public static XAddress getAddressFromChangeKey(SKey key) {
        assert key.getKind().equals(KIND_XCHANGE);
        String name = key.getName();
        int firstSlash = name.indexOf("/");
        String address = name.substring(firstSlash);
        XAddress xa = XX.toAddress(address);
        return xa;
    }
    
    /**
     * @param modelAddr The address of the model containing the {@link XValue}.
     * @param rev The revision number of the change that set the value.
     * @param transindex The index of the event that set the value in the
     *            change.
     * @return a GAE {@link Key} representing an internal part of a Xydra change
     *         entity
     */
    static SKey createValueKey(XAddress modelAddr, long rev, int transindex) {
        assert modelAddr.getAddressedType() == XType.XMODEL;
        return XGae.get().datastore()
                .createKey(KIND_XVALUE, rev + "+" + transindex + "/" + modelAddr.toURI());
    }
    
    /**
     * @param key The GAE {@link Key} who'se type to check.
     * @return true if the given GAE {@link Key} represents a Xydra change
     *         entity
     */
    private static boolean isChangeKey(SKey key) {
        return key.getKind().equals(KIND_XCHANGE);
    }
    
    /**
     * Check that the revision encoded in the given key matches the given
     * revision.
     * 
     * @param key A key for a change entity as returned by
     *            {@link createChangeKey}.
     * @param key The key to check
     */
    static boolean assertRevisionInKey(SKey key, long rev) {
        assert isChangeKey(key) : "key = " + key + " with kind " + key.getKind();
        return getRevisionFromChangeKey(key) == rev;
    }
    
    /**
     * @param key a non-null change key that has been created via
     *            {@link #createChangeKey(XAddress, long)}.
     * @return the revision number encoded in a change key.
     */
    public static long getRevisionFromChangeKey(SKey key) {
        assert isChangeKey(key) : key.toString();
        String keyStr = key.getName();
        return getRevisionFromChangeKey(keyStr);
    }
    
    public static long getRevisionFromChangeKey(String keyStr) {
        int p = keyStr.indexOf("/");
        assert p > 0 : keyStr;
        String revStr = keyStr.substring(0, p);
        return Long.parseLong(revStr);
    }
    
    public static String toString(SKey key) {
        return key.getKind() + "|" + key.getName();
    }
    
    /**
     * @param key must have format 'KIND|NAME'
     * @return a gae key
     */
    public static SKey toKey(String key) {
        int index = key.indexOf("|");
        assert index > 0;
        String kind = key.substring(0, index);
        String name = key.substring(index + 1, key.length());
        return XGae.get().datastore().createKey(kind, name);
    }
    
}
