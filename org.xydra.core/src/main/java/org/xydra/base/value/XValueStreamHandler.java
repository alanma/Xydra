package org.xydra.base.value;

import org.xydra.base.XAddress;
import org.xydra.base.XId;


/**
 * A stream of XValue as occurring in serialisation formats.
 *
 * Currently not used by XML serialisation (
 * {@link org.xydra.core.serialize.xml.XmlSerializer}) nor by JOSN (
 * {@link org.xydra.core.serialize.json.JsonSerializer}).
 *
 * Currently only used for serialising values <em>to</em> CSV format. Not used
 * for parsing yet.
 *
 * Unclear if this interface will be needed/supported in the long-term.
 *
 * @author xamde
 */
public interface XValueStreamHandler {

    void startValue();

    void endValue();

    void startCollection(ValueType type);

    void endCollection();

    void javaNull();

    void address(XAddress address);

    void javaBoolean(Boolean a);

    void javaDouble(Double a);

    void javaInteger(Integer a);

    void javaLong(Long a);

    void javaString(String a);

    void xid(XId a);

}
