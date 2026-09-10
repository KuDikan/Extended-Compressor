package icu.kudikan.extendedcompressor.contanier;

import com.blakebr0.cucumber.container.BaseContainerMenu;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.slot.BaseItemStackHandlerSlot;
import com.blakebr0.cucumber.inventory.slot.OutputSlot;
import com.blakebr0.extendedcrafting.container.slot.CatalystSlot;
import com.google.common.primitives.Ints;
import icu.kudikan.extendedcompressor.init.ModMenuTypes;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ExtendedCompressorContainer extends BaseContainerMenu {
    private ExtendedCompressorContainer(MenuType<?> type, int id, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(type, id, playerInventory, ExtendedCompressorTileEntity.createInventoryHandler(), buffer.readBlockPos());
    }

    private ExtendedCompressorContainer(MenuType<?> type, int id, Inventory playerInventory, BaseItemStackHandler inventory, BlockPos pos) {
        super(type, id, pos);
        this.addSlot(new OutputSlot(inventory, 0, 135, 48));
        this.addSlot(new BaseItemStackHandlerSlot(inventory, 1, 65, 48));
        this.addSlot(new CatalystSlot(inventory, 2, 38, 48));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 112 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 170));
        }
    }

    public static ExtendedCompressorContainer create(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        return new ExtendedCompressorContainer(ModMenuTypes.EXTENDED_COMPRESSOR.get(), windowId, playerInventory, buffer);
    }

    public static ExtendedCompressorContainer create(int windowId, Inventory playerInventory, BaseItemStackHandler inventory, BlockPos pos) {
        return new ExtendedCompressorContainer(ModMenuTypes.EXTENDED_COMPRESSOR.get(), windowId, playerInventory, inventory, pos);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int slotNumber) {
        var itemstack = ItemStack.EMPTY;
        var slot = this.slots.get(slotNumber);

        if (slot.hasItem()) {
            var itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (slotNumber < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else {
                ItemStack inputStack = this.slots.get(1).getItem();
                if (inputStack.isEmpty() || (inputStack.is(itemstack1.getItem()) && inputStack.getCount() < Ints.saturatedCast(inputStack.getMaxStackSize() * 16777216L))) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotNumber < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotNumber < 39) {
                    if (!this.moveItemStackTo(itemstack1, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
