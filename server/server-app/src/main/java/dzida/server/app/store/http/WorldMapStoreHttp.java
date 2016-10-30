package dzida.server.app.store.http;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.world.map.Layer;
import dzida.server.app.world.map.Terrain;
import dzida.server.app.world.map.TilesetData;
import dzida.server.app.world.map.TilesetRef;
import dzida.server.app.world.map.WorldMapData;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.world.map.Tileset;
import dzida.server.core.world.map.Tileset.TerrainType;
import dzida.server.core.world.map.WorldMap;
import dzida.server.core.world.map.WorldMapStore;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class WorldMapStoreHttp implements WorldMapStore {
    private final WorldMapLoader worldMapLoader;

    private final LoadingCache<Key<WorldMap>, WorldMap> worldMaps = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<Key<WorldMap>, WorldMap>() {
                public WorldMap load(@Nonnull Key<WorldMap> key) {
                    return transformMap(worldMapLoader.loadMap(key));
                }
            });

    private final LoadingCache<Key<Tileset>, Tileset> tilesets = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<Key<Tileset>, Tileset>() {
                public Tileset load(@Nonnull Key<Tileset> key) {
                    return transformTileset(worldMapLoader.loadTileset(key));
                }
            });

    public WorldMapStoreHttp(WorldMapLoader worldMapLoader) {
        this.worldMapLoader = worldMapLoader;
    }

    private static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    @Override
    public WorldMap getMap(Key<WorldMap> worldMapKey) {
        try {
            return worldMaps.get(worldMapKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Tileset getTileset(Key<Tileset> tilesetKey) {
        try {
            return tilesets.get(tilesetKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Tileset transformTileset(TilesetData tilesetData) {
        ImmutableMap.Builder<Integer, TerrainType> builder = ImmutableMap.builder();

        tilesetData.getTiles().entrySet().forEach(tileTerrainEntry -> {
            int tile = Integer.parseInt(tileTerrainEntry.getKey());
            ImmutableSet<Integer> terrains = ImmutableSet.copyOf(tileTerrainEntry.getValue().getTerrain());
            TerrainType terrainType = resolveTerrainType(tilesetData.getTerrains(), terrains);
            builder.put(tile, terrainType);
        });

        return new Tileset(
                new Key<>(tilesetData.getName()),
                builder.build()
        );
    }

    private TerrainType resolveTerrainType(List<Terrain> terrainTypes, Set<Integer> terrains) {
        if (terrains.size() == 1) {
            Integer terrain = terrains.iterator().next();
            String terrainName = terrainTypes.get(terrain).getName();
            if (terrainName.equals("water")) {
                return TerrainType.WATER;
            }
            if (terrainName.equals("grass")) {
                return TerrainType.GRASS;
            }
            if (terrainName.equals("soil")) {
                return TerrainType.SOIL;
            }
        }
        if (terrains.size() == 2) {
            Iterator<Integer> terrainIterator = terrains.iterator();
            String terrainOne = terrainTypes.get(terrainIterator.next()).getName();
            String terrainTwo = terrainTypes.get(terrainIterator.next()).getName();
            if ((terrainOne.equals("water") && terrainTwo.equals("grass")) ||
                    (terrainOne.equals("grass") && terrainTwo.equals("water"))) {
                return TerrainType.WATER_GRASS;
            }
        }
        return TerrainType.OTHER;
    }

    private WorldMap transformMap(WorldMapData worldMapData) {
        TilesetRef tilesetRef = worldMapData.getTilesets().get(0);
        String tilesetName = removeExtension(tilesetRef.getSource());
        Point spawnPoint = new Point(worldMapData.getProperties().getSpawnPointX(), worldMapData.getProperties().getSpawnPointY());

        Layer groundLayer = worldMapData.getLayers().get(0);

        return new WorldMap(
                worldMapData.getWidth(),
                worldMapData.getHeight(),
                new Key<>(tilesetName),
                spawnPoint,
                groundLayer.getData(),
                tilesetRef.getFirstgid()
        );
    }
}
