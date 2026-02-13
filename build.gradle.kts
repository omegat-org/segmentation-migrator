import java.io.FileInputStream
import java.util.Properties

plugins {
    application
    jacoco
    alias(libs.plugins.git.version) apply false
}

application {
    mainClass.set("org.omegat.core.segmentation.SegmentationConfMigrator")
}

val dotgit = project.file(".git")
if (dotgit.exists()) {
    apply(plugin = libs.plugins.git.version.get().pluginId)
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    val baseVersion = details.lastTag.substring(1)
    version = when {
        details.isCleanTag -> baseVersion
        else -> baseVersion + "-" + details.commitDistance + "-" + details.gitHash + "-SNAPSHOT"
    }
} else {
    val gitArchival = project.file(".git-archival.properties")
    val props = Properties()
    props.load(FileInputStream(gitArchival))
    val versionDescribe = props.getProperty("describe")
    val regex = "^v\\d+\\.\\d+\\.\\d+$".toRegex()
    version = when {
        regex.matches(versionDescribe) -> versionDescribe.substring(1)
        else -> versionDescribe.substring(1) + "-SNAPSHOT"
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = libs.versions.gradle.get()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.xml)
    implementation(libs.jackson.jaxb)
    implementation(libs.jspecify.annotations)
    runtimeOnly(libs.slf4j.simple)
    testImplementation(libs.junit4)
}

tasks.named<Test>("test") {
    useJUnit()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Task to create a fat JAR
val fatJar = tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Build fatJar for user convenience."
    archiveClassifier.set("fat") // Optional: adds '-fat' to the JAR name
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(sourceSets.main.get().output) // Include compiled classes
    dependsOn(configurations.runtimeClasspath) // Ensure runtime dependencies are resolved
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

// Make the fatJar task depend on the build task, so it runs by default
tasks.build {
    dependsOn(fatJar)
}

// Optionally, make it the default artifact generated
artifacts {
    add("archives", fatJar)
}

