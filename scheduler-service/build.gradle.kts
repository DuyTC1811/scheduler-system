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
    implementation("org.quartz-scheduler:quartz:2.5.0")

    // LOG4J
    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")

    // KAFKA
    implementation("org.apache.kafka:kafka-clients:4.0.0")

    // HikariCP
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.5")

    // YAML
    implementation("org.yaml:snakeyaml:2.4")
}

tasks.test {
    useJUnitPlatform()
}