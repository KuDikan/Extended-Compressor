package icu.kudikan.extendedcompressor.mixin;

import com.blakebr0.extendedcrafting.api.crafting.ICompressorRecipe;
import com.blakebr0.extendedcrafting.compat.JadeCompat;
import com.blakebr0.extendedcrafting.lib.ModTooltips;
import icu.kudikan.extendedcompressor.block.ExtendedCompressorBlock;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

@Mixin(JadeCompat.class)
public class JadeCompatMixin {
    @Final
    @Shadow
    private static ResourceLocation COMPRESSOR_PROVIDER;

    @Inject(method = "registerClient(Lsnownee/jade/api/IWailaClientRegistration;)V",
            at = @At("TAIL"))
    public void registerClient(IWailaClientRegistration registration, CallbackInfo ci) {
        registration.registerBlockComponent(new IBlockComponentProvider() {
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
                ExtendedCompressorTileEntity compressor = (ExtendedCompressorTileEntity) accessor.getBlockEntity();
                ICompressorRecipe recipe = compressor.getActiveRecipe();
                if (recipe != null) {
                    Level level = accessor.getLevel();
                    ItemStack output = recipe.getResultItem(level.registryAccess());
                    tooltip.add(ModTooltips.CRAFTING.args(output.getCount(), output.getHoverName()).build());
                }

            }

            public ResourceLocation getUid() {
                return COMPRESSOR_PROVIDER;
            }
        }, ExtendedCompressorBlock.class);
    }
}
