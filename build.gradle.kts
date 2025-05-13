plugins {
    application
    jacoco
}

application {
    mainClass.set("org.omegat.core.segmentation.SegmentationConfMigrator")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "8.13"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.xml)
    implementation(libs.jackson.jaxb)
    implementation(libs.jetbrains.annotations)
    testImplementation(libs.junit4)
}

tasks.named<Test>("test") {
    useJUnit()
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

