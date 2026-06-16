package dev.chililisoup.creativecraftingmenus.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.chililisoup.creativecraftingmenus.config.ModConfig;
import dev.chililisoup.creativecraftingmenus.gui.CreativeMenuTab;
import dev.chililisoup.creativecraftingmenus.gui.LoomMenuTab;
import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

//? if >= 1.21.11 {
import com.mojang.blaze3d.platform.cursor.CursorTypes;
//?}

//? if > 1.21.6 {
import net.minecraft.client.input.CharacterEvent;
//?}

//? if >= 26
@SuppressWarnings({"NameDoesntMatchTargetClass", "LocalMayUseName"})
@Mixin(value = CreativeModeInventoryScreen.class, priority = 999)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.@NotNull ItemPickerMenu> {
    @Unique private static final Identifier CRAFTING_INVENTORY_BACKGROUND =
            CreativeCraftingMenus.id("textures/gui/container/creative_crafting_inventory.png");
    @Unique private static final Identifier ALT_LOOM_MENU_BACKGROUND =
            CreativeCraftingMenus.id("textures/gui/container/creative_loom_menu_alt.png");
    @Unique private static final Identifier SELECTED_MENU_TAB = CreativeCraftingMenus.id("container/creative_menu_tab_selected");
    @Unique private static final Identifier UNSELECTED_MENU_TAB = CreativeCraftingMenus.id("container/creative_menu_tab_unselected");

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Shadow @Final
    //? if >= 26
    private
    static SimpleContainer CONTAINER;
    @Shadow protected abstract void selectTab(CreativeModeTab tab);
    @Shadow protected abstract boolean checkTabClicked(CreativeModeTab tab, double xm, double ym);
    @Shadow protected abstract int getTabX(CreativeModeTab tab);
    @Shadow protected abstract int getTabY(CreativeModeTab tab);
    @Shadow public abstract boolean isInventoryOpen();
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private CreativeInventoryListener listener;
    @Shadow private EditBox searchBox;
    @Shadow private @Nullable List<Slot> originalSlots;
    @Shadow private @Nullable Slot destroyItemSlot;

    @Unique
    private void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
        this.topPos = (this.height - this.imageHeight) / 2;
        if (this.searchBox != null) this.searchBox.setY(this.topPos + 6);
    }

    @Unique
    private void resetHeight() {
        this.setImageHeight(136);
    }

    @Inject(
            method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTabs;getDefaultTab()Lnet/minecraft/world/item/CreativeModeTab;",
            ordinal = 0
    ))
    private void menuTabInit(CallbackInfo ci) {
        //? if < 1.21.11
        //if (this.minecraft == null) return;

        if (selectedTab instanceof CreativeMenuTab<?> menuTab)
            menuTab.init(this, this.minecraft.player);
    }

    @Inject(
            method = "init", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/InventoryMenu;removeSlotListener(Lnet/minecraft/world/inventory/ContainerListener;)V"
    ))
    private void menuTabSubInit(CallbackInfo ci) {
        if (selectedTab instanceof CreativeMenuTab<?> menuTab)
            menuTab.subInit();
    }

    @Inject(method = "selectTab", at = @At("HEAD"))
    private void updateMenuTabs(CreativeModeTab tab, CallbackInfo ci) {
        //? if < 1.21.11
        //if (this.minecraft == null) return;

        if (selectedTab == tab) return;
        if (selectedTab instanceof CreativeMenuTab<?> oldTab)
            oldTab.remove();
        if (this.listener != null && tab instanceof CreativeMenuTab<?> newTab)
            newTab.init(this, this.minecraft.player);
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void makeAdjustments(CreativeModeTab tab, CallbackInfo ci) {
        int targetHeight;
        if (tab instanceof CreativeMenuTab<?> newTab && Minecraft.getInstance().gui.screen() == this) {
            if (this.listener != null) newTab.subInit();
            targetHeight = 166;
        } else targetHeight = 136;

        if (this.imageHeight != targetHeight) {
            this.setImageHeight(targetHeight);
            this.repositionElements();
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void cleanup(CallbackInfo ci) {
        this.resetHeight();
    }

    @Inject(method = "getTabX", at = @At("HEAD"), cancellable = true)
    private void getMenuTabX(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
        if (tab instanceof CreativeMenuTab) {
            ModConfig config = ModConfig.HANDLER.instance();
            cir.setReturnValue(config.tabAlignment == ModConfig.Alignment.RIGHT ?
                    this.imageWidth + config.tabSpacingX :
                    -config.tabSpacingX - 26
            );
        }
    }

    @Inject(method = "getTabY", at = @At("HEAD"), cancellable = true)
    private void getMenuTabY(CreativeModeTab tab, CallbackInfoReturnable<Integer> cir) {
        if (tab instanceof CreativeMenuTab) {
            int tabSpacingY = ModConfig.HANDLER.instance().tabSpacingY;
            // TODO: Filter once every time the config changes instead of every damn time this method is called
            List<CreativeMenuTab<?>> filtered = CreativeMenuTabs.MENU_TABS.stream().filter(CreativeMenuTab::shouldDisplay).toList();
            int count = filtered.size();
            int index = filtered.indexOf(tab);
            int size = (count - 1) * tabSpacingY + count * 26;
            int top = (this.height - size) / 2;
            cir.setReturnValue(top + index * (26 + tabSpacingY) - this.topPos);
        }
    }

    @Inject(
            //? if < 26 {
            /*method = "renderTabButton",
            *///?} else
            method = "extractTabButton",
            at = @At("HEAD"),
            cancellable = true
    )
    //? if < 1.21.11 {
    /*private void renderMenuTabButton(GuiGraphicsExtractor guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
    *///?} else
    private void renderMenuTabButton(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, CreativeModeTab tab, CallbackInfo ci) {
        if (!(tab instanceof CreativeMenuTab)) return;

        boolean selected = tab == selectedTab && Minecraft.getInstance().gui.screen() == this;
        int x = this.getTabX(tab) + this.leftPos;
        int y = this.getTabY(tab) + this.topPos;
        Identifier sprite = selected ? SELECTED_MENU_TAB : UNSELECTED_MENU_TAB;

        //? if >= 1.21.11 {
        if (!selected && mouseX > x && mouseY > y && mouseX < x + 26 && mouseY < y + 26)
            guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
        //?}

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 26, 26);
        guiGraphics.item(tab.getIconItem(), x + 5, y + 5);

        ci.cancel();
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderLabels", at = @At(
            *///?} else
            method = "extractLabels", at = @At(
            value = "INVOKE",
            //? if >= 26 {
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
            //?} elif >= 1.21.6 {
            /*target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
            *///?} else
            //target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"
    ))
    //? if >= 1.21.6 {
    private void drawMenuTabLabels(
    //?} else
    //private int drawMenuTabLabels(
            GuiGraphicsExtractor guiGraphics,
            Font font,
            Component title,
            int x,
            int y,
            int color,
            boolean dropShadow,
            Operation</*? >= 1.21.6 {*/Void/*?} else {*//*Integer*//*?}*/> original
    ) {
        if (selectedTab instanceof CreativeMenuTab<?> menuTab) {
            menuTab.drawTitle(
                    (mX, mY, mColor) -> original.call(guiGraphics, font, title, mX, mY, mColor, dropShadow),
                    x,
                    y,
                    color
            );
            /*? < 1.21.6 {*//*return*//*?}*/ guiGraphics.text(font, this.playerInventoryTitle, 9, this.imageHeight - 94, color, dropShadow);
        } else /*? < 1.21.6 {*//*return*//*?}*/ original.call(guiGraphics, font, title, x, y, color, dropShadow);
    }

    @Inject(
            //? if < 26 {
            /*method = "renderBg",
            *///?} else
            method = "extractBackground",
            at = @At("TAIL")
    )
    private void renderTabMenu(
            //? if < 26 {
            /*GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci
            *///?} else
            GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci
    ) {
        if (selectedTab instanceof CreativeMenuTab<?> menuTab)
            menuTab.render(this, guiGraphics, partialTick, mouseX, mouseY);
    }

    @Inject(method = "checkTabClicked", at = @At("HEAD"), cancellable = true)
    private void checkMenuTabClicked(CreativeModeTab tab, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (tab instanceof CreativeMenuTab) {
            int x = this.getTabX(tab);
            int y = this.getTabY(tab);
            cir.setReturnValue(mouseX >= x && mouseX <= x + 26 && mouseY >= y && mouseY <= y + 26);
        }
    }

    @Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
    private void checkMenuTabHovering(
            GuiGraphicsExtractor guiGraphics,
            CreativeModeTab tab,
            int mouseX,
            int mouseY,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(tab instanceof CreativeMenuTab)) return;
        if (this.isHovering(this.getTabX(tab) + 3, this.getTabY(tab) + 3, 21, 21, mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(this.font, tab.getDisplayName(), mouseX, mouseY);
            cir.setReturnValue(true);
        } else cir.setReturnValue(false);
    }

    @WrapOperation(
            method = "slotClicked", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/InventoryMenu;clicked(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V")
    )
    private void modifyInventoryClick(
            InventoryMenu instance,
            int slotIndex,
            int mouseButton,
            ContainerInput clickType,
            Player player,
            Operation<Void> original,
            @Local(argsOnly = true) @Nullable Slot slot
    ) {
        if (!(selectedTab instanceof CreativeMenuTab<?> menuTab)) {
            original.call(instance, slotIndex, mouseButton, clickType, player);
        } else if (clickType == ContainerInput.QUICK_MOVE && (mouseButton == 0 || mouseButton == 1) && slotIndex >= 0) {
            CreativeMenuTab.CreativeTabMenu<?> tabMenu = menuTab.getMenu();
            if (slot != null && slot.container instanceof Inventory) {
                ItemStack itemStack = tabMenu.quickMoveFromInventory(player, slotIndex);
                while (!itemStack.isEmpty() && ItemStack.isSameItem(slot.getItem(), itemStack))
                    itemStack = tabMenu.quickMoveFromInventory(player, slotIndex);
            } else tabMenu.clicked(slotIndex, mouseButton, clickType, player);
        } else this.menu.clicked(slot == null ? slotIndex : slot.index, mouseButton, clickType, player);
    }

    @Definition(id = "destroyItemSlot", field = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;destroyItemSlot:Lnet/minecraft/world/inventory/Slot;")
    @Expression("? == this.destroyItemSlot")
    @WrapOperation(method = "slotClicked", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean clearMenuTabSlots(
            Object left, Object right, Operation<Boolean> original, @Local(argsOnly = true) ContainerInput clickType
    ) {
        boolean base = original.call(left, right);
        if (!base) return false;
        if (!(selectedTab instanceof CreativeMenuTab)) return true;
        if (clickType == ContainerInput.QUICK_MOVE)
            this.menu.slots.forEach(slot -> {
                if (slot.mayPlace(ItemStack.EMPTY))
                    slot.set(ItemStack.EMPTY);
            });
        return true;
    }

    @Inject(
            method = "keyPressed", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"),
            cancellable = true
    )
    private void menuTabKeyPressed(
            //? if > 1.21.6 {
            KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir
            //?} else
            //int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir
    ) {
        //? if <= 1.21.6
        //KeyEvent keyEvent = new KeyEvent(keyCode, scanCode, modifiers);

        if (selectedTab instanceof CreativeMenuTab<?> menuTab && menuTab.keyPressed(keyEvent))
            cir.setReturnValue(true);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void allowMenuTabTyping(
            //? if > 1.21.6 {
            CharacterEvent characterEvent, CallbackInfoReturnable<Boolean> cir
            //?} else
            //char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir
    ) {
        if (selectedTab instanceof CreativeMenuTab)
            cir.setReturnValue(super.charTyped(
                    //? if > 1.21.6 {
                    characterEvent
                    //?} else
                    //codePoint, modifiers
            ));
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void menuTabMouseClicked(
            //? if > 1.21.6 {
            MouseButtonEvent mouseButtonEvent, boolean isDoubleClick, CallbackInfoReturnable<Boolean> cir
            //?} else
            //double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir
    ) {
        //? if <= 1.21.6
        //MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(mouseX, mouseY, button);

        if (selectedTab instanceof CreativeMenuTab<?> menuTab && menuTab.mouseClicked(mouseButtonEvent))
            cir.setReturnValue(true);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void menuTabMouseReleased(
            //? if > 1.21.6 {
            MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> cir
            //?} else
            //double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir
    ) {
        //? if <= 1.21.6
        //MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(mouseX, mouseY, button);

        if (selectedTab instanceof CreativeMenuTab<?> menuTab && menuTab.mouseReleased(mouseButtonEvent))
            cir.setReturnValue(true);
    }

    @Inject(
            method = "mouseReleased", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"),
            cancellable = true
    )
    private void checkTabReleased(
            //? if > 1.21.6 {
            MouseButtonEvent mouseButtonEvent,
            CallbackInfoReturnable<Boolean> cir,
            @Local(ordinal = 0) double x,
            @Local(ordinal = 1) double y
            //?} else {
            /*double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> cir,
            @Local(ordinal = 2) double x,
            @Local(ordinal = 3) double y
            *///?}
    ) {
        for (CreativeMenuTab<?> menuTab : CreativeMenuTabs.MENU_TABS.stream().filter(CreativeMenuTab::shouldDisplay).toList()) {
            if (this.checkTabClicked(menuTab, x, y)) {
                this.selectTab(menuTab);
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(
            method = "mouseDragged", at = @At(
            value = "INVOKE",
            //? if > 1.21.6 {
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"),
            //?} else
            //target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;mouseDragged(DDIDD)Z"),
            cancellable = true
    )
    private void menuTabDragged(
            //? if > 1.21.6 {
            MouseButtonEvent mouseButtonEvent, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir
             //?} else
            //double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir
    ) {
        //? if <= 1.21.6
        //MouseButtonEvent mouseButtonEvent = new MouseButtonEvent(mouseX, mouseY, button);

        if (selectedTab instanceof CreativeMenuTab<?> menuTab && menuTab.mouseDragged(mouseButtonEvent))
            cir.setReturnValue(true);
    }

    @WrapOperation(
            method = "mouseScrolled", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;mouseScrolled(DDDD)Z")
    )
    private boolean menuTabScrolled(
            CreativeModeInventoryScreen instance,
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            Operation<Boolean> original
    ) {
        if (original.call(instance, mouseX, mouseY, scrollX, scrollY)) return true;
        return selectedTab instanceof CreativeMenuTab<?> menuTab && menuTab.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }

    @Definition(id = "oldTab", local = @Local(type = CreativeModeTab.class, ordinal = 1))
    @Definition(id = "getType", method = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;")
    @Definition(id = "INVENTORY", field = "Lnet/minecraft/world/item/CreativeModeTab$Type;INVENTORY:Lnet/minecraft/world/item/CreativeModeTab$Type;")
    @Expression("oldTab.getType() == INVENTORY")
    @ModifyExpressionValue(method = "selectTab", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean setupMenuTab(boolean original, @Local(ordinal = 1) CreativeModeTab oldTab) {
        if (!(selectedTab instanceof CreativeMenuTab<?> menuTab))
            return original || oldTab.getType() == CreativeModeTab.Type.CREATIVE_CRAFTING_MENUS_MENU;

        if (this.minecraft.player == null) return false;
        AbstractContainerMenu invMenu = this.minecraft.player.inventoryMenu;
        if (this.originalSlots == null) this.originalSlots = ImmutableList.copyOf(this.menu.slots);
        this.menu.slots.clear();

        for (int i = 9; i < 45; i++) {
            CreativeModeInventoryScreen.SlotWrapper wrapped = new CreativeModeInventoryScreen.SlotWrapper(
                    invMenu.slots.get(i),
                    i,
                    9 + (i % 9) * 18,
                    i >= 36 ? 142 : 66 + (i / 9) * 18
            );
            wrapped.index = this.menu.slots.size();
            this.menu.slots.add(wrapped);
        }

        this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 142);
        this.menu.slots.add(this.destroyItemSlot);

        menuTab.getMenu().slots.forEach(slot -> {
            CreativeModeInventoryScreen.SlotWrapper wrapped =
                    new CreativeModeInventoryScreen.SlotWrapper(slot, slot.index, slot.x, slot.y);
            wrapped.index = this.menu.slots.size();
            this.menu.slots.add(wrapped);
        });

        return false;
    }

    @WrapOperation(
            method = "selectTab", at = @At(
            value = "NEW",
            target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen$SlotWrapper;"
    ))
    private static CreativeModeInventoryScreen.SlotWrapper moveSlots(Slot slot, int index, int x, int y, Operation<CreativeModeInventoryScreen.SlotWrapper> original) {
        if (ModConfig.HANDLER.instance().inventoryCraftingGrid) {
            if (index == 0) {
                x = 173;
                y = 20;
            } else if (index >= 1 && index < 5) {
                x = 117 + ((index + 1) % 2) * 18;
                y = 10 + ((index - 1) / 2) * 18;
            } else if ((index >= 5 && index < 9) || index == 45)
                x -= 27;
        }

        return original.call(slot, index, x, y);
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderBg", at = @At(
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V",
            *///?} else {
            method = "extractBackground", at = @At(
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V",
            //?}
            value = "INVOKE"
    ))
    private static void movePaperDoll(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int a, float b, float c, float d, LivingEntity livingEntity, Operation<Void> original) {
        if (ModConfig.HANDLER.instance().inventoryCraftingGrid)
            original.call(guiGraphics, x1 - 27, y1, x2 - 27, y2, a, b, c, d, livingEntity);
        else original.call(guiGraphics, x1, y1, x2, y2, a, b, c, d, livingEntity);
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "renderBg", at = @At(
            *///?} else
            method = "extractBackground", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab;getBackgroundTexture()Lnet/minecraft/resources/Identifier;"
    ))
    private Identifier swapBackgroundTexture(CreativeModeTab instance, Operation<Identifier> original) {
        if (instance instanceof LoomMenuTab) return ModConfig.HANDLER.instance().altLoomMenu ?
                ALT_LOOM_MENU_BACKGROUND :
                original.call(instance);
        else return this.isInventoryOpen() && ModConfig.HANDLER.instance().inventoryCraftingGrid ?
                CRAFTING_INVENTORY_BACKGROUND :
                original.call(instance);
    }

    @WrapOperation(
            method = "slotClicked", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"
    ))
    private CreativeModeTab.Type wrapSlotClickedInventoryCheck(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        CreativeModeTab.Type type = original.call(instance);
        return type == CreativeModeTab.Type.CREATIVE_CRAFTING_MENUS_MENU ?
                CreativeModeTab.Type.INVENTORY : type;
    }

    @WrapOperation(
            //? if < 26 {
            /*method = "render", at = @At(
            *///?} else
            method = "extractRenderState", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"
    ))
    private CreativeModeTab.Type wrapRenderInventoryCheck(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        CreativeModeTab.Type type = original.call(instance);
        return type == CreativeModeTab.Type.CREATIVE_CRAFTING_MENUS_MENU ?
                CreativeModeTab.Type.INVENTORY : type;
    }
}
