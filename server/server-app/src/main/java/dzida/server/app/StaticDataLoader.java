package dzida.server.app;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class StaticDataLoader {
    public <T> T loadJsonFromServer(String path, TypeToken<T> typeToken) {
        long startTime = System.currentTimeMillis();
        try {
            URL url = UriBuilder.fromUri(Configuration.getStaticServerAddress()).path("assets").path(path).build().toURL();
            System.out.printf("Downloading json file from %s \n", url);
            InputStream inputStream = url.openStream();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
            T data = new Gson().fromJson(jsonReader, typeToken.getType());

            long loadTime = System.currentTimeMillis() - startTime;
            System.out.printf("File %s downloaded in %dms \n", path, loadTime);

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}