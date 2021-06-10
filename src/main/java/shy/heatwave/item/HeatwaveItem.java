package shy.heatwave.item;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import shy.heatwave.HeatwaveIdentifier;

public class HeatwaveItem extends Item {
    public HeatwaveItem(Settings settings) {
        super(settings);
    }

    public void register(String name) {
        registerItem(name);
    }

    protected final void registerItem(String name) {
        final HeatwaveIdentifier ident = new HeatwaveIdentifier(name);
        Registry.register(Registry.ITEM, ident, this);
        HeatwaveItems.ITEMS.add(this);
    }
}
