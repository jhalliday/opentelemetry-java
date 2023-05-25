plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Profiling API"
otelJava.moduleName.set("io.opentelemetry.api.profile")

dependencies {
  api(project(":api:all"))
}
