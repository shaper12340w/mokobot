plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
    java
}

group = "dev.shaper.rypolixy"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

application {
    mainClass.set("dev.shaper.rypolixy.MainKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.topi.wtf/releases")
    maven("https://maven.lavalink.dev/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Kord
    implementation("dev.kord:kord-core:0.15.0")
    implementation("dev.kord:kord-core-voice:0.15.0")
    implementation("dev.kord:kord-voice:0.15.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.fusesource.jansi:jansi:2.4.1")

    // AudioPlayer
    implementation("dev.arbjerg:lavaplayer:2.2.2")
    implementation("dev.arbjerg:lavadsp:0.7.8")
    implementation("xyz.gianlu.librespot:librespot-java:1.6.5")
    implementation("com.github.sapher:youtubedl-java:1.1")

    // JSON/YAML Processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.2")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.moshi:moshi-adapters:1.8.0")
    implementation("org.json:json:20250107")

    // Option Parser
    implementation("us.jimschubert:kopper:0.0.4")

    // Database
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "dev.shaper.rypolixy.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}