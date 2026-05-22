package icu.kudikan.extendedcompressor;


import icu.kudikan.extendedcompressor.client.ModMenuScreens;
import icu.kudikan.extendedcompressor.client.ModTESRs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ExtendedCompressor.MODID, dist = Dist.CLIENT)
public class ExtendedCompressorClient {
    public ExtendedCompressorClient(IEventBus modEventBus, ModContainer container) {
//        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.register(new ModMenuScreens());
        modEventBus.register(new ModTESRs());
    }
}
