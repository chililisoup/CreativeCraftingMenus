package dev.chililisoup.creativecraftingmenus.util;

import dev.chililisoup.creativecraftingmenus.CreativeCraftingMenus;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.*;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ServerResourceProvider {
    private static @Nullable ReloadableServerResources RESOURCES;
    private static boolean RESOURCE_LOAD_ATTEMPTED;

    public static void tryProcessRecipes(BiConsumer<RecipeManager, HolderLookup.Provider> process) {
        @Nullable IntegratedServer singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
        if (singleplayerServer != null) {
            process.accept(singleplayerServer.getRecipeManager(), singleplayerServer.registryAccess());
            return;
        }

        @Nullable ReloadableServerResources resources = resources();
        if (resources != null)
            process.accept(resources.getRecipeManager(), resources.fullRegistries().lookup());
    }

    public static<T> List<T> getFromRegistry(ResourceKey<@NotNull Registry<@NotNull T>> key) {
        @Nullable RegistryAccess registryAccess = registryAccess();
        if (registryAccess != null) {
            var reg = registryAccess.lookup(key);
            if (reg.isPresent()) return reg.get().stream().toList();
        }

        return List.of();
    }

    public static List<Item> getFromTag(TagKey<@NotNull Item> tag) {
        return BuiltInRegistries.ITEM.get(tag).map(
                holders -> holders.stream().map(Holder::value).toList()
        ).orElseGet(List::of);
//        return getFromPredicate(ref -> ref.tags().anyMatch(itemTag -> itemTag.equals(tag)));
    }

    public static List<Item> getFromComponent(DataComponentType<?> component) {
        return getFromPredicate(ref -> ref.value().components().has(component));
    }

    public static<T> List<Holder.Reference<@NotNull T>> getRegistryElements(
            ResourceKey<@NotNull Registry<@NotNull T>> key
    ) {
        @Nullable RegistryAccess registryAccess = registryAccess();
        return registryAccess == null ?
                List.of() :
                registryAccess
                        .lookupOrThrow(key)
                        .listElements()
                        .toList();
    }

    public static List<Item> getFromPredicate(Predicate<Holder.Reference<@NotNull Item>> predicate) {
        @Nullable RegistryAccess registryAccess = registryAccess();
        if (registryAccess != null) {
            var reg = registryAccess.lookup(Registries.ITEM);
            if (reg.isPresent()) return reg.get().listElements()
                    .filter(predicate)
                    .sorted(Comparator.comparing(reference -> reference.key().identifier()))
                    .map(Holder.Reference::value).toList();
        }

        return List.of();
    }

    private static @Nullable RegistryAccess registryAccess() {
        @Nullable ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.registryAccess();
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
