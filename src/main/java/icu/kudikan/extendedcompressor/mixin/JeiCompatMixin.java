package icu.kudikan.extendedcompressor.mixin;

import com.blakebr0.extendedcrafting.compat.jei.JeiCompat;
import com.blakebr0.extendedcrafting.compat.jei.category.CompressorCraftingCategory;
import com.blakebr0.extendedcrafting.config.ModConfigs;
import icu.kudikan.extendedcompressor.client.screen.ExtendedCompressorScreen;
import icu.kudikan.extendedcompressor.init.ModBlocks;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JeiCompat.class)
public class JeiCompatMixin {
    @Inject(method = "registerRecipeCatalysts(Lmezz/jei/api/registration/IRecipeCatalystRegistration;)V",
            at = @At("TAIL"))
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration, CallbackInfo ci) {
        if (ModConfigs.ENABLE_COMPRESSOR.get()) {
            registration.addRecipeCatalyst(new ItemStack(ModBlocks.EXTENDED_COMPRESSOR.get()), CompressorCraftingCategory.RECIPE_TYPE);
        }
    }

    @Inject(method = "registerGuiHandlers(Lmezz/jei/api/registration/IGuiHandlerRegistration;)V",
            at = @At("TAIL"))
    public void registerGuiHandlers(IGuiHandlerRegistration registration, CallbackInfo ci) {
        if (ModConfigs.ENABLE_COMPRESSOR.get()) {
            registration.addRecipeClickArea(ExtendedCompressorScreen.class, 97, 47, 21, 14, CompressorCraftingCategory.RECIPE_TYPE);
        }
    }
}
