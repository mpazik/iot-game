package dzida.server.core.basic.connection;

import dzida.server.core.basic.Result;

public interface VerifyingConnectionServer<T, S> {
    Result verifyConnection(S connectionData);

    void onConnection(Connector<T> connector, S connectionData);
}
