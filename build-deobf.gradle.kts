plugins {
    id("java-library")
    id("idea")
    id("net.fabricmc.fabric-loom")
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("dev.kikugie.stonecutter")
    id("dev.kikugie.fletching-table.fabric")
    id("mod-build-common")
}

val mod = `mod-common`.mod.get()
val deps = mod.deps

version = mod.archiveVersion
base.archivesName = mod.id

loom {
    mod.fileExists("build/resources/main/fabric.mod.json") { fabricModJsonPath = it }
    mod.fileExists("build/resources/main/${mod.id}.classtweaker") { accessWidenerPath = it }

    runConfigs.all {
        ideConfigGenerated(false)
    }

    runs {
        register("testClient") {
            client()
            name = "Test Client"
            vmArgs("-Dmixin.debug.export=true")
            runDir = "../../run"
            ideConfigGenerated(true)
        }
    }
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "${mod.id}.mixins.json") {
            env("CLIENT")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${deps.minecraft}")

    implementation("net.fabricmc:fabric-loader:${deps.fabricLoader}")
    api("net.fabricmc.fabric-api:fabric-api:${deps.fabricApi}")
    implementation("dev.isxander:yet-another-config-lib:${deps.yacl}-fabric")
    implementation("eu.pb4:placeholder-api:${deps.placeholderApi}")

    compileOnly("com.terraformersmc:modmenu:${deps.modmenu}")
    mod.prop("deps.trinkets") { compileOnly("eu.pb4:trinkets:${it}") }
}

java {
    targetCompatibility = deps.java
    sourceCompatibility = deps.java
}
