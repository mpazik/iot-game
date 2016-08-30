package dzida.server.app.dispatcher;

import com.google.common.collect.ImmutableSet;

public interface ServerMessage {
    ImmutableSet<Class<?>> classes = ImmutableSet.of(
            ConnectedToServer.class,
            DisconnectedFromServer.class,
            NotConnectedToServer.class
    );

    final class ConnectedToServer implements ServerMessage {
        public final String serverKey;

        public ConnectedToServer(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    final class DisconnectedFromServer implements ServerMessage {
        public final String serverKey;

        public DisconnectedFromServer(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    final class NotConnectedToServer implements ServerMessage {
        public final String serverKey;
        public final String errorMessage;

        public NotConnectedToServer(String serverKey, String errorMessage) {
            this.serverKey = serverKey;
            this.errorMessage = errorMessage;
        }
    }
}
