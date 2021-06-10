package shy.heatwave.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import shy.heatwave.HeatwaveIdentifier;
import shy.heatwave.item.HeatwaveItems;

public class HeatwaveBlock extends Block {
    public HeatwaveBlock(Settings settings) {
        super(settings);
    }

    public void register(String name) {
        registerBlock(name);
    }

    protected final void registerBlock(String name) {
        final HeatwaveIdentifier ident = new HeatwaveIdentifier(name);
        Registry.register(Registry.BLOCK, ident, this);
        final BlockItem item = new BlockItem(this, new FabricItemSettings().group(ItemGroup.MISC));
        Registry.register(Registry.ITEM, ident, item);
        HeatwaveItems.ITEMS.add(item);
        HeatwaveBlocks.BLOCKS.add(this);
    }
}
