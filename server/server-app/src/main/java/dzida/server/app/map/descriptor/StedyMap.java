package dzida.server.app.map.descriptor;

public class StedyMap implements MapDescriptor {
    private final String mapName;

    public StedyMap(String mapName) {
        this.mapName = mapName;
    }

    @Override
    public String getMapName() {
        return mapName;
    }
}
