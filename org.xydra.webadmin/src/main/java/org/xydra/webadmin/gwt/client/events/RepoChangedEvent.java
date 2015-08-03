package org.xydra.webadmin.gwt.client.events;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;

import com.google.web.bindery.event.shared.Event;

/**
 * repo has changes; only used after having registered / added models
 *
 * @author xamde
 */
public class RepoChangedEvent extends Event<IRepoChangedEventHandler> {

	private final XAddress repoAddress;

	private final EntityStatus status;

	private final RepoDataModel moreInfos;

	public static final Type<IRepoChangedEventHandler> TYPE = new Type<IRepoChangedEventHandler>();

	public interface IRepoChangedEventHandler {
		void onRepoChange(RepoChangedEvent event);
	}

	public RepoChangedEvent(final XAddress repoAddress, final EntityStatus status, final RepoDataModel repoDataModel) {
		this.repoAddress = repoAddress;
		this.status = status;
		this.moreInfos = repoDataModel;
	}

	public XAddress getRepoAddress() {
		return this.repoAddress;
	}

	@Override
	protected void dispatch(final IRepoChangedEventHandler handler) {
		handler.onRepoChange(this);
	}

	@Override
	public Event.Type<IRepoChangedEventHandler> getAssociatedType() {
		return TYPE;
	}

	public EntityStatus getStatus() {
		return this.status;
	}

	public RepoDataModel getMoreInfos() {
		return this.moreInfos;
	}
}
