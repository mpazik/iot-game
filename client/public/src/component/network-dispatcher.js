define(function (require, exports, module) {

    const dispatcherServerKey = 'dispatcher';
    const connections = new Map();
    const packetQueue = [];
    var socket = null;


    function isConnected() {
        return socket != null && socket.readyState === WebSocket.OPEN;
    }

    const connectionState = {
        CONNECTING: 0,
        OPEN: 1,
        CLOSING: 2,
        CLOSED: 3
    };

    function close(serverKey) {
        send(dispatcherServerKey, disconnectFromServerMessage(serverKey));
    }

    function send(serverKey, message) {
        const packetToServer = [serverKey, message];
        if (isConnected()) {
            sendServerPackets([packetToServer]);
        } else {
            packetQueue.push(packetToServer);
        }
    }

    function sendServerPackets(packets) {
        socket.send(JSON.stringify(packets))
    }

    function connectToServerMessage(serverKey, connectionData) {
        return JSON.stringify([1, {serverKey, connectionData}]);
    }

    function disconnectFromServerMessage(serverKey) {
        return JSON.stringify([2, {serverKey}]);
    }

    function disconnectAll() {
        for (const connection of connections.values()) {
            connection.readyState = connectionState.CLOSING;
            connection.onClose();
            connection.readyState = connectionState.CLOSED;
        }
    }

    function handleDispatcherMessage(data) {
        const message = JSON.parse(data);
        const messageId = message[0];
        const obj = message[1];
        if (messageId === 1) {
            const connection = connections.get(obj.serverKey);
            connection.onOpen();
            connection.readyState = connectionState.OPEN;
        } else if (messageId === 2) {
            const connection = connections.get(obj.serverKey);
            connection.readyState = connectionState.CLOSING;
            connection.onClose();
            connection.readyState = connectionState.CLOSED;

            connections.delete(obj.serverKey);
        } else if (messageId === 3) {
            const connection = connections.get(obj.serverKey);
            connection.readyState = connectionState.CLOSING;
            connection.onError(obj['errorMessage']);
            connection.readyState = connectionState.CLOSED;
            connections.delete(obj.serverKey);
        } else {
            console.warn(`Unrecognized message from dispatcher: ${message}`)
        }
    }

    function connect() {
        socket = new WebSocket(Configuration.serverAddress);
        socket.onopen = () => {
            sendServerPackets(packetQueue);
            packetQueue.length = 0;
        };
        socket.onmessage = (event) => {
            const serverPackages = JSON.parse(event.data);
            for (const serverPackage of serverPackages) {
                const serverKey = serverPackage[0];
                const data = serverPackage[1];
                if (serverKey == dispatcherServerKey) {
                    handleDispatcherMessage(data);
                } else if (connections.has(serverKey)) {
                    connections.get(serverKey).onMessage(data);
                } else {
                    console.error(`Message from server ${serverKey} to which there is no connection`)
                }
            }
        };
        socket.onerror = (error) => {
            disconnectAll();
            console.error(error);
        };
        socket.onclose = () => {
            disconnectAll();
            console.error('Server closed connection.');
        };
    }

    module.exports = {
        connectionState: connectionState,
        newSocket (serverKey, connectionData) {
            if (connections.has(serverKey)) {
                throw new Error(`Connection already created for the server ${serverKey}`);
            }
            const connection = {
                send(data) {
                    send(serverKey, data);
                },
                close() {
                    close(serverKey);
                },
                readyState: connectionState.CONNECTING,
                onMessage: () => {
                },
                onClose: () => {
                },
                onOpen: () => {
                },
                onError: () => {
                },
            };
            connections.set(serverKey, connection);
            if (socket == null) {
                connect()
            }
            send(dispatcherServerKey, connectToServerMessage(serverKey, connectionData));
            return connection;
        }
    };
});