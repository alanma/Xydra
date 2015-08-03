package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;


@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public abstract class AbstractObjectIndex {

    private static final String CLASSNAME = "org.xydra.core.index.impl.memory.AbstractObjectIndex";

    /**
     * convert value to key
     *
     * @param value The value to transform into an {@link XId}.
     * @return an XId parsed from an encoded XValue
     */
    public static XId valueToXId(final XValue value) {
        String key;
        if(value instanceof XStringValue) {
            key = "" + ((XStringValue)value).contents().hashCode();
            if(key.startsWith("-")) {
                key = "m" + key.substring(1);
            } else {
                key = "p" + key;
            }
        } else if(value instanceof XDoubleValue) {
            key = "" + ((XDoubleValue)value).contents();
            key = "a" + key.replace('.', '-');
        } else if(value instanceof XIntegerValue) {
            key = "a" + ((XIntegerValue)value).contents();
        } else if(value instanceof XBooleanValue) {
            key = "" + ((XBooleanValue)value).contents();
        } else if(value instanceof XLongValue) {
            key = "a" + ((XLongValue)value).contents();
        } else if(value instanceof XId) {
            // trivial
            return (XId)value;
        } else {
            // collection types
            assert value instanceof XCollectionValue<?> : "Support for indexing type "
                    + value.getClass().getName() + " has not been implemented yet";
            throw new RuntimeException("Indexing collection types such as "
                    + value.getClass().getName() + " is not supported.");
        }
        final XId xid = BaseRuntime.getIDProvider().fromString(key);
        return xid;
    }

    protected XId actor = X.getIDProvider().fromString(CLASSNAME);
    protected XId fieldId;

    protected XWritableObject indexObject;

    /**
     * @param fieldId The id of the field to index.
     * @param indexObject see
     *            {@link org.xydra.store.NamingUtils#getIndexModelId(XId, String)}
     *            to obtain a suitable XId for your index object
     */
    public AbstractObjectIndex(final XId fieldId, final XWritableObject indexObject) {
        this.fieldId = fieldId;
        this.indexObject = indexObject;
    }

}
