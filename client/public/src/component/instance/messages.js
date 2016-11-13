define(function (require, exports, module) {
    module.exports = {
        Disconnected: function () {
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
        InitialData: function (state) {
            this.state = state;
        },
        ServerMessage: function (message) {
            this.message = message;
        },
        ServerError: function (error) {
            this.message = error;
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
        WorldObjectRemoved: function (worldObject) {
            this.worldObject = worldObject;
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
        UserCharacter: function (characterId, userId, userNick) {
            this.characterId = characterId;
            this.userId = userId;
            this.userNick = userNick;
        },
        CharacterHealed: function (characterId, healed) {
            this.characterId = characterId;
            this.healed = healed;
        },
    };
});