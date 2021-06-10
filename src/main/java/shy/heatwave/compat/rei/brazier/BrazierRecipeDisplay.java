package shy.heatwave.compat.rei.brazier;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import shy.heatwave.recipe.BrazierRecipe;

import java.util.Collections;
import java.util.List;

public class BrazierRecipeDisplay implements Display {
    final BrazierRecipe recipe;

    public BrazierRecipeDisplay(BrazierRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return Collections.singletonList(EntryIngredients.ofIngredient(recipe.getInput()));
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(EntryIngredients.of(recipe.getOutput()));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return BrazierRecipeCategory.CATEGORY;
    }
}
