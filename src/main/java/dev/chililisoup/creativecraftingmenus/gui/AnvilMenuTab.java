package dev.chililisoup.creativecraftingmenus.gui;

import dev.chililisoup.creativecraftingmenus.util.MenuHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import static net.minecraft.client.gui.screens.inventory.AnvilScreen.*;

public class AnvilMenuTab extends CreativeMenuTab<AnvilMenuTab.AnvilTabMenu> {
    private @Nullable EditBox name;

    public AnvilMenuTab(Component displayName, Supplier<ItemStack> iconGenerator) {
        super(displayName, iconGenerator);
    }

    @Override
    AnvilTabMenu createMenu(Player player) {
        return new AnvilTabMenu(player);
    }

    @Override
    public void subInit() {
        if (this.screen == null || this.menu == null) return;
        if (this.name != null) this.screen.removeWidget(this.name);

        this.name = new EditBox(
                screen.getFont(),
                screen.leftPos + 67,
                screen.topPos + 24,
                103,
                12,
                Component.translatable("container.repair")
        ) {
            @Override
            public boolean isVisible() {
                return this.canConsumeInput() && super.isVisible();
            }
        };

        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setInvertHighlightedTextColor(false);
        this.name.setBordered(false);
        this.name.setMaxLength(Integer.MAX_VALUE);
        this.name.setResponder(this::onNameChanged);
        this.screen.addRenderableWidget(this.name);
        this.firstSlotChanged(this.menu.slots.getFirst().getItem());
    }

    @Override
    public void remove() {
        if (this.screen != null && this.name != null)
            this.screen.removeWidget(this.name);
        this.name = null;
        super.remove();
    }

    @Override
    public void drawTitle(TitleDrawer titleDrawer, int x, int y, int color) {
        super.drawTitle(titleDrawer, x + 57, y, color);
    }

    @Override
    public void render(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (this.menu == null || this.name == null) return;

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.menu.slots.get(0).hasItem() ? TEXT_FIELD_SPRITE : TEXT_FIELD_DISABLED_SPRITE,
                screen.leftPos + 64,
                screen.topPos + 20, 110, 16
        );

        if ((this.menu.slots.get(0).hasItem() || this.menu.slots.get(1).hasItem()) && !this.menu.slots.get(2).hasItem()) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    ERROR_SPRITE,
                    screen.leftPos + 108,
                    screen.topPos + 45,
                    28,
                    21
            );
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isEscape() || this.name == null) return false;
        return this.name.keyPressed(keyEvent) || this.name.canConsumeInput();
    }

    private void onNameChanged(String string) {
        if (this.menu == null) return;

        Slot slot = this.menu.getSlot(0);
        if (!slot.hasItem()) return;

        this.menu.setItemName(
                !slot.getItem().has(DataComponents.CUSTOM_NAME) && string.equals(slot.getItem().getHoverName().getString()) ?
                        "" : string
        );
    }

    public void firstSlotChanged(ItemStack itemStack) {
        if (this.name != null && this.screen != null) {
            this.name.setValue(itemStack.isEmpty() ? "" : itemStack.getHoverName().getString());
            this.name.setEditable(!itemStack.isEmpty());
            if (itemStack.isEmpty()) {
                if (this.screen.getFocused() == this.name) this.screen.clearFocus();
            } else this.screen.setFocused(this.name);
        }
    }

    public class AnvilTabMenu extends CreativeTabMenu<AnvilTabMenu> {
        private final Container inputSlots;
        private final ResultContainer resultSlots = new ResultContainer();

        AnvilTabMenu(Player player) {
            super(player);
            this.inputSlots = MenuHelper.simpleContainer(this, 2);
            this.addSlot(new Slot(this.inputSlots, 0, 36, 47) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    AnvilMenuTab.this.firstSlotChanged(this.getItem());
                }
            });
            this.addSlot(new Slot(this.inputSlots, 1, 85, 47));
            this.addSlot(MenuHelper.resultSlot(this, this.resultSlots, 0, 143, 47));
        }

        @Override
        AnvilTabMenu copyWithPlayer(@NotNull Player player) {
            return this.copyContentsTo(new AnvilTabMenu(player));
        }

        @Override
        public @NotNull ItemStack quickMoveFromInventory(@NotNull Player player, int slotIndex) {
            ItemStack resultStack = ItemStack.EMPTY;
            Slot slot = this.player.inventoryMenu.slots.get(slotIndex);
            if (!slot.hasItem()) return resultStack;

            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (!this.moveItemStackTo(slotStack, 0, 2, false))
                return ItemStack.EMPTY;

            if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (slotStack.getCount() == resultStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, slotStack);

            return resultStack;
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            if (container == this.inputSlots)
                this.resultSlots.setItem(0, this.inputSlots.getItem(0).copy());
            else if (container == this.resultSlots) {
                this.inputSlots.clearContent();
                AnvilMenuTab.this.firstSlotChanged(ItemStack.EMPTY);
            }
        }

        @Override
        public void removed(@NotNull Player player) {
            if (player.hasInfiniteMaterials()) for (int i = 0; i < this.inputSlots.getContainerSize(); i++)
                player.getInventory().placeItemBackInInventory(this.inputSlots.removeItemNoUpdate(i));
        }

        public void setItemName(String string) {
            if (!this.getSlot(2).hasItem()) return;
            ItemStack itemStack = this.getSlot(2).getItem();
            if (StringUtil.isBlank(string)) itemStack.remove(DataComponents.CUSTOM_NAME);
            else itemStack.set(DataComponents.CUSTOM_NAME, Component.literal(string));
        }
    }
}
