plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry - Profile Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.profile")

dependencies {
  api(project(":sdk:all"))
  api(project(":sdk:profile"))

  implementation(project(":sdk-extensions:autoconfigure-spi"))

  testImplementation(project(":sdk:testing"))
  testImplementation(project(":sdk:profile-testing"))
}
