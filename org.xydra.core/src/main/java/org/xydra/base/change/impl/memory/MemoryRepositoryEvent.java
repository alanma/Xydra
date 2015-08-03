package org.xydra.base.change.impl.memory;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryEvent;


/**
 * An implementation of {@link XRepositoryEvent}
 *
 * @author kaidel
 *
 */
@RunsInGWT(true)
public class MemoryRepositoryEvent extends MemoryAtomicEvent implements XRepositoryEvent {

    private static final long serialVersionUID = 4709068915672914712L;

    /**
     * Creates a new {@link XRepositoryEvent} of the add-type (an model was
     * added to the repository this event refers to)
     *
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the model was
     *            added to - repository {@link XId} must not be null
     * @param modelId The {@link XId} of the added model - must not be null
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository or if the given modelId is null
     */
    public static XRepositoryEvent createAddEvent(final XId actor, final XAddress target, final XId modelId) {
        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD,
                REVISION_OF_ENTITY_NOT_SET, false, false);
    }

    public static XRepositoryEvent createFrom(final XRepositoryEvent re) {
        final MemoryRepositoryEvent event = new MemoryRepositoryEvent(re.getActor(), re.getTarget(),
                re.getModelId(), re.getChangeType(), re.getOldModelRevision(), re.inTransaction(),
                re.isImplied());
        return event;
    }

    /**
     * Creates a new {@link XRepositoryEvent} of the add-type (an model was
     * added to the repository this event refers to)
     *
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the model was
     *            added to - repository {@link XId} must not be null
     * @param modelId The {@link XId} of the added model - must not be null
     * @param modelRev the model revision before this event happened
     * @param inTrans
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository or if the given modelId is null
     */
    public static XRepositoryEvent createAddEvent(final XId actor, final XAddress target, final XId modelId,
            final long modelRev, final boolean inTrans) {
        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.ADD, modelRev, inTrans,
                false);
    }

    /**
     * Creates a new {@link XRepositoryEvent} of the remove-type (an model was
     * removed from the repository this event refers to)
     *
     * @param actor The {@link XId} of the actor
     * @param target The {@link XAddress} of the repository which the model was
     *            removed from - repository {@link XId} must not be null
     * @param modelId The {@link XId} of the removed model - must not be null
     * @param oldModelRevison of the remove event
     * @param inTrans if in transaction
     * @return An {@link XRepositoryEvent} of the add-type
     * @throws IllegalArgumentException if the given {@link XAddress} doesn't
     *             refer to an repository, if the given modelId is null or if
     *             the given modelRevision equals
     *             {@link XEvent#REVISION_OF_ENTITY_NOT_SET}
     */
    public static XRepositoryEvent createRemoveEvent(final XId actor, final XAddress target, final XId modelId,
            final long oldModelRevison, final boolean inTrans) {
        if(oldModelRevison < 0 && oldModelRevison != REVISION_NOT_AVAILABLE) {
            throw new IllegalArgumentException(
                    "model revision must be set for repository REMOVE events, was "
                            + oldModelRevison);
        }

        return new MemoryRepositoryEvent(actor, target, modelId, ChangeType.REMOVE,
                oldModelRevison, inTrans, false);
    }

    /* The XId of the model that was added/deleted */
    private XId modelId;

    /* the model revision before this event happened */
    private long modelRevision;

    /**
     * private constructor, use the createEvent methods for instantiating a
     * MemoryRepositoryEvent
     *
     * @param actor
     * @param target
     * @param modelId
     * @param changeType
     * @param oldModelRevision the model revision before this event happened
     * @param inTrans
     * @param implied
     */
    private MemoryRepositoryEvent(final XId actor, final XAddress target, final XId modelId, final ChangeType changeType,
            final long oldModelRevision, final boolean inTrans, final boolean implied) {
        super(target, changeType, actor, inTrans, implied);

        if(target.getRepository() == null || target.getModel() != null) {
            throw new IllegalArgumentException("target must refer to a repository, was: " + target);
        }

        if(modelId == null) {
            throw new IllegalArgumentException("model Id must be set for repository events");
        }
        if(oldModelRevision < RevisionConstants.NOT_EXISTING
                && oldModelRevision != REVISION_OF_ENTITY_NOT_SET
                && oldModelRevision != REVISION_NOT_AVAILABLE) {
            throw new IllegalArgumentException("invalid modelRevision: " + oldModelRevision);
        }

        this.modelId = modelId;
        this.modelRevision = oldModelRevision;
    }

    /**
     * GWT only
     */
    protected MemoryRepositoryEvent() {

    }

    @Override
    public boolean equals(final Object object) {

        if(!super.equals(object)) {
            return false;
        }

        if(!(object instanceof XRepositoryEvent)) {
            return false;
        }
        final XRepositoryEvent event = (XRepositoryEvent)object;

        if(!this.modelId.equals(event.getModelId())) {
            return false;
        }

        if(this.modelRevision != event.getOldModelRevision()) {
            return false;
        }

        return true;
    }

    @Override
    public XAddress getChangedEntity() {
        return Base.resolveModel(getTarget(), getModelId());
    }

    @Override
    public XId getModelId() {
        return this.modelId;
    }

    @Override
    public long getOldModelRevision() {
        return this.modelRevision;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        // newValue
        result ^= this.modelId.hashCode();

        // old revisions
        result += this.modelRevision;

        return result;
    }

    /**
     * Format: {MOF}Event
     *
     * r{mRev}/{oRev}/{fRev}
     *
     * {'ADD'|'REMOVE'}
     *
     * '[' {'+'|'-'} 'inTxn]' '[' {'+'|'-'} 'implied]'
     *
     * @{target *{id/value}, where xRef = '-' for
     *          {@link RevisionConstants#REVISION_OF_ENTITY_NOT_SET} and '?' for
     *          {@link RevisionConstants#REVISION_NOT_AVAILABLE}.
     *
     *          by actor: '{actorId}'
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RepositoryEvent");

        sb.append(" rev:");
        sb.append(rev2str(getRevisionNumber()));
        sb.append(" old:");
        sb.append(rev2str(getOldModelRevision()));
        sb.append("/");
        sb.append(rev2str(getOldObjectRevision()));
        sb.append("/");
        sb.append(rev2str(getOldFieldRevision()));

        addChangeTypeAndFlags(sb);
        sb.append(" @" + getTarget());
        sb.append(" *" + this.modelId + "*");
        sb.append("                 (actor:'" + getActor() + "')");
        return sb.toString();
    }

}
