package dev.chililisoup.creativecraftingmenus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.chililisoup.creativecraftingmenus.gui.CreativeMenuTab;
import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreativeModeTabs.class, priority = 1001)
public abstract class CreativeModeTabsMixin {
    private static final @Unique Identifier CRAFTING_INVENTORY_BACKGROUND =
            CreativeCraftingMenus.id("textures/gui/container/creative_crafting_inventory.png");

    @WrapOperation(
            method = "bootstrap", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/CreativeModeTab$Builder;backgroundTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/world/item/CreativeModeTab$Builder;",
            ordinal = 1
    ))
    private static CreativeModeTab.Builder replaceInventoryBackground(CreativeModeTab.Builder instance, Identifier identifier, Operation<CreativeModeTab.Builder> original) {
        return instance.backgroundTexture(CRAFTING_INVENTORY_BACKGROUND);
    }

    @Inject(method = "buildAllTabContents", at = @At("HEAD"))
    private static void hideBeforePagination(CreativeModeTab.ItemDisplayParameters params, CallbackInfo ci) {
        CreativeMenuTabs.MENU_TABS.forEach(CreativeMenuTab::hide);
    }

    @Inject(method = "buildAllTabContents", at = @At("TAIL"))
    private static void showAfterPagination(CreativeModeTab.ItemDisplayParameters params, CallbackInfo ci) {
        CreativeMenuTabs.MENU_TABS.forEach(CreativeMenuTab::show);
    }
}
