package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;
import org.assertj.core.api.AbstractAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerDispatcherTest {
    private final int connectionId = 100;
    private ProbeServer serverC;
    private ProbeServer serverB;
    private ProbeServer serverA;
    private ServerDispatcher serverDispatcher;
    private ProbeClientConnection connection;

    @Before
    public void setUp() throws Exception {
        serverDispatcher = new ServerDispatcher();
        serverA = new ProbeServer("serverA");
        serverB = new ProbeServer("serverB");
        serverC = new ProbeServer("serverC");
        serverDispatcher.addServer(serverA);
        serverDispatcher.addServer(serverB);
        serverDispatcher.addServer(serverC);
        connection = new ProbeClientConnection();
    }

    @Test
    public void clientIsConnectedToSelectedServers() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionData\":\"test\"}]") + "], " +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]" +
                "]");


        ProbeServer.assertThat(serverA)
                .hasConnection(connectionId)
                .hasConnectionData(connectionId, data -> assertThat(data).isEqualTo("test"));

        ProbeServer.assertThat(serverB)
                .hasConnection(connectionId)
                .hasConnectionData(connectionId, data -> assertThat(data).isNull());

        ProbeServer.assertThat(serverC).hasNotConnection(connectionId);
    }

    @Test
    public void clientReceiveMessageWithServerIdToWhichItWasConnected() {
        ProbeServer serverD = new ProbeServer("serverD") {
            public void handleConnection(int connectionId, ClientConnection connectionController, String connectionData) {
                // This message should be send after the message about connecting to the server D.
                connectionController.send("Initial data from server D");
            }
        };
        serverDispatcher.addServer(serverD);
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionData\":\"test\"}]") + "], " +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]" +
                "]");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverA\",\"serverId\":1}]") + "]]",
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverD\",\"serverId\":4}]") + "]]",
                "[[4," + escapeJson("Initial data from server D") + "]]"
        );
    }

    @Test
    public void clientReceiveMessageWithErrorIfThereIsNoServerToWhichItWantedToConnect() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"Could not find a server with the key: serverD.\"}]") + "]]"
        );
    }

    @Test
    public void clientReceiveMessageWithErrorIfServerDoNotAcceptClient() {
        ProbeServer serverD = new ProbeServer("serverD") {
            @Override
            public Result verifyConnection(int connectionId, String connectionData) {
                return Result.error("You shall not pass.");
            }
        };
        serverDispatcher.addServer(serverD);
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"You shall not pass.\"}]") + "]]"
        );
    }

    @Test
    public void messagesAreDispatchedToCorrectServer() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]," +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]," +
                "[2, " + escapeJson("messageToServerB") + "]," +
                "[2, " + escapeJson("message2") + "]" +
                "]");

        ProbeServer.assertThat(serverA)
                .hasConnection(connectionId)
                .hasMessages(connectionId, messages -> assertThat(messages).isEmpty());

        ProbeServer.assertThat(serverB)
                .hasConnection(connectionId)
                .hasMessages(connectionId, messages -> assertThat(messages).containsExactly("messageToServerB", "message2"));

        ProbeServer.assertThat(serverC).hasNotConnection(connectionId);
    }

    @Test
    public void messageIsNotDispatchedToServerIfClientIsNotConnectedToServer() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[" +
                "[1, " + escapeJson("messageToServerA") + "]" +
                "]");

        ProbeServer.assertThat(serverA)
                .hasNotConnection(connectionId)
                .hasMessages(connectionId, messages -> assertThat(messages).isNull());
    }

    @Test
    public void clientDisconnectionFromServerIsDispatchedToServer() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        ProbeServer.assertThat(serverA).hasConnection(connectionId);

        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[2, {\"serverId\":1}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection(connectionId);
    }

    @Test
    public void clientDisconnectionFromDispatcherIsDispatchedToAllConnectedServers() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[" +
                "[0, " + escapeJson("[1, {\"serverId\":1}]") + "]," +
                "[0, " + escapeJson("[1, {\"serverId\":2}]") + "]" +
                "]");
        serverDispatcher.handleDisconnection(connectionId);

        ProbeServer.assertThat(serverA).hasNotConnection(connectionId);
        ProbeServer.assertThat(serverB).hasNotConnection(connectionId);
    }

    @Test
    public void clientDisconnectionFromServerDoesNothingIfClientWasNotConnected() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[2, {\"serverId\":1}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection(connectionId);
    }


    @Test
    public void serverCanSendMessageToClient() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.send(connectionId, "Test message");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverA\",\"serverId\":1}]") + "]]",
                "[[1,\"Test message\"]]"
        );
    }

    @Test
    public void serverCanDisconnectClient() {
        serverDispatcher.handleConnection(connectionId, connection);
        serverDispatcher.handleMessage(connectionId, "[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.disconnect(connectionId);

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverA\",\"serverId\":1}]") + "]]",
                "[[0," + escapeJson("[2,{\"serverId\":1}]") + "]]");
    }

    private String escapeJson(String json) {
        return "\"" + json.replace("\"", "\\\"") + "\"";
    }
}

class ProbeClientConnection implements ClientConnection {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void disconnect() {
    }

    @Override
    public void send(String data) {
        messages.add(data);
    }

    public List<String> getMessages() {
        return messages;
    }
}

class ProbeServer implements Server {
    private final String key;
    private final Map<Integer, List<String>> messages = new HashMap<>();
    private final Map<Integer, String> connectionData = new HashMap<>();
    private final Map<Integer, ClientConnection> connectionControllers = new HashMap<>();

    public ProbeServer(String key) {
        this.key = key;
    }

    public static Assert assertThat(ProbeServer actual) {
        return new Assert(actual);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void handleMessage(int connectionId, String message) {
        if (!hasConnection(connectionId)) {
            throw new RuntimeException("Can not handle message from not connected client");
        }
        messages.get(connectionId).add(message);
    }

    @Override
    public void handleConnection(int connectionId, ClientConnection clientConnection, String connectionData) {
        this.connectionData.put(connectionId, connectionData);
        connectionControllers.put(connectionId, clientConnection);
        messages.put(connectionId, new ArrayList<>());
    }

    @Override
    public void handleDisconnection(int connectionId) {
        if (!hasConnection(connectionId)) {
            throw new RuntimeException("Can not handle disconnection from not connected client");
        }
        connectionData.remove(connectionId);
        connectionControllers.remove(connectionId);
        messages.remove(connectionId);
    }

    private boolean hasConnection(int connectionId) {
        return connectionControllers.containsKey(connectionId);
    }

    public void send(int connectionId, String data) {
        connectionControllers.get(connectionId).send(data);
    }

    public void disconnect(int connectionId) {
        connectionControllers.get(connectionId).disconnect();
    }

    public static final class Assert extends AbstractAssert<Assert, ProbeServer> {

        public Assert(ProbeServer actual) {
            super(actual, Assert.class);
        }

        public Assert hasConnection(int connectionId) {
            isNotNull();
            if (!actual.hasConnection(connectionId)) {
                failWithMessage("Expected from server to has a connection <%s>", connectionId);
            }
            return this;
        }

        public Assert hasNotConnection(int connectionId) {
            isNotNull();
            if (actual.hasConnection(connectionId)) {
                failWithMessage("Expected from server to has not a connection <%s>", connectionId);
            }
            return this;
        }

        public Assert hasConnectionData(int connectionId, Consumer<String> stringValidator) {
            isNotNull();
            String connectionData = actual.connectionData.get(connectionId);
            try {
                stringValidator.accept(connectionData);
            } catch (AssertionError assertionError) {
                failWithMessage("Validation error for connection <%s>, with connection data <%s> \n details: <%s>", connectionId, connectionData, assertionError.getMessage());
            }
            return this;
        }

        public Assert hasMessages(int connectionId, Consumer<List<String>> messagesValidator) {
            isNotNull();
            List<String> messages = actual.messages.get(connectionId);
            try {
                messagesValidator.accept(messages);
            } catch (AssertionError assertionError) {
                failWithMessage("Validation error  for connection <%s> with messages <%s> \n details: <%s>", connectionId, messages, assertionError.getMessage());
            }
            return this;
        }
    }
}