package dev.chililisoup.creativecraftingmenus.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.chililisoup.creativecraftingmenus.config.ModConfig;
import dev.chililisoup.creativecraftingmenus.util.MenuHelper;
import dev.chililisoup.creativecraftingmenus.util.ServerResourceProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minecraft.client.gui.screens.inventory.LoomScreen.*;

public class LoomMenuTab extends CreativeMenuTab<LoomMenuTab.LoomTabMenu, LoomMenuTab> {
    protected static final Identifier SELECTED_LAYER = CreativeCraftingMenus.id("container/layer_selected");
    protected static final Identifier HIGHLIGHTED_LAYER = CreativeCraftingMenus.id("container/layer_highlighted");
    protected static final Identifier UNSELECTED_LAYER = CreativeCraftingMenus.id("container/layer_unselected");
    protected static final Identifier ARROW_UP = CreativeCraftingMenus.id("container/arrow_up");
    protected static final Identifier ARROW_UP_HIGHLIGHTED = CreativeCraftingMenus.id("container/arrow_up_highlighted");
    protected static final Identifier ARROW_DOWN = CreativeCraftingMenus.id("container/arrow_down");
    protected static final Identifier ARROW_DOWN_HIGHLIGHTED = CreativeCraftingMenus.id("container/arrow_down_highlighted");

    private static final DyeColor[] COLORS = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY,    DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED,        DyeColor.ORANGE,  DyeColor.YELLOW,
            DyeColor.LIME,  DyeColor.GREEN,      DyeColor.CYAN,    DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,  DyeColor.PURPLE,     DyeColor.MAGENTA, DyeColor.PINK
    };

    private final ArrayList<Holder.Reference<@NotNull BannerPattern>> patterns = new ArrayList<>();
    private Page.RenderFunction pageRenderer = Page.RenderFunction.EMPTY;
    private Page selectedPage = Page.PATTERN;
    private int selectedLayer = -1;
    private int cachedLayerSize = 0;
    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private BannerFlagModel flag;

    public LoomMenuTab(Component displayName, Supplier<ItemStack> iconGenerator) {
        super(LoomTabMenu::new, displayName, iconGenerator);
    }

    @Override
    public void subInit() {
        this.scrolling = false;

        ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
        this.flag = new BannerFlagModel(modelPart);

        if (patterns.isEmpty())
            patterns.addAll(ServerResourceProvider.getRegistryElements(Registries.BANNER_PATTERN));

        if (this.pageRenderer == Page.RenderFunction.EMPTY)
            this.pageRenderer = this.selectedPage.rendererSupplier.apply(this);
    }

    @Override
    public void drawTitle(TitleDrawer titleDrawer, int x, int y, int color) {
        super.drawTitle(titleDrawer, x, y - 2, color);
    }

    @Override
    public void render(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (this.menu == null) return;

        ItemStack itemStack = this.menu.resultSlots.getItem(0);
        Item item = itemStack.getItem();
        if (!(item instanceof BannerItem bannerItem)) return;
        DyeColor bannerColor = bannerItem.getColor();
        guiGraphics.submitBannerPatternRenderState(
                this.flag,
                bannerColor,
                itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY),
                screen.leftPos + 166,
                screen.topPos + 8,
                screen.leftPos + 186,
                screen.topPos + 48
        );

        for (int i = 0; i < Page.values().length; i++) {
            Page page = Page.values()[i];
            int x = screen.leftPos + 6;
            int y = screen.topPos + 12 + i * 20;
            boolean selected = page == this.selectedPage;

            boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 18;
            if (hovered) {
                if (!selected) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                guiGraphics.setTooltipForNextFrame(page.tooltip, mouseX, mouseY);
            }

            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    selected ? SELECTED_TAB : (hovered ? HIGHLIGHTED_TAB : UNSELECTED_TAB),
                    x,
                    y,
                    16,
                    18
            );
            guiGraphics.renderItem(page.icon, x, y + 1);
        }

        this.renderScrollBar(screen, guiGraphics, mouseX, mouseY);
        this.renderButtons(screen, guiGraphics, mouseX, mouseY);
        this.renderDyes(screen, guiGraphics, mouseX, mouseY);
        this.renderPageContents(screen, guiGraphics, mouseX, mouseY);
    }

    private void renderBannerOnButton(GuiGraphics guiGraphics, int x, int y, TextureAtlasSprite textureAtlasSprite) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x + 4, y + 2);
        float u0 = textureAtlasSprite.getU0();
        float u1 = u0 + (textureAtlasSprite.getU1() - textureAtlasSprite.getU0()) * 21.0F / 64.0F;
        float size = textureAtlasSprite.getV1() - textureAtlasSprite.getV0();
        float v0 = textureAtlasSprite.getV0() + size / 64.0F;
        float v1 = v0 + size * 40.0F / 64.0F;
        guiGraphics.fill(0, 0, 5, 10, DyeColor.GRAY.getTextureDiffuseColor());
        guiGraphics.blit(textureAtlasSprite.atlasLocation(), 0, 0, 5, 10, u0, u1, v0, v1);
        guiGraphics.pose().popMatrix();
    }

    private void renderDyeIcon(GuiGraphics guiGraphics, DyeColor color, int x, int y) {
        if (ModConfig.HANDLER.instance().dyeItemColorIcons) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(x + 1, y);
            guiGraphics.pose().scale(3F / 4F);
            guiGraphics.renderItem(DyeItem.byColor(color).getDefaultInstance(), 0, 0);
            guiGraphics.pose().popMatrix();
        } else guiGraphics.fill(x + 2, y + 2, x + 12, y + 12, color.getTextureDiffuseColor());
    }

    private void renderScrollBar(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = screen.leftPos + 83;
        int y = screen.topPos + 13 + (int) (41.0F * this.scrollOffs);

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.isScrollBarActive() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE,
                x,
                y,
                12,
                15
        );

        if (mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 15)
            guiGraphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
    }

    private void renderButtons(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int left = screen.leftPos + 99;
        int top = screen.topPos + (ModConfig.HANDLER.instance().altLoomMenu ? 67 : 8);

        for (int i = 0; i < 3; i++) {
            String name = switch (i) {
                case 0 -> "add";
                case 1 -> "remove";
                default -> "clear";
            };

            int x = left + i * 20;
            boolean hovered = mouseX >= x && mouseY >= top && mouseX < x + 18 && mouseY < top + 13;
            if (hovered) {
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                guiGraphics.setTooltipForNextFrame(
                        Component.translatable("container.creative_crafting_menus.loom." + name + "_button"),
                        mouseX,
                        mouseY
                );
            }

            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    CreativeCraftingMenus.id(String.format("container/%s_button%s", name, hovered ? "_highlighted" : "")),
                    x,
                    top,
                    18,
                    11
            );
        }
    }

    private void renderDyes(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu == null) return;

        DyeColor dyeColor = this.menu.getColors().get(this.selectedLayer + 1);

        int left = screen.leftPos + 100;
        int top = screen.topPos + (ModConfig.HANDLER.instance().altLoomMenu ? 9 : 21);

        for (int i = 0; i < COLORS.length; i++) {
            int x = left + (i % 4) * 14;
            int y = top + (i / 4) * 14;

            DyeColor color = COLORS[i];
            boolean selected = dyeColor == color;
            boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14;

            if (hovered) {
                if (!selected) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                guiGraphics.setTooltipForNextFrame(DyeItem.byColor(color).getName(), mouseX, mouseY);
            }

            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    selected ? SELECTED_DYE : (hovered ? HIGHLIGHTED_DYE : UNSELECTED_DYE),
                    x,
                    y,
                    14,
                    14
            );

            this.renderDyeIcon(guiGraphics, color, x, y);
        }
    }

    private void renderPageContents(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu == null) return;

        int left = screen.leftPos + 24;
        int top = screen.topPos + 13;

        this.pageRenderer.render(guiGraphics, left, top, mouseX, mouseY);
    }

    private static Page.RenderFunction getPatternPageRenderer(LoomMenuTab instance) {
        if (instance.menu == null) return Page.RenderFunction.EMPTY;

        DyeColor dyeColor = instance.menu.getColors().get(instance.selectedLayer + 1);

        List<BannerPatternLayers.Layer> layers = instance.menu.getLayers();
        Holder<@NotNull BannerPattern> selectedPattern =
                (instance.selectedLayer < 0 || layers.isEmpty()) ?
                        null : layers.get(instance.selectedLayer).pattern();

        return (guiGraphics, left, top, mouseX, mouseY) -> {
            for (int i = instance.startIndex; i < instance.patterns.size() && i < 16 + instance.startIndex; i++) {
                int x = left + ((i - instance.startIndex) % 4) * 14;
                int y = top + ((i - instance.startIndex) / 4) * 14;

                Holder<@NotNull BannerPattern> pattern = instance.patterns.get(i);
                boolean selected = selectedPattern == pattern;
                boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14;

                if (hovered) {
                    if (!selected) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                    guiGraphics.setTooltipForNextFrame(
                            Component.translatable(pattern.value().translationKey() + "." + dyeColor.getName()),
                            mouseX,
                            mouseY
                    );
                }

                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        selected ? PATTERN_SELECTED_SPRITE : (hovered ? PATTERN_HIGHLIGHTED_SPRITE : PATTERN_SPRITE),
                        x,
                        y,
                        14,
                        14
                );

                instance.renderBannerOnButton(guiGraphics, x, y, guiGraphics.getSprite(Sheets.getBannerMaterial(pattern)));
            }
        };
    }

    private static Page.RenderFunction getLayersPageRenderer(LoomMenuTab instance) {
        if (instance.menu == null) return Page.RenderFunction.EMPTY;

        List<DyeColor> colors = instance.menu.getColors();
        ArrayList<BannerPatternLayers.@Nullable Layer> layers = new ArrayList<>();
        layers.add(null);
        layers.addAll(instance.menu.getLayers());

        return (guiGraphics, left, top, mouseX, mouseY) -> {
            for (int i = instance.startIndex; i < layers.size() && i < 4 + instance.startIndex; i++) {
                int y = top + (i - instance.startIndex) * 14;

                BannerPatternLayers.@Nullable Layer layer = layers.get(i);
                Holder<@NotNull BannerPattern> pattern = layer == null ? instance.patterns.getFirst() : layer.pattern();
                DyeColor dyeColor = colors.get(i);

                boolean selected = instance.selectedLayer == i - 1;
                boolean upVisible = i > 1;
                boolean downVisible = i > 0 && i < layers.size() - 1;
                boolean upHovered = upVisible &&
                        mouseX >= left + 45 && mouseY >= y && mouseX < left + 56 && mouseY < y + 7;
                boolean downHovered = downVisible && !upHovered &&
                        mouseX >= left + 45 && mouseY >= y + 7 && mouseX < left + 56 && mouseY < y + 14;
                boolean hovered = !upHovered && !downHovered &&
                        mouseX >= left && mouseY >= y && mouseX < left + 56 && mouseY < y + 14;

                if (hovered) {
                    if (!selected) guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                    guiGraphics.setTooltipForNextFrame(
                            i == 0 ?
                                    instance.menu.getBannerItem().getName() :
                                    Component.translatable(pattern.value().translationKey() + "." + dyeColor.getName()),
                            mouseX,
                            mouseY
                    );
                }

                if (upHovered || downHovered) {
                    guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                    guiGraphics.setTooltipForNextFrame(
                            Component.translatable(
                                    "container.creative_crafting_menus.loom.move_layer_" + (upHovered ? "up" : "down")
                            ),
                            mouseX,
                            mouseY
                    );
                }

                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        selected ? SELECTED_LAYER : (hovered ? HIGHLIGHTED_LAYER : UNSELECTED_LAYER),
                        left,
                        y,
                        56,
                        14
                );

                if (upVisible) guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        upHovered ? ARROW_UP_HIGHLIGHTED : ARROW_UP,
                        left + 46,
                        y + 1,
                        9,
                        6
                );

                if (downVisible) guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        downHovered ? ARROW_DOWN_HIGHLIGHTED : ARROW_DOWN,
                        left + 46,
                        y + 7,
                        9,
                        6
                );

                guiGraphics.drawString(Minecraft.getInstance().font, (i > 0) ? String.valueOf(i) : "-", left + 3, y + 3, 0xFFFFFFFF);
                instance.renderBannerOnButton(guiGraphics, left + 14, y, guiGraphics.getSprite(Sheets.getBannerMaterial(pattern)));
                instance.renderDyeIcon(guiGraphics, dyeColor, left + 28, y);
            }
        };
    }

    private static Page.RenderFunction getPresetsPageRenderer(LoomMenuTab instance) {
        return Page.RenderFunction.EMPTY;
    }

    private static @Nullable Runnable checkPatternPageClicked(LoomMenuTab instance, int left, int top, double mouseX, double mouseY) {
        if (instance.menu == null) return null;

        List<BannerPatternLayers.Layer> layers = instance.menu.getLayers();
        Holder<@NotNull BannerPattern> selectedPattern =
                (instance.selectedLayer < 0 || layers.isEmpty()) ?
                        null : layers.get(instance.selectedLayer).pattern();

        for (int i = instance.startIndex; i < instance.patterns.size() && i < 16 + instance.startIndex; i++) {
            Holder<@NotNull BannerPattern> pattern = instance.patterns.get(i);
            if (selectedPattern == pattern) continue;

            int x = left + ((i - instance.startIndex) % 4) * 14;
            int y = top + ((i - instance.startIndex) / 4) * 14;
            if (mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14)
                return () -> instance.menu.setPattern(pattern, instance.selectedLayer);
        }

        return null;
    }

    private static @Nullable Runnable checkLayersPageClicked(LoomMenuTab instance, int left, int top, double mouseX, double mouseY) {
        if (instance.menu == null) return null;

        int size = instance.menu.getLayers().size() + 1;
        for (int i = instance.startIndex; i < size && i < 4 + instance.startIndex; i++) {
            int y = top + (i - instance.startIndex) * 14;

            boolean upVisible = i > 1;
            boolean downVisible = i > 0 && i < size - 1;

            int layer = i - 1;

            if (upVisible && mouseX >= left + 45 && mouseY >= y && mouseX < left + 56 && mouseY < y + 7)
                return () -> instance.menu.moveLayer(layer, -1);

            if (downVisible && mouseX >= left + 45 && mouseY >= y + 7 && mouseX < left + 56 && mouseY < y + 14)
                return () -> instance.menu.moveLayer(layer, 1);

            if (instance.selectedLayer != i - 1 && mouseX >= left && mouseY >= y && mouseX < left + 56 && mouseY < y + 14)
                return () -> {
                    instance.selectedLayer = layer;
                    instance.update();
                };
        }

        return null;
    }

    private static @Nullable Runnable checkPresetsPageClicked(LoomMenuTab instance, int left, int top, double mouseX, double mouseY) {
        return null;
    }

    private int checkButtonClicked(double mouseX, double mouseY) {
        if (this.screen == null) return -1;

        int left = screen.leftPos + 99;
        int top = screen.topPos + (ModConfig.HANDLER.instance().altLoomMenu ? 67 : 8);

        for (int i = 0; i < 3; i++) {
            int x = left + i * 20;
            if (mouseX >= x && mouseY >= top && mouseX < x + 18 && mouseY < top + 13)
                return i;
        }

        return -1;
    }

    private @Nullable DyeColor checkDyeClicked(double mouseX, double mouseY) {
        if (this.screen == null || this.menu == null) return null;

        DyeColor dyeColor = this.menu.getColors().get(this.selectedLayer + 1);
        int left = this.screen.leftPos + 100;
        int top = this.screen.topPos + (ModConfig.HANDLER.instance().altLoomMenu ? 9 : 21);

        for (int i = 0; i < COLORS.length; i++) {
            DyeColor color = COLORS[i];
            if (dyeColor == color) continue;

            int x = left + (i % 4) * 14;
            int y = top + (i / 4) * 14;
            if (mouseX >= x && mouseY >= y && mouseX < x + 14 && mouseY < y + 14)
                return color;
        }

        return null;
    }

    private @Nullable Page checkPageClicked(double mouseX, double mouseY) {
        if (this.screen == null) return null;

        for (int i = 0; i < Page.values().length; i++) {
            Page page = Page.values()[i];
            if (page == this.selectedPage) continue;

            int x = this.screen.leftPos + 6;
            int y = this.screen.topPos + 12 + i * 20;
            if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 18)
                return page;
        }

        return null;
    }

    private @Nullable Runnable checkPageContentsClicked(double mouseX, double mouseY) {
        return this.screen != null ? this.selectedPage.clickChecker.check(
                this,
                this.screen.leftPos + 24,
                this.screen.topPos + 13,
                mouseX,
                mouseY
        ) : null;
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent) {
        if (this.screen == null) return false;

        int x = this.screen.leftPos + 83;
        int y = this.screen.topPos + 13;
        if (mouseButtonEvent.x() >= x && mouseButtonEvent.x() < x + 12 && mouseButtonEvent.y() >= y && mouseButtonEvent.y() < y + 56)
            this.scrolling = true;

        if (checkButtonClicked(mouseButtonEvent.x(), mouseButtonEvent.y()) >= 0) return true;
        if (checkPageClicked(mouseButtonEvent.x(), mouseButtonEvent.y()) != null) return true;
        if (checkDyeClicked(mouseButtonEvent.x(), mouseButtonEvent.y()) != null) return true;
        return checkPageContentsClicked(mouseButtonEvent.x(), mouseButtonEvent.y()) != null;
    }

    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        this.scrolling = false;
        if (this.menu == null) return false;

        int button = checkButtonClicked(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (button >= 0) {
            switch (button) {
                case 0 -> this.menu.addLayer(this.patterns.get(1));
                case 1 -> this.menu.removeLayer(this.selectedLayer);
                default -> this.menu.resetBanner();
            }
            return true;
        }

        Page page = checkPageClicked(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (page != null) {
            this.selectedPage = page;
            this.pageRenderer = this.selectedPage.rendererSupplier.apply(this);
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
            return true;
        }

        DyeColor color = checkDyeClicked(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (color != null) {
            this.menu.setColor(color, this.selectedLayer);
            return true;
        }

        Runnable onClick = checkPageContentsClicked(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (onClick != null) {
            onClick.run();
            return true;
        }

        return false;
    }

    private boolean isScrollBarActive() {
        return this.getOffscreenRows() > 0;
    }

    private int getOffscreenRows() {
        return this.selectedPage.getOffscreenRows.apply(this);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent) {
        if (this.screen == null || !this.scrolling || !this.isScrollBarActive())
            return false;

        int top = this.screen.topPos + 13;
        int bottom = top + 56;
        this.scrollOffs = ((float) mouseButtonEvent.y() - top - 7.5F) / (bottom - top - 15.0F);
        this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
        this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5) * this.selectedPage.columns;
        return true;
    }

    @Override
    public boolean mouseScrolled(double distance) {
        if (this.isScrollBarActive()) {
            int offscreenRows = this.getOffscreenRows();
            float deltaY = (float) distance / offscreenRows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - deltaY, 0.0F, 1.0F);
            this.startIndex = (int) (this.scrollOffs * offscreenRows + 0.5) * this.selectedPage.columns;
        }

        return true;
    }

    @Override
    public void remove() {
        this.selectedPage = Page.PATTERN;
        this.pageRenderer = Page.RenderFunction.EMPTY;
        this.selectedLayer = -1;
        this.cachedLayerSize = 0;
        this.scrollOffs = 0.0F;
        this.startIndex = 0;
        super.remove();
    }

    @Override
    public void dispose() {
        this.patterns.clear();
        this.selectedPage = Page.PATTERN;
        this.pageRenderer = Page.RenderFunction.EMPTY;
        this.selectedLayer = -1;
        this.cachedLayerSize = 0;
        this.scrollOffs = 0.0F;
        this.startIndex = 0;
        super.dispose();
    }

    private void update() {
        if (this.menu == null) return;

        List<BannerPatternLayers.Layer> layers = this.menu.getLayers();
        if (layers.size() != this.cachedLayerSize) {
            this.selectedLayer = (this.cachedLayerSize > layers.size()) ?
                    Math.min(this.selectedLayer, layers.size() - 1) :
                    layers.size() - 1;
            this.cachedLayerSize = layers.size();
        }

        this.startIndex = (int) (this.scrollOffs * this.getOffscreenRows() + 0.5) * this.selectedPage.columns;
        this.pageRenderer = this.selectedPage.rendererSupplier.apply(this);
    }

    private enum Page {
        PATTERN(
                Component.translatable("container.creative_crafting_menus.loom.pattern"),
                Items.CREEPER_BANNER_PATTERN.getDefaultInstance(),
                LoomMenuTab::getPatternPageRenderer,
                LoomMenuTab::checkPatternPageClicked,
                menuTab -> (menuTab.patterns.size() - 13) / 4,
                4
        ),
        LAYERS(
                Component.translatable("container.creative_crafting_menus.loom.layers"),
                Items.BOOK.getDefaultInstance(),
                LoomMenuTab::getLayersPageRenderer,
                LoomMenuTab::checkLayersPageClicked,
                menuTab -> menuTab.menu != null ? menuTab.menu.getLayers().size() - 3 : 0,
                1
        ),
        PRESETS(
                Component.translatable("container.creative_crafting_menus.loom.presets"),
                Items.WHITE_BANNER.getDefaultInstance(),
                LoomMenuTab::getPresetsPageRenderer,
                LoomMenuTab::checkPresetsPageClicked,
                menuTab -> (menuTab.patterns.size() - 5) / 4,
                4
        );

        private final Component tooltip;
        private final ItemStack icon;
        private final Function<LoomMenuTab, RenderFunction> rendererSupplier;
        private final ClickChecker clickChecker;
        private final Function<LoomMenuTab, Integer> getOffscreenRows;
        private final int columns;

        Page(
                final Component tooltip,
                final ItemStack icon,
                final Function<LoomMenuTab, RenderFunction> rendererSupplier,
                final ClickChecker clickChecker,
                final Function<LoomMenuTab, Integer> getOffscreenRows,
                final int columns
                ) {
            this.tooltip = tooltip;
            this.icon = icon;
            this.clickChecker = clickChecker;
            this.rendererSupplier = rendererSupplier;
            this.getOffscreenRows = getOffscreenRows;
            this.columns = columns;
        }

        private interface RenderFunction {
            RenderFunction EMPTY = (guiGraphics, left, top, mouseX, mouseY) -> {};

            void render(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY);
        }

        private interface ClickChecker {
            @Nullable Runnable check(LoomMenuTab instance, int left, int top, double mouseX, double mouseY);
        }
    }

    public static class LoomTabMenu extends CreativeTabMenu<LoomMenuTab> {
        private final ResultContainer resultSlots = new ResultContainer();

        LoomTabMenu(LoomMenuTab menuTab, Player player) {
            super(menuTab, player);

            LoomTabMenu self = this;
            this.addSlot(new Slot(this.resultSlots, 0, 168, 57) {
                @Override
                public boolean mayPlace(@NotNull ItemStack itemStack) {
                    return false;
                }

                @Override
                public void onTake(@NotNull Player player, @NotNull ItemStack itemStack) {
                    self.onTake(itemStack);
                }
            });

            this.addSlot(MenuHelper.resultSlot(this, this.resultSlots, 0, 168, 57));
            this.resultSlots.setItem(0, Items.WHITE_BANNER.getDefaultInstance());
        }

        @Override
        LoomTabMenu copyWithPlayer(@NotNull Player player) {
            return this.copyContentsTo(new LoomTabMenu(this.menuTab, player));
        }

        @Override
        public @NotNull ItemStack quickMoveFromInventory(@NotNull Player player, int slotIndex) {
            Slot slot = this.player.inventoryMenu.slots.get(slotIndex);
            if (!slot.hasItem()) return ItemStack.EMPTY;

            ItemStack slotStack = slot.getItem();

            if (slotStack.getItem() instanceof BannerItem) {
                this.resultSlots.setItem(0, slotStack.copyWithCount(1));
                this.menuTab.update();
            }

            return ItemStack.EMPTY;
        }

        private void addLayer(Holder<@NotNull BannerPattern> pattern) {
            ItemStack itemStack = this.resultSlots.getItem(0);
            if (itemStack.isEmpty()) return;

            if (itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers().size() >= 16)
                return;

            itemStack.update(
                    DataComponents.BANNER_PATTERNS,
                    BannerPatternLayers.EMPTY,
                    bannerPatternLayers -> new BannerPatternLayers.Builder().addAll(bannerPatternLayers).add(pattern, DyeColor.WHITE).build()
            );
            this.menuTab.update();
        }

        private void removeLayer(int layer) {
            if (layer < 0) return;

            ItemStack itemStack = this.resultSlots.getItem(0);
            if (itemStack.isEmpty()) return;

            ArrayList<BannerPatternLayers.Layer> layers = new ArrayList<>(this.getLayers());
            if (layer >= layers.size()) return;
            layers.remove(layer);

            itemStack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(layers));
            this.menuTab.update();
        }

        private void setPattern(Holder<@NotNull BannerPattern> pattern, int layer) {
            if (layer < 0) return;

            ItemStack itemStack = this.resultSlots.getItem(0);
            if (itemStack.isEmpty()) return;

            List<BannerPatternLayers.Layer> layers = this.getLayers();
            if (layer >= layers.size()) return;

            ArrayList<BannerPatternLayers.Layer> updated = new ArrayList<>(layers);
            updated.set(layer, new BannerPatternLayers.Layer(pattern, layers.get(layer).color()));

            itemStack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(updated));
            this.menuTab.update();
        }

        private void setColor(DyeColor color, int layer) {
            ItemStack itemStack = this.resultSlots.getItem(0);
            if (itemStack.isEmpty()) return;

            if (layer < 0) {
                ItemStack swapped = BannerBlock.byColor(color).asItem().getDefaultInstance();
                swapped.applyComponents(itemStack.getComponentsPatch());

                this.resultSlots.setItem(0, swapped);
                this.menuTab.update();
                return;
            }

            List<BannerPatternLayers.Layer> layers = this.getLayers();
            if (layer >= layers.size()) return;

            ArrayList<BannerPatternLayers.Layer> updated = new ArrayList<>(layers);
            updated.set(layer, new BannerPatternLayers.Layer(layers.get(layer).pattern(), color));

            itemStack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(updated));
            this.menuTab.update();
        }

        private void moveLayer(int layer, int change) {
            if (layer < 0) return;

            ItemStack itemStack = this.resultSlots.getItem(0);
            if (itemStack.isEmpty()) return;

            List<BannerPatternLayers.Layer> layers = this.getLayers();
            if (layer >= layers.size()) return;

            ArrayList<BannerPatternLayers.Layer> updated = new ArrayList<>(layers);
            BannerPatternLayers.Layer from = updated.get(layer);
            BannerPatternLayers.Layer to = updated.get(layer + change);
            updated.set(layer, to);
            updated.set(layer + change, from);

            itemStack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(updated));
            this.menuTab.update();
        }

        private void resetBanner() {
            this.resultSlots.setItem(0, Items.WHITE_BANNER.getDefaultInstance());
            this.menuTab.update();
        }

        private List<BannerPatternLayers.Layer> getLayers() {
            return this.resultSlots.getItem(0).getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers();
        }

        private List<DyeColor> getColors() {
            ArrayList<DyeColor> list = new ArrayList<>();
            list.add(this.getBannerItem().getColor());
            list.addAll(this.getLayers().stream().map(BannerPatternLayers.Layer::color).toList());
            return list;
        }

        private BannerItem getBannerItem() {
            Item item = this.resultSlots.getItem(0).getItem();
            if (item instanceof BannerItem bannerItem) return bannerItem;

            BannerItem bannerItem = (BannerItem) Items.WHITE_BANNER;
            this.resultSlots.setItem(0, bannerItem.getDefaultInstance());
            this.menuTab.update();
            return bannerItem;
        }

        private void onTake(ItemStack itemStack) {
            ItemStack copy = itemStack.copy();
            Minecraft.getInstance().schedule(() -> this.resultSlots.setItem(0, copy));
        }
    }
}
