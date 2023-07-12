/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profile;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.otlp.profile.ProfileRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.export.ProfileExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Exports profiles using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcProfileExporter implements ProfileExporter {

  private final GrpcExporter<ProfileRequestMarshaler> delegate;

  /**
   * Returns a new {@link OtlpGrpcProfileExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcProfileExporter} instance.
   */
  public static OtlpGrpcProfileExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcProfileExporterBuilder builder() {
    return new OtlpGrpcProfileExporterBuilder();
  }

  OtlpGrpcProfileExporter(GrpcExporter<ProfileRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Submits all the given profiles in a single batch to the OpenTelemetry collector.
   *
   * @param profiles the list of sampled profiles to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    ProfileRequestMarshaler request = ProfileRequestMarshaler.create(profiles);
    return delegate.export(request, profiles.size());
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
