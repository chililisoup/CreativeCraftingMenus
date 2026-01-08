package dev.chililisoup.creativecraftingmenus;

import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ClientModInitializer;

public class CreativeCraftingMenus implements ClientModInitializer {
    public static final String MOD_ID = "creativecraftingmenus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        CreativeMenuTabs.init();
        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> LOGGER.info("disconnectin"));
    }
}
