package icu.kudikan.extendedcompressor.inventory;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class UnlimitItemStackHandler extends BaseItemStackHandler {
    protected UnlimitItemStackHandler(int size, OnContentsChangedFunction onContentsChanged) {
        super(size, onContentsChanged);
        setDefaultSlotLimit(1073741824);
    }

    public static BaseItemStackHandler create(int size) {
        return create(size, (builder) -> {
        });
    }

    public static BaseItemStackHandler create(int size, Consumer<BaseItemStackHandler> builder) {
        return create(size, null, builder);
    }

    public static BaseItemStackHandler create(int size, OnContentsChangedFunction onContentsChanged, Consumer<BaseItemStackHandler> builder) {
        BaseItemStackHandler handler = new UnlimitItemStackHandler(size, onContentsChanged);
        builder.accept(handler);
        return handler;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return Math.min(this.getSlotLimit(slot), Ints.saturatedCast(stack.getMaxStackSize() * 16777216L));
    }
}
