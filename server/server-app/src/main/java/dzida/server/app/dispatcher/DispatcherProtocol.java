package dzida.server.app.dispatcher;

import dzida.server.app.protocol.json.JsonProtocol;

public class DispatcherProtocol {
    public static JsonProtocol createSerializer() {
        return new JsonProtocol.Builder()
                .registerParsingMessageType(1, ConnectToServerMessage.class)
                .registerParsingMessageType(2, DisconnectFromServerMessage.class)
                .registerSerializationMessageType(1, ConnectedToServerMessage.class)
                .registerSerializationMessageType(2, DisconnectedFromServer.class)
                .registerSerializationMessageType(3, NotConnectedToServerMessage.class)
                .build();
    }

    public final static class ConnectToServerMessage {
        final String serverKey;
        final String connectionData;

        public ConnectToServerMessage(String serverKey, String connectionData) {
            this.serverKey = serverKey;
            this.connectionData = connectionData;
        }
    }

    public final static class DisconnectFromServerMessage {
        public final String serverKey;

        public DisconnectFromServerMessage(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    public final static class ConnectedToServerMessage {
        public final String serverKey;

        public ConnectedToServerMessage(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    public final static class DisconnectedFromServer {
        public final String serverKey;

        public DisconnectedFromServer(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    public final static class NotConnectedToServerMessage {
        public final String serverKey;
        public final String errorMessage;

        public NotConnectedToServerMessage(String serverKey, String errorMessage) {
            this.serverKey = serverKey;
            this.errorMessage = errorMessage;
        }
    }
}
