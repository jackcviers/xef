plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

dependencies {
    implementation(libs.flyway.core)
    implementation(libs.hikari)
    implementation(libs.klogging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.logback)
    implementation(libs.openai.client)
    implementation(libs.suspendApp.core)
    implementation(libs.suspendApp.ktor)
    implementation(libs.uuid)
    implementation(projects.xefKotlin)
    implementation(projects.xefCore)
    implementation(projects.xefLucene)
    implementation(projects.xefPostgresql)
}

tasks.getByName<Copy>("processResources") {
    dependsOn(projects.xefGpt4all.dependencyProject.tasks.getByName("jvmProcessResources"))
    from("${projects.xefGpt4all.dependencyProject.buildDir}/processedResources/jvm/main")
    into("$buildDir/resources/main")
}


