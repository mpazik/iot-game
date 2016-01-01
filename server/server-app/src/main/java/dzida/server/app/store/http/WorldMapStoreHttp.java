package dzida.server.app.store.http;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dzida.server.app.store.http.loader.WorldMapLoader;
import dzida.server.core.basic.entity.Key;
import dzida.server.core.world.WorldMapStore;
import dzida.server.core.world.model.WorldMap;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WorldMapStoreHttp implements WorldMapStore {
    private final WorldMapLoader worldMapLoader;

    private final LoadingCache<Key<WorldMap>, WorldMap> worldMaps = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build(
                    new CacheLoader<Key<WorldMap>, WorldMap>() {
                        public WorldMap load(@Nonnull Key<WorldMap> key) {
                            return worldMapLoader.loadMap(key);
                        }
                    });


    public WorldMapStoreHttp(WorldMapLoader worldMapLoader) {
        this.worldMapLoader = worldMapLoader;
    }

    @Override
    public WorldMap getMap(Key<WorldMap> worldMapKey) {
        try {
            return worldMaps.get(worldMapKey);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
