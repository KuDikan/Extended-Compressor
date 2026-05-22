package icu.kudikan.extendedcompressor.tileentity;

import com.blakebr0.cucumber.energy.BaseEnergyStorage;
import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.CachedRecipe;
import com.blakebr0.cucumber.inventory.OnContentsChangedFunction;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.cucumber.util.Localizable;
import com.blakebr0.extendedcrafting.api.crafting.ICompressorRecipe;
import com.blakebr0.extendedcrafting.config.ModConfigs;
import com.blakebr0.extendedcrafting.init.ModRecipeTypes;
import com.google.common.primitives.Ints;
import icu.kudikan.extendedcompressor.Config;
import icu.kudikan.extendedcompressor.contanier.ExtendedCompressorContainer;
import icu.kudikan.extendedcompressor.init.ModTileEntities;
import icu.kudikan.extendedcompressor.inventory.UnlimitItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ExtendedCompressorTileEntity extends BaseInventoryTileEntity implements MenuProvider {
    private static final int POWER_CAPACITY = (int) Math.clamp(ModConfigs.COMPRESSOR_POWER_CAPACITY.get() * Config.INSTANCE.extendedCompressorPowerCapMultiplier.getAsDouble(), 1, Integer.MAX_VALUE);
    private static final double POWER_RATE_MULTIPLIER = Config.INSTANCE.extendedCompressorPowerRateMultiplier.getAsDouble();
    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private final BaseEnergyStorage energy;
    private final CachedRecipe<CraftingInput, ICompressorRecipe> recipe;
    private ItemStack materialStack = ItemStack.EMPTY;
    private List<MaterialInput> inputs = NonNullList.create();
    private long materialCount;
    private int progress;
    private boolean ejecting = false;

    public ExtendedCompressorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.EXTENDED_COMPRESSOR.get(), pos, state);
        this.inventory = createInventoryHandler((slot) -> this.setChanged());
        this.recipeInventory = BaseItemStackHandler.create(2);
        this.energy = new BaseEnergyStorage(POWER_CAPACITY, this::setChangedFast);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.COMPRESSOR.get());
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ExtendedCompressorTileEntity tile) {
        var recipe = tile.getActiveRecipe();
        var output = tile.inventory.getStackInSlot(0);
        var input = tile.inventory.getStackInSlot(1);

        if (!input.isEmpty()) {
            if (tile.materialStack.isEmpty() || tile.materialCount <= 0) {
                tile.materialStack = input.copyWithCount(64);
                tile.setChangedFast();
            }

            var index = tile.canInsertItem(input);
            if (index > -1) {
                tile.insertItem(index, input);
                tile.setChangedFast();
            }
        }

        if (recipe != null && tile.getEnergy().getEnergyStored() > 0) {
            if (tile.materialCount >= recipe.getCount(0)) {
                if (tile.progress >= recipe.getPowerCost()) {
                    var result = recipe.assemble(tile.toCraftingInput(), level.registryAccess());

                    if (StackHelper.canCombineStacks(result, output)) {
                        tile.updateResult(result);
                        tile.progress = 0;
                        tile.materialCount -= recipe.getCount(0);

                        tile.consumeInputs(recipe.getCount(0));

                        if (tile.materialCount <= 0) {
                            tile.materialStack = ItemStack.EMPTY;
                            tile.ejecting = false;
                        }

                        tile.setChangedFast();
                    }
                } else {
                    tile.process(recipe);
                    tile.setChangedFast();
                }
            }
        }

        if (tile.ejecting && !tile.inputs.isEmpty()) {
            var newestInput = tile.getNewestInput();
            var newestStack = newestInput.stack;

            if (tile.materialCount > 0 && !newestStack.isEmpty() && (output.isEmpty() || StackHelper.areStacksEqual(newestStack, output))) {
                int addCount = Ints.saturatedCast(Math.min(newestInput.count, newestStack.getMaxStackSize() - output.getCount()));
                if (addCount > 0) {
                    var toAdd = StackHelper.withSize(newestStack, addCount, false);

                    tile.updateResult(toAdd);
                    tile.materialCount -= addCount;

                    newestInput.count -= addCount;

                    if (newestInput.count <= 0) {
                        tile.inputs.removeLast();
                    }

                    if (tile.materialCount < 1) {
                        tile.materialStack = ItemStack.EMPTY;
                        tile.ejecting = false;
                    }

                    if (tile.progress > 0)
                        tile.progress = 0;

                    tile.setChangedFast();
                }
            }
        }

        tile.dispatchIfChanged();
    }

    public static BaseItemStackHandler createInventoryHandler() {
        return createInventoryHandler(null);
    }

    public static BaseItemStackHandler createInventoryHandler(OnContentsChangedFunction onContentsChanged) {
        return UnlimitItemStackHandler.create(3, onContentsChanged, builder -> {
            builder.setOutputSlots(0);
            builder.setCanInsert((slot, stack) -> slot == 1);
        });
    }

    private static List<MaterialInput> loadMaterialInputs(HolderLookup.Provider lookup, CompoundTag tag) {
        var list = tag.getList("Inputs", 10);
        var inputs = new ArrayList<MaterialInput>();

        for (int i = 0; i < list.size(); i++) {
            inputs.add(MaterialInput.load(lookup, list.getCompound(i)));
        }

        // backwards compatibility
        // if there is a material stack set but no inputs then we add it as an input
        if (tag.contains("MaterialStack") && inputs.isEmpty()) {
            var stack = ItemStack.parseOptional(lookup, tag.getCompound("MaterialStack"));
            var count = tag.getLong("MaterialCount");

            if (count > 0) {
                inputs.add(new MaterialInput(stack, count));
            }
        }

        return inputs;
    }

    private static void saveMaterialInputs(HolderLookup.Provider lookup, CompoundTag tag, List<MaterialInput> inputs) {
        var list = new ListTag();
        for (var input : inputs) {
            list.add(input.save(lookup));
        }

        tag.put("Inputs", list);
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        this.materialCount = tag.getLong("MaterialCount");
        this.materialStack = ItemStack.parseOptional(lookup, tag.getCompound("MaterialStack"));
        this.progress = tag.getInt("Progress");
        this.ejecting = tag.getBoolean("Ejecting");
        this.energy.deserializeNBT(lookup, tag.get("Energy"));

        this.inputs = loadMaterialInputs(lookup, tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putLong("MaterialCount", this.materialCount);
        tag.put("MaterialStack", this.materialStack.saveOptional(lookup));
        tag.putInt("Progress", this.progress);
        tag.putBoolean("Ejecting", this.ejecting);
        tag.putInt("Energy", this.energy.getEnergyStored());

        saveMaterialInputs(lookup, tag, this.inputs);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Localizable.of("container.extendedcompressor.extended_compressor").build();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return ExtendedCompressorContainer.create(windowId, playerInventory, this.inventory, this.getBlockPos());
    }

    public BaseEnergyStorage getEnergy() {
        return this.energy;
    }

    public ItemStack getMaterialStack() {
        return this.materialStack;
    }

    public boolean hasMaterialStack() {
        return !this.materialStack.isEmpty();
    }

    public long getMaterialCount() {
        return this.materialCount;
    }

    public boolean isEjecting() {
        return this.ejecting;
    }

    public void toggleEjecting() {
        if (this.materialCount > 0) {
            this.ejecting = !this.ejecting;
            this.setChangedAndDispatch();
        }
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean hasRecipe() {
        return this.recipe.exists();
    }

    public ICompressorRecipe getActiveRecipe() {
        if (this.level == null)
            return null;

        var catalyst = this.inventory.getStackInSlot(2);

        this.recipeInventory.setStackInSlot(0, this.materialStack);
        this.recipeInventory.setStackInSlot(1, catalyst);

        return this.recipe.checkAndGet(this.toCraftingInput(), this.level);
    }

    public int getEnergyRequired() {
        if (this.hasRecipe())
            return this.recipe.get().getPowerCost();

        return 0;
    }

    public int getMaterialsRequired() {
        if (this.hasRecipe())
            return this.recipe.get().getCount(0);

        return 0;
    }

    public List<MaterialInput> getInputs() {
        return this.inputs;
    }

    private void process(ICompressorRecipe recipe) {
        int extract = (int) Math.clamp(recipe.getPowerRate() * POWER_RATE_MULTIPLIER, 1, Integer.MAX_VALUE);
        int difference = recipe.getPowerCost() - this.progress;
        if (difference < extract)
            extract = difference;

        int extracted = this.energy.extractEnergy(extract, false);
        this.progress += extracted;
    }

    private void updateResult(ItemStack stack) {
        var result = this.inventory.getStackInSlot(0);

        if (result.isEmpty()) {
            this.inventory.setStackInSlot(0, stack);
        } else {
            this.inventory.setStackInSlot(0, StackHelper.grow(result, stack.getCount()));
        }
    }

    private int canInsertItem(ItemStack stack) {
        var size = this.inputs.size();
        if (size == 0)
            return 0;

        for (int i = 0; i < size; i++) {
            var input = this.inputs.get(i);
            if (StackHelper.areStacksEqual(stack, input.stack))
                return i;
        }

        if (size < 100 && this.recipe.exists()) {
            var recipeStack = this.recipe.get().getIngredients().getFirst();
            if (recipeStack.test(stack))
                return size;
        }

        return -1;
    }

    private void insertItem(int index, ItemStack stack) {
        int consumeAmount = stack.getCount();

        if (this.inputs.isEmpty() || this.inputs.size() == index) {
            this.inputs.add(new MaterialInput(stack.copy(), consumeAmount));
        } else {
            var input = this.inputs.get(index);

            if (StackHelper.areStacksEqual(stack, input.stack)) {
                input.count += consumeAmount;
            } else {
                this.inputs.add(new MaterialInput(stack.copy(), consumeAmount));
            }
        }

        stack.shrink(consumeAmount);

        this.materialCount += consumeAmount;
    }

    private MaterialInput getNewestInput() {
        return this.inputs.getLast();
    }

    private void consumeInputs(int amount) {
        for (int i = this.inputs.size() - 1; i > -1; i--) {
            var input = this.inputs.get(i);
            if (input.count > amount) {
                input.count -= amount;
                break;
            } else {
                amount -= (int) input.count;
                this.inputs.remove(i);
            }
        }
    }

    private CraftingInput toCraftingInput() {
        return this.recipeInventory.toShapelessCraftingInput();
    }

    public static class MaterialInput {
        public ItemStack stack;
        public long count;

        public MaterialInput(ItemStack stack, long count) {
            this.stack = stack;
            this.count = count;
        }

        public static MaterialInput load(HolderLookup.Provider lookup, CompoundTag tag) {
            var stack = ItemStack.parseOptional(lookup, tag.getCompound("Item"));
            var count = tag.getLong("Count");

            return new MaterialInput(stack, count);
        }

        public Component getDisplayName() {
            return Component.literal(this.count + "x ").append(this.stack.getHoverName());
        }

        public CompoundTag save(HolderLookup.Provider lookup) {
            var tag = new CompoundTag();

            tag.put("Item", stack.copyWithCount(1).save(lookup));
            tag.putLong("Count", count);

            return tag;
        }
    }
}
