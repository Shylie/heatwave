package shy.heatwave.compat.rei.brazier;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import shy.heatwave.block.HeatwaveBlocks;
import shy.heatwave.recipe.BrazierRecipe;

@Environment(EnvType.CLIENT)
public class BrazierPlugin implements REIClientPlugin {
    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
        registry.registerNotSerializable(BrazierRecipeCategory.CATEGORY);
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(BrazierRecipeCategory.INSTANCE);
        registry.addWorkstations(BrazierRecipeCategory.CATEGORY,
                EntryIngredients.of(HeatwaveBlocks.BRAZIER), EntryIngredients.of(HeatwaveBlocks.SOUL_BRAZIER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(BrazierRecipe.class, BrazierRecipeDisplay::new);
    }
}
