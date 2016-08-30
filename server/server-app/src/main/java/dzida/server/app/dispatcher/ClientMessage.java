package dzida.server.app.dispatcher;

import com.google.common.collect.ImmutableSet;

public interface ClientMessage {
    ImmutableSet<Class<?>> classes = ImmutableSet.of(
            ConnectToServer.class,
            DisconnectFromServer.class
    );

    final class ConnectToServer implements ClientMessage {
        final String serverKey;
        final String connectionData;

        public ConnectToServer(String serverKey, String connectionData) {
            this.serverKey = serverKey;
            this.connectionData = connectionData;
        }
    }

    final class DisconnectFromServer implements ClientMessage {
        public final String serverKey;

        public DisconnectFromServer(String serverKey) {
            this.serverKey = serverKey;
        }
    }
}
