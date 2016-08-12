package dzida.server.app.network;

import java.util.Optional;

public interface ConnectionHandler<UserId> {

    /**
     * @return None if user can not connect to the system, long user id if the user successfully pass authentication.
     */
    Optional<UserId> authenticateUser(String authToken);

    void handleConnection(UserId userId, ConnectionController connectionController);

    void handleMessage(UserId userId, String message);

    void handleDisconnection(UserId userId);

    interface ConnectionController {
        void disconnect();

        void send(String data);
    }
}
