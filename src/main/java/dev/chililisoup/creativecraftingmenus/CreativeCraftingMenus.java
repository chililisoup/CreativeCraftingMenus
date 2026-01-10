package dev.chililisoup.creativecraftingmenus;

import dev.chililisoup.creativecraftingmenus.config.ModConfig;
import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ClientModInitializer;

public class CreativeCraftingMenus implements ClientModInitializer {
    public static final String MOD_ID = "creative_crafting_menus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
        CreativeMenuTabs.init();
    }
}
