package dev.chililisoup.creativecraftingmenus;

import dev.chililisoup.creativecraftingmenus.reg.CreativeMenuTabs;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//? if fabric
import net.fabricmc.api.ClientModInitializer;

//? if neoforge {
/*import net.neoforged.fml.common.Mod;
@Mod(CreativeCraftingMenus.MOD_ID)
*///?}
public class CreativeCraftingMenus /*? if fabric {*/implements ClientModInitializer/*?}*/ {
    public static final String MOD_ID = "creativecraftingmenus";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        CreativeMenuTabs.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    //? if fabric {
    @Override
    public void onInitializeClient() {
        init();
    }
    //?}

    //? if neoforge {
    /*public CreativeCraftingMenus() {
        init();
    }
    *///?}
}
