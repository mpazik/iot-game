package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

class Configuration {
    public static String getContainerHost() {
        return System.getProperty("containerHost", "localhost");
    }

    public static URI getContainerWsAddress() {
        return UriBuilder.fromPath("").host(getContainerHost()).scheme("ws").build();
    }

    public static URI getStaticServerAddress() {
        return URI.create(System.getProperty("containerHost", "http://localhost:8080"));
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
