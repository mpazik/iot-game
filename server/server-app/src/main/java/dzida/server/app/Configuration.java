package dzida.server.app;

import java.net.URI;

class Configuration {
    public static URI getContainerAddress() {
        return URI.create(System.getProperty("containerAddress", "ws://localhost"));
    }

    public static int getFirstInstancePort() {
        return Integer.parseInt(System.getProperty("instancePort", "7001"));
    }

    public static String[] getInitialInstances() {
        return System.getProperty("instances", "eden").split(",");
    }

    public static boolean isDevMode() {
        return "true".equals(System.getProperty("devMode"));
    }
}
