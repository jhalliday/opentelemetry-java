/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.profile;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.profiles.v1.alternatives.pprofextended.internal.Profile;
import io.opentelemetry.sdk.profile.data.ProfileData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ProfileMarshaler extends MarshalerWithSize {

  private final ValueTypeMarshaler[] sampleTypeMarshalers;
  private final SampleMarshaler[] sampleMarshalers;
  private final MappingMarshaler[] mappingMarshalers;
  private final LocationMarshaler[] locationMarshalers;
  private final long[] locationIndices;
  private final FunctionMarshaler[] functionMarshalers;
  private final KeyValueMarshaler[] attributeMarshalers;
  private final AttributeUnitMarshaler[] attributeUnitMarshalers;
  private final LinkMarshaler[] linkMarshalers;
  private final String[] stringTable;
  private final long dropFrames;
  private final long keepFrames;
  private final long timeNanos;
  private final long durationNanos;
  private final ValueTypeMarshaler periodTypeMarshaler;
  private final long period;
  private final long[] comment;
  private final int defaultSampleType;

  static ProfileMarshaler create(ProfileData profileData) {

    ValueTypeMarshaler[] sampleTypeMarshalers = ValueTypeMarshaler.createRepeated(profileData.getSampleTypes());
    SampleMarshaler[] sampleMarshalers = SampleMarshaler.createRepeated(profileData.getSamples());
    MappingMarshaler[] mappingMarshalers = MappingMarshaler.createRepeated(profileData.getMappings());
    LocationMarshaler[] locationMarshalers = LocationMarshaler.createRepeated(profileData.getLocations());
    FunctionMarshaler[] functionMarshalers = FunctionMarshaler.createRepeated(profileData.getFunctions());
    KeyValueMarshaler[] attributeMarshalers = KeyValueMarshaler.createRepeated(profileData.getAttributes());
    AttributeUnitMarshaler[] attributeUnitsMarshalers = AttributeUnitMarshaler.createRepeated(profileData.getAttributeUnits());
    LinkMarshaler[] linkMarshalers = LinkMarshaler.createRepeated(profileData.getLinks());
    ValueTypeMarshaler periodTypeMarshaler = ValueTypeMarshaler.create(profileData.getPeriodType());

    return new ProfileMarshaler(
        sampleTypeMarshalers,
        sampleMarshalers,
        mappingMarshalers,
        locationMarshalers,
        profileData.getLocationIndices(),
        functionMarshalers,
        attributeMarshalers,
        attributeUnitsMarshalers,
        linkMarshalers,
        profileData.getStringTable().toArray(new String[] {}),
        profileData.getDropFrames(),
        profileData.getKeepFrames(),
        profileData.getTimeNanos(),
        profileData.getDurationNanos(),
        periodTypeMarshaler,
        profileData.getPeriod(),
        profileData.getComment(),
        profileData.getDefaultSampleType()
    );
  }

  private ProfileMarshaler(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      MappingMarshaler[] mappingMarshalers,
      LocationMarshaler[] locationMarshalers,
      long[] locationIndices,
      FunctionMarshaler[] functionMarshalers,
      KeyValueMarshaler[] attributeMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkMarshalers,
      String[] stringTable,
      long dropFrames,
      long keepFrames,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      long[] comment,
      int defaultSampleType
  ) {
    super(calculateSize(
        sampleTypeMarshalers,
        sampleMarshalers,
        mappingMarshalers,
        locationMarshalers,
        locationIndices,
        functionMarshalers,
        attributeMarshalers,
        attributeUnitMarshalers,
        linkMarshalers,
        stringTable,
        dropFrames,
        keepFrames,
        timeNanos,
        durationNanos,
        periodTypeMarshaler,
        period,
        comment,
        defaultSampleType
    ));
    this.sampleTypeMarshalers = sampleTypeMarshalers;
    this.sampleMarshalers = sampleMarshalers;
    this.mappingMarshalers = mappingMarshalers;
    this.locationMarshalers = locationMarshalers;
    this.locationIndices = locationIndices;
    this.functionMarshalers = functionMarshalers;
    this.attributeMarshalers = attributeMarshalers;
    this.attributeUnitMarshalers = attributeUnitMarshalers;
    this.linkMarshalers = linkMarshalers;
    this.stringTable = stringTable;
    this.dropFrames = dropFrames;
    this.keepFrames = keepFrames;
    this.timeNanos = timeNanos;
    this.durationNanos = durationNanos;
    this.periodTypeMarshaler = periodTypeMarshaler;
    this.period = period;
    this.comment = comment;
    this.defaultSampleType = defaultSampleType;
  }


  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    output.serializeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    output.serializeRepeatedMessage(Profile.MAPPING, mappingMarshalers);
    output.serializeRepeatedMessage(Profile.LOCATION, locationMarshalers);
    output.serializeRepeatedInt64(Profile.LOCATION_INDICES, locationIndices);
    output.serializeRepeatedMessage(Profile.FUNCTION, functionMarshalers);
    output.serializeRepeatedMessage(Profile.ATTRIBUTE_TABLE, attributeMarshalers);
    output.serializeRepeatedMessage(Profile.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    output.serializeRepeatedMessage(Profile.LINK_TABLE, linkMarshalers);
    for (String s : stringTable) {
      output.serializeString(Profile.STRING_TABLE, s.getBytes(StandardCharsets.UTF_8));
    }
    output.serializeInt64(Profile.DROP_FRAMES, dropFrames);
    output.serializeInt64(Profile.KEEP_FRAMES, keepFrames);
    output.serializeInt64(Profile.TIME_NANOS, timeNanos);
    output.serializeInt64(Profile.DURATION_NANOS, durationNanos);
    output.serializeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    output.serializeInt64(Profile.PERIOD, period);
    output.serializeRepeatedInt64(Profile.COMMENT, comment);
    output.serializeInt64(Profile.DEFAULT_SAMPLE_TYPE, defaultSampleType);
  }

  private static int calculateSize(
      ValueTypeMarshaler[] sampleTypeMarshalers,
      SampleMarshaler[] sampleMarshalers,
      MappingMarshaler[] mappingMarshalers,
      LocationMarshaler[] locationMarshalers,
      long[] locationIndices,
      FunctionMarshaler[] functionMarshalers,
      KeyValueMarshaler[] attributeMarshalers,
      AttributeUnitMarshaler[] attributeUnitMarshalers,
      LinkMarshaler[] linkMarshalers,
      String[] stringTable,
      long dropFrames,
      long keepFrames,
      long timeNanos,
      long durationNanos,
      ValueTypeMarshaler periodTypeMarshaler,
      long period,
      long[] comment,
      int defaultSampleType
  ) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE_TYPE, sampleTypeMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.SAMPLE, sampleMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.MAPPING, mappingMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.LOCATION, locationMarshalers);
    size += MarshalerUtil.sizeRepeatedInt64(Profile.LOCATION_INDICES, locationIndices);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.FUNCTION, functionMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTE_TABLE, attributeMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.ATTRIBUTE_UNITS, attributeUnitMarshalers);
    size += MarshalerUtil.sizeRepeatedMessage(Profile.LINK_TABLE, linkMarshalers);
    for (String s : stringTable) {
      size += MarshalerUtil.sizeBytes(Profile.STRING_TABLE, s.getBytes(StandardCharsets.UTF_8));
    }
    //output.serializeRepeatedMessage();
    size += MarshalerUtil.sizeInt64(Profile.DROP_FRAMES, dropFrames);
    size += MarshalerUtil.sizeInt64(Profile.KEEP_FRAMES, keepFrames);
    size += MarshalerUtil.sizeInt64(Profile.TIME_NANOS, timeNanos);
    size += MarshalerUtil.sizeInt64(Profile.DURATION_NANOS, durationNanos);
    size += MarshalerUtil.sizeMessage(Profile.PERIOD_TYPE, periodTypeMarshaler);
    size += MarshalerUtil.sizeInt64(Profile.PERIOD, period);
    size += MarshalerUtil.sizeRepeatedInt64(Profile.COMMENT, comment);
    size += MarshalerUtil.sizeInt64(Profile.DEFAULT_SAMPLE_TYPE, defaultSampleType);
    return size;
  }
}
