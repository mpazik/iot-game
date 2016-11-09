package dzida.server.app.store.http;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.app.world.map.Layer;
import dzida.server.app.world.map.TailObjectProperty;
import dzida.server.app.world.map.Terrain;
import dzida.server.app.world.map.TilesetData;
import dzida.server.app.world.map.TilesetRef;
import dzida.server.app.world.map.WorldMapData;
import dzida.server.core.basic.entity.Id;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.basic.unit.Point;
import dzida.server.core.time.TimeService;
import dzida.server.core.world.map.Tileset;
import dzida.server.core.world.map.Tileset.TerrainType;
import dzida.server.core.world.map.WorldMap;
import dzida.server.core.world.map.WorldMapStore;
import dzida.server.core.world.object.WorldObject;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class WorldMapStoreHttp implements WorldMapStore {
    private final WorldMapLoader worldMapLoader;
    private final TimeService timeService;

    private final LoadingCache<Key<WorldMap>, WorldMapData> worldMaps = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<Key<WorldMap>, WorldMapData>() {
                public WorldMapData load(@Nonnull Key<WorldMap> key) {
                    return worldMapLoader.loadMap(key);
                }
            });

    private final LoadingCache<Key<Tileset>, TilesetData> tilesets = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<Key<Tileset>, TilesetData>() {
                public TilesetData load(@Nonnull Key<Tileset> key) {
                    return worldMapLoader.loadTileset(key);
                }
            });

    public WorldMapStoreHttp(WorldMapLoader worldMapLoader, TimeService timeService) {
        this.worldMapLoader = worldMapLoader;
        this.timeService = timeService;
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
            return transformMap(worldMaps.get(worldMapKey));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<WorldObject> getInitialMapObjects(Key<WorldMap> worldMapKey) {
        try {
            return getObjects(worldMaps.get(worldMapKey));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<WorldObject> getObjects(WorldMapData worldMapData) throws ExecutionException {
        List<Layer> layers = worldMapData.getLayers();
        ImmutableList.Builder<WorldObject> builder = ImmutableList.builder();
        if (layers.size() != 2 || worldMapData.getTilesets().size() != 2) {
            return builder.build();
        }
        Layer objectLayer = layers.get(1);
        TilesetRef tilesetRef = worldMapData.getTilesets().get(1);
        String tilesetName = removeExtension(tilesetRef.getSource());
        TilesetData objectTileset = tilesets.get(new Key<>(tilesetName));
        int[] objectData = objectLayer.getData();
        for (int y = 0; y < objectLayer.getHeight(); y++) {
            for (int x = 0; x < objectLayer.getWidth(); x++) {
                int tileObjectId = objectData[y * objectLayer.getHeight() + x];
                if (tileObjectId == 0) {
                    continue;
                }
                TailObjectProperty tailObjectProperty = objectTileset.getTileproperties().get(Integer.toString(tileObjectId - tilesetRef.getFirstgid()));
                int objectKind = Integer.parseInt(tailObjectProperty.getObjectId());
                builder.add(new WorldObject(new Id<>(objectKind), x, y, timeService.getCurrentTime()));
            }
        }
        return builder.build();
    }

    @Override
    public Tileset getTileset(Key<Tileset> tilesetKey) {
        try {
            return transformTileset(tilesets.get(tilesetKey));
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
