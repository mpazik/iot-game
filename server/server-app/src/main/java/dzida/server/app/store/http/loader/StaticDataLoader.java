package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dzida.server.app.Configuration;
import dzida.server.app.Serializer;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class StaticDataLoader {
    private final Serializer serializer;

    public StaticDataLoader(Serializer serializer) {
        this.serializer = serializer;
    }

    public <T> T loadJsonFromServer(String path, Class<T> clazz) {
        return loadJsonFromServer(path, TypeToken.of(clazz));
    }

    public <T> T loadJsonFromServer(String path, TypeToken<T> typeToken) {
        long startTime = System.currentTimeMillis();
        try {
            URL url = UriBuilder.fromUri(Configuration.getStaticServerAddress()).path(path).build().toURL();
            System.out.printf("Downloading json file from %s \n", url);

            URLConnection urlConnection = url.openConnection();
            String contentEncoding = urlConnection.getContentEncoding();
            JsonReader jsonReader = new JsonReader(requestInputStream(url.openStream(), contentEncoding));
            T data = serializer.fromJson(jsonReader, typeToken.getType());

            long loadTime = System.currentTimeMillis() - startTime;
            System.out.printf("File %s downloaded in %dms \n", path, loadTime);

            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStreamReader requestInputStream(InputStream inputStream, String contentEncoding) {
        if ("gzip".equals(contentEncoding)) {
            try {
                return new InputStreamReader(new GZIPInputStream(inputStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new InputStreamReader(inputStream);
    }
}
