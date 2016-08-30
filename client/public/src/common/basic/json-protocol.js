define(function (require, exports, module) {

    function parseJson(string) {
        try {
            return JSON.parse(string);
        } catch (e) {
            return undefined;
        }
    }

    class JsonProtocol {
        constructor(parsingMessageTypes, serializationMessageTypes) {
            this.parsingMessageTypes = parsingMessageTypes;
            this.serializationMessageTypes = serializationMessageTypes;
        }

        parse(data) {
            const parsedData = parseJson(data);
            const messageCode = parsedData[0];
            const messageConstructor = this.parsingMessageTypes[messageCode];
            const message = parsedData[1];
            Object.setPrototypeOf(message, messageConstructor.prototype);
            return message
        };

        serialize(message) {
            const messageCode = message.constructor.name;
            if (!this.serializationMessageTypes.hasOwnProperty(messageCode)) {
                throw "Tried to serialized not supported message type: " + messageCode
            }
            return JSON.stringify([messageCode, message]);
        };
    }

    module.exports = JsonProtocol;
});
