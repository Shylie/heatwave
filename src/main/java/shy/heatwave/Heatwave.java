package shy.heatwave;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shy.heatwave.block.HeatwaveBlocks;
import shy.heatwave.item.HeatwaveItems;
import shy.heatwave.recipe.BrazierRecipe;

public class Heatwave implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "heatwave";
    public static final String MOD_NAME = "Heatwave";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");

        HeatwaveItems.registerItems();
        HeatwaveBlocks.registerBlocks();

        Registry.register(Registry.RECIPE_SERIALIZER, BrazierRecipe.Serializer.ID, BrazierRecipe.Serializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, BrazierRecipe.Type.ID, BrazierRecipe.Type.INSTANCE);
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }
}