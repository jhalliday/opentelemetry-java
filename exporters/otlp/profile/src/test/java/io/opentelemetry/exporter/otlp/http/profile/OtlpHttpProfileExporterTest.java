/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.profile;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.profile.ResourceProfileMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.profile.v1.ResourceProfiles;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

class OtlpHttpProfileExporterTest
    extends AbstractHttpTelemetryExporterTest<ProfileData, ResourceProfiles> {

  protected OtlpHttpProfileExporterTest() {
    super("profile", "/v1/profile", ResourceProfiles.getDefaultInstance());
  }

  @Override
  protected TelemetryExporterBuilder<ProfileData> exporterBuilder() {
    OtlpHttpProfileExporterBuilder builder = OtlpHttpProfileExporter.builder();
    return new TelemetryExporterBuilder<ProfileData>() {
      @Override
      public TelemetryExporterBuilder<ProfileData> setEndpoint(String endpoint) {
        builder.setEndpoint(endpoint);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setTimeout(long timeout, TimeUnit unit) {
        builder.setTimeout(timeout, unit);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setTimeout(Duration timeout) {
        builder.setTimeout(timeout);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setCompression(String compression) {
        builder.setCompression(compression);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> addHeader(String key, String value) {
        builder.addHeader(key, value);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setAuthenticator(Authenticator authenticator) {
        Authenticator.setAuthenticatorOnDelegate(builder, authenticator);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setTrustedCertificates(byte[] certificates) {
        builder.setTrustedCertificates(certificates);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setSslContext(
          SSLContext ssLContext, X509TrustManager trustManager) {
        builder.setSslContext(ssLContext, trustManager);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setClientTls(
          byte[] privateKeyPem, byte[] certificatePem) {
        builder.setClientTls(privateKeyPem, certificatePem);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setRetryPolicy(RetryPolicy retryPolicy) {
        RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<ProfileData> setChannel(io.grpc.ManagedChannel channel) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public TelemetryExporter<ProfileData> build() {
        return TelemetryExporter.wrap(builder.build());
      }
    };
  }

  @Override
  protected ProfileData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeProfileData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<ProfileData> telemetry) {
    return ResourceProfileMarshaler.create(telemetry);
  }
}
