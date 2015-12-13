package dzida.server.app;

public class Arbiter {

    private final Container container;

    public Arbiter(Container container) {
        this.container = container;
    }

    void startInstance(String instanceType, Container.StartInstanceCallback startInstanceCallback, Integer difficultyLevel) {
        container.startInstance(instanceType, startInstanceCallback, difficultyLevel);
    }
}
