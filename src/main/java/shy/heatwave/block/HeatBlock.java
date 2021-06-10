package shy.heatwave.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;

public class HeatBlock extends HeatwaveBlock {
    public static final IntProperty HEAT = IntProperty.of("heat", 0, 3);

    public HeatBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HEAT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HEAT);
    }
}
