//? if >= 26 {
package dev.chililisoup.creativecraftingmenus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.chililisoup.creativecraftingmenus.config.ModConfig;
import eu.pb4.trinkets.api.SlotGroup;
import eu.pb4.trinkets.impl.client.TrinketScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = CreativeModeInventoryScreen.class, priority = 1001)
public abstract class CreativeModeInventoryScreenTrinketsMixin implements TrinketScreen {
    @WrapMethod(method = "trinkets$getGroupRect")
    private Rect2i moveTrinketSlots(SlotGroup group, Operation<Rect2i> original) {
        if (!ModConfig.HANDLER.instance().inventoryCraftingGrid)
            return original.call(group);

        if (this.trinkets$getHandler().trinkets$getGroupNum(group) == 1)
            return new Rect2i(98, 19, 17, 17);

        Rect2i rect = original.call(group);
        rect.setX(rect.getX() - 27);
        return rect;
    }
}
//?}