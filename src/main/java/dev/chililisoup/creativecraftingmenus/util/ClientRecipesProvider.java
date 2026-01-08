package dev.chililisoup.creativecraftingmenus.util;

import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.*;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class ClientRecipesProvider {
    private static @Nullable ReloadableServerResources RESOURCES;
    private static boolean RESOURCE_LOAD_ATTEMPTED;

    public static void tryProcess(BiConsumer<RecipeManager, HolderLookup.Provider> process) {
        @Nullable IntegratedServer singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
        if (singleplayerServer != null) {
            process.accept(singleplayerServer.getRecipeManager(), singleplayerServer.registryAccess());
            return;
        }

        @Nullable ReloadableServerResources resources = resources();
        if (resources != null)
            process.accept(resources.getRecipeManager(), resources.fullRegistries().lookup());
    }

    private static @Nullable ReloadableServerResources resources() {
        if (RESOURCES != null) return RESOURCES;
        if (RESOURCE_LOAD_ATTEMPTED) return null;

        RESOURCE_LOAD_ATTEMPTED = true;
        try { RESOURCES = createServerResources(); }
        catch (Exception e) { CreativeCraftingMenus.LOGGER.error("Unable to load recipes!"); }

        return RESOURCES;
    }

    private static ReloadableServerResources createServerResources() throws ExecutionException, InterruptedException {
        CompletableFuture<WorldStem> completableFuture = WorldLoader.load(
                new WorldLoader.InitConfig(
                        new WorldLoader.PackConfig(
                                ModPackResourcesUtil.createClientManager(),
                                WorldDataConfiguration.DEFAULT,
                                false,
                                false
                        ),
                        Commands.CommandSelection.INTEGRATED,
                        LevelBasedPermissionSet.GAMEMASTER
                ),
                dataLoadContext -> {
                    WorldDimensions.Complete complete = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen())
                            .bake(dataLoadContext.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM));
                    return new WorldLoader.DataLoadOutput<>(
                            new PrimaryLevelData(
                                    new LevelSettings(
                                            "test",
                                            GameType.CREATIVE,
                                            false,
                                            Difficulty.HARD,
                                            true,
                                            new GameRules(WorldDataConfiguration.DEFAULT.enabledFeatures()),
                                            WorldDataConfiguration.DEFAULT
                                    ),
                                    WorldOptions.defaultWithRandomSeed(),
                                    complete.specialWorldProperty(),
                                    complete.lifecycle()
                            ),
                            complete.dimensionsRegistryAccess()
                    );
                },
                WorldStem::new,
                Util.backgroundExecutor(),
                Minecraft.getInstance()
        );
        Minecraft.getInstance().managedBlock(completableFuture::isDone);
        return completableFuture.get().dataPackResources();
    }
}
