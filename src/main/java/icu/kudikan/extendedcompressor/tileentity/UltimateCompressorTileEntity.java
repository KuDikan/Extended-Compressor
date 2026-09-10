package icu.kudikan.extendedcompressor.tileentity;

import com.blakebr0.cucumber.util.Localizable;
import com.blakebr0.extendedcrafting.api.crafting.ICompressorRecipe;
import com.google.common.primitives.Ints;
import icu.kudikan.extendedcompressor.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * The Ultimate Compressor, built on top of the Extended Quantum Compressor.
 * <ul>
 *     <li>Output slot is effectively infinite (up to {@link Integer#MAX_VALUE} items).</li>
 *     <li>Crafting is batched: one operation consumes every stored material and produces all results at once.</li>
 * </ul>
 */
public class UltimateCompressorTileEntity extends ExtendedCompressorTileEntity {
    public UltimateCompressorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.ULTIMATE_COMPRESSOR.get(), pos, state);
    }

    @Override
    protected int getPowerCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getPowerRate(ICompressorRecipe recipe) {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container.extendedcompressor.ultimate_compressor").build();
    }

    @Override
    protected void onRecipeCraft(ICompressorRecipe recipe, ItemStack result) {
        var count = recipe.getCount(0);
        if (count <= 0) return;
        var batch = Ints.saturatedCast(Math.min(this.materialCount / count, result.getMaxStackSize() * 16777216L - getInventory().getStackInSlot(0).getCount()));
        if (batch <= 0) return;
        this.updateResult(result.copyWithCount(batch));
        this.materialCount -= (long) count * batch;
        this.consumeInputs((long) count * batch);
    }
}
