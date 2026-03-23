plugins {
    id("fabric-loom") version "1.15-SNAPSHOT" apply false
    id("dev.kikugie.stonecutter")
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22" apply false
}

stonecutter active "1.21.11"

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.6") {
            replace("RenderType::guiTextured", "RenderPipelines.GUI_TEXTURED")
            replace("net.minecraft.client.renderer.RenderType", "net.minecraft.client.renderer.RenderPipelines")
            replace("renderTooltip", "setTooltipForNextFrame")
            replace("renderComponentTooltip", "setComponentTooltipForNextFrame")
            replace("pushPose", "pushMatrix")
            replace("popPose", "popMatrix")
        }

        string(current.parsed > "1.21.6") {
            replace(
                "dev.chililisoup.creativecraftingmenus.util.VersionHelper.KeyEvent",
                "net.minecraft.client.input.KeyEvent"
            )
            replace(
                "dev.chililisoup.creativecraftingmenus.util.VersionHelper.MouseButtonEvent",
                "net.minecraft.client.input.MouseButtonEvent"
            )
        }

        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            replace(".location()", ".identifier()")
        }
    }
}

stonecutter handlers {
    inherit("accesswidener", "classtweaker")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.parchmentmc.org")
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.nucleoid.xyz/")
    }
}