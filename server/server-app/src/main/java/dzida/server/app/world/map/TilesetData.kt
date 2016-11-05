package dzida.server.app.world.map

data class TilesetData(
        val image: String,
        val name: String,
        val terrains: List<Terrain>,
        val tiles: Map<String, Tile>,
        val tileproperties: Map<String, TailObjectProperty>
)

data class Terrain(val name: String)

data class Tile(val terrain: List<Int>)

data class TailObjectProperty(val objectId: String)