import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

plugins {
    java
    id("io.quarkus")
    id("com.github.jmongard.git-semver-plugin") version "0.4.2"
}

repositories {
    mavenLocal()
    mavenCentral()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("commons-codec:commons-codec:1.15")

    testImplementation("io.quarkus:quarkus-junit5:2.3.0.Final")
}

group = "com.katacoda"

project.version = project.findProperty("ciSemVer") ?: semver.version

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    val addGradleProperties by creating {
        description = "Write project properties to application properties."

        doLast {
            val properties = Properties()
            properties.load(FileInputStream(file("build/resources/main/application.properties")))
            properties.setProperty("project.version", project.version.toString())
            properties.store(FileOutputStream("build/resources/main/application.properties"), null)
        }
    }

    processResources { finalizedBy("addGradleProperties") }

    val ver by creating {
        doLast {
            println("Version: " + project.version)
        }
    }
}
