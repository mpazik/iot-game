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

    const entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
    };

    function escapeHtml(string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            return entityMap[s];
        });
    }

    const clientCommands = {
        help(){
            const helpText = `/help<br />
Type and press enter to send a message.<br />
<b>/w</b> <i>nick</i> <i>message</i> - whisper a message to the user with the given nick
<b>/list</b> - list all users connected to the instance.
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
            const user = args.slice(0, i);
            const message = args.slice(i + 1);
            socket.send(`MSG ${user} ${message}`);
            displayMessage({type: 'whisper', text: `to: ${user} : ${message}`})
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
            const user = args.slice(0, i);
            const message = escapeHtml(args.slice(i + 1));
            if (channel === currentInstanceKey) {
                displayMessage({type: 'message', text: `${user} : ${message}`})
            } else {
                displayMessage({type: 'message', text: `${channel} : ${user} : ${message}`})
            }
        },
        joined (channel, user) {
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `user <i>${user}</i> joined`})
            } else {
                displayMessage({type: 'command', text: `user <i>${user}</i> joined to channel <i>${channel}</i>`})
            }
        },
        quited (channel, user) {
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `user <i>${user}</i> quited`})
            } else {
                displayMessage({type: 'command', text: `user <i>${user}</i> quited from channel <i>${channel}</i>`})
            }
        },
        list (channel, args) {
            const i = args.indexOf(' ');
            const userNum = args.slice(0, i);
            const users = args.slice(i + 1);
            if (channel === currentInstanceKey) {
                displayMessage({type: 'command', text: `/list: users(${userNum}): ${users}`})
            } else {
                displayMessage({type: 'command', text: `/list: ${channel} : users(${userNum}): ${users}`})
            }
        },
        closed (channel) {
            if (channel === currentInstanceKey) {
                currentInstanceKey = null;
            } else {
                displayMessage({type: 'command', text: `Connection with ${channel} chat channel has been lost.`})
            }
        }
    };

    const serverCommands = {
        msg(args) {
            const i = args.indexOf(' ');
            const user = args.slice(0, i);
            const message = escapeHtml(args.slice(i + 1));
            displayMessage({type: 'whisper', text: `from: ${user} : ${message}`})
        },
        error(args) {
            displayMessage({type: 'error', text: args})
        },
        channel(args) {
            const i = args.indexOf(' ');
            const channel = args.slice(0, i);
            const j = args.indexOf(' ', i + 1);
            var channelCommand;
            var channelCommandArgs;
            if (j === -1) {
                channelCommand = args.slice(i + 1).toLowerCase();
            } else {
                channelCommand = args.slice(i + 1, j).toLowerCase();
                channelCommandArgs = args.slice(j + 1);
            }
            if (!channelCommands.hasOwnProperty(channelCommand)) {
                console.error(`Unsupported channel command: ${channelCommand}`);
                return
            }
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
        connect (userToken) {
            socket = NetworkDispatcher.newSocket('chat', userToken);
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
        quiteFromInstanceChannel () {
            socket.send(`QUIT ${currentInstanceKey}`);
            currentInstanceKey = null;
        },
        joinToInstanceChannel (instanceKey) {
            if (currentInstanceKey != null) {
                this.quiteFromInstanceChannel();
            }
            currentInstanceKey = '#' + instanceKey;
            if (statePublisher.value === 'connected') {
                socket.send(`JOIN ${currentInstanceKey}`);
            }
        },
        state: statePublisher,
        chatMessage: new Publisher.StreamPublisher((push) => {
            displayMessage = push;
        })
    };
});