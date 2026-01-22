import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.github.goooler.shadow") version "8.1.8"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "me.clearedSpore"
version = "2.7"

repositories {
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.gravemc.net/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.1.0")

    //implementation(kotlin("reflect"))
    //implementation(kotlin("stdlib"))

    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("org.yaml:snakeyaml:2.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") {
        exclude(group = "com.google.code.gson")
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    implementation("com.github.Exlll.ConfigLib:configlib-yaml:v4.6.1")
    implementation("com.github.Clearedspore:SporeAPI:4.0")
    compileOnly("org.dizitart:nitrite:4.3.2")
    compileOnly(platform("org.dizitart:nitrite-bom:4.3.2"))
    compileOnly("org.dizitart:nitrite-mvstore-adapter:4.3.2")

    compileOnly("org.reflections:reflections:0.10.2")

    compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.3")

    compileOnly("net.dv8tion:JDA:6.1.2")
    compileOnly("me.clip:placeholderapi:2.11.6")

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
}

tasks {
    shadowJar {
        relocate("co.aikar.commands", "me.clearedspore.sporeCore.shaded.acf")
        relocate("co.aikar.locales", "me.clearedspore.sporeCore.shaded.acf.locales")
        relocate("org.bstats", "me.clearedspore.sporeCore.shaded.bstats")

        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}