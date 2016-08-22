package dzida.server.core.basic.connection;

import dzida.server.core.basic.Result;

public interface VerifyingConnectionServer<T, S> {

    /**
     * If the result is valid the connection has to be opened by the server.
     * If the result is valid the connection can not be opened by the server.
     */
    Result onConnection(Connector<T> connector, S connectionData);
}
