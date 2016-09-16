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
 * Joining to the new instance
 * ----------------------------
 *  1. Stop displaying the game.
 *  2. Disconnecting form the previous instance if there was a connection.
 *  3. Connection to the new instance server.
 *  4.
 */

define(function (require, exports, module) {
    const Publisher = require('../../common/basic/publisher');
    const InstanceNetwork = require('./instance-network');
    const Targeting = require('../targeting');
    const Render = require('../../render/render');
    const Dispatcher = require('../dispatcher');
    const Commands = require('./commands');
    const Messages = require('./messages');
    const MainPlayer = require('../../store/main-player');
    const ItemStore = require('../../store/item');
    const Skills = require('../../common/model/skills');
    const Analytics = require('../analytics');
    const network = new InstanceNetwork();
    var currentInstanceKey = null;

    var setState = null;
    const statePublisher = new Publisher.StatePublisher('ready-to-connect', function (f) {
        return setState = f;
    });

    network.state.subscribe(function (networkState) {
        switch (networkState) {
            case InstanceNetwork.State.CONNECTED:
                Dispatcher.messageStream.subscribeOnce(Messages.InitialData, (data) => {
                    showGame();
                    console.log("Got initial data");
                });
                break;
            case InstanceNetwork.State.DISCONNECTED:
                disconnected();
                break;
            case InstanceNetwork.State.ERROR:
                error();
                break;
        }
    });

    Dispatcher.userEventStream.subscribe('skill-triggered', function (event) {
        const skill = event.skill;
        if (skill.id == Skills.Ids.EAT_APPLE) {
            Dispatcher.userEventStream.publish('eat-apple', null);
        } else if (skill.id == Skills.Ids.CREATE_ARROWS) {
            if (!ItemStore.checkSkillItemRequirements(skill.id)) return;
            const skillUsed = new Messages.SkillUsed(MainPlayer.characterId(), skill.id);
            Dispatcher.messageStream.publish(Messages.SkillUsed, skillUsed);
        }
    });

    MainPlayer.playerLiveState.subscribe((live) => {
        if (live) {
            Dispatcher.userEventStream.subscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.subscribe('move-to', sendMoveCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-world-map', sendUseSkillOnWorldMapCommand);
            Dispatcher.userEventStream.subscribe('skill-used-on-world-object', sendUseSkillOnWorldObjectCommand);
            Dispatcher.userEventStream.subscribe('eat-apple', eatAppleCommand);
        } else {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-world-map', sendUseSkillOnWorldMapCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-world-object', sendUseSkillOnWorldObjectCommand);
            Dispatcher.userEventStream.unsubscribe('eat-apple', eatAppleCommand);
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
        network.sendCommand(new Commands.Move(data.x, data.y));
    }

    function sendUseSkillOnCharacterCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommand(new Commands.UseSkillOnCharacter(data.skillId, data.characterId));
    }

    function sendUseSkillOnWorldMapCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommand(new Commands.UseSkillOnWorldMap(data.skillId, data.x, data.y));
    }

    function sendUseSkillOnWorldObjectCommand(data) {
        if (!ItemStore.checkSkillItemRequirements(data.skillId)) return;
        network.sendCommand(new Commands.UseSkillOnWorldObject(data.skillId, data.worldObjectId));
    }

    function eatAppleCommand() {
        if (!ItemStore.checkSkillItemRequirements(Skills.Ids.EAT_APPLE)) return;
        const skillUsed = new Messages.SkillUsed(MainPlayer.characterId(), Skills.Ids.EAT_APPLE);
        Dispatcher.messageStream.publish(Messages.SkillUsed, skillUsed);

        const isAppleRotten = (Math.random() * 3) < 1; // 1/3 that apple will be rotten
        if (isAppleRotten) {
            const effects = ['invert', 'twist'];
            const randomEffect = effects[Math.floor(Math.random() * effects.length)];
            Render.runEffect(randomEffect);
            Analytics.sendEvent('eat-rotten-apple');
            network.sendCommand(new Commands.EatRottenApple());
        } else {
            network.sendCommand(new Commands.EatApple());
        }
    }

    function connect(instanceKey, userToken) {
        if (currentInstanceKey != null) {
            network.disconnect();
        }
        network.connect(instanceKey, userToken);
        currentInstanceKey = instanceKey;
        setState('connecting');
    }

    function disconnect() {
        network.disconnect();
    }

    function showGame() {
        setState('running');
        Render.initWorld();
    }

    function error() {
        setState('error');
    }

    function readyToConnect() {
        setState('ready-to-connect')
    }

    function disconnected() {
        setState('disconnected');
        Render.cleanWorld();
        if (MainPlayer.playerLiveState.state) {
            Dispatcher.userEventStream.unsubscribe('map-clicked', sendMoveCommand);
            Dispatcher.userEventStream.unsubscribe('skill-used-on-character', sendUseSkillOnCharacterCommand);
        }
        Dispatcher.messageStream.publish(Messages.Disconnected, {});
    }

    module.exports = {
        state: statePublisher,
        init: function (gameElement) {
            Render.init(gameElement);
        },
        readyToConnect,
        connect,
        disconnect,
        sendCommand: function (command) {
            network.sendCommand(command)
        }
    };
});