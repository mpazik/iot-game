define(function (require, exports, module) {
    const Publisher = require('../common/basic/publisher');
    const MessageIds = require('../common/packet/messages').ids;
    const StoreRegistrar = require('../component/store-registrar');

    const key = 'character';
    const state = new Map();
    var characterSpawned = null;
    var characterDied = null;

    const eventHandlers = {
        [MessageIds.CharacterSpawned]: (event) => {
            const character = event.character;
            state.set(character.id, character);
            characterSpawned(character)
        },
        [MessageIds.CharacterDied]: (event) => {
            state.delete(event.characterId);
            characterDied(event.characterId);
        }
    };

    StoreRegistrar.registerStore({
        key,
        eventHandlers,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(character => {
                state.set(character.id, character)
            });
        }
    });

    module.exports = {
        key,
        characterSpawnedStream: new Publisher.StreamPublisher((push) => characterSpawned = push),
        characterDiedStream: new Publisher.StreamPublisher((push) => characterDied = push),
        CharacterType: {
            Player: 0,
            Bot: 1
        },
        characterExists: (characterId) => !!state.get(characterId),
        characters: () => Array.from(state.values()),
        isCharacterEnemyFor: (characterId1, characterId2) => {
            // relation is two directional so if char1 is enemy of char2 then char2 is enemy of char1.
            // enemy is any one of different type, bots are enemies of the players.
            return state.get(characterId1).type != state.get(characterId2).type
        },
        character: (characterId) => state.get(characterId),
        charactersOfType: (type) => state.filterValues((character) => character.type == type)
    };
});