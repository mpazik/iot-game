define(function (require, exports, module) {
    const Pixi = require('lib/pixi');
    const CharacterStore = require('../store/character');
    const MainPlayer = require('../store/main-player');
    const MoveStore = require('../store/move');
    const SkillStore = require('../store/skill');
    const Point = require('../unit/point');
    const CharacterModel = require('./model/character');
    const TileSize = require('../component/configuration').tileSize;
    const Targeting = require('../component/targeting');
    const Skills = require('../common/model/skills');
    const Timer = require('../component/timer');

    const layer = new Pixi.Container();
    const pointsLayer = new Pixi.Container();
    const characterModels = [];
    var mainPlayerModel = null;

    const damagePointFont = {
        font: "16px Arial",
        fill: 0xff3333,
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
    const runningAnimations = [];

    function processAnimations() {
        const timeNow = Date.now();
        runningAnimations.remove(function (animation) {
            const animationTime = timeNow - animation.started;
            const animationType = animationTypes[animation.type];
            if (animationTime >= animationType.duration) {
                // remove animation
                if (typeof animation.onFinish === 'function') {
                    animation.onFinish();
                }
                return true;
            }

            const ratio = animationTime / animationType.duration;
            animate(ratio, animation.object, animation.offset, animationType);
            return false;
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
        const point = new Pixi.Text(value, damagePointFont);
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
        }
        return false;
    }

    Targeting.targetingState.subscribe(function (skill) {
        const mainCharacterId = MainPlayer.characterId();
        characterModels.forEach(function (character) {
            character.makeNonInteractive();
        });
        if (skill !== null && skill.type === Skills.Types.ATTACK) {
            characterModels
                .filter((character) => isSkillTarget(mainCharacterId, character.id, skill))
                .forEach((character) => character.makeInteractive());
        }
    });

    function createCharacterModel(character) {
        var model = new CharacterModel(character);
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

    CharacterStore.characterSpawnedStream.subscribe(function (character) {
        createCharacterModel(character);
    });

    CharacterStore.characterDiedStream.subscribe(function (characterToRemove) {
        removeCharacterModel(characterToRemove);
    });

    SkillStore.characterGotDamageStream.subscribe(function (event) {
        const characterModel = characterModels.find(function (charcterModel) {
            return event.characterId === charcterModel.id;
        });
        startDamagePointsAnimation(characterModel.position, event.damage);
        characterModel.updateHpBar(SkillStore.percentHealth(event.characterId));
    });

    module.exports = {
        init: function () {
            layer.removeChildren();
            characterModels.length = 0;
            CharacterStore.characters().forEach((character) => {
                createCharacterModel(character);
            });
        },
        recalculatePositions: function () {
            processAnimations();
            const time = Timer.currentTimeOnServer();
            characterModels.forEach(function (characterModel) {
                characterModel.position = Point.multiplyInPlace(MoveStore.positionAtTime(characterModel.id, time), TileSize);
                characterModel.rotatable.rotation = MoveStore.angleAtTime(characterModel.id, time)
            });
            if (mainPlayerModel) {
                MainPlayer.positionInPixels.x = mainPlayerModel.position.x;
                MainPlayer.positionInPixels.y = mainPlayerModel.position.y;
                MainPlayer.position.x = mainPlayerModel.position.x / TileSize;
                MainPlayer.position.y = mainPlayerModel.position.y / TileSize;
            }
        },
        layer: layer,
        pointsLayer: pointsLayer
    };
});