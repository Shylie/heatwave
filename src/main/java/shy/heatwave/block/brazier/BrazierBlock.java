package shy.heatwave.block.brazier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import shy.heatwave.block.HeatBlock;
import shy.heatwave.recipe.BrazierRecipe;

import java.util.Optional;
import java.util.Random;

public class BrazierBlock extends HeatBlock implements Waterloggable {
    public static final VoxelShape BOTTOM_SHAPE;
    public static final VoxelShape MIDDLE_SHAPE_1;
    public static final VoxelShape MIDDLE_SHAPE_2;
    public static final VoxelShape TOP_SHAPE;
    public static final VoxelShape BASE_SHAPE;
    public static final VoxelShape COLLISION_SHAPE;

    public BrazierBlock() {
        super(FabricBlockSettings.copy(Blocks.STONE).nonOpaque().luminance((state) -> state.get(HEAT) * 5));
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.WATERLOGGED);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (!(Boolean)state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, true).with(HEAT, 0), 3);
                world.getFluidTickScheduler().schedule(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return BASE_SHAPE;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER) {
            return getDefaultState().with(Properties.WATERLOGGED, true);
        }
        else if (ctx.getPlayer() != null && ctx.getPlayer().getOffHandStack().getItem() instanceof FlintAndSteelItem) {
            if (!ctx.getPlayer().getAbilities().creativeMode && ctx.getPlayer().getOffHandStack().isDamageable()) {
                ctx.getPlayer().getOffHandStack().damage(1, ctx.getPlayer(),
                        e -> e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND));
            }
            return getDefaultState().with(HEAT, 3);
        }
        else if (ctx.getPlayer() != null && ctx.getPlayer().getOffHandStack().getItem() instanceof FireChargeItem) {
            if (!ctx.getPlayer().getAbilities().creativeMode) {
                ctx.getPlayer().getOffHandStack().decrement(1);
            }
            return getDefaultState().with(HEAT, 3);
        }
        else {
            return getDefaultState().with(HEAT, 0);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BASE_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(HEAT) > 0 && player.getStackInHand(hand).getItem() instanceof ShovelItem) {
            if (!player.getAbilities().creativeMode && player.getStackInHand(hand).isDamageable()) {
                player.getStackInHand(hand).damage(1, player,
                        e -> e.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            }
            world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.0F);
            world.setBlockState(pos, state.with(HEAT, 0));
            return ActionResult.success(world.isClient);
        }
        if (state.get(HEAT) < 3 && player.getStackInHand(hand).getItem() instanceof FlintAndSteelItem) {
            if (!player.getAbilities().creativeMode && player.getStackInHand(hand).isDamageable()) {
                player.getStackInHand(hand).damage(1, player,
                        e -> e.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
            }
            world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            world.setBlockState(pos, state.with(HEAT, 3));
            return ActionResult.success(world.isClient);
        }
        if (state.get(HEAT) < 3 && player.getStackInHand(hand).getItem() instanceof FireChargeItem) {
            if (!player.getAbilities().creativeMode) {
                player.getStackInHand(hand).decrement(1);
            }
            world.playSound(player, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, 0.9F);
            world.setBlockState(pos, state.with(HEAT, 3));
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float distance) {
        super.onLandedUpon(world, state, pos, entity, distance);
        if (entity instanceof final ItemEntity itemEntity) {
            BrazierRecipe.LocationInventory inv = new BrazierRecipe.LocationInventory(itemEntity.getStack(), pos);

            for (int check = 0; check < 64; check++) {
                Optional<BrazierRecipe> match = world.getRecipeManager()
                        .getFirstMatch(BrazierRecipe.Type.INSTANCE, inv, world);

                if (match.isPresent()) {
                    ItemStack result = match.get().getOutput().copy();
                    world.spawnEntity(new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), result));
                    itemEntity.getStack().decrement(1);

                    final BrazierRecipe.Offset[] offsets = match.get().getMatchingOffsets(world, pos);
                    if (offsets != null) {
                        for (BrazierRecipe.Offset offset : offsets) {
                            removeHeat(world, pos.add(offset.offset));
                        }
                    }
                }
                else {
                    break;
                }
            }
        }
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (state.get(HEAT) > 0 && !entity.isFireImmune() && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            entity.damage(DamageSource.ON_FIRE, 1.0F);
        }

        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(HEAT) == 0) {
            return;
        }

        if (random.nextInt(24) == 0) {
            world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F, 0.9F, false);
        }

        double x;
        double y;
        double z;
        for(int i = 0; i < state.get(HEAT); i++) {
            x = (double)pos.getX() + 0.25D + random.nextDouble() / 2.0D;
            y = (double)pos.getY() + random.nextDouble() * 0.5D + 0.75D;
            z = (double)pos.getZ() + 0.25D + random.nextDouble() / 2.0D;
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void removeHeat(World world, BlockPos pos) {
        if (world.getBlockState(pos).contains(HEAT)) {
            final BlockState state = world.getBlockState(pos);
            final int value = state.get(HEAT) - 1;
            world.setBlockState(pos, state.with(HEAT, Math.max(0, Math.min(value, 3))));
        }
    }

    static {
        BOTTOM_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D);
        MIDDLE_SHAPE_1 = Block.createCuboidShape(4.0D, 3.0D, 4.0D, 12.0D, 5.0D, 12.0D);
        MIDDLE_SHAPE_2 = Block.createCuboidShape(6.0D, 5.0D, 6.0D, 10.0D, 10.0D, 10.0D);
        TOP_SHAPE = Block.createCuboidShape(3.0D, 10.0D, 3.0D, 13.0D, 12.0D, 13.0D);
        BASE_SHAPE = VoxelShapes.union(BOTTOM_SHAPE, MIDDLE_SHAPE_1, MIDDLE_SHAPE_2, TOP_SHAPE);
        COLLISION_SHAPE = VoxelShapes.union(BASE_SHAPE, Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D));
    }
}
