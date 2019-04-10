plugins {
    java
    id("maven")
    id("me.champeau.gradle.jmh") version "0.4.8"
    id("org.sonarqube") version "2.7"
    id("com.github.ben-manes.versions") version "0.21.0"
}

group = "org.bk"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(fileTree("lib").include("*.jar"))
    implementation("com.github.JasperGeurtz:JBWAPI:develop-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("io.jenetics:jenetics:4.4.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configurations.testImplementation.get().extendsFrom(configurations.implementation.get())
configurations.jmhCompile.get().extendsFrom(configurations.implementation.get())

tasks {
    check {
        dependsOn("jmh")
    }

    test {
        useJUnitPlatform()
    }
}

jmh {
    resultFormat = "JSON"
}