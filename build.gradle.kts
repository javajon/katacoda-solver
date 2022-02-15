import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    java
    id("io.quarkus")
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
    implementation("com.jayway.jsonpath:json-path:2.7.0")

    testImplementation("io.quarkus:quarkus-junit5:2.4.1.Final")
}

group = "com.katacoda"

fun String.runCommand(currentWorkingDir: File = file("./")): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        workingDir = currentWorkingDir
        commandLine = this@runCommand.split("\\s".toRegex())
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

val gitBranch = "git rev-parse --abbrev-ref HEAD".runCommand()
val gitCommitIdLast = "git rev-list --tags --max-count=1".runCommand()
val gitTag = "git describe --tags $gitCommitIdLast".runCommand()

project.version = project.findProperty("ciSemVer") ?: gitTag + "-" + gitBranch

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
            println("version: " + gitTag + "-" + gitBranch)
        }
    }
}
