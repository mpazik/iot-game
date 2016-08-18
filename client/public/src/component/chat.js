define(function (require, exports, module) {
    const NetworkDispatcher = require('./network-dispatcher');
    const Publisher = require('../common/basic/publisher');

    var setState;
    var currentInstanceKey = null;
    var socket = null;
    var displayMessage = null;
    const statePublisher = new Publisher.StatePublisher('disconnected', function (f) {
        return setState = f;
    });

    const clientCommands = {
        help(){
            const helpText = `/help<br />
Type and press enter to send a message.<br />
<b>/w</b> <i>nick</i> <i>message</i> - whisper a message to the player with the given nick
<b>/list</b> - list all players connected to the instance.
`;
            displayMessage({type: 'command', text: helpText})
        },
        w(args) {
            const i = args.indexOf(' ');
            if (args == null || i === -1) {
                displayMessage({
                    type: 'error',
                    text: '<b>/w</b> command requires <i>nick</i> and <i>message</i> params',
                });
                return
            }
            const player = args.slice(0, i);
            const message = args.slice(i + 1);
            socket.send(`MSG ${player} ${message}`);
            displayMessage({type: 'whisper', text: `to: ${player} : ${message}`})
        },
        list() {
            socket.send(`LIST ${currentInstanceKey}`)
        },
        send(args) {
            socket.send(`MSG ${currentInstanceKey} ${args}`)
        }
    };

    const channelCommands = {
        msg (channel, args) {
            const i = args.indexOf(' ');
            const player = args.slice(0, i);
            const message = args.slice(i + 1);
            if (channel === currentInstanceKey) {
                displayMessage({type: 'message', text: `${player} : ${message}`})
            } else {
                displayMessage({type: 'message', text: `${channel} : ${player} : ${message}`})
            }
        },
        joined (channel, player) {
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `player <i>${player}</i> joined`})
            } else {
                displayMessage({type: 'command', text: `player <i>${player}</i> joined to channel <i>${channel}</i>`})
            }
        },
        quited (channel, player) {
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `player <i>${player}</i> quited`})
            } else {
                displayMessage({type: 'command', text: `player <i>${player}</i> quited from channel <i>${channel}</i>`})
            }
        },
        list (channel, args) {
            const i = args.indexOf(' ');
            const userNum = args.slice(0, i);
            const users = args.slice(i + 1);
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `/list: players(${userNum}): ${users}`})
            } else {
                displayMessage({type: 'command', text: `/list: ${channel} : players(${userNum}): ${users}`})
            }
        }
    };

    const serverCommands = {
        msg(args) {
            const i = args.indexOf(' ');
            const player = args.slice(0, i);
            const message = args.slice(i + 1);
            displayMessage({type: 'whisper', text: `from: ${player} : ${message}`})
        },
        error(args) {
            displayMessage({type: 'error', text: args})
        },
        channel(args) {
            const i = args.indexOf(' ');
            const channel = args.slice(0, i);
            const j = args.indexOf(' ', i + 1);
            const channelCommand = args.slice(i + 1, j).toLowerCase();
            const channelCommandArgs = args.slice(j + 1);
            channelCommands[channelCommand](channel, channelCommandArgs);
        }
    };

    function parseServerMessage(message) {
        console.log('chat', message);
        const i = message.indexOf(' ');
        const command = message.slice(0, i).toLowerCase();
        const args = message.slice(i + 1);
        serverCommands[command](args);
    }

    module.exports = {
        send: function (message) {
            if (message.charAt(0) === '/') {
                var command = message.slice(1);
                var args = null;
                const i = message.indexOf(' ');
                if (i !== -1) {
                    command = message.slice(1, i).toLowerCase();
                    args = message.slice(i + 1);
                }
                if (clientCommands.hasOwnProperty(command)) {
                    clientCommands[command](args);
                } else {
                    clientCommands.help();
                }
            } else {
                clientCommands.send(message);
            }
        },
        connect (userNick) {
            socket = NetworkDispatcher.newSocket('chat', userNick);
            socket.onMessage = parseServerMessage;
            socket.onOpen = () => {
                if (currentInstanceKey != null) {
                    socket.send(`JOIN ${currentInstanceKey}`);
                }
                setState('connected')
            };
            socket.onClose = () => {
                setState('disconnected')
            };
        },
        joinToInstanceChannel (instanceKey) {
            if (statePublisher.value !== 'connected') {
                currentInstanceKey = '#' + instanceKey;
            } else {
                socket.send(`QUIT ${currentInstanceKey}`);
                currentInstanceKey = '#' + instanceKey;
                socket.send(`JOIN ${currentInstanceKey}`);
            }
        },
        state: statePublisher,
        chatMessage: new Publisher.StreamPublisher((push) => {
            displayMessage = push;
        })
    };
});