define(function (require, exports, module) {

    function WorldState(name, width, height, tileset, spawnPoint, tiles) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.tileset = tileset;
        this.spawnPoint = spawnPoint;
        this.tiles = tiles;
    }

    module.exports = WorldState;
});