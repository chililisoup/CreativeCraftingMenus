package dev.chililisoup.creativecraftingmenus.gui.components;

import dev.chililisoup.creativecraftingmenus.util.VersionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.StonecutterScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

//? if > 1.21.6 {
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.input.MouseButtonEvent;
//?}

public class DropdownSelector<T> extends ObjectSelectionList<DropdownSelector.Entry<T>> {
    private final int closedHeight;
    private final int openHeight;
    private final int openX;
    private final int openY;
    public boolean open = false;

    public DropdownSelector(int x, int openX, int openY, int width, int closedHeight, int openHeight) {
        super(Minecraft.getInstance(), width, openHeight, 0, 14);
        this.openX = openX;
        this.openY = openY;
        this.closedHeight = closedHeight;
        this.openHeight = openHeight;
        this.setX(x);
    }

    public void updateEntries(Collection<Entry<T>> entries) {
        this.replaceEntries(List.copyOf(entries));
        super.setSelected(this.children().stream().findFirst().orElse(null));
        this.setHeight(this.openHeight);
        this.setScrollAmount(0.0);
    }

    public @Nullable T value() {
        Entry<T> selected = this.getSelected();
        return selected != null ? selected.value : null;
    }

    @Override
    public void setSelected(DropdownSelector.Entry<T> entry) {
        this.open = false;
        this.playDownSound(this.minecraft.getSoundManager());
        if (this.getSelected() == entry) return;
        super.setSelected(entry);
    }

    @Override
    public int getX() {
        return this.open ? this.openX : super.getX();
    }

    @Override
    public int getY() {
        return this.open ? this.openY : super.getY();
    }

    @Override
    public int getWidth() {
        return this.open ? super.getX() - this.openX + super.getWidth() : super.getWidth();
    }

    @Override
    public int getHeight() {
        return this.open ? this.openHeight : this.closedHeight;
    }

    @Override
    public int getRowLeft() {
        return this.getX() + 3;
    }

    @Override
    public int getRowWidth() {
        return this.getWidth() - 6;
    }

    @Override
    //? if >= 26 {
    public void extractWidgetRenderState(
    //?} else
    //public void renderWidget(
            @NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick
    ) {
        if (!this.open) {
            //? if >= 26 {
            this.extractListBackground(guiGraphics);
            //?} else
            //this.renderListBackground(guiGraphics);

            if (mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getRight() && mouseY < this.getBottom()) {
                guiGraphics.fill(
                        this.getX(),
                        this.getY(),
                        this.getRight(),
                        this.getBottom(),
                        0x40FFFFFF
                );
                //? if > 1.21.6
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }

            Entry<T> selected = this.getSelected();
            if (selected != null) VersionHelper.drawScrollingString(
                    guiGraphics,
                    selected.name,
                    this.getX() + 3,
                    this.getX() + 3,
                    this.getRight() - 3,
                    this.getY(),
                    this.getBottom()
            );

            return;
        }

        //? if > 1.21.6
        if (this.isHovered()) guiGraphics.requestCursor(CursorType.DEFAULT);

        //? if >= 26 {
        super.extractWidgetRenderState(
        //?} else
        //super.renderWidget(
                guiGraphics, mouseX, mouseY, partialTick
        );
    }

    @Override
    //? if > 1.21.6 {
    public boolean mouseClicked(@NotNull MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
    //?} else
    //public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.open) {
            //? if > 1.21.6
            int button = mouseButtonEvent.button();
            if (button == 0 && !this.children().isEmpty()) {
                this.open = true;
                this.setScrollAmount(0);
                Entry<T> selected = this.getSelected();
                if (selected != null)
                    //? if > 1.21.6 {
                    this.scrollToEntry(selected);
                    //?} else
                    //this.centerScrollOn(selected);

                this.playDownSound(this.minecraft.getSoundManager());
            }
            return true;
        }

        //? if > 1.21.6 {
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
        //?} else
        //return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(
            //? if > 1.21.6 {
            @NotNull MouseButtonEvent mouseButtonEvent
            //?} else
            //double mouseX, double mouseY, int button
    ) {
        return this.open && super.mouseReleased(
                //? if > 1.21.6 {
                mouseButtonEvent
                //?} else
                //mouseX, mouseY, button
        );
    }

    @Override
    public boolean mouseDragged(
            //? if > 1.21.6 {
            @NotNull MouseButtonEvent mouseButtonEvent, double dragX, double dragY
            //?} else
            //double mouseX, double mouseY, int button, double dragX, double dragY
    ) {
        if (!this.open) return false;
        if (!this.scrolling) return super.mouseDragged(
                //? if > 1.21.6 {
                mouseButtonEvent, dragX, dragY
                //?} else
                //mouseX, mouseY, button, dragX, dragY
        );

        //? if > 1.21.6
        double mouseY = mouseButtonEvent.y();

        if (mouseY < this.getY()) this.setScrollAmount(0.0);
        else if (mouseY > this.getBottom()) this.setScrollAmount(this.maxScrollAmount());
        else this.setScrollAmount(this.scrollAmount() + dragY * Math.max(
                1.0,
                (double) Math.max(1, this.maxScrollAmount()) / (this.getHeight() -  this.scrollerHeight())
        ));

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.open && super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

    public static class Entry<T> extends ObjectSelectionList.Entry<DropdownSelector.Entry<T>> {
        final Component name;
        final T value;

        public Entry(final Component name, final T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.name);
        }

        @Override
        //? if > 1.21.6 {
        //? if >= 26 {
        public void extractContent(
        //?} else
        //public void renderContent(
                @NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick
        ) {
        //?} else
        //public void render(GuiGraphicsExtractor guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            //? if > 1.21.6 {
            int left = this.getContentX();
            int top = this.getContentY();
            int width = this.getContentWidth();
            int height = this.getContentHeight();
            //?} else
            //width -= 4;

            if (hovered) {
                guiGraphics.fill(left, top, left + width, top + height, 0x40FFFFFF);

                //? if > 1.21.6
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
            }

            VersionHelper.drawScrollingString(
                    guiGraphics, this.name, left, left, left + width, top, top + height
            );
        }
    }
}
