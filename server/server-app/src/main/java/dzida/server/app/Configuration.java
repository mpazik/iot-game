package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class Configuration {
    public static String getContainerHost() {
        return System.getProperty("containerHost", "localhost");
    }

    public static URI getContainerWsAddress() {
        return UriBuilder.fromPath("").host(getContainerHost()).scheme("ws").build();
    }

    public static URI getStaticServerAddress() {
        return URI.create(System.getProperty("assetsAddress", "http://localhost:8080/dev/lib/dzida-assets"));
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

    public static void pirnt() {
        System.out.println("Configuration listing");
        System.out.println("---------------------");
        System.out.println("dev mode: " + isDevMode());
        System.out.println("container host: " + getContainerHost());
        System.out.println("container rest port: " + getContainerRestPort());
        System.out.println("container first instance port: " + getFirstInstancePort());
        System.out.println("static server address: " + getStaticServerAddress());
        System.out.println("initial instances: " + String.join(",", getInitialInstances()));
        System.out.println("---------------------\n");
    }
}
