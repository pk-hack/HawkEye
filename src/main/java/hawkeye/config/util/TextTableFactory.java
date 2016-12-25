package hawkeye.config.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import hawkeye.config.model.TextTable;
import lombok.AllArgsConstructor;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

@AllArgsConstructor(onConstructor = @_(@Inject))
public class TextTableFactory {
    private static final int MAXIMUM_SIZE = 256;

    private GsonSingleton gson;

    public TextTable createFromFile(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        File file = new File(filename);
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        return createFromInputStream(is);
    }

    private TextTable createFromInputStream(InputStream is) throws UnsupportedEncodingException {
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));

        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> characterStringMap = gson.getGson().fromJson(reader, type);

        String[] table = new String[MAXIMUM_SIZE];
        for (Map.Entry<String, String> entry : characterStringMap.entrySet()) {
            int key = Integer.parseInt(entry.getKey());
            table[key] = entry.getValue();
        }

        return new TextTable(table);
    }
}
