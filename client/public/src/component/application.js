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

    var address = 'ws://localhost:7001';
    const nick = 'test' + Math.round(Math.random() * 100);

    function addNickToUrl(url) {
        return url + "/?nick=" + nick;
    }

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('started', function (f) {
        return setState = f;
    });

    network.state.subscribe(function (networkState) {
        switch (networkState) {
            case Network.State.CONNECTED:
                Dispatcher.messageStream.subscribeOnce(MessagesIds.InitialData, (response) => {
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
                    address = data.address;
                    connect();
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
            Dispatcher.userEventStream.subscribe('join-battle', (map) => {
                network.sendCommands([new Commands.JoinBattle(map)])
            });
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

    function start() {
        loadGameAssets().then(() => {
            connect();
        });

    }

    function connect() {
        network.connect(addNickToUrl(address));
    }

    function disconnect() {
        network.disconnect();
        Render.clean();
    }

    function loadGameAssets() {
        setState("loading game assets");
        return ResourcesStore.load();
    }

    function showGame() {
        setState('running');
        Render.init(document.getElementById('game'));
    }

    function error() {
        setState('error');
    }

    function disconnected() {
        setState('disconnected');
    }

    module.exports = {
        state: statePublisher,
        start: start,
        reconnect: function () {
            connect(address)
        }
    };
});