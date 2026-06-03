plugins {
    id("fabric-loom") version "1.16-SNAPSHOT" apply false
    id("dev.kikugie.stonecutter")
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22" apply false
}

stonecutter active "26.1"

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

        string(current.parsed >= "26") {
            replace("classTweaker v2 named", "classTweaker v2 official")
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("guiGraphics.renderItem(", "guiGraphics.item(")
            replace("guiGraphics.drawString(", "guiGraphics.text(")
            replace("guiGraphics.drawCenteredString(", "guiGraphics.centeredText(")
            replace("guiGraphics.submitEntityRenderState(", "guiGraphics.entity(")
            replace("guiGraphics.submitBannerPatternRenderState(", "guiGraphics.bannerPattern(")
            replace("ClickType", "ContainerInput")
            replace(
                "net.fabricmc.fabric.impl.client.itemgroup.FabricCreativeGuiComponents",
                "net.fabricmc.fabric.impl.client.creativetab.FabricCreativeGuiComponents"
            )
            replace(
                "FabricCreativeGuiComponents.COMMON_GROUPS",
                "FabricCreativeGuiComponents.COMMON_TABS"
            )
        }
    }
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