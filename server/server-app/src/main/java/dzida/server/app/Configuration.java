package dzida.server.app;

import org.apache.log4j.Logger;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;

public class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class);

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
            throw new NullPointerException("Property for key: " + key + " is not set. Look at mvn:exec configuration to see what properties are expected.");
        }
        return property;
    }

    public static void print() {
        log.info("Configuration listing");
        log.info("---------------------");
        log.info("dev mode: " + isDevMode());
        log.info("container host: " + getContainerHost());
        log.info("container rest port: " + getContainerRestPort());
        log.info("container first instance port: " + getGameServerPort());
        log.info("static server instanceKey: " + getStaticServerAddress());
        //noinspection ConfusingArgumentToVarargsMethod
        log.info("initial instances: " + String.join(",", getInitialInstances()));
        log.info("server time offset " + getServerTimeOffset());

        Properties p = System.getProperties();
        Enumeration keys = p.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (!key.startsWith("database")) {
                continue;
            }
            String value = (String) p.get(key);
            log.info(key + ": " + value);
        }

        log.info("---------------------\n");
    }
}
