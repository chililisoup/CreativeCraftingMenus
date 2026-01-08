package dev.chililisoup.creativecraftingmenus.reg;

import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import dev.chililisoup.creativecraftingmenus.gui.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Supplier;

public class CreativeMenuTabs {
    public static final List<CreativeMenuTab<?, ?>> MENU_TABS;

    static {
        MENU_TABS = List.of(
                register(
                        AnvilMenuTab::new,
                        "anvil_menu",
                        "container.repair",
                        Items.ANVIL::getDefaultInstance
                ),
                register(
                        CraftingMenuTab::new,
                        "cartography_menu",
                        "container.cartography_table",
                        Items.CARTOGRAPHY_TABLE::getDefaultInstance
                ),
                register(
                        CraftingMenuTab::new,
                        "crafting_menu",
                        "container.crafting",
                        Items.CRAFTING_TABLE::getDefaultInstance
                ),
                register(
                        CraftingMenuTab::new,
                        "loom_menu",
                        "container.loom",
                        Items.LOOM::getDefaultInstance
                ),
                register(
                        CraftingMenuTab::new,
                        "smithing_menu",
                        "container.upgrade",
                        Items.SMITHING_TABLE::getDefaultInstance
                )
        );
    }

    private static<T extends CreativeMenuTab<?, T>> T register(
            CreativeMenuTab.MenuTabConstructor<T> constructor,
            String name,
            String translationKey,
            Supplier<ItemStack> iconGenerator
    ) {
        T menuTab = new CreativeMenuTab.Builder<>(constructor)
                .title(Component.translatable(translationKey))
                .backgroundTexture(CreativeCraftingMenus.id(
                        String.format("textures/gui/container/creative_%s.png", name)
                ))
                .icon(iconGenerator)
                .build();

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), CreativeCraftingMenus.id(name)),
                menuTab
        );

        FabricCreativeGuiComponents.COMMON_GROUPS.add(menuTab);

        return menuTab;
    }

    public static void init() {
        ClientPlayConnectionEvents.DISCONNECT.register((a, b) -> MENU_TABS.forEach(CreativeMenuTab::dispose));
    }
}
