plugins {
    id("java")
}

group = "zxc.mrdrag0nxyt"
version = "1.0.0"

repositories {
    mavenCentral()

    // https://wiki.placeholderapi.com/developers
    maven { url = uri("https://repo.extendedclip.com/releases/") }

    // https://docs.papermc.io/paper/dev/project-setup
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
}