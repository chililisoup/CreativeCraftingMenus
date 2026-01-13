package dev.chililisoup.creativecraftingmenus.gui;

import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class CreativeMenuTab<M extends CreativeMenuTab.CreativeTabMenu<T>, T extends CreativeMenuTab<M, T>> extends CreativeModeTab {
    protected static final Identifier SELECTED_TAB = CreativeCraftingMenus.id("container/creative_menu_inner_tab_selected");
    protected static final Identifier HIGHLIGHTED_TAB = CreativeCraftingMenus.id("container/creative_menu_inner_tab_highlighted");
    protected static final Identifier UNSELECTED_TAB = CreativeCraftingMenus.id("container/creative_menu_inner_tab_unselected");
    protected static final Identifier SELECTED_DYE = CreativeCraftingMenus.id("container/dye_selected");
    protected static final Identifier HIGHLIGHTED_DYE = CreativeCraftingMenus.id("container/dye_highlighted");
    protected static final Identifier UNSELECTED_DYE = CreativeCraftingMenus.id("container/dye_unselected");

    private final TabMenuConstructor<M, T> menuConstructor;
    protected @Nullable AbstractContainerScreen<?> screen;
    protected @Nullable M menu = null;
    private boolean hidden = false;

    CreativeMenuTab(TabMenuConstructor<M, T> menuConstructor, Component displayName, Supplier<ItemStack> iconGenerator) {
        //noinspection DataFlowIssue
        super(null, -1, Type.INVENTORY, displayName, iconGenerator, CreativeModeTab.Builder.EMPTY_GENERATOR);
        this.menuConstructor = menuConstructor;
    }

    public final void init(AbstractContainerScreen<?> screen, Player player) {
        this.screen = screen;
        if (this.menu == null)
            //noinspection unchecked
            this.menu = this.menuConstructor.accept((T) this, player);
        else if (this.menu.player != player)
            //noinspection unchecked
            this.menu = (M) this.menu.copyWithPlayer(player);
    }

    public void subInit() {}

    public void remove() {
        if (this.menu != null) {
            this.menu.removed(this.menu.player);
            this.menu = null;
        }
        this.screen = null;
    }

    public void dispose() {
        this.screen = null;
        this.menu = null;
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        return false;
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent) {
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent) {
        return false;
    }

    public boolean mouseScrolled(double distance) {
        return false;
    }

    @Override
    public boolean shouldDisplay() {
        return !hidden && super.shouldDisplay();
    }

    public void hide() {
        this.hidden = true;
    }

    public void show() {
        this.hidden = false;
    }

    public @NotNull M getMenu() {
        assert this.menu != null;
        return this.menu;
    }

    public interface TitleDrawer {
        void draw(int x, int y, int color);
    }

    public void drawTitle(TitleDrawer titleDrawer, int x, int y, int color) {
        titleDrawer.draw(x, y, color);
    }

    public void render(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {}

    public interface TabMenuConstructor<M extends CreativeTabMenu<T>, T extends CreativeMenuTab<M, T>> {
        M accept(T menuTab, Player player);
    }

    public static abstract class CreativeTabMenu<T extends CreativeMenuTab<?, T>> extends AbstractContainerMenu {
        protected final T menuTab;
        protected final Player player;

        CreativeTabMenu(T menuTab, Player player) {
            super(null, 0);
            this.menuTab = menuTab;
            this.player = player;
        }

        abstract CreativeTabMenu<T> copyWithPlayer(@NotNull Player player);

        protected <M extends CreativeTabMenu<T>> M copyContentsTo(M other) {
            other.initializeContents(
                    this.getStateId(),
                    this.slots.stream().map(Slot::getItem).toList(),
                    this.getCarried()
            );

            return other;
        }

        @Override
        public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
            ItemStack resultStack = ItemStack.EMPTY;
            if (slotIndex < 0 || this.slots.size() <= slotIndex) return resultStack;
            Slot slot = this.slots.get(slotIndex);
            if (!slot.hasItem()) return resultStack;

            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (!this.player.inventoryMenu.moveItemStackTo(slotStack, 9, 45, true))
                return ItemStack.EMPTY;

            if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (slotStack.getCount() == resultStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, resultStack);

            return resultStack;
        }

        public abstract @NotNull ItemStack quickMoveFromInventory(@NotNull Player player, int slotIndex);

        @Override
        protected @NotNull Slot addSlot(Slot slot) {
            slot.index = this.slots.size();
            this.slots.add(slot);
            return slot;
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public @NotNull ItemStack getCarried() {
            return this.player.inventoryMenu.getCarried();
        }

        @Override
        public void setCarried(@NotNull ItemStack itemStack) {
            this.player.inventoryMenu.setCarried(itemStack);
        }

        @Override
        public void removed(@NotNull Player player) {}
    }
    
    public interface MenuTabConstructor<T extends CreativeMenuTab<?, T>> {
        T accept(
                Component component,
                Supplier<ItemStack> supplier
        );
    }

    public static class Builder<T extends CreativeMenuTab<?, T>> extends CreativeModeTab.Builder {
        MenuTabConstructor<T> constructor;
        private boolean hasDisplayName = false;

        public Builder(MenuTabConstructor<T> constructor) {
            //noinspection DataFlowIssue
            super(null, -1);
            this.constructor = constructor;
        }

        @Override
        public @NotNull Builder<T> title(@NotNull Component displayName) {
            hasDisplayName = true;
            super.title(displayName);
            return this;
        }

        @Override
        public @NotNull Builder<T> icon(@NotNull Supplier<ItemStack> iconGenerator) {
            super.icon(iconGenerator);
            return this;
        }

        @Override
        public @NotNull Builder<T> backgroundTexture(@NotNull Identifier backgroundTexture) {
            super.backgroundTexture(backgroundTexture);
            return this;
        }

        @Override
        public @NotNull T build() {
            if (!hasDisplayName)
                throw new IllegalStateException("No display name set for ItemGroup");

            T menuTab = constructor.accept(this.displayName, this.iconGenerator);
            menuTab.alignedRight = true;
            menuTab.canScroll = false;
            menuTab.showTitle = this.showTitle;
            menuTab.backgroundTexture = this.backgroundTexture;
            return menuTab;
        }
    }
}
