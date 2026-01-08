plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

class ModData {
    val version = property("mod.version") as String
    val group = property("mod.group") as String
    val id = property("mod.id") as String
    val name = property("mod.name") as String
    val authors = property("mod.authors") as String
    val description = property("mod.description") as String
    val homepage = property("mod.homepage") as String
    val sources = property("mod.sources") as String
    val issues = property("mod.issues") as String
    val license = property("mod.license") as String
}

val mod = ModData()
val minecraft = property("deps.minecraft") as String

modstitch {
    minecraftVersion = minecraft
    javaVersion = when {
        sc.current.parsed >= "1.20.6" -> 21
        sc.current.parsed >= "1.18" -> 17
        sc.current.parsed >= "1.17" -> 16
        else -> 8
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = mod.id
        modName = mod.name
        modVersion = "${mod.version}+$minecraft"
        modGroup = mod.group
        modAuthor = mod.authors
        modLicense = mod.license
        modDescription = mod.description

        fun <K: Any, V: Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            put("mod_homepage", mod.homepage)
            put("mod_sources", mod.sources)
            put("mod_issues", mod.issues)
            put("mod_author_list", mod.authors.split(", ").joinToString("\",\""))
            prop("deps.minecraft_range") { put("minecraft_range", it) }
            prop("deps.neoforge_range") { put("neoforge_range", it) }
        }
    }

    // Fabric Loom (Fabric)
    loom {
        prop("deps.fabric_loader") { fabricLoaderVersion = it }

        // Configure loom like normal in this block.
        configureLoom {
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
    }

    mixin {
        addMixinsToModManifest = true
        configs.register(mod.id)
    }
}

// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
dependencies {
    prop("deps.fapi") { modstitchModApi("net.fabricmc.fabric-api:fabric-api:${it}") }
}

modstitch.onEnable {
    modstitch.moddevgradle {
        tasks.named("createMinecraftArtifacts") {
            dependsOn("stonecutterGenerate")
        }
    }

    val finalJarTasks = listOf(
        modstitch.finalJarTask
    )

    tasks.register<Copy>("buildAndCollect") {
        group = "build"

        finalJarTasks.forEach { jar ->
            dependsOn(jar)
            from(jar.flatMap { it.archiveFile })
        }

        into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
        dependsOn("build")
    }
}

tasks.named("generateModMetadata") {
    dependsOn("stonecutterGenerate")
}

tasks.register<Delete>("buildCollectAndClean") {
    group = "build"

    delete(layout.buildDirectory.dir("libs"))
    delete(layout.buildDirectory.dir("devlibs"))

    dependsOn("buildAndCollect")
}