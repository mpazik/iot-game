define(function (require, exports, module) {

    function parseJson(string) {
        try {
            return JSON.parse(string);
        } catch (e) {
            return undefined;
        }
    }

    class Serializer {
        constructor(parsingMessageTypes, serializationMessageTypes) {
            this.parsingMessageTypes = parsingMessageTypes;
            this.serializationMessageTypes = serializationMessageTypes;
        }

        parse(data) {
            const parsedData = parseJson(data);
            const messageCode = parsedData[0];
            const messageConstructor = this.parsingMessageTypes.get(messageCode);
            const message = parsedData[1];
            Object.setPrototypeOf(message, messageConstructor.prototype);
            return message
        };

        serialize(message) {
            const messageCode = this.serializationMessageTypes.get(message.constructor);
            return JSON.stringify([messageCode, message]);
        };
    }

    module.exports = {
        test: 'test',
        Builder () {
            const parsingMessageTypes = new Map();
            const serializationMessageTypes = new Map();
            return {
                registerParsingMessageType: function (typeCode, constructor) {
                    parsingMessageTypes.set(typeCode, constructor);
                    return this;
                },
                registerSerializationMessageType: function (typeCode, constructor) {
                    serializationMessageTypes.set(constructor, typeCode);
                    return this;
                },
                build() {
                    return new Serializer(parsingMessageTypes, serializationMessageTypes);
                }
            };
        }
    }
});
