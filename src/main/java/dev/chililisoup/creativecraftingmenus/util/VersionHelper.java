package dev.chililisoup.creativecraftingmenus.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

//? if <= 1.21.6 {
/*import net.minecraft.client.gui.screens.Screen;
*///?}

//? if < 1.21.11 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
*///?}

//? if < 26 {
/*import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.item.DyeItem;
*///?}

public final class VersionHelper {
    //? if >= 26 {
    private static @Nullable Map<DyeColor, Item> DYE_ITEMS = null;
    private static @Nullable Map<DyeColor, Item> BANNER_ITEMS = null;

    private static Map<DyeColor, Item> getDyeItemMap() {
        if (DYE_ITEMS != null) return DYE_ITEMS;
        DYE_ITEMS = ServerResourceProvider.getFromComponent(DataComponents.DYE).stream().collect(Collectors.toMap(
                item -> item.components().getOrDefault(DataComponents.DYE, DyeColor.WHITE),
                item -> item
        ));
        return DYE_ITEMS;
    }

    private static Map<DyeColor, Item> getBannerItemMap() {
        if (BANNER_ITEMS != null) return BANNER_ITEMS;
        BANNER_ITEMS = ServerResourceProvider.getFromTag(ItemTags.BANNERS).stream().collect(Collectors.toMap(
                item -> item instanceof BannerItem bannerItem ? bannerItem.getColor() : DyeColor.WHITE,
                item -> item
        ));
        return BANNER_ITEMS;
    }
    //?}

    public static Item getDyeItem(DyeColor color) {
        //? if >= 26 {
        return getDyeItemMap().get(color);
        //?} else
        //return DyeItem.byColor(color);
    }

    public static Item getBannerItem(DyeColor color) {
        //? if >= 26 {
        return getBannerItemMap().get(color);
        //?} else
        //return BannerBlock.byColor(color).asItem();
    }

    public static void drawScrollingString(GuiGraphicsExtractor guiGraphics, Component text, int center, int minX, int maxX, int minY, int maxY) {
        //? if < 1.21.11 {
        /*AbstractWidget.renderScrollingString(
                guiGraphics,
                Minecraft.getInstance().font,
                text,
                center,
                minX,
                minY,
                maxX,
                maxY,
                -1
        );
        *///?} else {
        guiGraphics.textRenderer().acceptScrolling(
                text,
                center,
                minX,
                maxX,
                minY,
                maxY
        );
        //?}
    }

    //? if <= 1.21.6 {
    /*public record KeyEvent(int key, int scancode, int modifiers) {
        public boolean isConfirmation() {
            return this.key == 257 || this.key == 335;
        }

        public boolean isEscape() {
            return this.key == 256;
        }
    }

    public record MouseButtonEvent(double x, double y, int button) {
        public boolean hasShiftDown() {
            return Screen.hasShiftDown();
        }
    }
    *///?}
}
