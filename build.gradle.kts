import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" 
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
    id("io.github.goooler.shadow") version "8.1.8"
}

// --- [필수 선언부: 여기서부터는 절대 생략하면 안 됩니다] ---
class NMSVersion(val nmsVersion: String, val serverVersion: String)
infix fun String.toNms(that: String): NMSVersion = NMSVersion(this, that)

val isCI = System.getenv("CI") != null
val SUPPORTED_VERSIONS: List<NMSVersion> = listOfNotNull(
    "v1_20_R1" toNms "1.20.1-R0.1-SNAPSHOT",
    "v1_20_R2" toNms "1.20.2-R0.1-SNAPSHOT",
    "v1_20_R3" toNms "1.20.4-R0.1-SNAPSHOT",
    "v1_20_R4" toNms "1.20.6-R0.1-SNAPSHOT",
    "v1_21_R1" toNms "1.21.1-R0.1-SNAPSHOT",
    "v1_21_R2" toNms "1.21.3-R0.1-SNAPSHOT",
    "v1_21_R3" toNms "1.21.4-R0.1-SNAPSHOT",
    "v1_21_R4" toNms "1.21.5-R0.1-SNAPSHOT",
    "v1_21_R5" toNms "1.21.8-R0.1-SNAPSHOT",
    "v1_21_R6_old" toNms "1.21.10-R0.1-SNAPSHOT",
    "v1_21_R6" toNms "1.21.11-R0.1-SNAPSHOT",
    if (!isCI) "v1_20_R4_spigot" toNms "1.20.6-R0.1-SNAPSHOT" else null
)

val pluginVersion: String = project.findProperty("version")?.toString() ?: "1.0.0"
// -------------------------------------------------------

group = "io.th0rgal"
version = pluginVersion

allprojects {
    apply(plugin = "java")

    repositories {
        flatDir {
            dirs(file("../libs"), file("libs"))
        }
        mavenCentral()
        maven("https://repo.triumphteam.dev/snapshots") 
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        maven("https://repo.oraxen.com/releases")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(path = ":core"))
    SUPPORTED_VERSIONS.forEach { 
        implementation(project(path = ":${it.nmsVersion}", configuration = "reobf")) 
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        filesNotMatching(listOf("**/*.png", "**/*.ogg", "**/plugin.yml")) {
            expand(mapOf("version" to pluginVersion))
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    shadowJar {
        SUPPORTED_VERSIONS.forEach { dependsOn(":${it.nmsVersion}:reobfJar") }
        archiveClassifier.set("")
        archiveFileName.set("oraxen-${pluginVersion}-MIDCORE.jar")
        
        manifest {
            attributes(mapOf(
                "Version" to pluginVersion, 
                "Created-By" to "Lee Ki-young (MIDCORE)", 
                "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
            ))
        }
    }
    build { dependsOn(shadowJar) }
}

bukkit {
    main = "io.th0rgal.oraxen.OraxenPlugin"
    version = pluginVersion
    name = "Oraxen"
    apiVersion = "1.18"
    authors = listOf("th0rgal", "Lee Ki-young")
}
