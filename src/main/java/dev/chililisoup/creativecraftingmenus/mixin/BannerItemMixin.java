//? if <= 1.21.4 {
/*package dev.chililisoup.creativecraftingmenus.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.chililisoup.creativecraftingmenus.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BannerItem.class)
public abstract class BannerItemMixin {
    @WrapOperation(method = "appendHoverTextFromBannerBlockEntityTag", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private static int showMoreLines(int layerCount, int maxLines, Operation<Integer> original, @Local BannerPatternLayers layers) {
        return original.call(
                layerCount,
                ModConfig.HANDLER.instance().bannerTooltipChanges ?
                        (layers.layers().size() > 9 ? 8 : 9) :
                        maxLines
        );
    }

    @WrapOperation(
            method = "appendHoverTextFromBannerBlockEntityTag", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
    ))
    private static MutableComponent highlightUnfriendlyLines(MutableComponent instance, ChatFormatting chatFormatting, Operation<MutableComponent> original, @Local int i) {
        return original.call(
                instance,
                (i >= 6 && ModConfig.HANDLER.instance().bannerTooltipChanges) ?
                        ChatFormatting.RED :
                        chatFormatting
        );
    }

    @Inject(method = "appendHoverTextFromBannerBlockEntityTag", at = @At("TAIL"))
    private static void addHiddenLineInfo(ItemStack stack, List<Component> tooltipComponents, CallbackInfo ci, @Local BannerPatternLayers layers) {
        if (ModConfig.HANDLER.instance().bannerTooltipChanges && layers.layers().size() > 9)
            tooltipComponents.add(Component
                    .translatable("container.shulkerBox.more", layers.layers().size() - 8)
                    .withStyle(ChatFormatting.RED)
                    .withStyle(ChatFormatting.ITALIC)
            );
    }
}
*///?}