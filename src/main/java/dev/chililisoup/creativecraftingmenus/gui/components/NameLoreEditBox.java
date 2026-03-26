package dev.chililisoup.creativecraftingmenus.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;

//? if > 1.21.6 {
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.input.KeyEvent;
//?}

public class NameLoreEditBox extends MultiLineEditBox {
    public boolean isEditable = true;

    public NameLoreEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height
    ) {
        super(
                font,
                x,
                y,
                width,
                height,
                CommonComponents.EMPTY,
                //? if >= 1.21.6 {
                CommonComponents.EMPTY,
                0xFFE0E0E0,
                true,
                0xFFD0D0D0,
                true,
                true
                //?} else
                //CommonComponents.EMPTY
        );
    }

    @Override
    public boolean isFocused() {
        return this.isEditable && super.isFocused();
    }

    @Override
    public boolean keyPressed(
            //? if > 1.21.6 {
            @NotNull KeyEvent event
             //?} else
            //int keyCode, int scanCode, int modifiers
    ) {
        return this.isEditable && super.keyPressed(
                //? if > 1.21.6 {
                event
                //?} else
                //keyCode, scanCode, modifiers
        );
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getRight() + 15 && mouseY < this.getBottom();
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() + 3;
    }

    @Override
    //? if >= 26 {
    public int scrollBarY() {
    //?} else
    //protected int scrollBarY() {
        return
                //? if >= 26 {
                this.scrollable() ?
                //?} else
                //this.scrollbarVisible() ?
                        super.scrollBarY() : this.getY();
    }

    @Override
    protected int scrollerHeight() {
        return 15;
    }

    //? if > 1.21.6
    @Override
    protected boolean isOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= this.scrollBarX() && mouseX < this.scrollBarX() + 12 && mouseY >= this.getY() && mouseY < this.getBottom();
    }

    //? if <= 1.21.6 {
    /*@Override
    public boolean updateScrolling(double mouseX, double mouseY, int button) {
        this.scrolling = this.scrollbarVisible() && this.isValidClickButton(button) && this.isOverScrollbar(mouseX, mouseY);
        return this.scrolling;
    }
    *///?}

    @Override
    //? if >= 26 {
    protected void extractScrollbar(
    //?} else
    //protected void renderScrollbar(
            //? if > 1.21.6 {
            @NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY
             //?} else
            //@NotNull GuiGraphicsExtractor guiGraphics
    ) {
        boolean scrollable =
                //? if >= 26 {
                this.scrollable();
                //?} else
                //this.scrollbarVisible();

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                scrollable ? StonecutterScreen.SCROLLER_SPRITE : StonecutterScreen.SCROLLER_DISABLED_SPRITE,
                this.scrollBarX(),
                this.scrollBarY(),
                12,
                this.scrollerHeight()
        );

        //? if > 1.21.6 {
        if (scrollable && this.isOverScrollbar(mouseX, mouseY))
            guiGraphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        //?}
    }
}
