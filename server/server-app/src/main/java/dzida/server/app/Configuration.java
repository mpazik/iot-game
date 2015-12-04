package dzida.server.app;

import java.net.URI;

class Configuration {
    static URI getContainerAddress() {
        return URI.create(System.getProperty("containerAddress", "ws://localhost"));
    }

    static int getFirstInstancePort() {
        return Integer.parseInt(System.getProperty("instancePort", "7001"));
    }

    static String[] getInitialInstances() {
        return System.getProperty("instances", "eden").split(",");
    }
}
