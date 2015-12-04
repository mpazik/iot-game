define(function (require, exports, module) {

    function Indexer() {
        this.removed = [];
        this.size = 0;
    }

    Indexer.prototype.index = function () {
        if (this.removed.length > 0) {
            return this.removed.pop();
        }
        var index = this.size;
        this.size += 1;
        return index;
    };
    Indexer.prototype.remove = function (index) {
        this.removed.push(index);
    };

    function UniqueIndexer() {
        this.indexLenght = 0;
    }

    UniqueIndexer.prototype = {

        index: function () {
            const index = this.indexLenght;
            this.indexLenght += 1;
            return index;
        },

        remove: function (index) {
        }
    };

    Indexer.UniqueIndexer = UniqueIndexer;

    module.exports = Indexer;

});