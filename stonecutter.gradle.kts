plugins {
    id("dev.kikugie.stonecutter")

    val modstitchVersion = "0.7.1-unstable"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
    id("dev.isxander.modstitch.shadow") version modstitchVersion apply false
    id("fabric-loom") version "1.14-SNAPSHOT" apply false
}
stonecutter active "1.21.11-fabric"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.isxander.dev/releases")
    }
}