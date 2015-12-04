define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MessageIds = require('../common/packet/messages').ids;
    const StoreRegistrar = require('../component/store-registrar');
    const CharacterStore = require('./character');
    const ResourcesStore = require('./resources');

    const key = 'skill';
    const state = new Map();
    var characterGotDamage = null;

    function characterInitState(characterType) {
        if (characterType == CharacterStore.CharacterType.Bot) {
            return {
                health: 100,
                maxHealth: 100,
                cooldownTill: null
            }
        }
        if (characterType == CharacterStore.CharacterType.Player) {
            return {
                health: 200,
                maxHealth: 200,
                cooldownTill: null
            }
        }
    }

    const eventHandlers = {
        [MessageIds.CharacterSpawned]: (event) => {
            state.set(event.character.id, characterInitState(event.character.type));
        },
        [MessageIds.CharacterDied]: (event) => {
            state.delete(event.characterId);
        },
        [MessageIds.CharacterGotDamage]: (event) => {
            state.get(event.characterId).health -= event.damage;
            characterGotDamage(event);
        },
        [MessageIds.SkillUsed]: (event) => {
            const skillCooldown = ResourcesStore.skill(event.skillId).cooldown;
            const cooldown = skillCooldown ? skillCooldown : 0;
            state.get(event.casterId).cooldownTill = Date.now() + cooldown;
        }
    };

    StoreRegistrar.registerStore({
        key,
        eventHandlers,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(elem => state.set(elem.characterId, elem.skillData));
        }
    });

    module.exports = {
        key,
        characterGotDamageStream: new Publisher.StreamPublisher((push) => characterGotDamage = push),
        health: (characterId) => state.get(characterId).health,
        percentHealth: (characterId) => {
            const character = state.get(characterId);
            return character.health / character.maxHealth;
        },
        isOnCooldown: (characterId, time) => {
            const cooldownTill = state.get(characterId).cooldownTill;
            return cooldownTill && time < cooldownTill;
        },
        skill: id => ResourcesStore.skill(id)
    };
});