package dev.chililisoup.creativecraftingmenus.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.chililisoup.creativecraftingmenus.config.ModConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> ModConfig.HANDLER.instance().generateScreen(parentScreen);
    }
}
