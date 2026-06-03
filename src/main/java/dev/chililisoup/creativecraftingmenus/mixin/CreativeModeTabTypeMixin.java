package dev.chililisoup.creativecraftingmenus.mixin;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$Type")
enum CreativeModeTabTypeMixin {
    CREATIVE_CRAFTING_MENUS_MENU
}
