define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MessageIds = require('../common/packet/messages').ids;
    const StoreRegistrar = require('../component/store-registrar');
    const ResourcesStore = require('./resources');
    const Dispatcher = require('../component/dispatcher');

    const key = 'skill';
    const state = new Map();

    const eventHandlers = {
        [MessageIds.CharacterSpawned]: (event) => {
            state.set(event.character.id, event.skillData);
        },
        [MessageIds.CharacterDied]: (event) => {
            state.delete(event.characterId);
        },
        [MessageIds.CharacterGotDamage]: (event) => {
            state.get(event.characterId).health -= event.damage;
        },
        [MessageIds.SkillUsedOnCharacter]: (event) => {
            setCooldown(event.casterId, event.skillId)
        },
        [MessageIds.SkillUsedOnWorldMap]: (event) => {
            setCooldown(event.casterId, event.skillId)
        },
        [MessageIds.SkillUsedOnWorldObject]: (event) => {
            setCooldown(event.casterId, event.skillId)
        },
        [MessageIds.SkillUsed]: (event) => {
            setCooldown(event.casterId, event.skillId)
        }
    };

    function setCooldown(casterId, skillId) {
        const skillCooldown = ResourcesStore.skill(skillId).cooldown;
        const cooldown = skillCooldown ? skillCooldown : 0;
        state.get(casterId).cooldownTill = Date.now() + cooldown;
    }

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
        characterGotDamageStream: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribe(MessageIds.CharacterGotDamage, (event) => {
                push(event);
            });
        }),
        characterUsedSkillOnCharacter: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribe(MessageIds.SkillUsedOnCharacter, (event) => {
                push({
                    characterId: event.casterId,
                    skill: ResourcesStore.skill(event.skillId),
                    targetId: event.targetId
                })
            });
        }),
        health: (characterId) => state.get(characterId).health,
        percentHealth: (characterId) => {
            const character = state.get(characterId);
            return character.health / character.maxHealth;
        },
        characterAnimation: (skillId) => {
            const skill = ResourcesStore.skill(skillId);
            if (!skill) {
                return null;
            }
            const animation = skill.characterAnimation;
            if (typeof animation === 'string') {
                return animation;
            }
            if (typeof animation === 'object') {
                const animationIndex = Math.floor(Math.random() * animation.length);
                return animation[animationIndex];
            }
            return null;
        },
        skill: skillId => ResourcesStore.skill(skillId)
    };
});