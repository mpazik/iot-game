define(function (require, exports, module) {
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
    const WorldBoard = require('../render/world-board');
    const Parcel = require('../store/parcel');

    const characterModels = [];
    var mainPlayerModel = null;

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
        characterModels.push(model);
        WorldBoard.addObject(model);
        if (character.id === MainPlayer.characterId()) {
            mainPlayerModel = model;
        }
    }

    function removeCharacterModel(characterId) {
        const index = characterModels.findIndex(function (character) {
            return character.id === characterId;
        });
        WorldBoard.removeObject(characterModels[index]);
        characterModels.splice(index, 1);
        if (characterId === MainPlayer.characterId()) {
            mainPlayerModel = null;
        }
    }

    function characterPosition(characterModel, time) {
        return Point.multiplyInPlace(MoveStore.positionAtTime(characterModel.id, time), TileSize);
    }

    CharacterStore.characterSpawnedStream.subscribe(function (character) {
        createCharacterModel(character);
    });

    CharacterStore.characterDiedStream.subscribe(function (characterToRemove) {
        removeCharacterModel(characterToRemove);
    });

    module.exports = {
        init: function () {
            characterModels.length = 0;
            CharacterStore.characters().forEach(createCharacterModel);
        },
        recalculatePositions: function () {
            function setStateIfAnimationIsDifferentOnFinished(characterModel, state, time) {
                if (characterModel.sprite.getState(state) !== state || characterModel.sprite.isFinished(time)) {
                    characterModel.sprite.setState(state, time)
                }
            }

            const time = Timer.currentTimeOnServer();
            characterModels.forEach(function (characterModel) {
                const newPosition = characterPosition(characterModel, time);
                newPosition.y -= 80;
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
                const y = mainPlayerModel.position.y + 80;
                MainPlayer.positionInPixels.x = mainPlayerModel.position.x;
                MainPlayer.positionInPixels.y = y;
                MainPlayer.position.x = mainPlayerModel.position.x / TileSize;
                MainPlayer.position.y = y / TileSize;
                Parcel.checkCurrentParcel();
            }
            WorldBoard.sortDisplayOrder();
        }
    };
});