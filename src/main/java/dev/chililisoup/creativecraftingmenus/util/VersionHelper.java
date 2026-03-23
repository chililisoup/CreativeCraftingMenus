package dev.chililisoup.creativecraftingmenus.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

//? if <= 1.21.6 {
/*import net.minecraft.client.gui.screens.Screen;
*///?}

//? if < 1.21.11 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
*///?}

public final class VersionHelper {
    public static void drawScrollingString(GuiGraphics guiGraphics, Component text, int center, int minX, int maxX, int minY, int maxY) {
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
