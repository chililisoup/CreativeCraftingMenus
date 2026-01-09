package dev.chililisoup.creativecraftingmenus.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.datafixers.util.Pair;
import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.chililisoup.creativecraftingmenus.util.ServerResourceProvider;
import dev.chililisoup.creativecraftingmenus.util.MenuHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screens.inventory.SmithingScreen.ARMOR_STAND_ANGLE;
import static net.minecraft.client.gui.screens.inventory.SmithingScreen.ARMOR_STAND_TRANSLATION;
import static net.minecraft.client.gui.screens.inventory.StonecutterScreen.*;

public class SmithingMenuTab extends CreativeMenuTab<SmithingMenuTab.SmithingTabMenu, SmithingMenuTab> {
    private static final Identifier SELECTED_TAB = CreativeCraftingMenus.id("container/creative_menu_inner_tab_selected");
    private static final Identifier UNSELECTED_TAB = CreativeCraftingMenus.id("container/creative_menu_inner_tab_unselected");
    private static final Identifier PLACEHOLDER_TRIM = CreativeCraftingMenus.id("container/placeholder_trim_smithing_template");

    private final ArmorStandRenderState armorStandPreview = new ArmorStandRenderState();
    private final ArrayList<Pair<Holder.Reference<@NotNull TrimPattern>, ItemStack>> trimTemplates = new ArrayList<>();
    private Page selectedPage = Page.TRIM_PATTERN;

    public SmithingMenuTab(Component displayName, Supplier<ItemStack> iconGenerator) {
        super(SmithingTabMenu::new, displayName, iconGenerator);

        this.armorStandPreview.entityType = EntityType.ARMOR_STAND;
        this.armorStandPreview.showBasePlate = false;
        this.armorStandPreview.showArms = true;
        this.armorStandPreview.xRot = 25.0F;
        this.armorStandPreview.bodyRot = 210.0F;
    }

    @Override
    public void subInit() {
        if (trimTemplates.isEmpty()) {
            trimTemplates.add(Pair.of(null, Items.BARRIER.getDefaultInstance()));
            trimTemplates.addAll(ServerResourceProvider.getRegistryElements(Registries.TRIM_PATTERN).stream().map(
                    patternRef -> Pair.of(
                            patternRef,
                            BuiltInRegistries.ITEM.get(
                                    patternRef.value().assetId().withSuffix("_armor_trim_smithing_template")
                            ).map(ref -> ref.value().getDefaultInstance()).orElse(ItemStack.EMPTY)
                    )
            ).toList());
        }

//        CreativeCraftingMenus.LOGGER.info(ServerResourceProvider.getFromComponent(DataComponents.PROVIDES_TRIM_MATERIAL));
    }

    @Override
    public void render(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SCROLLER_SPRITE,
                screen.leftPos + 118,
                screen.topPos + 16,
                12,
                15
        );

        guiGraphics.submitEntityRenderState(
                this.armorStandPreview,
                25.0F,
                ARMOR_STAND_TRANSLATION, ARMOR_STAND_ANGLE,
                null,
                screen.leftPos + screen.imageWidth - 55,
                screen.topPos + 20,
                screen.leftPos + screen.imageWidth - 15,
                screen.topPos + 80
        );

        for (int i = 0; i < Page.values().length; i++) {
            Page page = Page.values()[i];
            int x = screen.leftPos + 33;
            int y = screen.topPos + 15 + i * 19;
            boolean selected = page == this.selectedPage;

            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    selected ? SELECTED_TAB : UNSELECTED_TAB,
                    x,
                    y,
                    16,
                    18
            );
            guiGraphics.renderItem(page.icon, x, y + 1);
            if (mouseX > x && mouseY > y && mouseX < x + 16 && mouseY < y + 18) {
                if (!selected) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                guiGraphics.setTooltipForNextFrame(page.tooltip, mouseX, mouseY);
            }
        }

        this.selectedPage.renderFunction.render(this, screen, guiGraphics);
    }

    private static void renderTrimPatternPage(SmithingMenuTab instance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics) {
        if (instance.menu == null) return;
        ItemStack menuStack = instance.menu.getSlot(1).getItem();
        var trim = menuStack.get(DataComponents.TRIM);

        for (int i = 0; i < instance.trimTemplates.size() && i < 12; i++) {
            int x = screen.leftPos + 51 + (i % 4) * 16;
            int y = screen.topPos + 16 + (i / 4) * 18;

            boolean selected = trim == null ?
                    (i == 0) :
                    trim.pattern() == instance.trimTemplates.get(i).getFirst();

            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    selected ? RECIPE_SELECTED_SPRITE : RECIPE_SPRITE,
                    x,
                    y,
                    16,
                    18
            );
            ItemStack itemStack = instance.trimTemplates.get(i).getSecond();
            if (itemStack.isEmpty())
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PLACEHOLDER_TRIM, x, y + 1, 16, 16);
            else guiGraphics.renderItem(itemStack, x, y + 1);
        }
    }

    private static void renderTrimMaterialPage(SmithingMenuTab instance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics) {

    }

    private static void renderItemMaterialPage(SmithingMenuTab instance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics) {

    }

    private @Nullable Page checkPageClicked(double mouseX, double mouseY) {
        if (this.screen == null) return null;

        for (int i = 0; i < Page.values().length; i++) {
            Page page = Page.values()[i];
            if (page == this.selectedPage) continue;
            int x = this.screen.leftPos + 33;
            int y = this.screen.topPos + 15 + i * 19;

            if (mouseX > x && mouseY > y && mouseX < x + 16 && mouseY < y + 18)
                return page;
        }

        return null;
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent) {
        return checkPageClicked(mouseButtonEvent.x(), mouseButtonEvent.y()) != null;
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        Page page = checkPageClicked(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (page != null) this.selectedPage = page;
        return page != null;
    }

    @Override
    public void remove() {
        this.updateArmorStandPreview(ItemStack.EMPTY);
        super.remove();
    }

    @Override
    public void dispose() {
        this.trimTemplates.clear();
        super.dispose();
    }

    private void updateArmorStandPreview(ItemStack itemStack) {
        this.armorStandPreview.leftHandItemStack = ItemStack.EMPTY;
        this.armorStandPreview.leftHandItemState.clear();
        this.armorStandPreview.headEquipment = ItemStack.EMPTY;
        this.armorStandPreview.headItem.clear();
        this.armorStandPreview.chestEquipment = ItemStack.EMPTY;
        this.armorStandPreview.legsEquipment = ItemStack.EMPTY;
        this.armorStandPreview.feetEquipment = ItemStack.EMPTY;
        if (itemStack.isEmpty()) return;

        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        EquipmentSlot equipmentSlot = equippable != null ? equippable.slot() : null;
        ItemModelResolver itemModelResolver = Minecraft.getInstance().getItemModelResolver();
        switch (equipmentSlot) {
            case HEAD:
                if (HumanoidArmorLayer.shouldRender(itemStack, EquipmentSlot.HEAD))
                    this.armorStandPreview.headEquipment = itemStack.copy();
                else itemModelResolver.updateForTopItem(
                        this.armorStandPreview.headItem,
                        itemStack,
                        ItemDisplayContext.HEAD,
                        null,
                        null,
                        0
                );
                break;
            case CHEST:
                this.armorStandPreview.chestEquipment = itemStack.copy();
                break;
            case LEGS:
                this.armorStandPreview.legsEquipment = itemStack.copy();
                break;
            case FEET:
                this.armorStandPreview.feetEquipment = itemStack.copy();
                break;
            case null:
            default:
                this.armorStandPreview.leftHandItemStack = itemStack.copy();
                itemModelResolver.updateForTopItem(
                        this.armorStandPreview.leftHandItemState,
                        itemStack,
                        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        null,
                        null,
                        0
                );
        }
    }

    private enum Page {
        TRIM_PATTERN (
                Component.translatable("container.creative_crafting_menus.smithing.trim_pattern"),
                Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.getDefaultInstance(),
                SmithingMenuTab::renderTrimPatternPage
        ),
        TRIM_MATERIAL (
                Component.translatable("container.creative_crafting_menus.smithing.trim_material"),
                Items.AMETHYST_SHARD.getDefaultInstance(),
                SmithingMenuTab::renderTrimMaterialPage
        ),
        ITEM_MATERIAL (
                Component.translatable("container.creative_crafting_menus.smithing.item_material"),
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getDefaultInstance(),
                SmithingMenuTab::renderItemMaterialPage
        );

        private final Component tooltip;
        private final ItemStack icon;
        private final RenderFunction renderFunction;

        Page(final Component tooltip, final ItemStack icon, final RenderFunction renderFunction) {
            this.tooltip = tooltip;
            this.icon = icon;
            this.renderFunction = renderFunction;
        }

        private interface RenderFunction {
            void render(SmithingMenuTab instance, AbstractContainerScreen<?> screen, GuiGraphics guiGraphics);
        }
    }

    public static class SmithingTabMenu extends CreativeTabMenu<SmithingMenuTab> {
        private final Container inputSlots;
        private final ResultContainer resultSlots = new ResultContainer();

        SmithingTabMenu(SmithingMenuTab menuTab, Player player) {
            super(menuTab, player);
            this.inputSlots = MenuHelper.simpleContainer(this, 1);
            this.addSlot(new Slot(this.inputSlots, 0, 9, 16));
            this.addSlot(MenuHelper.resultSlot(this, this.resultSlots, 0, 9, 54));
        }

        @Override
        SmithingTabMenu copyWithPlayer(@NotNull Player player) {
            return this.copyContentsTo(new SmithingTabMenu(this.menuTab, player));
        }

        @Override
        public @NotNull ItemStack quickMoveFromInventory(@NotNull Player player, int slotIndex) {
            ItemStack resultStack = ItemStack.EMPTY;
            Slot slot = this.player.inventoryMenu.slots.get(slotIndex);
            if (!slot.hasItem()) return resultStack;

            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            if (!this.moveItemStackTo(slotStack, 0, 1, false))
                return ItemStack.EMPTY;

            if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (slotStack.getCount() == resultStack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, slotStack);

            return resultStack;
        }

        private ItemStack updateResult(ItemStack inputStack) {
            ItemStack result = inputStack.copy();
            result.applyComponents(DataComponentMap.builder().set(
                    DataComponents.TRIM,
                    new ArmorTrim(
                            ServerResourceProvider.getRegistryElements(Registries.TRIM_MATERIAL).getLast(),
                            this.menuTab.trimTemplates.get(6).getFirst()
                    )
            ).build());

            this.resultSlots.setItem(0, result);
            return result;
        }

        @Override
        public void slotsChanged(@NotNull Container container) {
            if (container == this.inputSlots) {
                this.menuTab.updateArmorStandPreview(
                        this.updateResult(this.inputSlots.getItem(0))
                );
            } else if (container == this.resultSlots) {
                this.inputSlots.clearContent();
                this.menuTab.updateArmorStandPreview(ItemStack.EMPTY);
            }
        }

        @Override
        public void removed(@NotNull Player player) {
            if (player.hasInfiniteMaterials()) for (int i = 0; i < this.inputSlots.getContainerSize(); i++)
                player.getInventory().placeItemBackInInventory(this.inputSlots.removeItemNoUpdate(i));
        }
    }
}
