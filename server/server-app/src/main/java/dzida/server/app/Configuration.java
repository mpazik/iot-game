package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

class Configuration {
    public static String getContainerHost() {
        return System.getProperty("containerAddress", "localhost");
    }

    public static URI getContainerWsAddress() {
        return UriBuilder.fromPath("").host(getContainerHost()).scheme("ws").build();
    }

    public static int getContainerRestPort() {
        return Integer.parseInt(System.getProperty("restPort", "7000"));
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
