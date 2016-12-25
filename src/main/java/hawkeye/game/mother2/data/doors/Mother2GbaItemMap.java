package hawkeye.game.mother2.data.doors;

import com.google.inject.Inject;
import hawkeye.config.util.DeserializationFactory;
import hawkeye.config.util.GsonSingleton;
import lombok.NonNull;
import lombok.Value;

import java.util.*;

public class Mother2GbaItemMap {
    private static final String ITEM_MAP_FILENAME = "config/mother2/sfc-gba-item-map.json";

    @Value
    private static class ItemMapEntry {
        @NonNull private int sfc;
        @NonNull private int gba;
    }

    public class ItemMapDeserializationFactory extends DeserializationFactory<ItemMapEntry> {
        public ItemMapDeserializationFactory(GsonSingleton gson) {
            super(gson, ItemMapEntry.class);
        }
    }

    private final Map<Integer, Integer> itemMap;

    @Inject
    public Mother2GbaItemMap(GsonSingleton gson) throws Exception {
        ItemMapDeserializationFactory deserializationFactory = new ItemMapDeserializationFactory(gson);
        Collection<ItemMapEntry> itemMapList = deserializationFactory.createCollectionFromFile(ITEM_MAP_FILENAME);

        Map<Integer, Integer> itemMap = new HashMap<>();
        for (ItemMapEntry itemMapEntry : itemMapList) {
            itemMap.put(itemMapEntry.getSfc(), itemMapEntry.getGba());
        }
        this.itemMap = Collections.unmodifiableMap(itemMap);
    }

    public Optional<Integer> getGbaItemNumber(int sfcItemNumber) {
        Integer gbaItemNumber = itemMap.get(sfcItemNumber);
        return Optional.ofNullable(gbaItemNumber);
    }
}
