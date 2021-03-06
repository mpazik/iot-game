define(function (require, exports, module) {
    const Configuration = require('configuration');
    const JsonProtocol = require('../common/basic/json-protocol');

    const dispatcherServerKey = 'dispatcher';
    const connections = new Map();
    const packetQueue = [];

    var socket = null;

    const ClientMessage = {
        ConnectToServer: function (serverKey, connectionData) {
            this.serverKey = serverKey;
            //noinspection JSUnusedGlobalSymbols
            this.connectionData = connectionData;
        },
        DisconnectFromServer: function (serverKey) {
            this.serverKey = serverKey;
        }
    };
    const ServerMessage = {
        ConnectedToServer: function (serverKey) {
            this.serverKey = serverKey;
        },
        DisconnectedFromServer: function (serverKey) {
            this.serverKey = serverKey;
        },
        NotConnectedToServer: function (serverKey, errorMessage) {
            this.serverKey = serverKey;
            this.errorMessage = errorMessage;
        }
    };

    const dispatcherProtocol = new JsonProtocol(ServerMessage, ClientMessage);

    function isConnected() {
        return socket != null && socket.readyState === WebSocket.OPEN;
    }

    const connectionState = {
        CONNECTING: 0,
        OPEN: 1,
        CLOSING: 2,
        CLOSED: 3
    };

    function send(serverKey, message) {
        const packetToServer = [serverKey, message];
        if (isConnected()) {
            sendServerPackets([packetToServer]);
        } else {
            packetQueue.push(packetToServer);
        }
    }

    function sendToDispatcher(message) {
        send(dispatcherServerKey, dispatcherProtocol.serialize(message));
    }

    function sendServerPackets(packets) {
        socket.send(JSON.stringify(packets))
    }

    function disconnectAll() {
        for (const connection of connections.values()) {
            if (connection.readyState === connectionState.OPEN) {
                connection.readyState = connectionState.CLOSING;
                connection.onClose();
            }
            connection.readyState = connectionState.CLOSED;
        }
        connections.clear();
        socket = null;
    }

    function handleDispatcherMessage(data) {
        const message = dispatcherProtocol.parse(data);
        if (message.constructor === ServerMessage.ConnectedToServer) {
            const connection = connections.get(message.serverKey);
            connection.onOpen();
            connection.readyState = connectionState.OPEN;
        } else if (message.constructor === ServerMessage.DisconnectedFromServer) {
            const connection = connections.get(message.serverKey);
            connection.readyState = connectionState.CLOSING;
            connection.onClose();
            connection.readyState = connectionState.CLOSED;
            connections.delete(message.serverKey);
        } else if (message.constructor === ServerMessage.NotConnectedToServer) {
            const connection = connections.get(message.serverKey);
            connection.readyState = connectionState.CLOSING;
            connection.onError(message.errorMessage);
            connection.readyState = connectionState.CLOSED;
            connections.delete(message.serverKey);
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
            console.info('Server closed connection.');
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
                    connections.delete(serverKey);
                    sendToDispatcher(new ClientMessage.DisconnectFromServer(serverKey));
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
            sendToDispatcher(new ClientMessage.ConnectToServer(serverKey, connectionData));
            return connection;
        },
        disconnect() {
            socket.close();
            connections.forEach(connection => {
                connection.onClose();
            });
            connections.clear();
        }
    };
});