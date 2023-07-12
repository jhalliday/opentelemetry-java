/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.profile.v1.internal.ExportProfileServiceRequest;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link Marshaler} to convert SDK {@link ProfileData} to OTLP ExportProfileServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ProfileRequestMarshaler extends MarshalerWithSize {

  private final ResourceProfileMarshaler[] resourceProfileMarshalers;

  /**
   * Returns a {@link ProfileRequestMarshaler} that can be used to convert the provided {@link
   * ProfileData} into a serialized OTLP ExportProfileServiceRequest.
   */
  public static ProfileRequestMarshaler create(Collection<ProfileData> profiles) {
    return new ProfileRequestMarshaler(ResourceProfileMarshaler.create(profiles));
  }

  private ProfileRequestMarshaler(ResourceProfileMarshaler[] resourceProfileMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportProfileServiceRequest.RESOURCE_PROFILES, resourceProfileMarshalers));
    this.resourceProfileMarshalers = resourceProfileMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportProfileServiceRequest.RESOURCE_PROFILES, resourceProfileMarshalers);
  }
}
