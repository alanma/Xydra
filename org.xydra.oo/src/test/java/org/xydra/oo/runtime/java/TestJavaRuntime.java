package org.xydra.oo.runtime.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XV;
import org.xydra.core.model.impl.memory.MemoryModel;
import org.xydra.core.util.DumpUtils;
import org.xydra.oo.testgen.alltypes.java.JavaFactory;
import org.xydra.oo.testgen.alltypes.shared.Colors;
import org.xydra.oo.testgen.alltypes.shared.IHasAllType;
import org.xydra.oo.testgen.alltypes.shared.IPerson;
import org.xydra.oo.testgen.alltypes.shared.MyLongBasedType;
import org.xydra.oo.testgen.tasks.shared.ITask;


public class TestJavaRuntime {
    
    public static void main(String[] args) throws IOException {
        TestJavaRuntime t = new TestJavaRuntime();
        t.useAllTypesRuntime();
    }
    
    @Test
    public void useAllTypesRuntime() throws IOException {
        
        // setup
        JavaTypeMapping.addSingleTypeMapping(MyLongBasedType.class, ValueType.Long,
                MyLongBasedType.MAPPER);
        
        XWritableModel model = new MemoryModel(XX.toId("actor"), "pass",
                XX.toAddress("/repo1/model1"));
        
        JavaFactory factory = new JavaFactory(model);
        IHasAllType alltypes = factory.createHasAllType("o1");
        IPerson p1 = factory.createPerson("p1");
        IPerson p2 = factory.createPerson("p2");
        IPerson p3 = factory.createPerson("p3");
        
        XId id = XX.toId("o1");
        // IHasAllType alltypes = (IHasAllType)Proxy.newProxyInstance(
        // IHasAllType.class.getClassLoader(), new Class<?>[] {
        // IHasAllType.class },
        // new OOJavaOnlyProxy(model, id));
        // model.createObject(id);
        //
        // IPerson p1 =
        // (IPerson)Proxy.newProxyInstance(IPerson.class.getClassLoader(),
        // new Class<?>[] { IPerson.class }, new OOJavaOnlyProxy(model,
        // XX.toId("p1")));
        // IPerson p2 =
        // (IPerson)Proxy.newProxyInstance(IPerson.class.getClassLoader(),
        // new Class<?>[] { IPerson.class }, new OOJavaOnlyProxy(model,
        // XX.toId("p2")));
        // IPerson p3 =
        // (IPerson)Proxy.newProxyInstance(IPerson.class.getClassLoader(),
        // new Class<?>[] { IPerson.class }, new OOJavaOnlyProxy(model,
        // XX.toId("p3")));
        
        // usage
        alltypes.bestFriends().add(p1);
        alltypes.bestFriends().add(p2);
        alltypes.bestFriends().add(p3);
        alltypes.friends().add(p1);
        alltypes.friends().add(p2);
        alltypes.friends().add(p3);
        
        assertEquals(id, alltypes.getId());
        
        assertFalse(alltypes.getJboolean());
        alltypes.setJboolean(true);
        assertTrue(alltypes.getJboolean());
        
        assertNull(alltypes.getJBoolean());
        alltypes.setJBoolean(true);
        assertTrue(alltypes.getJBoolean());
        
        assertNull(alltypes.getXboolean());
        alltypes.setXboolean(XV.toValue(true));
        assertTrue(alltypes.getXboolean().contents());
        
        assertNull(alltypes.getXbooleanlist());
        alltypes.setXbooleanlist(XV.toBooleanListValue(Arrays.asList(true, true, false)));
        assertFalse(alltypes.getXbooleanlist().isEmpty());
        
        // IMPROVE more assertions
        
        // a 1
        alltypes.setXstringset(XV.toStringSetValue(genStrings("a", 3)));
        alltypes.setXstringlist(XV.toStringListValue(genStrings("b", 3)));
        alltypes.setXstring(XV.toValue("c"));
        alltypes.setXlonglist(XV.toLongListValue((List<Long>)Arrays.asList(401l, 402l, 403l)));
        alltypes.setXlong(XV.toValue(5l));
        
        // f 6
        alltypes.setXintegerlist(XV.toIntegerListValue(Arrays.asList(601, 602, 602)));
        alltypes.setXinteger(XV.toValue(7));
        alltypes.setXidsortedset(XV.toIdSortedSetValue(Arrays.asList(XX.toId("h1"), XX.toId("h2"),
                XX.toId("h3"))));
        alltypes.setXidset(XV.toIdSetValue(Arrays.asList(XX.toId("i1"), XX.toId("i2"),
                XX.toId("i3"))));
        alltypes.setXidlist(XV.toIdListValue(Arrays.asList(XX.toId("j1"), XX.toId("j2"),
                XX.toId("j3"))));
        
        // k 11
        alltypes.setXid(XX.toId("k"));
        alltypes.setXdoublelist(XV.toDoubleListValue(Arrays.asList(121d, 122d, 123d)));
        alltypes.setXdouble(XV.toValue(13d));
        alltypes.setXbooleanlist(XV.toValue(new boolean[] { true, true, false }));
        alltypes.setXboolean(XV.toValue(true));
        
        // p 16
        alltypes.setXbinary(XV.toValue(new byte[] { (byte)161, (byte)162, (byte)163 }));
        alltypes.setXaddresssortedset(XV.toAddressSortedSetValue(Arrays.asList(
                XX.toAddress("/repo1/model1/q1"), XX.toAddress("/repo1/model1/q2"),
                XX.toAddress("/repo1/model1/q3"))));
        alltypes.setXaddressset(XV.toAddressSetValue(Arrays.asList(
                XX.toAddress("/repo1/model1/r1"), XX.toAddress("/repo1/model1/r2"),
                XX.toAddress("/repo1/model1/r3"))));
        alltypes.setXaddresslist(XV.toAddressListValue(Arrays.asList(
                XX.toAddress("/repo1/model1/s1"), XX.toAddress("/repo1/model1/s2"),
                XX.toAddress("/repo1/model1/s3"))));
        alltypes.setXaddress(XX.toAddress("/repo1/model1/t1"));
        
        // u 21
        model.createObject(XX.toId("u-p4"));
        IPerson p4 = (IPerson)Proxy.newProxyInstance(IPerson.class.getClassLoader(),
                new Class<?>[] { IPerson.class }, new OOJavaOnlyProxy(model, XX.toId("u-p4")));
        alltypes.setPartner(p4);
        alltypes.setJString("v");
        alltypes.setJLong(23l);
        alltypes.setJlong(24l);
        alltypes.setJInteger(25);
        
        // z 26
        alltypes.setJint(26);
        // A 27
        alltypes.setJDouble(27d);
        alltypes.setJdouble(28);
        alltypes.setJBoolean(true);
        alltypes.setJboolean(true);
        alltypes.setColor(Colors.Green);
        assertEquals(Colors.Green, alltypes.getColor());
        alltypes.setMyLongBasedType(new MyLongBasedType(3));
        assertEquals(3, alltypes.getMyLongBasedType().getInternalLong());
        
        DumpUtils.dump("filled", model);
    }
    
    private static String[] genStrings(String prefix, int n) {
        String[] s = new String[n];
        for(int i = 0; i < s.length; i++) {
            s[i] = prefix + i;
        }
        return s;
    }
    
    @Test
    public void useTasksRuntime() {
        // setup
        XWritableModel model = new MemoryModel(XX.toId("actor"), "pass",
                XX.toAddress("/repo1/model1"));
        org.xydra.oo.testgen.tasks.java.JavaFactory factory = new org.xydra.oo.testgen.tasks.java.JavaFactory(
                model);
        
        // usage
        ITask task = factory.createTask("o1");
        task.setTitle("Foo");
        task.setNote("Der Schorsch braucht das dringend");
        task.setChecked(false);
        ITask o1_2 = factory.createTask("o1_2");
        assert task.subTasks() != null;
        task.subTasks().add(o1_2);
        
        String t = task.getTitle();
        System.out.println(t);
        
        ITask task2 = factory.getTask("o1");
        assert task2 != null;
        assert task2.getTitle() != null;
        assert task2.getTitle().equals(t);
        
        DumpUtils.dump("filled", model);
    }
    
}
