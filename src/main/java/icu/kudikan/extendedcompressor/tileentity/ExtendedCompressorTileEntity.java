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
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExtendedCompressorTileEntity extends BaseInventoryTileEntity implements MenuProvider {
    private static final double POWER_RATE_MULTIPLIER = Config.INSTANCE.extendedCompressorPowerRateMultiplier.getAsDouble();
    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private final BaseEnergyStorage energy;
    private final CachedRecipe<CraftingInput, ICompressorRecipe> recipe;
    protected long materialCount;
    private ItemStack materialStack = ItemStack.EMPTY;
    private List<MaterialInput> inputs = NonNullList.create();
    private int progress;
    private boolean ejecting = false;

    public ExtendedCompressorTileEntity(BlockPos pos, BlockState state) {
        this(ModTileEntities.EXTENDED_COMPRESSOR.get(), pos, state);
    }

    public ExtendedCompressorTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = this.createInventory();
        this.recipeInventory = BaseItemStackHandler.create(2);
        this.energy = new BaseEnergyStorage(this.getPowerCapacity(), this::setChangedFast);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.COMPRESSOR.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExtendedCompressorTileEntity tile) {
        tile.tickInput();
        tile.tickRecipe(level);
        tile.tickEject();
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

    protected int getPowerCapacity() {
        return (int) Math.clamp(ModConfigs.COMPRESSOR_POWER_CAPACITY.get() * Config.INSTANCE.extendedCompressorPowerCapMultiplier.getAsDouble(), 1, Integer.MAX_VALUE);
    }

    private void tickInput() {
        var input = this.inventory.getStackInSlot(1);

        if (!input.isEmpty()) {
            if (this.materialStack.isEmpty() || this.materialCount <= 0) {
                this.materialStack = input.copyWithCount(64);
                this.setChangedFast();
            }

            var index = this.canInsertItem(input);
            if (index > -1) {
                this.insertItem(index, input);
                this.setChangedFast();
            }
        }
    }

    protected void onRecipeCraft(ICompressorRecipe recipe, ItemStack result) {
        this.updateResult(result);
        this.materialCount -= recipe.getCount(0);
        this.consumeInputs(recipe.getCount(0));
    }

    private void tickRecipe(Level level) {
        var recipe = this.getActiveRecipe();
        if (recipe != null && this.getEnergy().getEnergyStored() > 0) {
            if (this.materialCount >= recipe.getCount(0)) {
                if (this.progress >= recipe.getPowerCost()) {
                    var result = recipe.assemble(this.toCraftingInput(), level.registryAccess());

                    if (StackHelper.canCombineStacks(result, this.inventory.getStackInSlot(0))) {
                        this.onRecipeCraft(recipe, result);
                        this.progress = 0;
                        if (this.materialCount <= 0) {
                            this.materialStack = ItemStack.EMPTY;
                            this.ejecting = false;
                        }
                        this.setChangedFast();
                    }
                } else {
                    this.process(recipe);
                    this.setChangedFast();
                }
            }
        }
    }

    private void tickEject() {
        if (this.ejecting && !this.inputs.isEmpty()) {
            var newestInput = this.getNewestInput();
            var newestStack = newestInput.stack;
            var output = this.inventory.getStackInSlot(0);

            if (this.materialCount > 0 && !newestStack.isEmpty() && (output.isEmpty() || StackHelper.areStacksEqual(newestStack, output))) {
                int addCount = Ints.saturatedCast(Math.min(newestInput.count, newestStack.getMaxStackSize() - output.getCount()));
                if (addCount > 0) {
                    var toAdd = StackHelper.withSize(newestStack, addCount, false);

                    this.updateResult(toAdd);
                    this.materialCount -= addCount;

                    newestInput.count -= addCount;

                    if (newestInput.count <= 0) {
                        this.inputs.removeLast();
                    }

                    if (this.materialCount < 1) {
                        this.materialStack = ItemStack.EMPTY;
                        this.ejecting = false;
                    }

                    if (this.progress > 0)
                        this.progress = 0;

                    this.setChangedFast();
                }
            }
        }
    }

    protected BaseItemStackHandler createInventory() {
        return createInventoryHandler((slot) -> this.setChanged());
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
        this.energy.deserializeNBT(lookup, Optional.ofNullable(tag.get("Energy")).orElse(IntTag.valueOf(0)));

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

    protected int getPowerRate(ICompressorRecipe recipe) {
        return (int) Math.clamp(recipe.getPowerRate() * POWER_RATE_MULTIPLIER, 1, Integer.MAX_VALUE);
    }

    private void process(ICompressorRecipe recipe) {
        int extract = this.getPowerRate(recipe);
        int difference = recipe.getPowerCost() - this.progress;
        if (difference < extract)
            extract = difference;

        int extracted = this.energy.extractEnergy(extract, false);
        this.progress += extracted;
    }

    protected void updateResult(ItemStack stack) {
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

    protected void consumeInputs(long amount) {
        for (int i = this.inputs.size() - 1; i > -1; i--) {
            var input = this.inputs.get(i);
            if (input.count > amount) {
                input.count -= amount;
                break;
            } else {
                amount -= input.count;
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
