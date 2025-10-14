plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.gradle.develocity") version "4.2.2"
}
develocity {
    buildScan {
        publishing.onlyIf { "true".equals(System.getProperty("envIsCi")) }
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
    }
}
rootProject.name = "omegat-segmentation-migrator"
