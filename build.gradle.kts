import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.0"
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
  jacoco
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  // Spring Boot
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.0")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // AWS
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.0.0")

  // OpenAPI
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  // Jackson
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

  // Database
  implementation("com.zaxxer:HikariCP:5.1.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql")

  // Test
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
  testImplementation("org.wiremock:wiremock-standalone:3.5.4")
  testImplementation("org.testcontainers:postgresql:1.19.8")
  testImplementation("org.testcontainers:localstack:1.19.8")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
  testImplementation("org.springframework.security:spring-security-test")

  // Developer experience
  developmentOnly("org.springframework.boot:spring-boot-devtools")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}

// Jacoco code coverage
tasks.named("test") {
  finalizedBy("jacocoTestReport")
}

tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    html.required.set(true)
    xml.required.set(true)
  }
}
