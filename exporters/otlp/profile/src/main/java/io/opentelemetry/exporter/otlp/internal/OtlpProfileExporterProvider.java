/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.DATA_TYPE_PROFILES;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_GRPC;
import static io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;

import io.opentelemetry.exporter.internal.otlp.OtlpConfigUtil;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.http.profile.OtlpHttpProfileExporter;
import io.opentelemetry.exporter.otlp.http.profile.OtlpHttpProfileExporterBuilder;
import io.opentelemetry.exporter.otlp.profile.OtlpGrpcProfileExporter;
import io.opentelemetry.exporter.otlp.profile.OtlpGrpcProfileExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.profile.ConfigurableProfileExporterProvider;
import io.opentelemetry.sdk.profile.export.ProfileExporter;

/**
 * {@link ProfileExporter} SPI implementation for {@link OtlpGrpcProfileExporter} and {@link
 * OtlpHttpProfileExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OtlpProfileExporterProvider implements ConfigurableProfileExporterProvider {
  @Override
  public ProfileExporter createExporter(ConfigProperties config) {
    String protocol = OtlpConfigUtil.getOtlpProtocol(DATA_TYPE_PROFILES, config);

    if (protocol.equals(PROTOCOL_HTTP_PROTOBUF)) {
      OtlpHttpProfileExporterBuilder builder = httpBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_PROFILES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

      return builder.build();
    } else if (protocol.equals(PROTOCOL_GRPC)) {
      OtlpGrpcProfileExporterBuilder builder = grpcBuilder();

      OtlpConfigUtil.configureOtlpExporterBuilder(
          DATA_TYPE_PROFILES,
          config,
          builder::setEndpoint,
          builder::addHeader,
          builder::setCompression,
          builder::setTimeout,
          builder::setTrustedCertificates,
          builder::setClientTls,
          retryPolicy -> RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy));

      return builder.build();
    }
    throw new ConfigurationException("Unsupported OTLP profile protocol: " + protocol);
  }

  @Override
  public String getName() {
    return "otlp";
  }

  // Visible for testing
  OtlpHttpProfileExporterBuilder httpBuilder() {
    return OtlpHttpProfileExporter.builder();
  }

  // Visible for testing
  OtlpGrpcProfileExporterBuilder grpcBuilder() {
    return OtlpGrpcProfileExporter.builder();
  }
}
