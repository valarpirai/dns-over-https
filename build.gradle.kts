import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.valarpirai"
version = "1.0.2"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar>() {
    // Minimizing a shadow JAR
    minimize()
    relocate("okhttp3", "org.valarpirai.shaded.okhttp3")
    relocate("okio", "org.valarpirai.shaded.okio")
    relocate("com.squareup.moshi", "org.valarpirai.shaded.com.squareup.moshi")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/valarpirai/dns-over-https")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "dns-over-https"
            version = version

            from(components["java"])
        }
    }
}
