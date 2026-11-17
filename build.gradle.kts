plugins {
    id("java")
    id("io.freefair.lombok") version "9.8.3"
    id("com.gradleup.shadow") version "9.3.1"
}

group = "net.justryt.ultradrops"
version = "1.0.1"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    expand("version" to project.version)
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("org.bstats", "${project.group}.bstats")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.compileJava {
    mustRunAfter(tasks.clean)
}

tasks.processResources {
    mustRunAfter(tasks.clean)
}