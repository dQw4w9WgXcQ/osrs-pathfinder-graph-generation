plugins {
    java
}

group = "github.dqw4w9wgxcq"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.runelite.net")
    }
}

dependencies {
    implementation("net.runelite:cache:1.8.26")//provides gson 2.8.5, slf4j api 1.7.25, guava 23.2-jre
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("commons-cli:commons-cli:1.5.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }
}