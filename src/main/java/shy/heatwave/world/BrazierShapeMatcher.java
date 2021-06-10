package shy.heatwave.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import shy.heatwave.block.HeatBlock;
import shy.heatwave.recipe.BrazierRecipe;

import java.util.HashMap;
import java.util.Map;

public class BrazierShapeMatcher implements ChunkEventListener, WorldRenderListener {
    private final Map<BlockPos, Boolean> matched = new HashMap<>();

    private final BlockPos center;
    private final BrazierRecipe recipe;
    private int rotation;
    private boolean needsSetup;

    private int index;
    private float delta;

    public BrazierShapeMatcher(BlockPos center, BrazierRecipe recipe, World world) {
        this.center = center;
        this.recipe = recipe;
        rotation = 0;
        needsSetup = false;
        index = 0;
        delta = 0.0f;

        setupMatchMap(world);
    }

    @Override
    public void onBlockUpdate(BlockPos pos, World world, BlockState newState) {
        if (needsSetup) {
            setupMatchMap(world);
            needsSetup = false;
        }

        final BlockPos offset = pos.subtract(center);
        matched.put(offset, recipe.validateOffset(offset, rotation, newState));

        removeIfNeeded(world);
    }

    @Override
    public void onWorldRenderStart(WorldRenderContext wrc) {
        delta += wrc.tickDelta() * MinecraftClient.getInstance().getLastFrameDuration();
        if (delta >= 20.0f) {
            delta = 0.0f;
            index++;
        }

        matched.forEach((offset, matched) -> {
            if (!matched) {
                recipe.getMatchingOffset(offset, rotation).ifPresent(recipeOffset -> {
                    WorldRenderListeners.queueHighlight(center.add(offset),
                            recipeOffset.brazierTypes[index % recipeOffset.brazierTypes.length].getDefaultState().with(HeatBlock.HEAT, 3),
                            offset.equals(BlockPos.ORIGIN) ? new Vec3d(0.7, 0.7, 0.7) : new Vec3d(0.35, 0.35, 0.35));
                });
            }
        });
    }

    @Override
    public void onWorldRenderEnd(WorldRenderContext wrc) {
    }

    public boolean removeIfNeeded(World world) {
        for (boolean match : matched.values()) {
            if (!match) {
                return false;
            }
        }

        ChunkEventListeners.removeListener(world, this);
        WorldRenderListeners.removeListener(this);

        world.playSound(center.getX(), center.getY(), center.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.75f, 1, false);

        return true;
    }

    public void rotateLeft() {
        if (--rotation < 0) {
            rotation = 3;
        }
        needsSetup = true;
    }

    public void rotateRight() {
        if (++rotation > 3) {
            rotation = 0;
        }
        needsSetup = true;
    }

    private void setupMatchMap(World world) {
        matched.clear();
        for (BrazierRecipe.Offset offset : recipe.getOffsets()[rotation]) {
            final BlockPos testPos = BlockPos.ORIGIN.add(offset.offset);
            matched.put(testPos, recipe.validateOffset(testPos, rotation, world.getBlockState(center.add(offset.offset))));
        }
    }
}
