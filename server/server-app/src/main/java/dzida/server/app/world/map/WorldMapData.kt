package dzida.server.app.world.map

data class WorldMapData(
        val height: Int,
        val width: Int,
        val tilesets: List<TilesetRef>,
        val layers: List<Layer>,
        val properties: Properties
)

data class TilesetRef(val firstgid: Int, val source: String)

data class Layer(val width: Int, val height: Int, val data: IntArray)

data class Properties(val spawnPointX: Int, val spawnPointY: Int, val backgroundColor: String)
