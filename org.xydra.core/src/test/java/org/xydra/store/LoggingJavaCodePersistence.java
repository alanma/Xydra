package org.xydra.store;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.rmof.XWritableRepository;
import org.xydra.base.rmof.impl.delegate.WritableRepositoryOnPersistence;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XByteListValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.DemoModelUtil;
import org.xydra.core.XCopyUtils;
import org.xydra.core.model.impl.memory.MemoryRepository;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.memory.MemoryPersistence;


/**
 * A wrapper around a {@link XydraPersistence} that creates Java source code to
 * re-run the operations.
 * 
 * @author xamde
 */
public class LoggingJavaCodePersistence implements XydraPersistence {
	
	private static final Logger log = LoggerFactory.getLogger(LoggingJavaCodePersistence.class);
	
	private XydraPersistence persistence;
	private Writer w;
	private long count = 0;
	
	/**
	 * @param persistence a {@link XydraPersistence}
	 * @param w where to write the Java code to
	 */
	public LoggingJavaCodePersistence(XydraPersistence persistence, Writer w) {
		super();
		this.persistence = persistence;
		this.w = w;
		java("// ----------- " + new Date(System.currentTimeMillis()));
	}
	
	private void java(String string) {
		try {
			this.w.write(string + "\n");
			this.w.flush();
			log.trace("EXE: " + string);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void clear() {
		this.persistence.clear();
		java("this.p.clear();");
	}
	
	public long executeCommand(XID actorId, XCommand command) {
		stacktrace();
		String var = getVar();
		java("long " + var + " = this.p.executeCommand(" + toJava(actorId) + ", " + toJava(command)
		        + " );");
		long result = this.persistence.executeCommand(actorId, command);
		java("// result: "
		        + ((result == XCommand.FAILED) ? "FAILED"
		                : (result == XCommand.NOCHANGE ? "NOCHANGE" : result)));
		java("expectedVsActual(" + result + "," + var + ");");
		return result;
	}
	
	private void stacktrace() {
		try {
			throw new RuntimeException("TRACING...");
		} catch(RuntimeException e) {
			e.fillInStackTrace();
			java("/** Next was called by: <pre>");
			printStacktrace(e);
			java("</pre> */");
		}
	}
	
	private void printStacktrace(Throwable e) {
		for(StackTraceElement ste : e.getStackTrace()) {
			if(ste.getClassName().startsWith("sun.reflect.")) {
				break;
			}
			java("  " + ste.getClassName() + "." + ste.getMethodName() + " (" + ste.getFileName()
			        + ":" + ste.getLineNumber() + ")");
			Throwable cause = e.getCause();
			if(cause != null) {
				java(" caused by ");
				printStacktrace(cause);
			}
		}
	}
	
	private static String toJava(XID xid) {
		return "XX.toId(\"" + xid + "\")";
	}
	
	private String toJava(XCommand command) {
		if(command instanceof XAtomicCommand) {
			return toJava((XAtomicCommand)command);
		} else {
			return toJava((XTransaction)command);
		}
	}
	
	private String toJava(XTransaction txn) {
		long count = this.count++;
		java("XTransactionBuilder txnBuilder" + count
		        + " = new XTransactionBuilder(txn.getTarget());");
		java("for( XCommand command : txn) { txnBuilder" + count + ".addCommand(command); }");
		return "txnBuilder" + count + ".build()";
	}
	
	private String toJava(XAtomicCommand command) {
		java("// Command: " + command.getClass().getCanonicalName());
		switch(command.getChangeType()) {
		case ADD: {
			switch(command.getChangedEntity().getAddressedType()) {
			case XFIELD:
				if(command instanceof XFieldCommand) {
					XFieldCommand fieldCommand = (XFieldCommand)command;
					return "X.getCommandFactory().createAddValueCommand("
					        + toJava(command.getChangedEntity().getRepository()) + ", "
					        + toJava(command.getChangedEntity().getModel()) + ", "
					        + toJava(command.getChangedEntity().getObject()) + ", "
					        + toJava(command.getChangedEntity().getField()) + ", "
					        + fieldCommand.getRevisionNumber() + ", "
					        + toJava(fieldCommand.getValue()) + ", " + fieldCommand.isForced()
					        + ")";
				} else {
					assert command instanceof XObjectCommand;
					return "X.getCommandFactory().createAddFieldCommand("
					        + toJava(command.getChangedEntity().getRepository()) + ", "
					        + toJava(command.getChangedEntity().getModel()) + ", "
					        + toJava(command.getChangedEntity().getObject()) + ", "
					        + toJava(command.getChangedEntity().getField()) + ", "
					        + command.isForced() + ")";
				}
			case XOBJECT:
				return "X.getCommandFactory().createAddObjectCommand("
				        + toJava(command.getChangedEntity().getRepository()) + ", "
				        + toJava(command.getChangedEntity().getModel()) + ", "
				        + toJava(command.getChangedEntity().getObject()) + ", "
				        + command.isForced() + ")";
			case XMODEL:
				return "X.getCommandFactory().createAddModelCommand("
				        + toJava(command.getChangedEntity().getRepository()) + ", "
				        + toJava(command.getChangedEntity().getModel()) + ", " + command.isForced()
				        + ")";
			case XREPOSITORY:
				throw new IllegalStateException();
			}
		}
			break;
		case REMOVE: {
			switch(command.getChangedEntity().getAddressedType()) {
			case XFIELD:
				if(command instanceof XFieldCommand) {
					return "X.getCommandFactory().createRemoveValueCommand("
					        + toJava(command.getChangedEntity().getRepository()) + ", "
					        + toJava(command.getChangedEntity().getModel()) + ", "
					        + toJava(command.getChangedEntity().getObject()) + ", "
					        + toJava(command.getChangedEntity().getField()) + ", "
					        + command.getRevisionNumber() + ", " + command.isForced() + ")";
				} else {
					return "X.getCommandFactory().createRemoveFieldCommand("
					        + toJava(command.getChangedEntity().getRepository()) + ", "
					        + toJava(command.getChangedEntity().getModel()) + ", "
					        + toJava(command.getChangedEntity().getObject()) + ", "
					        + toJava(command.getChangedEntity().getField()) + ", "
					        + command.getRevisionNumber() + ", " + command.isForced() + ")";
				}
			case XOBJECT:
				return "X.getCommandFactory().createRemoveObjectCommand("
				        + toJava(command.getChangedEntity().getRepository()) + ", "
				        + toJava(command.getChangedEntity().getModel()) + ", "
				        + toJava(command.getChangedEntity().getObject()) + ", "
				        + command.getRevisionNumber() + ", " + command.isForced() + ")";
			case XMODEL:
				return "X.getCommandFactory().createRemoveModelCommand("
				        + toJava(command.getChangedEntity().getRepository()) + ", "
				        + toJava(command.getChangedEntity().getModel()) + ", "
				        + command.getRevisionNumber() + ", " + command.isForced() + ")";
			case XREPOSITORY:
				throw new IllegalStateException();
			}
		}
			break;
		case CHANGE: {
			XFieldCommand fieldCommand = (XFieldCommand)command;
			return "X.getCommandFactory().createChangeValueCommand("
			        + toJava(command.getChangedEntity().getRepository()) + ", "
			        + toJava(command.getChangedEntity().getModel()) + ", "
			        + toJava(command.getChangedEntity().getObject()) + ", "
			        + toJava(command.getChangedEntity().getField()) + ", "
			        + command.getRevisionNumber() + ", " + toJava(fieldCommand.getValue()) + ", "
			        + command.isForced() + ")";
		}
		case TRANSACTION: {
			throw new IllegalStateException();
		}
		}
		throw new IllegalStateException();
	}
	
	private String toJavaArray(XValue[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(XValue value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append(toJava(value));
		}
		return buf.toString();
	}
	
	private String toJava(XValue value) {
		switch(value.getType()) {
		case Address:
			return "XX.toAddress(\"" + value.toString() + "\")";
		case AddressSet: {
			StringBuffer buf = new StringBuffer("XV.toAddressSetValue( new XAddress[] { ");
			buf.append(toJavaArray(((XAddressSetValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case AddressSortedSet: {
			StringBuffer buf = new StringBuffer("XV.toAddressSortedSetValue( new XAddress[] { ");
			buf.append(toJavaArray(((XAddressSortedSetValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case AddressList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new XAddress[] { ");
			buf.append(toJavaArray(((XAddressListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case Boolean: {
			return "X.getValueFactory().createBooleanValue(" + ((XBooleanValue)value).contents()
			        + ")";
		}
		case BooleanList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new boolean[] { ");
			buf.append(toJavaArray(((XBooleanListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case ByteList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new byte[] { ");
			buf.append(toJavaArray(((XByteListValue)value).contents()));
			buf.append("})");
			return buf.toString();
			
		}
		case Double:
			return "XV.toValue(" + ((XDoubleValue)value).contents() + ")";
		case DoubleList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new double[] { ");
			buf.append(toJavaArray(((XDoubleListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case Integer:
			return "XV.toValue(" + ((XIntegerValue)value).contents() + ")";
		case IntegerList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new int[] { ");
			buf.append(toJavaArray(((XIntegerListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case Long:
			return "XV.toValue(" + ((XLongValue)value).contents() + "l)";
		case LongList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new long[] { ");
			buf.append(toJavaArray(((XLongListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case String:
			return "XV.toValue(" + toJava(((XStringValue)value).contents()) + ")";
		case StringList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new String[] { ");
			buf.append(toJavaArray(((XStringListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case StringSet: {
			StringBuffer buf = new StringBuffer("XV.toStringSetValue( new String[] { ");
			buf.append(toJavaArray(((XStringSetValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case XID:
			return toJava((XID)value);
		case XIDList: {
			StringBuffer buf = new StringBuffer("XV.toValue( new XID[] { ");
			buf.append(toJavaArray(((XIDListValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case XIDSet: {
			StringBuffer buf = new StringBuffer("XV.toIDSetValue( new XID[] { ");
			buf.append(toJavaArray(((XIDSetValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		case XIDSortedSet: {
			StringBuffer buf = new StringBuffer("XV.toIDSortedSetValue( new XID[] { ");
			buf.append(toJavaArray(((XIDSortedSetValue)value).contents()));
			buf.append("})");
			return buf.toString();
		}
		default:
			throw new IllegalStateException("Cannot handle " + value.getType());
		}
	}
	
	private Object toJavaArray(byte[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(byte value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + value);
		}
		return buf.toString();
	}
	
	private String toJavaArray(String[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(String value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + toJava(value));
		}
		return buf.toString();
		
	}
	
	private String toJava(String raw) {
		String escaped = raw.replace("\"", "\\\"");
		return "\"" + escaped + "\"";
	}
	
	private String toJavaArray(long[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(long value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + value + "l");
		}
		return buf.toString();
		
	}
	
	private String toJavaArray(int[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(int value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + value);
		}
		return buf.toString();
		
	}
	
	private static String toJavaArray(double[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(double value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + value);
		}
		return buf.toString();
	}
	
	private String toJavaArray(boolean[] values) {
		StringBuffer buf = new StringBuffer();
		boolean first = true;
		for(boolean value : values) {
			if(first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append("" + value);
		}
		return buf.toString();
	}
	
	private String getVar() {
		return "var" + this.count++;
	}
	
	public List<XEvent> getEvents(XAddress address, long beginRevision, long endRevision) {
		String var = getVar();
		java("List<XEvent> " + var + " = this.p.getEvents( " + toJava(address) + ", "
		        + beginRevision + ", " + endRevision + ");");
		java("consume(" + var + ");");
		return this.persistence.getEvents(address, beginRevision, endRevision);
	}
	
	public Set<XID> getModelIds() {
		String var = getVar();
		java("Set<XID> " + var + " = p.getModelIds();");
		java("consume(" + var + ");");
		return this.persistence.getModelIds();
	}
	
	public long getModelRevision(XAddress address) {
		String var = getVar();
		java("long " + var + " = this.p.getModelRevision(" + toJava(address) + ");");
		java("consume(" + var + ");");
		return this.persistence.getModelRevision(address);
	}
	
	public XWritableModel getModelSnapshot(XAddress address) {
		String var = getVar();
		java("XWritableModel " + var + " = this.p.getModelSnapshot(" + toJava(address) + ");");
		java("consume(" + var + ");");
		return this.persistence.getModelSnapshot(address);
	}
	
	public XWritableObject getObjectSnapshot(XAddress address) {
		String var = getVar();
		java("XWritableObject " + var + " = this.p.getObjectSnapshot(" + toJava(address) + ");");
		java("consume(" + var + ");");
		return this.persistence.getObjectSnapshot(address);
	}
	
	public XID getRepositoryId() {
		String var = getVar();
		java("XID " + var + " = this.p.getRepositoryId();");
		XID result = this.persistence.getRepositoryId();
		java("expectedVsActual(" + toJava(result) + "," + var + ");");
		return result;
	}
	
	public boolean hasModel(XID modelId) {
		String var = getVar();
		java("boolean " + var + " = this.p.hasModel(" + toJava(modelId) + ");");
		java("consume(" + var + ");");
		return this.persistence.hasModel(modelId);
	}
	
	public static void main(String[] args) throws IOException {
		XydraPersistence p = new MemoryPersistence(XX.toId("memrepo"));
		Writer w = new OutputStreamWriter(System.out);
		LoggingJavaCodePersistence lp = new LoggingJavaCodePersistence(p, w);
		XWritableRepository testRepo = new WritableRepositoryOnPersistence(lp, XX.toId("testactor"));
		
		// set up demo data
		MemoryRepository demoRepo = new MemoryRepository(XX.toId("demoActor"), null,
		        XX.toId("demoRepo"));
		DemoModelUtil.addPhonebookModel(demoRepo);
		
		// copy
		XCopyUtils.copyData(demoRepo, testRepo);
		w.flush();
		w.close();
	}
	
}
