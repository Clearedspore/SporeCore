import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "me.clearedSpore"
version = "2.1"

repositories {
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.gravemc.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")


    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")

    implementation("org.yaml:snakeyaml:2.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT") {
        exclude(group = "com.google.code.gson")
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    implementation("com.github.Exlll.ConfigLib:configlib-yaml:v4.6.1")
    implementation("com.github.Clearedspore:SporeAPI:1.8.4")
    implementation("org.dizitart:nitrite:4.3.2")
    implementation(platform("org.dizitart:nitrite-bom:4.3.2"))
    implementation("org.dizitart:nitrite-mvstore-adapter:4.3.2")

    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.shadowJar {
    relocate("co.aikar.commands", "me.clearedspore.sporeCore.shaded.acf")
    relocate("co.aikar.locales", "me.clearedspore.sporeCore.shaded.acf.locales")


    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }

    archiveFileName.set("${project.name}-${project.version}.jar")
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
