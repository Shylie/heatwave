package shy.heatwave;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import shy.heatwave.block.HeatwaveBlocks;
import shy.heatwave.world.BrazierShapeMatcher;
import shy.heatwave.world.ChunkEventListeners;
import shy.heatwave.world.WorldRenderListeners;

public class HeatwaveClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(HeatwaveBlocks.BRAZIER, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(HeatwaveBlocks.SOUL_BRAZIER, RenderLayer.getCutout());

        WorldRenderListeners.init();

        ClientPlayConnectionEvents.DISCONNECT.register(this::removeBrazierShapeMatchers);
    }

    private void removeBrazierShapeMatchers(ClientPlayNetworkHandler handler, MinecraftClient client) {
        ChunkEventListeners.removeAllListeners(BrazierShapeMatcher.class);
        WorldRenderListeners.removeAllListeners(BrazierShapeMatcher.class);
    }
}
