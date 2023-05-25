plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")
}

description = "OpenTelemetry Profiling SDK"
otelJava.moduleName.set("io.opentelemetry.sdk.profile")

dependencies {
  api(project(":api:profile"))
  api(project(":api:events"))
  api(project(":sdk:common"))

  testImplementation(project(":sdk:profile-testing"))

  testImplementation("org.awaitility:awaitility")

  annotationProcessor("com.google.auto.value:auto-value")
}
