package icu.kudikan.extendedcompressor.client;


import icu.kudikan.extendedcompressor.client.tesr.ExtendedCompressorRenderer;
import icu.kudikan.extendedcompressor.init.ModTileEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class ModTESRs {
    @SubscribeEvent
    public void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModTileEntities.EXTENDED_COMPRESSOR.get(), ExtendedCompressorRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.ULTIMATE_COMPRESSOR.get(), ExtendedCompressorRenderer::new);
    }
}
