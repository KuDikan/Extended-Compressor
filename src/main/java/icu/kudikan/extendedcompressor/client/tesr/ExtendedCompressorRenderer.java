package icu.kudikan.extendedcompressor.client.tesr;

import com.blakebr0.extendedcrafting.config.ModConfigs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;

public class ExtendedCompressorRenderer<T extends ExtendedCompressorTileEntity> implements BlockEntityRenderer<T> {
    public ExtendedCompressorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T tile, float v, PoseStack matrix, MultiBufferSource buffer, int i, int i1) {
        if (!ModConfigs.ENABLE_COMPRESSOR_RENDERER.get())
            return;

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null)
            return;

        var recipe = tile.getActiveRecipe();

        if (recipe != null) {
            var stack = recipe.getResultItem(level.registryAccess());

            if (!stack.isEmpty()) {
                matrix.pushPose();
                matrix.translate(0.5D, 1.3D, 0.5D);
                float scale = stack.getItem() instanceof BlockItem ? 0.9F : 0.75F;
                matrix.scale(scale, scale, scale);
                double tick = System.currentTimeMillis() / 400.0D;
                matrix.translate(0.0D, Math.sin(tick % (2 * Math.PI)) * 0.065D, 0.0D);
                matrix.mulPose(Axis.YP.rotationDegrees((float) ((tick * 40.0D) % 360)));
                minecraft.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, 234, i1, matrix, buffer, level, 0);
                matrix.popPose();
            }
        }
    }
}
