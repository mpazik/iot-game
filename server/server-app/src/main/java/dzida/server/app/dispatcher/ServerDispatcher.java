package dzida.server.app.dispatcher;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dzida.server.app.protocol.json.JsonProtocol;
import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.Server;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class ServerDispatcher implements Server<String> {
    private static final String dispatcherServerKey = "dispatcher";
    private static final Type packetType = new TypeToken<List<ServerMessage>>() {
    }.getType();

    private final Gson serializer;
    private final JsonProtocol dispatcherSerializer;
    private final Map<String, VerifyingConnectionServer<String, String>> servers;

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
        dispatcherSerializer = JsonProtocol.create(ClientMessage.classes, dzida.server.app.dispatcher.ServerMessage.classes);
    }

    public void addServer(String serverKey, VerifyingConnectionServer<String, String> server) {
        servers.put(serverKey, server);
    }

    public void removeServer(String serverKey) {
        System.out.println("remove server:" + serverKey);
        servers.remove(serverKey);
    }

    @Override
    public void onConnection(Connector<String> connector) {
        connector.onOpen(new DispatcherConnection(connector, serializer, dispatcherSerializer, servers));
    }

    private static final class DispatcherConnection implements ServerConnection<String> {
        private final Map<String, ServerConnection<String>> connectionsToServers = new HashMap<>();
        private final Connector<String> connectionHandler;
        private final Gson serializer;
        private final JsonProtocol dispatcherSerializer;
        private final Map<String, VerifyingConnectionServer<String, String>> servers;

        private DispatcherConnection(Connector<String> connectionHandler, Gson serializer, JsonProtocol dispatcherSerializer, Map<String, VerifyingConnectionServer<String, String>> servers) {
            this.connectionHandler = connectionHandler;
            this.serializer = serializer;
            this.dispatcherSerializer = dispatcherSerializer;
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

        private void handleCommand(String data) {
            Object message = dispatcherSerializer.parseMessage(data);
            whenTypeOf(message)
                    .is(ClientMessage.ConnectToServer.class)
                    .then(connectToServer -> {
                        connectToServer(connectToServer.serverKey, connectToServer.connectionData);
                    })
                    .is(ClientMessage.DisconnectFromServer.class)
                    .then(disconnectFromServer -> {
                        disconnectFromServer(disconnectFromServer.serverKey);
                    });
        }

        private void connectToServer(String serverKey, String connectionData) {
            VerifyingConnectionServer<String, String> serverOptional = servers.get(serverKey);

            if (serverOptional == null) {
                String errorMessage = "Could not find a server with the key: " + serverKey + ".";
                sendDispatcherMessageToClient(new dzida.server.app.dispatcher.ServerMessage.NotConnectedToServer(serverKey, errorMessage));
                return;
            }

            Connector<String> connector = new ServerConnector(serverKey, this);
            Result result = servers.get(serverKey).onConnection(connector, connectionData);
            result.consume(() -> {
            }, error -> {
                sendDispatcherMessageToClient(new dzida.server.app.dispatcher.ServerMessage.NotConnectedToServer(serverKey, error.getMessage()));
            });
        }

        private void sendToClient(String serverKey, String data) {
            List<ServerMessage> messagesToServers = ImmutableList.of(new ServerMessage(serverKey, data));
            String packet = serializer.toJson(messagesToServers, packetType);
            connectionHandler.onMessage(packet);
        }

        private void sendDispatcherMessageToClient(Object message) {
            String data = dispatcherSerializer.serializeMessage(message);
            sendToClient(dispatcherServerKey, data);
        }

        private void disconnectFromServer(String serverKey) {
            if (!connectionsToServers.containsKey(serverKey)) return;
            connectionsToServers.get(serverKey).close();
            connectionsToServers.remove(serverKey);
        }
    }

    private final static class ServerConnector implements Connector<String> {
        private final String serverKey;
        private final DispatcherConnection dispatcherConnection;

        private ServerConnector(String serverKey, DispatcherConnection dispatcherConnection) {
            this.serverKey = serverKey;
            this.dispatcherConnection = dispatcherConnection;
        }

        @Override
        public void onOpen(ServerConnection<String> serverConnection) {
            dispatcherConnection.sendDispatcherMessageToClient(new dzida.server.app.dispatcher.ServerMessage.ConnectedToServer(serverKey));
            dispatcherConnection.connectionsToServers.put(serverKey, serverConnection);
        }

        @Override
        public void onClose() {
            dispatcherConnection.sendDispatcherMessageToClient(new dzida.server.app.dispatcher.ServerMessage.DisconnectedFromServer(serverKey));
            dispatcherConnection.connectionsToServers.remove(serverKey);
        }

        @Override
        public void onMessage(String data) {
            dispatcherConnection.sendToClient(serverKey, data);
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
}

