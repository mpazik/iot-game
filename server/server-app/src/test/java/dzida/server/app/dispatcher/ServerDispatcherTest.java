package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.ClientConnection;
import dzida.server.core.basic.connection.ServerConnection;
import org.assertj.core.api.AbstractAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class ServerDispatcherTest {
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
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionData\":\"test\"}]") + "], " +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]" +
                "]");


        ProbeServer.assertThat(serverA)
                .hasConnection()
                .hasConnectionData(data -> assertThat(data).isEqualTo("test"));

        ProbeServer.assertThat(serverB)
                .hasConnection()
                .hasConnectionData(data -> assertThat(data).isNull());

        ProbeServer.assertThat(serverC).hasNotConnection();
    }

    @Test
    public void clientReceiveMessageWithServerIdToWhichItWasConnected() {
        ProbeServer serverD = new ProbeServer("serverD") {
            @Override
            public void onConnection(ClientConnection<String> clientConnection, String connectionData) {
                // This message should be send after the message about connecting to the server D.
                clientConnection.onMessage("Initial data from server D");
            }
        };
        serverDispatcher.addServer(serverD);
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionsData\":\"test\"}]") + "], " +
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
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"Could not find a server with the key: serverD.\"}]") + "]]"
        );
    }

    @Test
    public void clientReceiveMessageWithErrorIfServerDoNotAcceptClient() {
        ProbeServer serverD = new ProbeServer("serverD") {
            @Override
            public Result verifyConnection(String connectionData) {
                return Result.error("You shall not pass.");
            }
        };
        serverDispatcher.addServer(serverD);
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"You shall not pass.\"}]") + "]]"
        );
    }

    @Test
    public void messagesAreDispatchedToCorrectServer() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]," +
                "[0, " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]," +
                "[2, " + escapeJson("messageToServerB") + "]," +
                "[2, " + escapeJson("message2") + "]" +
                "]");

        ProbeServer.assertThat(serverA)
                .hasConnection()
                .hasMessages(messages -> assertThat(messages).isEmpty());

        ProbeServer.assertThat(serverB)
                .hasConnection()
                .hasMessages(messages -> assertThat(messages).containsExactly("messageToServerB", "message2"));

        ProbeServer.assertThat(serverC).hasNotConnection();
    }

    @Test
    public void messageIsNotDispatchedToServerIfClientIsNotConnectedToServer() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[1, " + escapeJson("messageToServerA") + "]" +
                "]");

        ProbeServer.assertThat(serverA)
                .hasNotConnection()
                .hasMessages(messages -> assertThat(messages).isEmpty());
    }

    @Test
    public void clientDisconnectionFromServerIsDispatchedToServer() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        ProbeServer.assertThat(serverA).hasConnection();

        connection.sendToServer("[[0, " + escapeJson("[2, {\"serverId\":1}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection();
    }

    @Test
    public void clientDisconnectionFromDispatcherIsDispatchedToAllConnectedServers() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[0, " + escapeJson("[1, {\"serverId\":1}]") + "]," +
                "[0, " + escapeJson("[1, {\"serverId\":2}]") + "]" +
                "]");
        connection.disconnect();

        ProbeServer.assertThat(serverA).hasNotConnection();
        ProbeServer.assertThat(serverB).hasNotConnection();
    }

    @Test
    public void clientDisconnectionFromServerDoesNothingIfClientWasNotConnected() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[2, {\"serverId\":1}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection();
    }

    @Test
    public void serverCanSendMessageToClient() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.send("Test message");

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverA\",\"serverId\":1}]") + "]]",
                "[[1,\"Test message\"]]"
        );
    }

    @Test
    public void serverCanDisconnectClient() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[0, " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.disconnectClient();

        assertThat(connection.getMessages()).containsExactly(
                "[[0," + escapeJson("[1,{\"serverKey\":\"serverA\",\"serverId\":1}]") + "]]",
                "[[0," + escapeJson("[2,{\"serverId\":1}]") + "]]");
    }

    private String escapeJson(String json) {
        return "\"" + json.replace("\"", "\\\"") + "\"";
    }
}

class ProbeClientConnection implements ClientConnection<String> {
    private final List<String> messages = new ArrayList<>();
    private ServerConnection<String> serverConnection;

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void onOpen(ServerConnection<String> serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onMessage(String data) {
        messages.add(data);
    }

    public void sendToServer(String data) {
        serverConnection.send(data);
    }

    public void disconnect() {
        serverConnection.close();
    }
}

class ProbeServer implements Server {
    private final String key;
    private final List<String> messages = new ArrayList<>();
    private String connectionData;
    private ClientConnection<String> clientConnection;

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
    public void onConnection(ClientConnection<String> clientConnection, String connectionData) {
        clientConnection.onOpen(new ServerConnection<String>() {
            @Override
            public void send(String message) {
                messages.add(message);
            }

            @Override
            public void close() {
                handleDisconnection();
            }
        });
        this.connectionData = connectionData;
        this.clientConnection = clientConnection;
    }

    private void handleDisconnection() {
        messages.clear();
        clientConnection = null;
        connectionData = null;
    }

    private boolean hasConnection() {
        return clientConnection != null;
    }

    public void send(String data) {
        clientConnection.onMessage(data);
    }

    public void disconnectClient() {
        clientConnection.onClose();
    }

    public static final class Assert extends AbstractAssert<Assert, ProbeServer> {

        public Assert(ProbeServer actual) {
            super(actual, Assert.class);
        }

        public Assert hasConnection() {
            isNotNull();
            if (!actual.hasConnection()) {
                failWithMessage("Expected from server to has a connection.");
            }
            return this;
        }

        public Assert hasNotConnection() {
            isNotNull();
            if (actual.hasConnection()) {
                failWithMessage("Expected from server to has not a connection.");
            }
            return this;
        }

        public Assert hasConnectionData(Consumer<String> stringValidator) {
            isNotNull();
            try {
                stringValidator.accept(actual.connectionData);
            } catch (AssertionError assertionError) {
                failWithMessage("Validation error for connection data <%s> \n details: <%s>", actual.connectionData, assertionError.getMessage());
            }
            return this;
        }

        public Assert hasMessages(Consumer<List<String>> messagesValidator) {
            isNotNull();
            try {
                messagesValidator.accept(actual.messages);
            } catch (AssertionError assertionError) {
                failWithMessage("Validation error for messages <%s> \n details: <%s>", actual.messages, assertionError.getMessage());
            }
            return this;
        }
    }
}