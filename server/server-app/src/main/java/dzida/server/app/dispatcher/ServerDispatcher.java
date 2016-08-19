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

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

public class ServerDispatcher implements ConnectionServer<String> {
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
        dispatcherSerializer = DispatcherProtocol.createSerializer();
    }

    public void addServer(String serverKey, VerifyingConnectionServer<String, String> server) {
        servers.put(serverKey, server);
    }

    @Override
    public void onConnection(ClientConnection<String> clientConnection) {
        clientConnection.onOpen(new DispatcherConnection(clientConnection, serializer, dispatcherSerializer, servers));
    }

    private static final class DispatcherConnection implements ServerConnection<String> {
        private final Map<String, ServerConnection<String>> connectionsToServers = new HashMap<>();
        private final ClientConnection<String> connectionHandler;
        private final Gson serializer;
        private final JsonProtocol dispatcherSerializer;
        private final Map<String, VerifyingConnectionServer<String, String>> servers;

        private DispatcherConnection(ClientConnection<String> connectionHandler, Gson serializer, JsonProtocol dispatcherSerializer, Map<String, VerifyingConnectionServer<String, String>> servers) {
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
                    .is(DispatcherProtocol.ConnectToServerMessage.class)
                    .then(connectToServerMessage -> {
                        connectToServer(connectToServerMessage.serverKey, connectToServerMessage.connectionData);
                    })
                    .is(DispatcherProtocol.DisconnectFromServerMessage.class)
                    .then(disconnectFromServerMessage -> {
                        disconnectFromServer(disconnectFromServerMessage.serverKey);
                    });
        }

        private void connectToServer(String serverKey, String connectionData) {
            VerifyingConnectionServer<String, String> serverOptional = servers.get(serverKey);

            if (serverOptional == null) {
                String errorMessage = "Could not find a server with the key: " + serverKey + ".";
                sendDispatcherMessageToClient(new DispatcherProtocol.NotConnectedToServerMessage(serverKey, errorMessage));
                return;
            }

            ClientConnection<String> clientConnection = new ClientConnection<String>() {
                @Override
                public void onOpen(ServerConnection<String> serverConnection) {
                    connectionsToServers.put(serverKey, serverConnection);
                }

                @Override
                public void onClose() {
                    sendDispatcherMessageToClient(new DispatcherProtocol.DisconnectedFromServer(serverKey));
                }

                @Override
                public void onMessage(String data) {
                    sendToClient(serverKey, data);
                }
            };
            Result connectionResult = servers.get(serverKey).verifyConnection(connectionData);

            connectionResult.consume(() -> {
                sendDispatcherMessageToClient(new DispatcherProtocol.ConnectedToServerMessage(serverKey));
                servers.get(serverKey).onConnection(clientConnection, connectionData);
            }, error -> {
                sendDispatcherMessageToClient(new DispatcherProtocol.NotConnectedToServerMessage(serverKey, error.getMessage()));
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

    private final static class ServerMessage {
        private final String serverKey;
        private final String data;

        private ServerMessage(String serverKey, String data) {
            this.serverKey = serverKey;
            this.data = data;
        }
    }
}

