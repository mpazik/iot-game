package dzida.server.app;

import dzida.server.core.entity.EntityChangesWithType;

import java.util.ArrayList;
import java.util.List;

public final class Packet {
    private final List<LegacyWsMessage> legacyWsMessages;
    private final List<EntityChangesWithType> changes;

    private Packet(List<LegacyWsMessage> legacyWsMessages, List<EntityChangesWithType> changes) {
        this.legacyWsMessages = legacyWsMessages;
        this.changes = changes;
    }

    public List<LegacyWsMessage> getLegacyWsMessages() {
        return legacyWsMessages;
    }

    public List<EntityChangesWithType> getChanges() {
        return changes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean empty = true;
        private final List<LegacyWsMessage> legacyWsMessages = new ArrayList<>(10);
        private final List<EntityChangesWithType> changes = new ArrayList<>(10);

        private Builder() {
        }

        public boolean isEmpty() {
            return empty;
        }

        public Builder addLegacyWsMessage(LegacyWsMessage message) {
            legacyWsMessages.add(message);
            empty = false;
            return this;
        }

        public Packet build() {
            return new Packet(legacyWsMessages, changes);
        }

        public void addChanges(EntityChangesWithType<?> changes) {
            this.changes.add(changes);
        }
    }
}
