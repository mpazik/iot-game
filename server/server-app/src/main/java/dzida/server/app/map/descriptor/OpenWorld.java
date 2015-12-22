package dzida.server.app.map.descriptor;

public class OpenWorld implements Scenario {
    private final String type;
    private final String mapName;

    public OpenWorld(String mapName) {
        this.mapName = mapName;
        this.type = "open-world";
    }

    @Override
    public String getMapName() {
        return mapName;
    }

    @Override
    public String getType() {
        return type;
    }
}
