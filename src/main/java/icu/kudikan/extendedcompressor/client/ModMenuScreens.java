package icu.kudikan.extendedcompressor.client;


import icu.kudikan.extendedcompressor.client.screen.ExtendedCompressorScreen;
import icu.kudikan.extendedcompressor.init.ModMenuTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ModMenuScreens {
    @SubscribeEvent
    public void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EXTENDED_COMPRESSOR.get(), ExtendedCompressorScreen::new);
    }
}
