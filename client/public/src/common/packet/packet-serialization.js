define(function (require, exports, module) {

    function serializeSingle(message) {
        return [message.messageId, message]
    }

    function serializeMany(messages) {
        return messages.map(serializeSingle);
    }

    function serialize(messages) {
        return JSON.stringify(messages.map(serializeSingle));
    }

    module.exports.serialize = serialize;

    function parseJson(string) {
        try {
            return JSON.parse(string);
        } catch (e) {
            return undefined;
        }
    }

    function isValidMessageId(id, constructors) {
        return typeof id === 'number' && typeof constructors[id] === 'function'
    }

    function areValidMessageFields(fields, types) {
        const typesAreValid = function () {
            return !types || types.every(function (type, i) {
                    return typeof fields[i] === type
                })
        };
        return typeof fields === 'object' && typesAreValid()
    }

    function isValidRawMessage(json, constructors) {
        // valid raw message has format of [id, [array with fields]]
        return (typeof json === 'object' &&
            json.length === 2 &&
            isValidMessageId(json[0], constructors) &&
            areValidMessageFields(json[1], constructors[json[0]].Types)
        );
    }

    function isValidArrayOfMessagesGroupedByType(json, constructors) {
        return (typeof json === 'object' && json.every(function (json, type) {
            return (typeof json === 'object' && json.every(function (json) {
                return isValidRawMessage(json, constructors[type])
            }));
        }));
    }

    function constructMessageObject(constructor, args) {
        function F() {
            constructor.apply(this, args);
        }

        F.prototype = constructor.prototype;
        return new F();
    }

    function deserializeMany(messages, constructors) {
        return messages.map(function (message) {
            const id = message[0];
            const fields = message[1];
            return constructMessageObject(constructors[id], fields)
        });
    }

    function deserialize(data, constructors) {
        const arrayOfTypes = parseJson(data);
        if (!arrayOfTypes || !isValidArrayOfMessagesGroupedByType(arrayOfTypes, constructors)) return;
        return arrayOfTypes.map(function (messages, type) {
            return deserializeMany(messages, constructors[type])
        })
    }

    module.exports.deserialize = deserialize;
});