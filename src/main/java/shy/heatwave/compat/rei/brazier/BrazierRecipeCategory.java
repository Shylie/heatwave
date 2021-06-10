package shy.heatwave.compat.rei.brazier;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Button;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import shy.heatwave.HeatwaveIdentifier;
import shy.heatwave.block.HeatwaveBlocks;
import shy.heatwave.recipe.BrazierRecipe;
import shy.heatwave.world.BrazierShapeMatcher;
import shy.heatwave.world.ChunkEventListeners;
import shy.heatwave.world.WorldRenderListeners;

import java.util.ArrayList;
import java.util.List;

public class BrazierRecipeCategory implements DisplayCategory<BrazierRecipeDisplay> {
    private BrazierRecipeCategory() { }

    public static final BrazierRecipeCategory INSTANCE = new BrazierRecipeCategory();

    public static final CategoryIdentifier<? extends BrazierRecipeDisplay> CATEGORY = CategoryIdentifier.of(new HeatwaveIdentifier(BrazierRecipe.Type.ID));

    @Override
    public @NotNull List<Widget> setupDisplay(BrazierRecipeDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 45, bounds.y + 6);

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 26, startPoint.y + 11)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x - 23, startPoint.y + 20))
                .entries(CategoryRegistry.getInstance().get(CATEGORY).getWorkstations().get(0)));
        widgets.add(Widgets.createArrow(new Point(startPoint.x - 4, startPoint.y + 10)));
        widgets.add(Widgets.createSlot(new Point(startPoint.x + 26, startPoint.y + 11))
                .entries(display.getOutputEntries().get(0))
                .disableBackground()
                .markOutput());
        widgets.add(Widgets.createSlot(new Point(startPoint.x - 23, startPoint.y + 1))
                .entries(display.getInputEntries().get(0))
                .markInput());
        final Button button = Widgets.createButton(
                new Rectangle(startPoint.x + 53, startPoint.y, 60, 20),
                new TranslatableText("heatwave.rei.brazier.show_in_world"));
        button.onClick((Button b) -> {
            final MinecraftClient client = MinecraftClient.getInstance();
            final HitResult result = client.crosshairTarget;
            final BlockPos center = result.getType() == HitResult.Type.BLOCK ? ((BlockHitResult)result).getBlockPos() : client.player.getBlockPos();
            final BrazierShapeMatcher matcher = new BrazierShapeMatcher(center, display.recipe, client.world);

            ChunkEventListeners.removeAllListeners(BrazierShapeMatcher.class);
            WorldRenderListeners.removeAllListeners(BrazierShapeMatcher.class);

            ChunkEventListeners.addListener(client.world, matcher);
            WorldRenderListeners.addListener(matcher);

            matcher.removeIfNeeded(client.world);
        });
        widgets.add(button);
        return widgets;
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(HeatwaveBlocks.BRAZIER);
    }

    @Override
    public Text getTitle() {
        return new TranslatableText("heatwave.rei.brazier.title");
    }

    @Override
    public int getDisplayHeight() {
        return 49;
    }

    @Override
    public CategoryIdentifier<? extends BrazierRecipeDisplay> getCategoryIdentifier() {
        return CATEGORY;
    }
}
