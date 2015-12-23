/**
 * Starting the game
 * ----------------------------
 *  1. Loading web resources (HTML, CSS, JS).
 *  2. Verification of the user.
 *  3. Loading game assets (textures, sounds) and getting address of the server to connect
 *  4. Connecting to the server.
 *  5. Waiting for initial message.
 *  6. Init world state based on initial message.`
 *  7. Show loaded game to player.
 *
 * Joining to the new instnace
 * ----------------------------
 *  1. Disconnecting.
 *  2. Got to 4 point of connecting.
 */

define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Network = require('./network');
    const Render = require('./../pixi/render');
    const ResourcesStore = require('../store/resources');
    const Dispatcher = require('./dispatcher');
    const Commands = require('../common/packet/commands').constructors;
    const MessagesIds = require('../common/packet/messages').ids;
    const MainPlayer = require('../store/main-player');
    const network = new Network();
    var userNick;

    function addNickToUrl(url) {
        return url + "/?nick=" + userNick;
    }

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('started', function (f) {
        return setState = f;
    });

    network.state.subscribe(function (networkState) {
        switch (networkState) {
            case Network.State.CONNECTED:
                Dispatcher.messageStream.subscribeOnce(MessagesIds.InitialData, () => {
                    showGame();
                    console.log("Got initial data");
                });
                break;
            case Network.State.DISCONNECTED:
                disconnected();
                break;
            case Network.State.ERROR:
                error();
                break;
        }
    });

    Dispatcher.messageStream.subscribe(MessagesIds.JoinToInstance, (data) => {
        // create type listener or something like that for network.
        const listener = function (networkState) {
            switch (networkState) {
                case Network.State.DISCONNECTED:
                    network.state.unsubscribe(listener);
                    connect(data.address);
                    break;
            }
        };
        network.state.subscribe(listener);
        disconnect();
    });
    MainPlayer.playerLiveState.subscribe((live) => {
        if (live) {
            Dispatcher.userEventStream.subscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-character', sendUseSkillCommand);
            Dispatcher.userEventStream.subscribe('join-battle', sendJoinBattle);
            Dispatcher.userEventStream.subscribe('go-to-home', goToHome);
        } else {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillCommand);
        }
    });

    function sendMoveCommand(data) {
        network.sendCommands([new Commands.Move(data.x, data.y)]);
    }

    function sendUseSkillCommand(data) {
        network.sendCommands([new Commands.UseSkill(data.skillId, data.characterId)]);
    }

    function sendJoinBattle(data) {
        network.sendCommands([new Commands.JoinBattle(data.map, data.difficultyLevel)]);
    }

    function goToHome() {
        network.sendCommands([new Commands.GoToHome()]);
    }

    function connect(address) {
        if (userNick == null) {
            throw 'userNick has to be defined before game can connect to the server';
        }
        network.connect(addNickToUrl(address));
    }

    function disconnect() {
        network.disconnect();
        Render.cleanWorld();
        if (MainPlayer.playerLiveState.state) {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillCommand);
        }
        Dispatcher.userEventStream.unsubscribe('join-battle', sendJoinBattle);
        Dispatcher.userEventStream.unsubscribe('go-to-home', goToHome);
    }

    function loadGameAssets() {
        setState("loading-game-assets");
        ResourcesStore.load().then(() => setState("ready-to-connect"));
    }

    function showGame() {
        setState('running');
        Render.initWorld();
    }

    function error() {
        setState('error');
    }

    function disconnected() {
        setState('disconnected');
    }

    module.exports = {
        state: statePublisher,
        init: function (gameElement) {
            loadGameAssets();
            Render.init(gameElement);
        },
        setUserNick: function (nick) {
            userNick = nick;
        },
        connect: connect,
        sendCommands: function(commands) {
            network.sendCommands(commands)
        }
    };
});