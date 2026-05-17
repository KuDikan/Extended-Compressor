package icu.kudikan.extendedcompressor.mixin;

import com.blakebr0.extendedcrafting.ExtendedCrafting;
import com.blakebr0.extendedcrafting.init.ModBlocks;
import com.blakebr0.extendedcrafting.singularity.Singularity;
import com.blakebr0.extendedcrafting.singularity.SingularityRegistry;
import com.blakebr0.extendedcrafting.singularity.SingularityUtils;
import com.google.gson.Gson;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileWriter;

@Mixin(SingularityRegistry.class)
public class SingularityRegistryMixin {
    @Final
    @Shadow
    private static Gson GSON;

    @Inject(method = "writeDefaultSingularityFiles()V",
            at = @At(value = "INVOKE", target = "Lcom/blakebr0/extendedcrafting/lib/ModSingularities;getDefaults()Ljava/util/List;"))
    public void writeDefaultSingularity(CallbackInfo ci,
                                        @Local(name = "dir") File dir) {
        Singularity compressor = new Singularity(ExtendedCrafting.resource("compressor"), "singularity.extendedcrafting.compressor", new int[]{0x97FFFF, 0x212121}, Ingredient.of(ModBlocks.COMPRESSOR.get()));
        var json = SingularityUtils.writeToJson(compressor);
        FileWriter writer = null;

        try {
            var file = new File(dir, compressor.getId().getPath() + ".json");
            writer = new FileWriter(file);

            GSON.toJson(json, writer);
            writer.close();
        } catch (Exception e) {
            ExtendedCrafting.LOGGER.error("An error occurred while generating default singularities", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
