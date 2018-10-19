import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    id("me.champeau.gradle.jmh") version "0.4.7"
}

group = "org.bk"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree("lib").include("*.jar"))

    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
    testCompile("org.assertj:assertj-core:3.9.0")
    testCompile("io.jenetics:jenetics:4.2.1")
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

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}