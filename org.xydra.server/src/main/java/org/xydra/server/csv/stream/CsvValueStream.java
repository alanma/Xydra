package org.xydra.server.csv.stream;

import org.xydra.base.XAddress;
import org.xydra.base.XId;

/**
 * CSV maintains a meta-field for the value type. Therefore its omitted here.
 * 
 * @author xamde
 * 
 */
public class CsvValueStream extends AbstractValueStream {

	@Override
	public void address(XAddress address) {
		this.buf.append(address.toString());
	}

	@Override
	public void xid(XId a) {
		this.buf.append(a.toString());
	}

	/* must encode single quotes ' as '' */
	@Override
	public String encode(String s) {
		return "'" + s.replace("'", "''") + "'";
	}

}
