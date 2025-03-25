plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // KAFKA
    implementation("org.apache.kafka:kafka-clients:4.0.0")

    // HikariCP
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.5")

    // FASTERXML
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}

tasks.test {
    useJUnitPlatform()
}