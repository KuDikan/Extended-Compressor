package icu.kudikan.extendedcompressor.init;

import icu.kudikan.extendedcompressor.ExtendedCompressor;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import icu.kudikan.extendedcompressor.tileentity.UltimateCompressorTileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExtendedCompressor.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedCompressorTileEntity>> EXTENDED_COMPRESSOR = register("extended_compressor", ExtendedCompressorTileEntity::new, () -> new Block[]{ModBlocks.EXTENDED_COMPRESSOR.get()});
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UltimateCompressorTileEntity>> ULTIMATE_COMPRESSOR = register("ultimate_compressor", UltimateCompressorTileEntity::new, () -> new Block[]{ModBlocks.ULTIMATE_COMPRESSOR.get()});

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return REGISTRY.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }


}
