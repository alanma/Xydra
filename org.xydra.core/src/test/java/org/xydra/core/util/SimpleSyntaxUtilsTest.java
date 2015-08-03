package org.xydra.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xydra.base.Base;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.LoggerTestHelper;
import org.xydra.core.XX;
import org.xydra.core.model.XModel;
import org.xydra.core.serialize.SerializedModel;
import org.xydra.core.serialize.XydraOut;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;


public class SimpleSyntaxUtilsTest {

    private static final Logger log = getLogger();

    static final XId PHONEBOOK = XX.toId("phonebook");

    static String test = "# declares the XObject 'hans' and the property 'phone', sets value of hans.phone to '123'.\n"
            + "hans.phone=123\n"
            + "hans.email=hans@example.com\n"
            + "hans.knows=[peter,john,dirk]\n"
            + "# declares peter as an object\n"
            + "peter\n"
            + "john.phone=1234\n";

    public static final void dump(final XReadableModel model) {
        log.debug(toXml(model));
    }

    public static final String toXml(final XReadableModel model) {
        final XydraOut out = new XmlOut();
        SerializedModel.serialize(model, out, true, false, true);
        return out.getData();
    }

    private static Logger getLogger() {
        LoggerTestHelper.init();
        return LoggerFactory.getLogger(SimpleSyntaxUtilsTest.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError1() {
        SimpleSyntaxUtils.toModel(PHONEBOOK, "hans.likes=ice\nhans.likes=fruit");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError2() {
        SimpleSyntaxUtils.toModel(PHONEBOOK, "hans=somevalue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError3() {
        dump(SimpleSyntaxUtils.toModel(PHONEBOOK, "hans with space=somevalue"));
    }

    @Test
    public void testParsing() {
        final XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
        assertTrue(model.hasObject(Base.toId("hans")));
        assertTrue(model.hasObject(Base.toId("peter")));
    }

    @Test
    public void testParsing2() {
        final XModel model = SimpleSyntaxUtils.toModel(PHONEBOOK, test);
        final String syntax = SimpleSyntaxUtils.toSimpleSyntax(model);
        final XModel model2 = SimpleSyntaxUtils.toModel(PHONEBOOK, syntax);
        /* John has rev 10 in one model and rev 11 in the other one */
        assertTrue(XCompareUtils.equalTree(model, model2));
        assertEquals(syntax, SimpleSyntaxUtils.toSimpleSyntax(model2));
    }

}
