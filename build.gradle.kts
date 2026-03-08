import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" 
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
    id("io.github.goooler.shadow") version "8.1.8"
}

// ... (NMSVersion 및 SUPPORTED_VERSIONS 설정은 동일) ...

allprojects {
    apply(plugin = "java")

    repositories {
        // 1순위: 기영님이 직접 넣은 libs 폴더
        flatDir {
            dirs(file("../libs"), file("libs"))
        }
        mavenCentral()
        
        // [수정] 기영님이 보내주신 TriumphTeam 공식 스냅샷 저장소 추가
        maven("https://repo.triumphteam.dev/snapshots") 
        
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net/")
        maven("https://repo.oraxen.com/releases")
        // JitPack은 제외된 상태 유지
    }
}

dependencies {
    // 로컬 jar 파일 포함 (IF-0.11.6.jar 등)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    
    implementation(project(path = ":core"))
    SUPPORTED_VERSIONS.forEach { 
        implementation(project(path = ":${it.nmsVersion}", configuration = "reobf")) 
    }
}

// ... (java 툴체인 및 tasks 설정은 동일) ...

tasks {
    shadowJar {
        SUPPORTED_VERSIONS.forEach { dependsOn(":${it.nmsVersion}:reobfJar") }
        archiveClassifier.set("")
        // 기영님의 MIDCORE 서버용 이름으로 저장
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
