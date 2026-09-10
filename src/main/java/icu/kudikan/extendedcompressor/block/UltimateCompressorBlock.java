package icu.kudikan.extendedcompressor.block;

import icu.kudikan.extendedcompressor.init.ModTileEntities;
import icu.kudikan.extendedcompressor.tileentity.UltimateCompressorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class UltimateCompressorBlock extends ExtendedCompressorBlock {
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UltimateCompressorTileEntity(pos, state);
    }

    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getServerTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, ModTileEntities.ULTIMATE_COMPRESSOR.get(), UltimateCompressorTileEntity::serverTick);
    }
}
