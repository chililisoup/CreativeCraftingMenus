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

public class CraftingMenuTab extends CreativeMenuTab<CraftingMenuTab.CraftingTabMenu, CraftingMenuTab> {
    public CraftingMenuTab(Component displayName, Supplier<ItemStack> iconGenerator) {
        super(CraftingTabMenu::new, displayName, iconGenerator);
    }

    @Override
    public void drawTitle(TitleDrawer titleDrawer, int x, int y, int color) {
        super.drawTitle(titleDrawer, x + 31, y, color);
    }

    public static class CraftingTabMenu extends CreativeTabMenu<CraftingMenuTab> {
        private final CraftingContainer craftSlots;
        private final ResultContainer resultSlots = new ResultContainer();

        CraftingTabMenu(CraftingMenuTab menuTab, Player player) {
            super(menuTab, player);
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
            return this.copyContentsTo(new CraftingTabMenu(this.menuTab, player));
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

        private static void slotChangedCraftingGrid(
                Player player,
                CraftingContainer craftingContainer,
                ResultContainer resultContainer,
                RecipeManager recipeManager,
                HolderLookup.Provider provider
        ) {
            Level level = player.level();
            CraftingInput craftingInput = craftingContainer.asCraftInput();
            ItemStack itemStack = ItemStack.EMPTY;
            Optional<RecipeHolder<@NotNull CraftingRecipe>> optional = recipeManager
                    .getRecipeFor(RecipeType.CRAFTING, craftingInput, level, (ResourceKey<@NotNull Recipe<?>>) null);

            if (optional.isPresent()) {
                RecipeHolder<@NotNull CraftingRecipe> recipeHolder2 = optional.get();
                CraftingRecipe craftingRecipe = recipeHolder2.value();
                resultContainer.setRecipeUsed(recipeHolder2);
                itemStack = craftingRecipe.assemble(craftingInput, provider);
            }

            resultContainer.setItem(0, itemStack);
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            ServerResourceProvider.tryProcessRecipes((recipeManager, provider) ->
                    slotChangedCraftingGrid(this.player, this.craftSlots, this.resultSlots, recipeManager, provider)
            );
        }

        @Override
        public void removed(@NotNull Player player) {
            if (player.hasInfiniteMaterials()) for (int i = 0; i < this.craftSlots.getContainerSize(); i++)
                player.getInventory().placeItemBackInInventory(this.craftSlots.removeItemNoUpdate(i));
        }
    }
}
