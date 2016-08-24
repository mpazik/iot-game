package dzida.server.app;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;

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

    public static int getGameServerPort() {
        return Integer.parseInt(System.getProperty("gameServerPort", "7001"));
    }

    public static String getLoginTokenSecret() {
        return System.getProperty("loginTokenKey", "login token secret");
    }

    public static String getReissueTokenSecret() {
        return System.getProperty("loginTokenKey", "reissue token secret");
    }

    /**
     * Return the number of millis that will be added to the Server current time. Required for debuging reasons to simulate client/server time differences.
     */
    public static long getServerTimeOffset() {
        return Long.parseLong(System.getProperty("serverTimeOffset", "0"));
    }

    public static String[] getInitialInstances() {
        return System.getProperty("instances", "eden").split(",");
    }

    public static boolean isDevMode() {
        return "true".equals(System.getProperty("devMode"));
    }

    public static String databaseUrl() {
        return getProperty("database.host");
    }

    public static String databaseName() {
        return getProperty("database.name");
    }

    public static String databaseUser() {
        return getProperty("database.user");
    }

    public static String databasePassword() {
        return getProperty("database.password");
    }

    private static String getProperty(String key) {
        String property = System.getProperty(key);
        if (property == null) {
            throw new NullPointerException("Property for key: " + key + " is not set. Start app with 'maven:exec' plugin");
        }
        return property;
    }

    public static void print() {
        System.out.println("Configuration listing");
        System.out.println("---------------------");
        System.out.println("dev mode: " + isDevMode());
        System.out.println("container host: " + getContainerHost());
        System.out.println("container rest port: " + getContainerRestPort());
        System.out.println("container first instance port: " + getGameServerPort());
        System.out.println("static server instanceKey: " + getStaticServerAddress());
        //noinspection ConfusingArgumentToVarargsMethod
        System.out.println("initial instances: " + String.join(",", getInitialInstances()));
        System.out.println("server time offset " + getServerTimeOffset());

        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (!key.startsWith("database")) {
                continue;
            }
            String value = (String) p.get(key);
            System.out.println(key + ": " + value);
        }

        System.out.println("---------------------\n");
    }
}
