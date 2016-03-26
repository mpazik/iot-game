package dzida.server.app.store.mapdb;

import dzida.server.app.Serializer;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;

public class WorldObjectStoreMapDbFactory {
    private final DB db;
    private final Serializer serializer;

    public WorldObjectStoreMapDbFactory(Serializer serializer) {
        this.serializer = serializer;
        this.db = DBMaker.fileDB(new File("worldObjectsDB"))
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();
    }

    public WorldObjectStoreMapDb createForInstnace(String instanceKey) {
        ConcurrentNavigableMap<Long, String> table = db.treeMap(instanceKey, BTreeKeySerializer.LONG, org.mapdb.Serializer.STRING);
        return new WorldObjectStoreMapDb(serializer, table);
    }
}
