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

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ServerDispatcher {
    public final int dispatcherServerId = 0;
    private final List<Server> servers;
    private final Map<Integer, ClientConnection> connectionHandlers;
    private final Map<Integer, Set<Integer>> connectionsToServers;
    private final Type packetType;
    private final Gson serializer;

    public ServerDispatcher() {
        servers = new ArrayList<>();
        servers.add(null); // reserve id 0 for dispatcher
        connectionHandlers = new HashMap<>();
        connectionsToServers = new HashMap<>();

        packetType = new TypeToken<List<ServerMessage>>() {
        }.getType();
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

    public Result handleConnection(int connectionId, ClientConnection clientConnection, @Nullable String connectionData) {
        connectionHandlers.put(connectionId, clientConnection);
        connectionsToServers.put(connectionId, new HashSet<>());
        return Result.ok();
    }

    public void handleDisconnection(int connectionId) {
        connectionsToServers.get(connectionId).forEach(serverId -> {
            servers.get(serverId).handleDisconnection(connectionId);
        });
        connectionHandlers.remove(connectionId);
        connectionsToServers.remove(connectionId);
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public void handlePacket(int connectionId, String packet) {
        List<ServerMessage> serverMessages = serializer.fromJson(packet, packetType);
        serverMessages.forEach(serverMessage -> {
            if (serverMessage.serverId == dispatcherServerId) {
                handleMessage(connectionId, serverMessage.data);
                return;
            }
            if (!connectionsToServers.get(connectionId).contains(serverMessage.serverId)) return;
            servers.get(serverMessage.serverId).handleMessage(connectionId, serverMessage.data);
        });
    }

    void handleMessage(int connectionId, String message) {
        JsonArray data = serializer.fromJson(message, JsonArray.class);
        int messageType = data.get(0).getAsInt();
        if (messageType == ConnectToServerMessage.id) {
            ConnectToServerMessage connectToServerMessage = serializer.fromJson(data.get(1), ConnectToServerMessage.class);
            connectToServer(connectionId, connectToServerMessage.serverKey, connectToServerMessage.connectionData);
        } else if (messageType == DisconnectFromServer.id) {
            DisconnectFromServer disconnectFromServer = serializer.fromJson(data.get(1), DisconnectFromServer.class);
            disconnectFromServer(connectionId, disconnectFromServer.serverId);
        }
    }

    private void connectToServer(int connectionId, String serverKey, String connectionData) {
        Optional<Server> serverOptional = servers.stream().filter(server -> server != null && server.getKey().equals(serverKey)).findAny();

        if (!serverOptional.isPresent()) {
            sendDispatcherMessageToClient(connectionId, new NotConnectedToServerMessage(serverKey, "Could not find an server with that key to connect"));
            return;
        }

        int serverIndex = servers.indexOf(serverOptional.get());
        ClientConnection clientConnection = new ClientConnection() {
            @Override
            public void disconnect() {
                sendDispatcherMessageToClient(connectionId, new DisconnectedFromServer(serverIndex));
            }

            @Override
            public void send(String data) {
                sendToClient(connectionId, serverIndex, data);
            }
        };
        Result connectionResult = servers.get(serverIndex).handleConnection(connectionId, clientConnection, connectionData);

        connectionResult.consume(() -> {
            connectionsToServers.get(connectionId).add(serverIndex);
            sendDispatcherMessageToClient(connectionId, new ConnectedToServerMessage(serverKey, serverIndex));
        }, error -> {
            sendDispatcherMessageToClient(connectionId, new NotConnectedToServerMessage(serverKey, error.getMessage()));
        });

    }

    private void disconnectFromServer(int connectionId, int systemIndex) {
        if (!connectionsToServers.get(connectionId).contains(systemIndex)) return;
        servers.get(systemIndex).handleDisconnection(connectionId);
        connectionsToServers.get(connectionId).remove(systemIndex);
    }

    private void sendToClient(int connectionId, int systemIndex, String data) {
        List<ServerMessage> messagesToSystems = ImmutableList.of(new ServerMessage(systemIndex, data));
        String packet = serializer.toJson(messagesToSystems, packetType);
        connectionHandlers.get(connectionId).send(packet);
    }

    private void sendDispatcherMessageToClient(int connectionId, ToClientMessage message) {
        List<Object> messageList = ImmutableList.of(message.getId(), message);
        String data = serializer.toJson(messageList);
        sendToClient(connectionId, dispatcherServerId, data);
    }

    private interface ToClientMessage {
        int getId();
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

    private final static class DisconnectFromServer {
        private static final int id = 2;
        private final int serverId;

        private DisconnectFromServer(int serverId) {
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

