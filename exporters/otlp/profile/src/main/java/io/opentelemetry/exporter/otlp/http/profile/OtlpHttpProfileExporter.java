/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.profile;

import io.opentelemetry.exporter.internal.okhttp.OkHttpExporter;
import io.opentelemetry.exporter.internal.otlp.profile.ProfileRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.export.ProfileExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Exports profiles using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpProfileExporter implements ProfileExporter {

  private final OkHttpExporter<ProfileRequestMarshaler> delegate;

  OtlpHttpProfileExporter(OkHttpExporter<ProfileRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpHttpProfileExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpHttpProfileExporter} instance.
   */
  public static OtlpHttpProfileExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpProfileExporterBuilder builder() {
    return new OtlpHttpProfileExporterBuilder();
  }

  /**
   * Submits all the given profiles in a single batch to the OpenTelemetry collector.
   *
   * @param profiles the list of sampled profiles to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<ProfileData> profiles) {
    ProfileRequestMarshaler exportRequest = ProfileRequestMarshaler.create(profiles);
    return delegate.export(exportRequest, profiles.size());
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
