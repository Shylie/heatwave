package shy.heatwave.world;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldRenderListeners {
    private static final Map<BlockPos, Pair<BlockState, Vec3d>> highlightQueue = new HashMap<>();
    private static final VertexConsumerProvider.Immediate immediate;

    private static final Set<WorldRenderListener> LISTENERS = new HashSet<>();

    public static void addListener(WorldRenderListener worldRenderListener) {
        LISTENERS.add(worldRenderListener);
    }

    public static void removeListener(WorldRenderListener listener) {
        LISTENERS.remove(listener);
    }

    public static void removeAllListeners() {
        LISTENERS.clear();
    }

    public static <T extends WorldRenderListener> void removeAllListeners(Class<T> clazz) {
        LISTENERS.removeIf(clazz::isInstance);
    }

    public static void init() {
        WorldRenderEvents.START.register(WorldRenderListeners::start);
        WorldRenderEvents.END.register(WorldRenderListeners::end);
    }

    public static void queueHighlight(BlockPos pos, BlockState state) {
        queueHighlight(pos, state, new Vec3d(0.5, 0.5, 0.5));
    }

    public static void queueHighlight(BlockPos pos, BlockState state, Vec3d scale) {
        highlightQueue.put(pos, new Pair<>(state, scale));
    }

    private static void start(WorldRenderContext wrc) {
        for (WorldRenderListener listener : LISTENERS) {
            listener.onWorldRenderStart(wrc);
        }
    }

    private static void end(WorldRenderContext wrc) {
        for (WorldRenderListener listener : LISTENERS) {
            listener.onWorldRenderEnd(wrc);
        }

        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        for (Map.Entry<BlockPos, Pair<BlockState, Vec3d>> entry : highlightQueue.entrySet()) {
            wrc.matrixStack().push();
            final Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

            final Pair<BlockState, Vec3d> pair = entry.getValue();

            final BlockPos pos = entry.getKey();

            final double x = pos.getX() - cameraPos.x;
            final double y = pos.getY() - cameraPos.y;
            final double z = pos.getZ() - cameraPos.z;
            wrc.matrixStack().translate(x + (1 - pair.getRight().x) / 2.0, y + (1 - pair.getRight().y) / 2.0, z + (1 - pair.getRight().z) / 2.0);
            wrc.matrixStack().scale((float)pair.getRight().x, (float)pair.getRight().y, (float)pair.getRight().z);

            MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
                    pair.getLeft(), wrc.matrixStack(), immediate, 0xF000F0, OverlayTexture.DEFAULT_UV);

            wrc.matrixStack().pop();
        }
        highlightQueue.clear();
        immediate.draw();
    }

    static {
        immediate = VertexConsumerProvider.immediate(new BufferBuilder(128));
    }
}
