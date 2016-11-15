package dzida.server.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;

public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static String getContainerHost() {
        return getProperty("containerHost", "localhost");
    }

    public static URI getContainerWsAddress() {
        return UriBuilder.fromPath("").host(getContainerHost()).scheme("ws").build();
    }

    public static URI getStaticServerAddress() {
        return URI.create(getProperty("assetsAddress", "http://localhost:8080/dev/lib/dzida-assets"));
    }

    public static int getContainerRestPort() {
        return Integer.parseInt(getProperty("restPort", "7000"));
    }

    public static int getGameServerPort() {
        return Integer.parseInt(getProperty("gameServerPort", "7001"));
    }

    public static String getLoginTokenSecret() {
        return getProperty("loginTokenKey", "login token secret");
    }

    public static String getReissueTokenSecret() {
        return getProperty("loginTokenKey", "reissue token secret");
    }

    /**
     * Return the number of millis that will be added to the Server current time. Required for debuging reasons to simulate client/server time differences.
     */
    public static long getServerTimeOffset() {
        return Long.parseLong(getProperty("serverTimeOffset", "0"));
    }

    public static String[] getInitialInstances() {
        return getProperty("instances", "archipelago,cave").split(",");
    }

    public static boolean isDevMode() {
        return "true".equals(getProperty("devMode"));
    }

    public static String databaseUrl() {
        return getProperty("database.host", "localhost:5432");
    }

    public static String databaseName() {
        return getProperty("database.name", "test_db");
    }

    public static String databaseUser() {
        return getProperty("database.user", "test_user");
    }

    public static String databasePassword() {
        return getProperty("database.password", "test_password");
    }

    private static String getProperty(String key) {
        String property = System.getProperty(key);
        if (property == null) {
            throw new NullPointerException("Property for key: " + key + " is not set. Look at mvn:exec configuration to see what properties are expected.");
        }
        return property;
    }

    public static String getProperty(String serverTimeOffset, String def) {
        return System.getProperty(serverTimeOffset, def);
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
