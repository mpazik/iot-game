define(function (require, exports, module) {
    module.exports = {
        ENTER_KEY_CODE: 13,
        of: function (character) {
            return character.charCodeAt();
        }
    };
});