package dev.chililisoup.creativecraftingmenus.config;

import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfig {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(CreativeCraftingMenus.id("config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve(
                            CreativeCraftingMenus.MOD_ID + ".json5"
                    ))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry(comment = "Spacing around the creative menu crafting tabs")
    public int tabSpacingX = 9;

    @SerialEntry
    public int tabSpacingY = 9;

    public Screen generateScreen(Screen parentScreen) {
        Component name = Component.translatable("creative_crafting_menus.config");

        return YetAnotherConfigLib.createBuilder()
                .title(name)
                .category(ConfigCategory.createBuilder()
                        .name(name)
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("creative_crafting_menus.config.tabSpacingX"))
                                .description(OptionDescription.of(Component.translatable("creative_crafting_menus.config.tabSpacingX.desc")))
                                .binding(
                                        HANDLER.defaults().tabSpacingX,
                                        () -> this.tabSpacingX,
                                        newVal -> {
                                            this.tabSpacingX = newVal;
                                            HANDLER.save();
                                        })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(0, 24)
                                        .step(1)
                                        .formatValue(val -> Component.literal(val + "px")))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("creative_crafting_menus.config.tabSpacingY"))
                                .description(OptionDescription.of(Component.translatable("creative_crafting_menus.config.tabSpacingY.desc")))
                                .binding(
                                        HANDLER.defaults().tabSpacingY,
                                        () -> this.tabSpacingY,
                                        newVal -> {
                                            this.tabSpacingY = newVal;
                                            HANDLER.save();
                                        })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(0, 24)
                                        .step(1)
                                        .formatValue(val -> Component.literal(val + "px")))
                                .build())
                        .build())
                .build()
                .generateScreen(parentScreen);
    }
}
