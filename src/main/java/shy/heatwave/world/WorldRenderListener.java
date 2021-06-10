package shy.heatwave.world;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface WorldRenderListener {
    void onWorldRenderStart(WorldRenderContext wrc);
    void onWorldRenderEnd(WorldRenderContext wrc);
}
