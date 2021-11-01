sourceControl {
    gitRepository(uri("https://github.com/chrsoo/ccrypt-j.git")) {
        producesModule("se.jabberwocky.ccrypt:ccrypt-j")
    }
}

pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}
rootProject.name="solver"

