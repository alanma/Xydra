package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.ICommitEventHandler;

import com.google.web.bindery.event.shared.Event;

/**
 * Model has been committed
 * 
 * @author Andreas
 */
public class CommittingEvent extends Event<ICommitEventHandler> {

	public enum CommitStatus {
		SUCCESS, SUCCESSANDPROCEED, FAILED;
	}

	private XAddress modelAddress;

	private CommitStatus status;

	private long newRevisionnumber;

	public static final Type<ICommitEventHandler> TYPE = new Type<ICommitEventHandler>();

	public interface ICommitEventHandler {
		void onCommit(CommittingEvent event);
	}

	public CommittingEvent(XAddress modelAddress, CommitStatus status, Long revisionNumber) {
		this.modelAddress = modelAddress;
		this.status = status;
		this.newRevisionnumber = revisionNumber;
	}

	public XAddress getModelAddress() {
		return this.modelAddress;
	}

	@Override
	protected void dispatch(ICommitEventHandler handler) {
		handler.onCommit(this);
	}

	@Override
	public Event.Type<ICommitEventHandler> getAssociatedType() {
		return TYPE;
	}

	public CommitStatus getStatus() {
		return this.status;
	}

	public long getNewRevision() {
		return this.newRevisionnumber;
	}

	public String getErrorMessage() {
		return "error messages are not very smart yet...";
	}

}
