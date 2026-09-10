package icu.kudikan.extendedcompressor.handler;


import icu.kudikan.extendedcompressor.init.ModTileEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class RegisterCapabilityHandler {
    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModTileEntities.EXTENDED_COMPRESSOR.get(), (block, direction) -> block.getEnergy());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntities.EXTENDED_COMPRESSOR.get(), (block, direction) -> block.getInventory());

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModTileEntities.ULTIMATE_COMPRESSOR.get(), (block, direction) -> block.getEnergy());

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModTileEntities.ULTIMATE_COMPRESSOR.get(), (block, direction) -> block.getInventory());
    }
}
