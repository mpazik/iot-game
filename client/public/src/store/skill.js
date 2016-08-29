define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const Messages = require('../component/instance/messages');
    const StoreRegistrar = require('../component/store-registrar');
    const ResourcesStore = require('./resources');
    const Dispatcher = require('../component/dispatcher');

    const key = 'skill';
    const state = new Map();


    Dispatcher.messageStream.subscribe(Messages.CharacterSpawned, (event) => {
        state.set(event.character.id, event.skillData);
    });
    Dispatcher.messageStream.subscribe(Messages.CharacterDied, (event) => {
        state.delete(event.characterId);
    });
    Dispatcher.messageStream.subscribe(Messages.CharacterGotDamage, (event) => {
        state.get(event.characterId).health -= event.damage;
    });
    Dispatcher.messageStream.subscribe(Messages.CharacterHealed, (event) => {
        state.get(event.characterId).health += event.healed;
    });
    Dispatcher.messageStream.subscribe(Messages.SkillUsedOnCharacter, (event) => {
        setCooldown(event.casterId, event.skillId)
    });
    Dispatcher.messageStream.subscribe(Messages.SkillUsedOnWorldMap, (event) => {
        setCooldown(event.casterId, event.skillId)
    });
    Dispatcher.messageStream.subscribe(Messages.SkillUsedOnWorldObject, (event) => {
        setCooldown(event.casterId, event.skillId)
    });
    Dispatcher.messageStream.subscribe(Messages.SkillUsed, (event) => {
        setCooldown(event.casterId, event.skillId)
    });

    function setCooldown(casterId, skillId) {
        const skillCooldown = ResourcesStore.skill(skillId).cooldown;
        const cooldown = skillCooldown ? skillCooldown : 0;
        state.get(casterId).cooldownTill = Date.now() + cooldown;
    }

    StoreRegistrar.registerStore({
        key,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(elem => state.set(elem.characterId, elem.skillData));
        }
    });

    module.exports = {
        key,
        characterHealthChangeStream: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribe(Messages.CharacterGotDamage, (event) => {
                push({characterId: event.characterId, change: -event.damage});
            });
            Dispatcher.messageStream.subscribe(Messages.CharacterHealed, (event) => {
                push({characterId: event.characterId, change: event.healed});
            });
        }),
        characterUsedSkillOnCharacter: new Publisher.StreamPublisher((push) => {
            Dispatcher.messageStream.subscribe(Messages.SkillUsedOnCharacter, (event) => {
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