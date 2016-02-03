define(function (require, exports, module) {
    const MessageIds = require('../common/packet/messages').ids;
    const Move = require('../unit/move');
    const StoreRegistrar = require('../component/store-registrar');

    const key = 'move';
    const state = new Map();

    const eventHandlers = {
        [MessageIds.CharacterMoved]: (event) => {
            state.set(event.characterId, event.move);
        },
        [MessageIds.CharacterSpawned]: (event) => {
            state.set(event.character.id, event.move);
        },
        [MessageIds.CharacterDied]: (event) => {
            state.delete(event.characterId);
        }
    };

    StoreRegistrar.registerStore({
        key,
        eventHandlers,
        state: () => Map.toObject(state),
        init: (initialState) => {
            state.clear();
            initialState.forEach(elem => state.set(elem.characterId, elem.move));
        }
    });

    module.exports = {
        key,
        angleAtTime: function (characterId, time) {
            return Move.angleAtTime(state.get(characterId), time);
        },

        positionAtTime: function (characterId, time) {
            return Move.positionAtTime(state.get(characterId), time);
        }
    };
});