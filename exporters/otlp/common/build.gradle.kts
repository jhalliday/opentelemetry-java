plugins {
  id("otel.java-conventions")
  id("otel.publish-conventions")

  id("otel.jmh-conventions")
  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry Protocol Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.internal.otlp")

val versions: Map<String, String> by project
dependencies {
  testImplementation(project(mapOf("path" to ":sdk:profile-testing")))
  protoSource("io.opentelemetry.proto:opentelemetry-proto:${versions["io.opentelemetry.proto"]}")

  api(project(":exporters:common"))

  implementation(project(":sdk-extensions:autoconfigure-spi"))

  compileOnly(project(":sdk:metrics"))
  compileOnly(project(":sdk:trace"))
  compileOnly(project(":sdk:logs"))

  implementation("com.squareup.okhttp3:okhttp")

  testImplementation(project(":sdk:metrics"))
  testImplementation(project(":sdk:trace"))
  testImplementation(project(":sdk:logs"))
  testImplementation(project(":sdk:testing"))

  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")

  jmhImplementation(project(":sdk:testing"))
  jmhImplementation("com.fasterxml.jackson.core:jackson-core")
  jmhImplementation("io.opentelemetry.proto:opentelemetry-proto")
  jmhImplementation("io.grpc:grpc-netty")
}

wire {
  root(
    "opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest",
    "opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest",
    "opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest",
    "opentelemetry.proto.collector.profile.v1.ExportProfileServiceRequest",
  )

  custom {
    schemaHandlerFactoryClass = "io.opentelemetry.gradle.ProtoFieldsWireHandlerFactory"
  }
}
