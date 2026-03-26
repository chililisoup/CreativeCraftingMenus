@file:Suppress("unused")

plugins {
    idea
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

class ModDeps {
    val fabricLoader = project.property("deps.fabric_loader") as String
    val minecraft = project.property("deps.minecraft") as String
    val minecraftRange = project.property("deps.minecraft_range") as String

    val fabricApi = project.property("deps.fabric_api") as String
    val yacl = project.property("deps.yacl") as String
    val placeholderApi = project.property("deps.placeholder_api") as String

    val modmenu = project.property("deps.modmenu") as String

    val java = when {
        minecraft >= "26" -> JavaVersion.VERSION_25
        minecraft >= "1.20.6" -> JavaVersion.VERSION_21
        minecraft >= "1.18" -> JavaVersion.VERSION_17
        minecraft >= "1.17" -> JavaVersion.VERSION_16
        else -> JavaVersion.VERSION_1_8
    }
}

class ModData {
    fun prop(name: String, consumer: (prop: String) -> Unit) {
        (findProperty(name) as? String?)
            ?.let(consumer)
    }

    fun fileExists(path: String, consumer: (prop: File) -> Unit) {
        val file = project.file(path)
        if (file.exists()) consumer.invoke(file)
    }

    val version = project.property("mod.version") as String
    val group = project.property("mod.group") as String
    val id = project.property("mod.id") as String
    val name = project.property("mod.name") as String
    val authors = project.property("mod.authors") as String
    val description = project.property("mod.description") as String
    val homepage = project.property("mod.homepage") as String
    val sources = project.property("mod.sources") as String
    val issues = project.property("mod.issues") as String
    val license = project.property("mod.license") as String

    val deps = ModDeps()

    val archiveVersion = "${this.version}+${deps.minecraft}"

    fun getProps(): Map<String, String> = mapOf(
        "mod_id" to this.id,
        "mod_name" to this.name,
        "mod_version" to "${this.version}+${this.deps.minecraft}",
        "mod_group" to this.group,
        "mod_author" to this.authors,
        "mod_license" to this.license,
        "mod_description" to this.description,
        "mod_homepage" to this.homepage,
        "mod_sources" to this.sources,
        "mod_issues" to this.issues,
        "mod_author_list" to this.authors.split(", ").joinToString("\",\""),
        "minecraft" to this.deps.minecraft,
        "minecraft_range" to this.deps.minecraftRange
    )
}

interface ModCommon {
    val mod: Property<ModData>
}

val extension = project.extensions.create<ModCommon>("mod-common")
extension.mod.convention(ModData())
val mod = extension.mod.get()

configurations.configureEach {
    resolutionStrategy {
        // make sure the desired version of loader is used. Sometimes old versions are pulled in transitively.
        force("net.fabricmc:fabric-loader:${mod.deps.fabricLoader}")
    }
}

tasks {
    named<ProcessResources>("processResources") {
        fun inputProps(props: Map<String, Any>): Map<String, Any> {
            inputs.properties(*props.map { entry -> entry.key to entry.value }.toTypedArray() )
            return props
        }

        val props = inputProps(mod.getProps())
        filesMatching("fabric.mod.json") { expand(props) }

        val mixinProps = inputProps(mapOf(
            "compatibility_level" to "JAVA_${mod.deps.java.majorVersion}"
        ))
        filesMatching("*.mixins.json") { expand(mixinProps) }
    }

    named("validateAccessWidener") {
        dependsOn("processResources")
    }

    register<Copy>("buildAndCollect") {
        group = "build"

        from(layout.buildDirectory.dir("libs"))
        include("*.jar")
        into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))

        dependsOn("build")
    }

    register<Delete>("buildCollectAndClean") {
        group = "build"

        delete(layout.buildDirectory.dir("libs"))
        delete(layout.buildDirectory.dir("devlibs"))

        dependsOn("buildAndCollect")
    }
}
