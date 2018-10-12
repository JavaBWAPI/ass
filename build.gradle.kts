plugins {
    java
    id("me.champeau.gradle.jmh") version "0.4.7"
}

group = "org.bk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree("lib").include("*.jar"))

    testCompile("org.junit.jupiter:junit-jupiter-api:5.0.2")
    testCompile("org.junit.platform:junit-platform-launcher:1.2.0")
    testCompile("org.junit.platform:junit-platform-engine:1.2.0")
    testCompile("org.assertj:assertj-core:3.9.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configurations.testCompile.extendsFrom(configurations.compileOnly)

tasks {
    "check" {
        dependsOn("jmh")
    }
}

