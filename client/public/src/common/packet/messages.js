/**
 * Server event it's a message, that contains the information needed to update state.
 * Stores keeps the states and should be able to reproduce current game state using events.
 * Stores always have to generate the same state from the same portion of events. Theirs behaviour should be permanent.
 */
define(function (require, exports, module) {
    const Outcome = require('../basic/outcome');

    const ServerMessage = function () {
        function ServerMessage(message) {
            this.message = message;
        }

        ServerMessage.Kinds = {
            INFO: 0,
            ERROR: 1
        };
        return ServerMessage;
    }();

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

            CharacterSpawned: function (characterId, position, characterType, nick) {
                this.characterId = characterId;
                this.position = position;
                this.characterType = characterType;
                this.nick = nick;
            },

            CharacterDied: function (characterId) {
                this.characterId = characterId;
            },

            CharacterMoved: function (characterId, move) {
                this.characterId = characterId;
                this.move = move;
            },

            SkillUsed: function (casterId, skillId, targetId) {
                this.casterId = casterId;
                this.skillId = skillId;
                this.targetId = targetId;
            },

            CharacterGotDamage: function (characterId, damage) {
                this.characterId = characterId;
                this.damage = damage;
            },

            InitialData: function (playerId, characterId, state) {
                this.playerId = playerId;
                this.characterId = characterId;
                this.state = state;
            },

            ServerMessage,

            Location: function (tiles) {
                this.tiles = tiles;
            },

            Player: function (player) {
                this.player = player;
            },

            PlayingPlayers: function (players) {
                this.players = players;
            },

            TimeSync: function (clientTime, serverTime) {
                this.clientTime = clientTime;
                this.serverTime = serverTime;
            }
        },
        ids: {
            InstanceStarted: 0,
            PlayerCreated: 1,
            CharacterSpawned: 5,
            CharacterDied: 6,
            CharacterMoved: 7,
            SkillUsed: 8,
            CharacterGotDamage: 9,
            Pong: 10,
            InitialData: 11,
            ServerMessage: 12,
            Location: 13,
            Player: 14,
            PlayingPlayers: 15,
            TimeSync: 16,
            JoinToInstance: 17
        },
        forId: []
    };

    require('./messages-functions').setIdToPrototype(events);
    require('./messages-functions').createForIds(events);
    module.exports = events;
});