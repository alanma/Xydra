package org.xydra.core;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.ReadOperation;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.crypto.HMAC_SHA256;
import org.xydra.core.crypto.SHA2;
import org.xydra.core.serialize.json.JsonEncoder;
import org.xydra.core.serialize.json.JsonOut;
import org.xydra.core.serialize.xml.XmlEncoder;
import org.xydra.core.serialize.xml.XmlOut;
import org.xydra.core.util.Clock;
import org.xydra.core.util.ConfigUtils;
import org.xydra.core.util.RegExUtil;
import org.xydra.perf.MapStats;
import org.xydra.perf.Stats;
import org.xydra.sharedutils.DebugUtils;
import org.xydra.sharedutils.ReflectionUtils;
import org.xydra.sharedutils.URLUtils;


/**
 * This class serves as documentation for the myriad of little util and tool
 * classes spread over the xydra code base.
 * 
 * <h3>Annotations</h3> What runs on which platform? {@link RunsInGWT},
 * {@link RequiresAppEngine}, {@link RunsInAppEngine}
 * 
 * Effects of this method: {@link ReadOperation}, {@link ModificationOperation}
 * 
 * <h3>Java</h3> {@link ReflectionUtils}, see also deprecated class Assertions
 * 
 * <h3>GWT</h3> MiniIO - framework with classes like {@link MiniWriter}.
 * 
 * <h3>Crypto</h3> {@link HMAC_SHA256}, {@link SHA2}
 * 
 * <h3>Serialisation</h3> XML: {@link XmlOut}, {@link XmlEncoder}, ...
 * 
 * JSON: {@link JsonOut}, {@link JsonEncoder},
 * 
 * {@link URLUtils}
 * 
 * <h3>Generic</h3> {@link Clock}, {@link ConfigUtils}, {@link DebugUtils}
 * (dumpStacktrace), {@link RegExUtil},
 * 
 * <h3>Performance</h3> {@link MapStats} and {@link Stats}
 * 
 * @author xamde
 * 
 */
public class XydraDevTools {
	
}
