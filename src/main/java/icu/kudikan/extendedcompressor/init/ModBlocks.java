package icu.kudikan.extendedcompressor.init;

import com.blakebr0.cucumber.item.BaseBlockItem;
import icu.kudikan.extendedcompressor.ExtendedCompressor;
import icu.kudikan.extendedcompressor.block.ExtendedCompressorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(Registries.BLOCK, ExtendedCompressor.MODID);
    public static final Map<String, Supplier<BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();

    public static final DeferredHolder<Block, Block> EXTENDED_COMPRESSOR = register("extended_compressor", ExtendedCompressorBlock::new);

    private static DeferredHolder<Block, Block> register(String name, Supplier<Block> block) {
        return register(name, block, b -> () -> new BaseBlockItem(b.get()));
    }

    private static DeferredHolder<Block, Block> register(String name, Supplier<Block> block, Rarity rarity) {
        return register(name, block, b -> () -> new BaseBlockItem(b.get(), p -> p.rarity(rarity)));
    }

    private static DeferredHolder<Block, Block> register(String name, Supplier<Block> block, Function<DeferredHolder<Block, Block>, Supplier<? extends BlockItem>> item) {
        var reg = REGISTRY.register(name, block);
        BLOCK_ITEMS.put(name, () -> item.apply(reg).get());
        return reg;
    }
}
