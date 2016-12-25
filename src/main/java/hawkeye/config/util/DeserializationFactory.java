package hawkeye.config.util;

import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;

import java.io.*;
import java.util.Collection;

@AllArgsConstructor
public abstract class DeserializationFactory<T> {
    private GsonSingleton gson;
    private Class<T> typeClass;

    public Collection<T> createCollectionFromFile(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        File file = new File(filename);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        return createCollectionFromInputStream(is);
    }

    public Collection<T> createCollectionFromString(String string) throws UnsupportedEncodingException {
        return createCollectionFromInputStream(new ByteArrayInputStream(string.getBytes()));
    }

    private Collection<T> createCollectionFromInputStream(InputStream is) throws UnsupportedEncodingException {
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        return gson.getGson().fromJson(reader, new ListOfJson<>(typeClass));
    }
}
