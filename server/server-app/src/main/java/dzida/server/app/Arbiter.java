package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class Arbiter {

    private final Container container;

    public Arbiter(Container container) {
        this.container = container;
    }

    public void startInstance(String instanceType, Container.StartInstanceCallback startInstanceCallback, Integer difficultyLevel) {
        container.startInstance(instanceType, startInstanceCallback, difficultyLevel);
    }

    public URI getHomeInstnceAddress() {
        return UriBuilder.fromUri(Configuration.getContainerAddress()).port(Configuration.getFirstInstancePort()).build();
    }
}
