define(function (require, exports, module) {
    const Pixi = require('pixi');
    const CharacterStore = require('../store/character');
    const MainPlayer = require('../store/main-player');
    const MoveStore = require('../store/move');
    const SkillStore = require('../store/skill');
    const Point = require('../unit/point');
    const CharacterModel = require('./model/character');
    const TileSize = require('configuration').tileSize;
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Timer = require('../component/timer');
    const Friends = require('../component/friends');

    const layer = new Pixi.Container();
    const pointsLayer = new Pixi.Container();
    const characterModels = [];
    var mainPlayerModel = null;

    const damageFont = {
        font: "16px Arial",
        fill: 0xff3333,
        stroke: 0x000000,
        strokeThickness: 2
    };

    const healedFont = {
        font: "16px Arial",
        fill: 0x33ff33,
        stroke: 0x000000,
        strokeThickness: 2
    };

    function interpolate(ratio, begin, end) {
        return begin + (end - begin) * ratio
    }

    function animate(ratio, object, offset, animation) {
        animation.properties.forEach(function (property) {
            const start = animation.start[property];
            const end = animation.end[property];
            object[property] = offset[property] + interpolate(ratio, start, end);
        })
    }

    const animationTypes = {
        point: {
            properties: ['y'],
            start: {
                y: -20
            },
            end: {
                y: -60
            },
            duration: 1500
        }
    };
    var runningAnimations = [];

    function processAnimations() {
        const timeNow = Date.now();
        runningAnimations = runningAnimations.filter((animation) => {
            const animationTime = timeNow - animation.started;
            const animationType = animationTypes[animation.type];
            const animationEnded = animationTime >= animationType.duration;
            if (animationEnded) {
                // remove animation
                if (typeof animation.onFinish === 'function') {
                    animation.onFinish();
                }
            }
            return !animationEnded;
        });
        runningAnimations.forEach((animation) => {
            const animationTime = timeNow - animation.started;
            const animationType = animationTypes[animation.type];
            const ratio = animationTime / animationType.duration;
            animate(ratio, animation.object, animation.offset, animationType);
        });
    }

    function registerAnimation(object, offset, type, onFinish) {
        // initialise offset
        animationTypes[type].properties.forEach(function (property) {
            if (!offset[property]) {
                offset[property] = 0;
            }
        });
        const animation = {
            started: Date.now(),
            object: object,
            offset: offset,
            type: type,
            onFinish: onFinish
        };
        runningAnimations.push(animation);
    }

    function startDamagePointsAnimation(position, value) {
        const font = value < 0 ? damageFont : healedFont;
        const point = new Pixi.Text(value, font);
        point.position.x = position.x - point.width / 2;
        point.position.y = position.y;
        registerAnimation(point, {y: position.y}, 'point', function () {
            pointsLayer.removeChild(point);
        });
        pointsLayer.addChild(point)
    }

    function isSkillTarget(mainCharacterId, characterId, skill) {
        if (skill.target == Skills.Targets.ENEMIES) {
            return CharacterStore.isCharacterEnemyFor(mainCharacterId, characterId)
        } else if (skill.target == Skills.Targets.USERS) {
            return mainCharacterId != characterId && !CharacterStore.isCharacterEnemyFor(mainCharacterId, characterId)
        }
        return false;
    }

    Targeting.targetingState.subscribe(function (skill) {
        const mainCharacterId = MainPlayer.characterId();
        characterModels.forEach(function (character) {
            character.makeNonInteractive();
        });
        if (skill !== null && (skill.type == Skills.Types.ATTACK || skill.type == Skills.Types.SPECIAL)) {
            characterModels
                .filter((character) => isSkillTarget(mainCharacterId, character.id, skill))
                .forEach((character) => character.makeInteractive());
        }
    });

    const userCharactersWithoutNick = new Map();
    Friends.friendshipPublisher.subscribe((event) => {
        const model = userCharactersWithoutNick.get(event.userId);
        if (model == null) {
            return;
        }
        model.createNick(event.nick);
        userCharactersWithoutNick.delete(event.userId);
    });

    function createCharacterModel(character) {
        const health = SkillStore.percentHealth(character.id);
        if (character.userId) {
            if (character.userId == MainPlayer.userId()) {
                character.nick = MainPlayer.userNick()
            } else {
                character.nick = Friends.friends.get(character.userId)
            }
        }
        const model = new CharacterModel(character, health);
        if (character.userId && character.nick == null) {
            userCharactersWithoutNick.set(character.userId, model);
        }
        const time = Timer.currentTimeOnServer();
        model.position = characterPosition(model, time);
        model.rotatable.rotation = MoveStore.angleAtTime(model.id, time);
        characterModels.push(model);
        layer.addChild(model);
        if (character.id === MainPlayer.characterId()) {
            mainPlayerModel = model;
        }
    }

    function removeCharacterModel(characterId) {
        const index = characterModels.findIndex(function (character) {
            return character.id === characterId;
        });
        layer.removeChild(characterModels[index]);
        characterModels.splice(index, 1);
        if (characterId === MainPlayer.characterId()) {
            mainPlayerModel = null;
        }
    }

    function characterPosition(characterModel, time) {
        return Point.multiplyInPlace(MoveStore.positionAtTime(characterModel.id, time), TileSize);
    }

    function findCharacterModel(characterId) {
        return characterModels.find(function (characterModel) {
            return characterId === characterModel.id;
        });
    }

    CharacterStore.characterSpawnedStream.subscribe(function (character) {
        createCharacterModel(character);
    });

    CharacterStore.characterDiedStream.subscribe(function (characterToRemove) {
        removeCharacterModel(characterToRemove);
    });

    SkillStore.characterHealthChangeStream.subscribe(function (event) {
        const characterModel = findCharacterModel(event.characterId);
        startDamagePointsAnimation(characterModel.position, event.change);
        characterModel.updateHpBar(SkillStore.percentHealth(event.characterId));
    });

    SkillStore.characterUsedSkillOnCharacter.subscribe(function (event) {
        const characterModel = findCharacterModel(event.characterId);
        if (event.skill.target == Skills.Targets.ENEMIES) {
            const enemyPosition = findCharacterModel(event.targetId).position;
            characterModel.rotatable.rotation = Point.angleFromTo(enemyPosition, characterModel.position)
        }
        const animation = SkillStore.characterAnimation(event.skill.id);
        if (animation) {
            characterModel.sprite.setState(animation, Timer.currentTimeOnServer());
        }
    });

    module.exports = {
        init: function () {
            layer.removeChildren();
            characterModels.length = 0;
            CharacterStore.characters().forEach(createCharacterModel);
        },
        recalculatePositions: function () {
            function setStateIfAnimationIsDifferentOnFinished(characterModel, state, time) {
                if (characterModel.sprite.getState(state) !== state || characterModel.sprite.isFinished(time)) {
                    characterModel.sprite.setState(state, time)
                }
            }

            processAnimations();
            const time = Timer.currentTimeOnServer();
            characterModels.forEach(function (characterModel) {
                const newPosition = characterPosition(characterModel, time);
                if (!Point.equal(newPosition, characterModel.position)) {
                    const angle = MoveStore.angleAtTime(characterModel.id, time) / Math.PI + 1;
                    if (angle > 0.25 && angle <= 0.75) {
                        setStateIfAnimationIsDifferentOnFinished(characterModel, 'moveLeft', time);
                    } else if (angle > 0.75 && angle <= 1.25) {
                        setStateIfAnimationIsDifferentOnFinished(characterModel, 'moveUp', time);
                    } else if (angle > 1.25 && angle <= 1.75) {
                        setStateIfAnimationIsDifferentOnFinished(characterModel, 'moveRight', time);
                    } else {
                        setStateIfAnimationIsDifferentOnFinished(characterModel, 'moveDown', time);
                    }
                }
                characterModel.sprite.update(time);
                characterModel.position = newPosition;
            });
            if (mainPlayerModel) {
                MainPlayer.positionInPixels.x = mainPlayerModel.position.x;
                MainPlayer.positionInPixels.y = mainPlayerModel.position.y;
                MainPlayer.position.x = mainPlayerModel.position.x / TileSize;
                MainPlayer.position.y = mainPlayerModel.position.y / TileSize;
            }
        },
        get layer() {
            return layer;
        },
        get pointsLayer() {
            return pointsLayer;
        }
    };
});