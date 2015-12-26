package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class Arbiter {

    private final Container container;

    public Arbiter(Container container) {
        this.container = container;
    }

    public void startInstance(String instanceKey, String instanceType, Container.StartInstanceCallback startInstanceCallback, Integer difficultyLevel) {
        container.startInstance(instanceKey, instanceType, startInstanceCallback, difficultyLevel);
    }

    public void killInstance(String instanceKey) {
        container.killInstance(instanceKey);
    }

    public URI getHomeInstnceAddress() {
        return UriBuilder.fromUri(Configuration.getContainerWsAddress()).port(Configuration.getFirstInstancePort()).build();
    }
}
