/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.profile.v1.internal.ResourceProfiles;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceProfile.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceProfileMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationScopeProfileMarshaler[] instrumentationScopeProfileMarshalers;

  /** Returns Marshalers of ResourceProfile created by grouping the provided Profiles. */
  @SuppressWarnings("AvoidObjectArrays")
  public static ResourceProfileMarshaler[] create(Collection<ProfileData> profiles) {
    Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(profiles);

    ResourceProfileMarshaler[] resourceProfileMarshalers =
        new ResourceProfileMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeProfileMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationScopeProfileMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationScopeInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationScopeProfileMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceProfileMarshalers[posResource++] =
          new ResourceProfileMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return resourceProfileMarshalers;
  }

  ResourceProfileMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeProfileMarshaler[] instrumentationScopeProfileMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationScopeProfileMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationScopeProfileMarshalers = instrumentationScopeProfileMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceProfiles.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceProfiles.SCOPE_PROFILES, instrumentationScopeProfileMarshalers);
    output.serializeString(ResourceProfiles.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeProfileMarshaler[] instrumentationScopeProfileMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceProfiles.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceProfiles.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceProfiles.SCOPE_PROFILES, instrumentationScopeProfileMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>>
      groupByResourceAndScope(Collection<ProfileData> profiles) {
    return MarshalerUtil.groupByResourceAndScope(
        profiles,
        ProfileData::getResource,
        ProfileData::getInstrumentationScopeInfo,
        ProfileMarshaler::create);
  }
}
