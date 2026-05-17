package icu.kudikan.extendedcompressor;

import com.blakebr0.extendedcrafting.init.ModCreativeModeTabs;
import com.mojang.logging.LogUtils;
import icu.kudikan.extendedcompressor.handler.RegisterCapabilityHandler;
import icu.kudikan.extendedcompressor.init.ModBlocks;
import icu.kudikan.extendedcompressor.init.ModItems;
import icu.kudikan.extendedcompressor.init.ModMenuTypes;
import icu.kudikan.extendedcompressor.init.ModTileEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(ExtendedCompressor.MODID)
@EventBusSubscriber(modid = ExtendedCompressor.MODID)
public class ExtendedCompressor {
    public static final String MODID = "extendedcompressor";
    public static final Logger LOGGER = LogUtils.getLogger();


    public ExtendedCompressor(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModBlocks.REGISTRY.register(modEventBus);
        ModItems.REGISTRY.register(modEventBus);
        ModTileEntities.REGISTRY.register(modEventBus);
        ModMenuTypes.REGISTRY.register(modEventBus);
        modEventBus.register(new RegisterCapabilityHandler());
    }

    @SubscribeEvent
    public static void onCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeModeTabs.CREATIVE_TAB.get()) {
            event.accept(ModBlocks.EXTENDED_COMPRESSOR.get());
        }
    }
}
