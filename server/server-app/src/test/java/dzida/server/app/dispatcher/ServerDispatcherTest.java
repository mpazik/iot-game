package dzida.server.app.dispatcher;

import dzida.server.core.basic.Result;
import dzida.server.core.basic.connection.Connector;
import dzida.server.core.basic.connection.ServerConnection;
import dzida.server.core.basic.connection.VerifyingConnectionServer;
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
    private ProbeConnector connection;

    @Before
    public void setUp() throws Exception {
        serverDispatcher = new ServerDispatcher();
        serverA = new ProbeServer();
        serverB = new ProbeServer();
        serverC = new ProbeServer();
        serverDispatcher.addServer("serverA", serverA);
        serverDispatcher.addServer("serverB", serverB);
        serverDispatcher.addServer("serverC", serverC);
        connection = new ProbeConnector();
    }

    @Test
    public void clientIsConnectedToSelectedServers() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionData\":\"test\"}]") + "], " +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]" +
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
        ProbeServer serverD = new ProbeServer() {
            @Override
            public void onConnection(Connector<String> clientConnection, String connectionData) {
                // This message should be send after the message about connecting to the server D.
                clientConnection.onMessage("Initial data from server D");
            }
        };
        serverDispatcher.addServer("serverD", serverD);
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\",\"connectionsData\":\"test\"}]") + "], " +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]" +
                "]");

        assertThat(connection.getMessages()).containsExactly(
                "[[\"dispatcher\"," + escapeJson("[1,{\"serverKey\":\"serverA\"}]") + "]]",
                "[[\"dispatcher\"," + escapeJson("[1,{\"serverKey\":\"serverD\"}]") + "]]",
                "[[\"serverD\"," + escapeJson("Initial data from server D") + "]]"
        );
    }

    @Test
    public void clientReceiveMessageWithErrorIfThereIsNoServerToWhichItWantedToConnect() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[\"dispatcher\"," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"Could not find a server with the key: serverD.\"}]") + "]]"
        );
    }

    @Test
    public void clientReceiveMessageWithErrorIfServerDoNotAcceptClient() {
        ProbeServer serverD = new ProbeServer() {
            @Override
            public Result verifyConnection(String connectionData) {
                return Result.error("You shall not pass.");
            }
        };
        serverDispatcher.addServer("serverD", serverD);
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverD\"}]") + "]]");

        assertThat(connection.getMessages()).containsExactly(
                "[[\"dispatcher\"," + escapeJson("[3,{\"serverKey\":\"serverD\",\"errorMessage\":\"You shall not pass.\"}]") + "]]"
        );
    }

    @Test
    public void messagesAreDispatchedToCorrectServer() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]," +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverB\"}]") + "]," +
                "[\"serverB\", " + escapeJson("messageToServerB") + "]," +
                "[\"serverB\", " + escapeJson("message2") + "]" +
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
        connection.sendToServer("[[\"serverA\", " + escapeJson("messageToServerA") + "]]");

        ProbeServer.assertThat(serverA)
                .hasNotConnection()
                .hasMessages(messages -> assertThat(messages).isEmpty());
    }

    @Test
    public void clientDisconnectionFromServerIsDispatchedToServer() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        ProbeServer.assertThat(serverA).hasConnection();

        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[2, {\"serverKey\":\"serverA\"}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection();
    }

    @Test
    public void clientDisconnectionFromDispatcherIsDispatchedToAllConnectedServers() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[" +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":1}]") + "]," +
                "[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":2}]") + "]" +
                "]");
        connection.disconnect();

        ProbeServer.assertThat(serverA).hasNotConnection();
        ProbeServer.assertThat(serverB).hasNotConnection();
    }

    @Test
    public void clientDisconnectionFromServerDoesNothingIfClientWasNotConnected() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[2, {\"serverKey\":1}]") + "]]");

        ProbeServer.assertThat(serverA).hasNotConnection();
    }

    @Test
    public void serverCanSendMessageToClient() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.send("Test message");

        assertThat(connection.getMessages()).containsExactly(
                "[[\"dispatcher\"," + escapeJson("[1,{\"serverKey\":\"serverA\"}]") + "]]",
                "[[\"serverA\",\"Test message\"]]"
        );
    }

    @Test
    public void serverCanDisconnectClient() {
        serverDispatcher.onConnection(connection);
        connection.sendToServer("[[\"dispatcher\", " + escapeJson("[1, {\"serverKey\":\"serverA\"}]") + "]]");
        serverA.disconnectClient();

        assertThat(connection.getMessages()).containsExactly(
                "[[\"dispatcher\"," + escapeJson("[1,{\"serverKey\":\"serverA\"}]") + "]]",
                "[[\"dispatcher\"," + escapeJson("[2,{\"serverKey\":\"serverA\"}]") + "]]");
    }

    private String escapeJson(String json) {
        return "\"" + json.replace("\"", "\\\"") + "\"";
    }
}

class ProbeConnector implements Connector<String> {
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

class ProbeServer implements VerifyingConnectionServer<String, String> {
    private final List<String> messages = new ArrayList<>();
    private String connectionData;
    private Connector<String> connector;

    public static Assert assertThat(ProbeServer actual) {
        return new Assert(actual);
    }

    @Override
    public Result verifyConnection(String connectionData) {
        return Result.ok();
    }

    @Override
    public void onConnection(Connector<String> connector, String connectionData) {
        connector.onOpen(new ServerConnection<String>() {
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
        this.connector = connector;
    }

    private void handleDisconnection() {
        messages.clear();
        connector = null;
        connectionData = null;
    }

    private boolean hasConnection() {
        return connector != null;
    }

    public void send(String data) {
        connector.onMessage(data);
    }

    public void disconnectClient() {
        connector.onClose();
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