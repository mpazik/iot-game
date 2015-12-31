package dzida.server.app.store.http.loader;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dzida.server.app.Configuration;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class StaticDataLoader {
    public <T> T loadJsonFromServer(String path, TypeToken<T> typeToken) {
        long startTime = System.currentTimeMillis();
        try {
            URL url = UriBuilder.fromUri(Configuration.getStaticServerAddress()).path(path).build().toURL();
            System.out.printf("Downloading json file from %s \n", url);

            URLConnection urlConnection = url.openConnection();
            String contentEncoding = urlConnection.getContentEncoding();
            JsonReader jsonReader = new JsonReader(requestInputStream(url.openStream(), contentEncoding));
            T data = new Gson().fromJson(jsonReader, typeToken.getType());

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
