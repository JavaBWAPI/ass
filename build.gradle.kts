plugins {
    java
    id("maven")
    id("me.champeau.gradle.jmh") version "0.5.0"
    id("com.github.ben-manes.versions") version "0.28.0"
}

group = "org.bk"
version = "2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(fileTree("lib").include("*.jar"))
    compileOnly("com.github.JavaBWAPI:JBwapi:1.5")
    compileOnly("com.github.luben:zstd-jni:1.4.+")
    compileOnly("com.jsoniter:jsoniter:0.9.+")

    testImplementation("org.junit.jupiter:junit-jupiter:5.+")
    testImplementation("org.assertj:assertj-core:3.+")
    testImplementation("io.jenetics:jenetics:5.+")
    testImplementation("org.mockito:mockito-core:3.+")
    testImplementation("com.github.davidmoten:rtree:0.8.7")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

configurations.testImplementation.get().extendsFrom(configurations.compileOnly.get())

tasks {
    check {
        dependsOn("jmh", "javadoc")
    }

    test {
        useJUnitPlatform()
    }

    javadoc {
        destinationDir = File("docs")
    }

}

jmh {
    resultFormat = "JSON"
    duplicateClassesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
}
