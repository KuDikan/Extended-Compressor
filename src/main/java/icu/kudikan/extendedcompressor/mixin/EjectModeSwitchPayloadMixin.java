package icu.kudikan.extendedcompressor.mixin;

import com.blakebr0.extendedcrafting.network.payload.EjectModeSwitchPayload;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EjectModeSwitchPayload.class)
public class EjectModeSwitchPayloadMixin {
    @Inject(
            method = "handleServer(Lcom/blakebr0/extendedcrafting/network/payload/EjectModeSwitchPayload;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
            at = @At("TAIL")
    )
    private static void handleServer(EjectModeSwitchPayload payload, IPayloadContext context, CallbackInfo ci) {
        context.enqueueWork(() -> {
            var player = context.player();
            var level = player.level();
            var tile = level.getBlockEntity(payload.pos());

            if (tile instanceof ExtendedCompressorTileEntity compressor) {
                compressor.toggleEjecting();
            }
        });
    }
}
