package shy.heatwave.recipe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import shy.heatwave.block.HeatBlock;
import shy.heatwave.HeatwaveIdentifier;
import shy.heatwave.block.brazier.BrazierBlock;
import shy.heatwave.net.PacketUtils;

import java.util.Optional;

public class BrazierRecipe implements Recipe<BrazierRecipe.LocationInventory> {
    public static class LocationInventory extends SimpleInventory {
        public final BlockPos pos;

        public LocationInventory(ItemStack stack, BlockPos pos) {
            super(stack);
            this.pos = pos;
        }
    }

    public static class Offset {
        public final BlockPos offset;
        public final BrazierBlock[] brazierTypes;

        public Offset(BlockPos offset, BrazierBlock[] brazierTypes) {
            this.offset = offset;
            this.brazierTypes = brazierTypes;
        }
    }

    private final Ingredient input;
    private final ItemStack output;
    private final Identifier id;
    private final Offset[][] offsets;

    public BrazierRecipe(Ingredient input, ItemStack output, Offset[] offsets, Identifier id) {
        this.input = input;
        this.output = output;
        this.offsets = new Offset[4][];
        this.offsets[0] = offsets;
        for (int i = 1; i < this.offsets.length; i++) {
            this.offsets[i] = rotate90(this.offsets[i - 1]);
        }
        this.id = id;
    }

    public BrazierRecipe(Ingredient input, ItemStack output, Offset[][] offsets, Identifier id) {
        this.input = input;
        this.output = output;
        this.offsets = offsets;
        this.id = id;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(LocationInventory inv, World world) {
        for (Offset[] _offsets : offsets) {
            boolean skip = false;
            for (Offset offset : _offsets) {
                if (!validateBrazier(world.getBlockState(inv.pos.add(offset.offset)), offset.brazierTypes)) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                return input.test(inv.getStack(0));
            }
        }

        return false;
    }

    @Override
    public ItemStack craft(LocationInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    public Ingredient getInput() {
        return input;
    }

    public Offset[][] getOffsets() {
        return offsets;
    }

    public Offset[] getMatchingOffsets(World world, BlockPos pos) {
        for (Offset[] _offsets : offsets) {
            boolean skip = false;
            for (Offset offset : _offsets) {
                if (!validateBrazier(world.getBlockState(pos.add(offset.offset)), offset.brazierTypes)) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                return _offsets;
            }
        }

        return null;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<BrazierRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();

        public static final String ID = "brazier";
    }

    public static class Serializer implements RecipeSerializer<BrazierRecipe> {
        private Serializer() {}

        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID = new HeatwaveIdentifier(Type.ID);

        public static class BrazierOffsetJsonFormat {
            int[] offset;
            JsonObject required;

            public boolean validate() {
                if (offset == null || offset.length != 3) {
                    throw new JsonSyntaxException("Position array length must be 3");
                }

                if (required == null) {
                    throw new JsonSyntaxException("Missing 'required' attribute");
                }

                final Ingredient ingredient = Ingredient.fromJson(required);
                final IntList list = ingredient.getMatchingItemIds();
                for (int i = 0; i < list.size(); i++) {
                    final Item item = Registry.ITEM.get(list.getInt(i));
                    if (!(item instanceof BlockItem)) {
                        throw new JsonSyntaxException("Item " + item + " not a BlockItem");
                    }
                    final Block block = ((BlockItem)item).getBlock();
                    if (!(block instanceof BrazierBlock)) {
                        throw new JsonSyntaxException("Block " + required + " is not a valid brazier block");
                    }
                }

                return offset[0] == 0 && offset[1] == 0 && offset[2] == 0;
            }

            public Offset toOffset() {
                final Ingredient ingredient = Ingredient.fromJson(required);
                final IntList list = ingredient.getMatchingItemIds();
                final BrazierBlock[] blocks = new BrazierBlock[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    final BlockItem item = (BlockItem)Registry.ITEM.get(list.getInt(i));
                    blocks[i] = (BrazierBlock)item.getBlock();
                }
                return new Offset(new BlockPos(offset[0], offset[1], offset[2]), blocks);
            }
        }

        public static class BrazierRecipeJsonFormat {
            JsonObject input;
            String output;
            BrazierOffsetJsonFormat[] offsets;

            public void validate() {
                if (input == null) {
                    throw new JsonSyntaxException("Missing input attribute");
                }

                if (output == null) {
                    throw new JsonSyntaxException("Missing output attribute");
                }

                Registry.ITEM.getOrEmpty(new Identifier(output))
                        .orElseThrow(() -> new JsonSyntaxException("No such item " + output));

                if (offsets == null || offsets.length == 0) {
                    throw new JsonSyntaxException("Missing offsets attribute");
                }

                boolean found = false;
                for (BrazierOffsetJsonFormat jsonOffset : offsets) {
                    if (jsonOffset.validate()) {
                        found = true;
                    }
                }

                if (!found) {
                    throw new JsonSyntaxException("Must have an offset at [0, 0, 0]");
                }
            }
        }

        @Override
        public BrazierRecipe read(Identifier id, JsonObject json) {
            BrazierRecipeJsonFormat recipeJson = new Gson().fromJson(json, BrazierRecipeJsonFormat.class);

            recipeJson.validate();

            final Ingredient input = Ingredient.fromJson(recipeJson.input);
            final ItemStack output = new ItemStack(Registry.ITEM.get(new Identifier(recipeJson.output)), 1);
            final Offset[] offsets = new Offset[recipeJson.offsets.length];

            for (int i = 0; i < recipeJson.offsets.length; i++) {
                offsets[i] = recipeJson.offsets[i].toOffset();
            }

            return new BrazierRecipe(input, output, offsets, id);
        }

        @Override
        public void write(PacketByteBuf buf, BrazierRecipe recipe) {
            recipe.getInput().write(buf);
            buf.writeItemStack(recipe.getOutput());
            final Offset[][] offsets = recipe.getOffsets();
            PacketUtils.writeArray(buf, offsets, (buf1, offsets1) -> PacketUtils.writeArray(buf1, offsets1, (buf2, offset) -> {
                buf2.writeBlockPos(offset.offset);
                PacketUtils.writeArray(buf2, offset.brazierTypes, (buf3, block) -> buf3.writeIdentifier(Registry.BLOCK.getId(block)));
            }));
        }

        @Override
        public BrazierRecipe read(Identifier id, PacketByteBuf buf) {
            Ingredient input = Ingredient.fromPacket(buf);
            ItemStack output = buf.readItemStack();
            Offset[][] offsets = PacketUtils.readArray(buf, Offset[][]::new,
                    buf1 -> PacketUtils.readArray(buf1, Offset[]::new, buf2 -> {
                final BlockPos pos = buf2.readBlockPos();
                final BrazierBlock[] required = PacketUtils.readArray(buf2, BrazierBlock[]::new,
                        buf3 -> (BrazierBlock)Registry.BLOCK.get(buf3.readIdentifier()));
                return new Offset(pos, required);
            }));

            return new BrazierRecipe(input, output, offsets, id);
        }
    }

    public Optional<Offset> getMatchingOffset(BlockPos offset, int rotation) {
        for (Offset testOffset : offsets[rotation]) {
            if (testOffset.offset.equals(offset)) {
                return Optional.of(testOffset);
            }
        }
        return Optional.empty();
    }

    public boolean validateOffset(BlockPos offset, int rotation, BlockState test) {
        for (Offset testOffset : offsets[rotation]) {
            if (testOffset.offset.equals(offset)) {
                return validateBrazier(test, testOffset.brazierTypes);
            }
        }
        return true;
    }

    private static boolean validateBrazier(BlockState state, BrazierBlock[] required) {
        for (BrazierBlock block : required) {
            if (state.getBlock() == block && state.get(HeatBlock.HEAT) > 0) {
                return true;
            }
        }

        return false;
    }

    private static Offset[] rotate90(Offset[] offsets) {
        Offset[] ret = new Offset[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            ret[i] = rotate90(offsets[i]);
        }
        return ret;
    }

    private static Offset rotate90(Offset offset) {
        return new Offset(new BlockPos(offset.offset.getZ(), offset.offset.getY(), -offset.offset.getX()), offset.brazierTypes);
    }
}
