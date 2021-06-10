package shy.heatwave.item;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import shy.heatwave.HeatwaveIdentifier;

import java.util.ArrayList;

public final class HeatwaveItems {
    private HeatwaveItems() {}

    public static final ArrayList<Item> ITEMS = new ArrayList<>();


    public static void registerItems() {

    }

    private static void registerItem(String name, Item item) {
        final HeatwaveIdentifier ident = new HeatwaveIdentifier(name);
        Registry.register(Registry.ITEM, ident, item);
        ITEMS.add(item);
    }
}
