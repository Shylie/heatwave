package shy.heatwave.block;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import shy.heatwave.block.brazier.BrazierBlock;

import java.util.ArrayList;

public final class HeatwaveBlocks {
    private HeatwaveBlocks() {}

    public static final ArrayList<Block> BLOCKS = new ArrayList<>();
    public static final BrazierBlock BRAZIER = new BrazierBlock();
    public static final BrazierBlock SOUL_BRAZIER = new BrazierBlock();

    public static final ArrayList<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new ArrayList<>();

    public static void registerBlocks() {
        BRAZIER.register("brazier");
        SOUL_BRAZIER.register("soul_brazier");
    }
}
