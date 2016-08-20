define(function (require, exports, module) {
    const Messages = require('../component/instance/messages');
    const Move = require('../unit/move');
    const StoreRegistrar = require('../component/store-registrar');
    const Dispatcher = require('../component/dispatcher');

    const key = 'move';
    const state = new Map();

    Dispatcher.messageStream.subscribe(Messages.CharacterMoved, (event) => {
        state.set(event.characterId, event.move);
    });
    Dispatcher.messageStream.subscribe(Messages.CharacterSpawned, (event) => {
        state.set(event.character.id, event.move);
    });
    Dispatcher.messageStream.subscribe(Messages.CharacterDied, (event) => {
        state.delete(event.characterId);
    });

    StoreRegistrar.registerStore({
        key,
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