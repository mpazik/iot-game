package dzida.server.app;

import java.util.ArrayList;
import java.util.List;

public final class Packet {
    private final List<LegacyWsMessage> legacyWsMessages;

    private Packet(List<LegacyWsMessage> legacyWsMessages) {
        this.legacyWsMessages = legacyWsMessages;
    }

    public List<LegacyWsMessage> getLegacyWsMessages() {
        return legacyWsMessages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean empty = true;
        private final List<LegacyWsMessage> legacyWsMessages = new ArrayList<>(10);

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
            return new Packet(legacyWsMessages);
        }
    }
}
