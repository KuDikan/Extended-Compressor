package icu.kudikan.extendedcompressor.handler;

import icu.kudikan.extendedcompressor.Config;
import icu.kudikan.extendedcompressor.ExtendedCompressor;
import icu.kudikan.extendedcompressor.tileentity.ExtendedCompressorTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = ExtendedCompressor.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class EventHandler {
    @SubscribeEvent
    public static void handleTooManyDropsBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isShiftKeyDown()) return;

        BlockPos pos = event.getPos();
        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof ExtendedCompressorTileEntity ec)) return;

        var count = ec.getInventory().getStacks().stream().mapToLong(ItemStack::getCount).sum();
        if (count >= Config.INSTANCE.extendedCompressorBreakProtectionThreshold.get()) {
            event.setCanceled(true);
            BlockState state = event.getState();
            player.sendSystemMessage(Component.translatable("text.extendedcompressor.too_many_item_entity_drops",
                    state.getBlock().getName().withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(count / 20)).withStyle(ChatFormatting.RED)));
            ec.setChanged();
        }
    }
}
