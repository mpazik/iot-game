/**
 * Server event it's a message, that contains the information needed to update state.
 * Stores keeps the states and should be able to reproduce current game state using events.
 * Stores always have to generate the same state from the same portion of events. Theirs behaviour should be permanent.
 */
define(function (require, exports, module) {
    const ServerMessage = function () {
        function ServerMessage(message, type) {
            //noinspection JSPotentiallyInvalidUsageOfThis
            this.message = message;
            this.type = type;
        }

        ServerMessage.Kinds = {
            INFO: 0,
            ERROR: 1
        };
        return ServerMessage;
    }();

    //noinspection JSUnusedGlobalSymbols
    const events = {
        constructors: {
            InstanceStarted: function () {
                // Is this an event. It does not change the state.
                // It is an event in different meaning.
            },
            PlayerCreated: function (player) {
                this.player = player;
            },

            PlayerLogIn: function (playerId) {
                this.playerId = playerId;
            },

            PlayerLogOut: function (playerId) {
                this.playerId = playerId;
            },

            CharacterSpawned: function (character, move, skillData) {
                this.character = character;
                this.move = move;
                this.skillData = skillData;
            },

            CharacterDied: function (characterId) {
                this.characterId = characterId;
            },

            CharacterMoved: function (characterId, move) {
                this.characterId = characterId;
                this.move = move;
            },

            SkillUsedOnCharacter: function (casterId, skillId, targetId) {
                this.casterId = casterId;
                this.skillId = skillId;
                this.targetId = targetId;
            },

            CharacterGotDamage: function (characterId, damage) {
                this.characterId = characterId;
                this.damage = damage;
            },

            InitialData: function (playerId, characterId, state, scenario) {
                this.playerId = playerId;
                this.characterId = characterId;
                this.state = state;
                this.scenario = scenario;
            },

            ServerMessage,

            Location: function (tiles) {
                this.tiles = tiles;
            },

            Player: function (player) {
                this.player = player;
            },

            PlayingPlayers: function (players) {
                //noinspection JSUnusedGlobalSymbols
                this.players = players;
            },

            TimeSync: function (clientTime, serverTime) {
                this.clientTime = clientTime;
                this.serverTime = serverTime;
            },

            ScenarioEnd: function (resolution) {
                this.resolution = resolution;
            },

            PlayerMessage: function (playerNick, message) {
                this.playerNick = playerNick;
                this.message = message;
            },

            SkillUsedOnWorldMap: function (casterId, skillId, x, y) {
                this.casterId = casterId;
                this.skillId = skillId;
                this.x = x;
                this.y = y;
            },

            WorldObjectCreated: function (worldObject) {
                this.worldObject = worldObject
            },

            SkillUsedOnWorldObject: function (casterId, skillId, worldObjectId) {
                this.casterId = casterId;
                this.skillId = skillId;
                this.worldObjectId = worldObjectId;
            },

            SkillUsed: function (casterId, skillId) {
                this.casterId = casterId;
                this.skillId = skillId;
            },

            WorldObjectRemoved: function (worldObjectId) {
                this.worldObjectId = worldObjectId;
            }
        },
        ids: {
            Disconnected: -1, // event not sent by server but triggered by client on disconnect
            InstanceStarted: 0,
            PlayerCreated: 1,
            CharacterSpawned: 5,
            CharacterDied: 6,
            CharacterMoved: 7,
            SkillUsedOnCharacter: 8,
            CharacterGotDamage: 9,
            InitialData: 11,
            ServerMessage: 12,
            Location: 13,
            Player: 14,
            PlayingPlayers: 15,
            TimeSync: 16,
            JoinToInstance: 17,
            ScenarioEnd: 19,
            PlayerMessage: 20,
            SkillUsedOnWorldMap: 21,
            WorldObjectCreated: 22,
            SkillUsedOnWorldObject: 23,
            WorldObjectRemoved: 24,
            SkillUsed: 25
        },
        forId: []
    };

    require('./messages-functions').setIdToPrototype(events);
    require('./messages-functions').createForIds(events);
    module.exports = events;
});