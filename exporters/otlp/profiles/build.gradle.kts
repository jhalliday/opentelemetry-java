plugins {
  id("otel.java-conventions")
  // TODO (jack-berg): uncomment when ready to publish
  // id("otel.publish-conventions")

  id("otel.animalsniffer-conventions")

  id("com.squareup.wire")
}

description = "OpenTelemetry - Profiles Exporter"
otelJava.moduleName.set("io.opentelemetry.exporter.otlp.profiles")

val versions: Map<String, String> by project
dependencies {
  protoSource("io.opentelemetry.proto:opentelemetry-proto:${versions["io.opentelemetry.proto"]}")

  api(project(":sdk:common"))
  api(project(":exporters:common"))
  implementation(project(":exporters:otlp:common"))

  annotationProcessor("com.google.auto.value:auto-value")

  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.google.protobuf:protobuf-java-util")
  testImplementation("io.opentelemetry.proto:opentelemetry-proto")
}

wire {
  root(
    "opentelemetry.proto.collector.profiles.v1experimental.ExportProfilesServiceRequest"
  )

  custom {
    schemaHandlerFactoryClass = "io.opentelemetry.gradle.ProtoFieldsWireHandlerFactory"
  }
}
