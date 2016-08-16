package dzida.server.app.dispatcher;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.ClientConnection;
import dzida.server.core.basic.connection.ConnectionServer;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerDispatcher implements ConnectionServer<String> {
    private static final String dispatcherServerKey = "dispatcher";
    private static final Type packetType = new TypeToken<List<ServerMessage>>() {
    }.getType();
    private final Map<String, VerifyingConnectionServer<String, String>> servers;
    private final Gson serializer;

    public ServerDispatcher() {
        servers = new HashMap<>();

        serializer = new GsonBuilder().registerTypeAdapter(ServerMessage.class, new TypeAdapter<ServerMessage>() {
            @Override
            public void write(JsonWriter out, ServerMessage serverMessage) throws IOException {
                out.beginArray()
                        .value(serverMessage.serverKey)
                        .value(serverMessage.data)
                        .endArray();
            }

            @Override
            public ServerMessage read(JsonReader in) throws IOException {
                in.beginArray();
                String serverKey = in.nextString();
                String data = in.nextString();
                in.endArray();
                return new ServerMessage(serverKey, data);
            }
        }).create();
    }

    public void addServer(String serverKey, VerifyingConnectionServer<String, String> server) {
        servers.put(serverKey, server);
    }

    @Override
    public void onConnection(ClientConnection<String> clientConnection) {
        clientConnection.onOpen(new DispatcherConnection(clientConnection, serializer, servers));
    }

    private interface ToClientMessage {
        int getId();
    }

    private static final class DispatcherConnection implements ServerConnection<String> {
        private final Map<String, ServerConnection<String>> connectionsToServers = new HashMap<>();
        private final ClientConnection<String> connectionHandler;
        private final Gson serializer;
        private final Map<String, VerifyingConnectionServer<String, String>> servers;

        private DispatcherConnection(ClientConnection<String> connectionHandler, Gson serializer, Map<String, VerifyingConnectionServer<String, String>> servers) {
            this.connectionHandler = connectionHandler;
            this.serializer = serializer;
            this.servers = servers;
        }

        @Override
        public void send(String packet) {
            List<ServerMessage> serverMessages = serializer.fromJson(packet, packetType);
            serverMessages.forEach(serverMessage -> {
                if (Objects.equals(serverMessage.serverKey, dispatcherServerKey)) {
                    handleCommand(serverMessage.data);
                    return;
                }
                if (!connectionsToServers.containsKey(serverMessage.serverKey)) return;
                connectionsToServers.get(serverMessage.serverKey).send(serverMessage.data);
            });
        }

        @Override
        public void close() {
            connectionsToServers.values().forEach(ServerConnection::close);
        }

        private void handleCommand(String message) {
            JsonArray data = serializer.fromJson(message, JsonArray.class);
            int messageType = data.get(0).getAsInt();
            if (messageType == ConnectToServerMessage.id) {
                ConnectToServerMessage connectToServerMessage = serializer.fromJson(data.get(1), ConnectToServerMessage.class);
                connectToServer(connectToServerMessage.serverKey, connectToServerMessage.connectionData);
            } else if (messageType == DisconnectFromServerMessage.id) {
                DisconnectFromServerMessage disconnectFromServerMessage = serializer.fromJson(data.get(1), DisconnectFromServerMessage.class);
                disconnectFromServer(disconnectFromServerMessage.serverKey);
            }
        }

        private void connectToServer(String serverKey, String connectionData) {
            VerifyingConnectionServer<String, String> serverOptional = servers.get(serverKey);

            if (serverOptional == null) {
                String errorMessage = "Could not find a server with the key: " + serverKey + ".";
                sendDispatcherMessageToClient(new NotConnectedToServerMessage(serverKey, errorMessage));
                return;
            }

            ClientConnection<String> clientConnection = new ClientConnection<String>() {
                @Override
                public void onOpen(ServerConnection<String> serverConnection) {
                    connectionsToServers.put(serverKey, serverConnection);
                }

                @Override
                public void onClose() {
                    sendDispatcherMessageToClient(new DisconnectedFromServer(serverKey));
                }

                @Override
                public void onMessage(String data) {
                    sendToClient(serverKey, data);
                }
            };
            Result connectionResult = servers.get(serverKey).verifyConnection(connectionData);

            connectionResult.consume(() -> {
                sendDispatcherMessageToClient(new ConnectedToServerMessage(serverKey));
                servers.get(serverKey).onConnection(clientConnection, connectionData);
            }, error -> {
                sendDispatcherMessageToClient(new NotConnectedToServerMessage(serverKey, error.getMessage()));
            });

        }

        private void sendToClient(String serverKey, String data) {
            List<ServerMessage> messagesToServers = ImmutableList.of(new ServerMessage(serverKey, data));
            String packet = serializer.toJson(messagesToServers, packetType);
            connectionHandler.onMessage(packet);
        }

        private void sendDispatcherMessageToClient(ToClientMessage message) {
            List<Object> messageList = ImmutableList.of(message.getId(), message);
            String data = serializer.toJson(messageList);
            sendToClient(dispatcherServerKey, data);
        }

        private void disconnectFromServer(String serverKey) {
            if (!connectionsToServers.containsKey(serverKey)) return;
            connectionsToServers.get(serverKey).close();
            connectionsToServers.remove(serverKey);
        }
    }

    private final static class ServerMessage {
        private final String serverKey;
        private final String data;

        private ServerMessage(String serverKey, String data) {
            this.serverKey = serverKey;
            this.data = data;
        }
    }

    private final static class ConnectToServerMessage {
        static final int id = 1;
        final String serverKey;
        final String connectionData;

        private ConnectToServerMessage(String serverKey, String connectionData) {
            this.serverKey = serverKey;
            this.connectionData = connectionData;
        }
    }

    private final static class DisconnectFromServerMessage {
        private static final int id = 2;
        private final String serverKey;

        private DisconnectFromServerMessage(String serverKey) {
            this.serverKey = serverKey;
        }
    }

    private final static class ConnectedToServerMessage implements ToClientMessage {
        public final String serverKey;

        private ConnectedToServerMessage(String serverKey) {
            this.serverKey = serverKey;
        }

        @Override
        public int getId() {
            return 1;
        }
    }

    private final static class DisconnectedFromServer implements ToClientMessage {
        public final String serverKey;

        private DisconnectedFromServer(String serverKey) {
            this.serverKey = serverKey;
        }

        @Override
        public int getId() {
            return 2;
        }
    }

    private final static class NotConnectedToServerMessage implements ToClientMessage {
        public final String serverKey;
        public final String errorMessage;

        private NotConnectedToServerMessage(String serverKey, String errorMessage) {
            this.serverKey = serverKey;
            this.errorMessage = errorMessage;
        }

        @Override
        public int getId() {
            return 3;
        }
    }
}

