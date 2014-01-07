package org.xydra.conf.test;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.Setting;
import org.xydra.conf.annotations.RequireConf;


@Setting("example-class")
@RunsInGWT(false)
public class SampleUser {
    
    @Setting("example-field")
    @RequireConf("example.field-foo")
    String foo = "bar";
    
    @RequireConf("example.foo")
    public void baz() {
        
    }
    
}
