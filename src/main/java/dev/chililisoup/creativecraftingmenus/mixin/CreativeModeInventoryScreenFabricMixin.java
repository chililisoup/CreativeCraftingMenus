package dev.chililisoup.creativecraftingmenus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.chililisoup.creativecraftingmenus.gui.CreativeMenuTab;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CreativeModeInventoryScreen.class, priority = 1001)
public abstract class CreativeModeInventoryScreenFabricMixin {
    @WrapOperation(
            method = "hasAdditionalPages", at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;size()I"
    ))
    private int filterOutMenuTabs(List<CreativeModeTab> instance, Operation<Integer> original) {
        return (int) new ArrayList<>(instance).stream().filter(tab -> !(tab instanceof CreativeMenuTab)).count();
    }
}
