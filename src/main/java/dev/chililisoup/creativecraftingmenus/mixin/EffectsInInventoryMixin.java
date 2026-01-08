package dev.chililisoup.creativecraftingmenus.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus.TAB_SPACING;

@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {
    @Shadow private @Final AbstractContainerScreen<?> screen;

    @Definition(id = "screen", field = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;screen:Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;")
    @Definition(id = "imageWidth", field = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;imageWidth:I")
    @Expression("this.screen.imageWidth")
    @ModifyExpressionValue(method = "canSeeEffects", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int adjustCanSeeEffectsX(int original) {
        return screen instanceof CreativeModeInventoryScreen ? original + 24 + 2 * TAB_SPACING : original;
    }

    @Definition(id = "screen", field = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;screen:Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;")
    @Definition(id = "imageWidth", field = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;imageWidth:I")
    @Expression("this.screen.imageWidth")
    @ModifyExpressionValue(method = "render", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int adjustRenderX(int original) {
        return screen instanceof CreativeModeInventoryScreen ? original + 24 + 2 * TAB_SPACING : original;
    }
}
