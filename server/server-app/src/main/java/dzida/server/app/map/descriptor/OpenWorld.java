package dzida.server.app.map.descriptor;

public class OpenWorld implements Scenario {
    private final String mapName;

    public OpenWorld(String mapName) {
        this.mapName = mapName;
    }

    @Override
    public String getMapName() {
        return mapName;
    }
}
