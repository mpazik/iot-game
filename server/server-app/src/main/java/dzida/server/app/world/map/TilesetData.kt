package dzida.server.app.world.map

data class TilesetData(
        val image: String,
        val name: String,
        val terrains: List<Terrain>,
        val tiles: Map<String, Tile>
)

data class Terrain(val name: String)

data class Tile(val terrain: List<Int>)