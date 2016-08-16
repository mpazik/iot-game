/**
 * Starting the game
 * ----------------------------
 *  1. Loading web resources (HTML, CSS, JS).
 *  2. Verification of the user.
 *  3. Loading game assets (textures, sounds) and getting address of the server to connect
 *  4. Connecting to the server.
 *  5. Waiting for initial message.
 *  6. Init world state based on initial message.
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
    const Targeting = require('./targeting');
    const Render = require('./../pixi/render');
    const ResourcesStore = require('../store/resources');
    const Dispatcher = require('./dispatcher');
    const Commands = require('../common/packet/commands').constructors;
    const MessagesIds = require('../common/packet/messages').ids;
    const MainPlayer = require('../store/main-player');
    const ItemStore = require('../store/item');
    const network = new Network();
    var instanceKey = Configuration.defaultInstance;
    var userNick;

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('started', function (f) {
        return setState = f;
    });

    network.state.subscribe(function (networkState) {
        switch (networkState) {
            case Network.State.CONNECTED:
                Dispatcher.messageStream.subscribeOnce(MessagesIds.InitialData, (data) => {
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
                    instanceKey = data.instanceKey;
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
            Dispatcher.userEventStream.subscribe('move-to', sendMoveCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-world-map', sendUseSkillOnWorldMapCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-world-object', sendUseSkillOnWorldObjectCommand);
            Dispatcher.userEventStream.subscribe('join-battle', sendJoinBattle);
            Dispatcher.userEventStream.subscribe('go-to-home', goToHome);
        } else {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-world-map', sendUseSkillOnWorldMapCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-world-object', sendUseSkillOnWorldObjectCommand);
        }
    });

    const isTargetingState = new Publisher.StatePublisher(false, push => {
        Targeting.targetingState.subscribe(skill => {
            if (skill == null && isTargetingState.value == true) {
                push(false);
            }
            if (skill != null && isTargetingState.value == false) {
                push(true);
            }
        });
    });

    isTargetingState.subscribe(isTargeting => {
        if (isTargeting) {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
        } else {
            Dispatcher.userEventStream.subscribe('map-clicked', sendMoveCommand);
        }
    });

    function sendMoveCommand(data) {
        network.sendCommands([new Commands.Move(data.x, data.y)]);
    }

    function sendUseSkillOnCharacterCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommands([new Commands.UseSkillOnCharacter(data.skillId, data.characterId)]);
    }

    function sendUseSkillOnWorldMapCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommands([new Commands.UseSkillOnWorldMap(data.skillId, data.x, data.y)]);
    }

    function sendUseSkillOnWorldObjectCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommands([new Commands.UseSkillOnWorldObject(data.skillId, data.worldObjectId)]);
    }

    function sendJoinBattle(data) {
        network.sendCommands([new Commands.JoinBattle(data.map, data.difficultyLevel)]);
    }

    function goToHome() {
        disconnect();
        instanceKey = Configuration.defaultInstance;
        connect();
    }

    function getCookie(cname) {
        var name = cname + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1);
            if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
        }
        return null;
    }

    function deleteCookie(name) {
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:01 GMT;';
    }

    function getAndCheckUserNick() {
        return new Promise((resolve, reject) => {
            const nick = getCookie('nick');
            if (nick == null) {
                return reject()
            }
            Request.Server.canPlayerLogin(nick).then(function () {
                resolve(nick)
            }).catch(function (error) {
                reject(error);
            });
        });
    }

    function connect() {
        if (userNick == null) {
            getAndCheckUserNick().then(nick=> {
                userNick = nick;
                connect();
            }).catch(error => {
                if (error) {
                    console.error(error);
                }
                setState('need-authentication');
            });
        } else {
            network.connect(userNick);
            setState('connecting');
        }
    }

    function disconnect() {
        network.disconnect();
        Render.cleanWorld();
        if (MainPlayer.playerLiveState.state) {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
        }
        Dispatcher.userEventStream.unsubscribe('join-battle', sendJoinBattle);
        Dispatcher.userEventStream.unsubscribe('go-to-home', goToHome);
        Dispatcher.messageStream.publish(MessagesIds.Disconnected, {});
    }

    function loadGameAssets() {
        setState("loading-game-assets");

        ResourcesStore.load().then(connect);
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
        setUser: function (nick) {
            document.cookie = "nick=" + nick;
        },
        connect,
        logout: function () {
            userNick = null;
            deleteCookie('nick');
            disconnect();
        },
        sendCommands: function (commands) {
            network.sendCommands(commands)
        }
    };
});