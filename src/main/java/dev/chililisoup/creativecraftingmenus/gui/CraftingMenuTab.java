package dev.chililisoup.creativecraftingmenus.gui;

import dev.chililisoup.creativecraftingmenus.util.ServerResourceProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

public class CraftingMenuTab extends CreativeMenuTab<CraftingMenuTab.CraftingTabMenu> {
    public CraftingMenuTab(Component displayName, Supplier<ItemStack> iconGenerator) {
        super(displayName, iconGenerator);
    }

    @Override
    CraftingTabMenu createMenu(Player player) {
        return new CraftingTabMenu(player);
    }

    @Override
    public void drawTitle(TitleDrawer titleDrawer, int x, int y, int color) {
        super.drawTitle(titleDrawer, x + 31, y, color);
    }

    public static class CraftingTabMenu extends CreativeTabMenu<CraftingTabMenu> {
        private final CraftingContainer craftSlots;
        private final ResultContainer resultSlots = new ResultContainer();

        CraftingTabMenu(Player player) {
            super(player);
            this.craftSlots = new TransientCraftingContainer(this, 3, 3);
            this.addSlot(new ResultSlot(this.player, this.craftSlots, this.resultSlots, 0, 134, 35));
            this.addCraftingGridSlots();
        }

        protected void addCraftingGridSlots() {
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    this.addSlot(new Slot(this.craftSlots, x + y * 3, 40 + x * 18, 17 + y * 18));
                }
            }
        }

        @Override
        CraftingTabMenu copyWithPlayer(@NotNull Player player) {
            return this.copyContentsTo(new CraftingTabMenu(player));
        }

        @Override
        public @NotNull ItemStack quickMoveFromInventory(@NotNull Player player, int slotIndex) {
            ItemStack resultStack = ItemStack.EMPTY;
            Slot slot = this.player.inventoryMenu.slots.get(slotIndex);
            if (!slot.hasItem()) return resultStack;

            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (!this.moveItemStackTo(slotStack, 1, 10, false))
                return ItemStack.EMPTY;

            if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (slotStack.getCount() == resultStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, slotStack);

            return resultStack;
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            RecipeManager recipeManager = ServerResourceProvider.getRecipeManager();
            if (recipeManager == null) return;
            HolderLookup.Provider provider = ServerResourceProvider.registryAccess();
            if (provider == null) return;

            Level level = this.player.level();
            CraftingInput craftingInput = this.craftSlots.asCraftInput();
            ItemStack itemStack = ItemStack.EMPTY;
            Optional<RecipeHolder<@NotNull CraftingRecipe>> optional = recipeManager
                    .getRecipeFor(RecipeType.CRAFTING, craftingInput, level, (ResourceKey<@NotNull Recipe<?>>) null);

            if (optional.isPresent()) {
                RecipeHolder<@NotNull CraftingRecipe> recipeHolder2 = optional.get();
                CraftingRecipe craftingRecipe = recipeHolder2.value();
                this.resultSlots.setRecipeUsed(recipeHolder2);
                itemStack = craftingRecipe.assemble(craftingInput, provider);
            }

            this.resultSlots.setItem(0, itemStack);
        }

        @Override
        public void removed(@NotNull Player player) {
            if (player.hasInfiniteMaterials()) for (int i = 0; i < this.craftSlots.getContainerSize(); i++)
                player.getInventory().placeItemBackInInventory(this.craftSlots.removeItemNoUpdate(i));
        }
    }
}
