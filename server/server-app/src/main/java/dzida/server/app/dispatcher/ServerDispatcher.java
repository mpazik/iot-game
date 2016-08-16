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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerDispatcher implements ConnectionServer<String> {
    private static final int dispatcherServerId = 0;
    private static final Type packetType = new TypeToken<List<ServerMessage>>() {
    }.getType();
    private final List<Server> servers;
    private final Gson serializer;

    public ServerDispatcher() {
        servers = new ArrayList<>();
        servers.add(null); // reserve id 0 for dispatcher

        serializer = new GsonBuilder().registerTypeAdapter(ServerMessage.class, new TypeAdapter<ServerMessage>() {
            @Override
            public void write(JsonWriter out, ServerMessage serverMessage) throws IOException {
                out.beginArray()
                        .value(serverMessage.serverId)
                        .value(serverMessage.data)
                        .endArray();
            }

            @Override
            public ServerMessage read(JsonReader in) throws IOException {
                in.beginArray();
                int systemIndex = in.nextInt();
                String data = in.nextString();
                in.endArray();
                return new ServerMessage(systemIndex, data);
            }
        }).create();
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    @Override
    public void onConnection(ClientConnection<String> clientConnection) {
        clientConnection.onOpen(new DispatcherConnection(clientConnection, serializer, servers));
    }

    private interface ToClientMessage {
        int getId();
    }

    private static final class DispatcherConnection implements ServerConnection<String> {
        private final Map<Integer, ServerConnection<String>> connectionsToServers = new HashMap<>();
        private final ClientConnection<String> connectionHandler;
        private final Gson serializer;
        private final List<Server> servers;

        private DispatcherConnection(ClientConnection<String> connectionHandler, Gson serializer, List<Server> servers) {
            this.connectionHandler = connectionHandler;
            this.serializer = serializer;
            this.servers = servers;
        }

        @Override
        public void send(String packet) {
            List<ServerMessage> serverMessages = serializer.fromJson(packet, packetType);
            serverMessages.forEach(serverMessage -> {
                if (serverMessage.serverId == dispatcherServerId) {
                    handleCommand(serverMessage.data);
                    return;
                }
                if (!connectionsToServers.containsKey(serverMessage.serverId)) return;
                connectionsToServers.get(serverMessage.serverId).send(serverMessage.data);
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
                disconnectFromServer(disconnectFromServerMessage.serverId);
            }
        }

        private void connectToServer(String serverKey, String connectionData) {
            Optional<Server> serverOptional = servers.stream().filter(server -> server != null && server.getKey().equals(serverKey)).findAny();

            if (!serverOptional.isPresent()) {
                String errorMessage = "Could not find a server with the key: " + serverKey + ".";
                sendDispatcherMessageToClient(new NotConnectedToServerMessage(serverKey, errorMessage));
                return;
            }

            int serverIndex = servers.indexOf(serverOptional.get());
            ClientConnection<String> clientConnection = new ClientConnection<String>() {
                @Override
                public void onOpen(ServerConnection<String> serverConnection) {
                    connectionsToServers.put(serverIndex, serverConnection);
                }

                @Override
                public void onClose() {
                    sendDispatcherMessageToClient(new DisconnectedFromServer(serverIndex));
                }

                @Override
                public void onMessage(String data) {
                    sendToClient(serverIndex, data);
                }
            };
            Result connectionResult = servers.get(serverIndex).verifyConnection(connectionData);

            connectionResult.consume(() -> {
                sendDispatcherMessageToClient(new ConnectedToServerMessage(serverKey, serverIndex));
                servers.get(serverIndex).onConnection(clientConnection, connectionData);
            }, error -> {
                sendDispatcherMessageToClient(new NotConnectedToServerMessage(serverKey, error.getMessage()));
            });

        }

        private void sendToClient(int systemIndex, String data) {
            List<ServerMessage> messagesToSystems = ImmutableList.of(new ServerMessage(systemIndex, data));
            String packet = serializer.toJson(messagesToSystems, packetType);
            connectionHandler.onMessage(packet);
        }

        private void sendDispatcherMessageToClient(ToClientMessage message) {
            List<Object> messageList = ImmutableList.of(message.getId(), message);
            String data = serializer.toJson(messageList);
            sendToClient(dispatcherServerId, data);
        }

        private void disconnectFromServer(int systemIndex) {
            if (!connectionsToServers.containsKey(systemIndex)) return;
            connectionsToServers.get(systemIndex).close();
            connectionsToServers.remove(systemIndex);
        }
    }

    private final static class ServerMessage {
        private final int serverId;
        private final String data;

        private ServerMessage(int serverId, String data) {
            this.serverId = serverId;
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
        private final int serverId;

        private DisconnectFromServerMessage(int serverId) {
            this.serverId = serverId;
        }
    }

    private final static class ConnectedToServerMessage implements ToClientMessage {
        public final String serverKey;
        public final int serverId;

        private ConnectedToServerMessage(String serverKey, int serverId) {
            this.serverKey = serverKey;
            this.serverId = serverId;
        }

        @Override
        public int getId() {
            return 1;
        }
    }

    private final static class DisconnectedFromServer implements ToClientMessage {
        public final int serverId;

        private DisconnectedFromServer(int serverId) {
            this.serverId = serverId;
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

