package shy.heatwave.world;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkEventListeners {
    private static final Map<World, Set<ChunkEventListener>> LISTENERS = new HashMap<>();
    private static MinecraftServer server = null;

    public static void addListener(World world, ChunkEventListener listener) {
        if (LISTENERS.get(world) == null) {
            Set<ChunkEventListener> listeners = new HashSet<>();
            listeners.add(listener);
            LISTENERS.put(world, listeners);
        }
        else {
            LISTENERS.get(world).add(listener);
        }
    }

    public static void removeListener(World world, ChunkEventListener listener) {
        if (LISTENERS.get(world) != null) {
            LISTENERS.get(world).remove(listener);
            if (LISTENERS.get(world).size() == 0) {
                LISTENERS.remove(world);
            }
        }
    }

    public static void removeAllListeners() {
        LISTENERS.clear();
    }

    public static <T extends ChunkEventListener> void removeAllListeners(Class<T> clazz) {
        for (Map.Entry<World, Set<ChunkEventListener>> entry : LISTENERS.entrySet()) {
            entry.getValue().removeIf(clazz::isInstance);
        }
    }

    public static void onBlockStateChange(World world, BlockPos pos, BlockState newState) {
        Set<ChunkEventListener> cels = LISTENERS.get(world);
        if (cels != null) {
            for (ChunkEventListener cel : cels) {
                cel.onBlockUpdate(pos, world, newState);
            }
        }
    }
}
