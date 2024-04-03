/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.profiles.v1.internal.ExportProfilesServiceRequest;
import io.opentelemetry.sdk.profile.data.ProfileContainerData;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link Marshaler} to convert SDK {@link ProfileContainerData} to OTLP ExportProfileServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ProfilesRequestMarshaler extends MarshalerWithSize {

  private final ResourceProfilesMarshaler[] resourceProfilesMarshalers;

  /**
   * Returns a {@link ProfilesRequestMarshaler} that can be used to convert the provided {@link
   * ProfileContainerData} into a serialized OTLP ExportProfileServiceRequest.
   */
  public static ProfilesRequestMarshaler create(Collection<ProfileContainerData> profiles) {
    return new ProfilesRequestMarshaler(ResourceProfilesMarshaler.create(profiles));
  }

  private ProfilesRequestMarshaler(ResourceProfilesMarshaler[] resourceProfilesMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportProfilesServiceRequest.RESOURCE_PROFILES, resourceProfilesMarshalers));
    this.resourceProfilesMarshalers = resourceProfilesMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportProfilesServiceRequest.RESOURCE_PROFILES, resourceProfilesMarshalers);
  }
}
