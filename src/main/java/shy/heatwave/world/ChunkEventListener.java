package shy.heatwave.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ChunkEventListener {
    void onBlockUpdate(BlockPos pos, World world, BlockState newState);
}
