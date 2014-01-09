package org.openepics.names.ui.names;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.model.NamePartRevisionStatus;
import org.openepics.names.model.NamePartRevisionType;
import org.openepics.names.services.NamePartService;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
public class NamePartView {

    public abstract class Change {
        private final NamePartRevisionStatus status;

        public Change(NamePartRevisionStatus status) {
            this.status = status;
        }

        public NamePartRevisionStatus getStatus() { return status; }
    }

    public class AddChange extends Change {
        public AddChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    public class DeleteChange extends Change {
        public DeleteChange(NamePartRevisionStatus status) {
            super(status);
        }
    }

    public class ModifyChange extends Change {
        private final @Nullable String newName;
        private final @Nullable String newFullName;

        public ModifyChange(NamePartRevisionStatus status, String newName, String newFullName) {
            super(status);
            this.newName = newName;
            this.newFullName = newFullName;
        }

        public @Nullable String getNewName() { return newName; }
        public @Nullable String getNewFullName() { return newFullName; }
    }

    private final NamePartService namePartService;
    private final @Nullable NamePartRevision currentRevision;
    private final @Nullable NamePartRevision pendingRevision;

    public NamePartView(NamePartService namePartService, @Nullable NamePartRevision currentRevision, @Nullable NamePartRevision pendingRevision) {
        this.namePartService = namePartService;
        this.currentRevision = currentRevision;
        this.pendingRevision = pendingRevision;
    }

    public NamePart getNamePart() { return baseRevision().getNamePart(); }

    public NamePartRevision getNameEvent() { return baseRevision(); }

    public Integer getId() { return baseRevision().getId(); }

    public @Nullable NamePartView getParent() { throw new IllegalStateException(); } // TODO

    public List<NamePartView> getChildren() { throw new IllegalStateException(); } // TODO

    public @Nullable Change getPendingChange() {
        if (pendingRevision == null) {
            return null;
        } else {
            if (pendingRevision.getRevisionType() == NamePartRevisionType.DELETE) {
                return new DeleteChange(pendingRevision.getStatus());
            } else if (pendingRevision.getRevisionType() == NamePartRevisionType.INSERT) {
                return new AddChange(pendingRevision.getStatus());
            } else if (pendingRevision.getRevisionType() == NamePartRevisionType.MODIFY) {
                final @Nullable String newName = !pendingRevision.getName().equals(currentRevision.getName()) ? pendingRevision.getName() : null;
                final @Nullable String newFullName = !pendingRevision.getName().equals(currentRevision.getName()) ? pendingRevision.getName() : null;
                return new ModifyChange(pendingRevision.getStatus(), newName, newFullName);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public boolean isDeleted() { return baseRevision().getRevisionType() == NamePartRevisionType.DELETE; }

    public String getNameCategory() { return baseRevision().getNameCategory().getDescription(); }

    public @Nullable NamePartRevision getPendingRevision() { return pendingRevision; }

    public String getName() { return baseRevision().getName(); }

    public String getFullName() { return baseRevision().getFullName(); }

    public List<String> getNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getName());
        }
        return pathElements.build().reverse();
    }

    public List<String> getFullNamePath() {
        final ImmutableList.Builder<String> pathElements = ImmutableList.builder();
        for (NamePartView pathElement = this; pathElement != null; pathElement = pathElement.getParent()) {
            pathElements.add(pathElement.getFullName());
        }
        return pathElements.build().reverse();
    }

    private NamePartRevision baseRevision() {
        return currentRevision != null ? currentRevision : pendingRevision;
    }
}
