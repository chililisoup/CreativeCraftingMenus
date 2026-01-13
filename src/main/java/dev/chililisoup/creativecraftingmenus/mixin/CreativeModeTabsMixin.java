package dev.chililisoup.creativecraftingmenus.mixin;

import dev.chililisoup.creativecraftingmenus.gui.CreativeMenuTab;
import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeModeTabs.class, priority = 1001)
public abstract class CreativeModeTabsMixin {
    @Inject(method = "buildAllTabContents", at = @At("HEAD"))
    private static void hideBeforePagination(CreativeModeTab.ItemDisplayParameters params, CallbackInfo ci) {
        CreativeMenuTabs.MENU_TABS.forEach(CreativeMenuTab::hide);
    }

    @Inject(method = "buildAllTabContents", at = @At("TAIL"))
    private static void showAfterPagination(CreativeModeTab.ItemDisplayParameters params, CallbackInfo ci) {
        CreativeMenuTabs.MENU_TABS.forEach(CreativeMenuTab::show);
    }
}
