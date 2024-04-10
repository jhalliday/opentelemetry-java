/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.profile.data.AttributeUnitData;
import io.opentelemetry.sdk.profile.data.FunctionData;
import io.opentelemetry.sdk.profile.data.LinkData;
import io.opentelemetry.sdk.profile.data.LocationData;
import io.opentelemetry.sdk.profile.data.MappingData;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.profile.data.SampleData;
import io.opentelemetry.sdk.profile.data.ValueTypeData;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
@AutoValue
public abstract class ImmutableProfileData implements ProfileData {

  @SuppressWarnings("TooManyParameters")
  public static ProfileData create(
      List<ValueTypeData> sampleTypes,
      List<SampleData> samples,
      List<MappingData> mappings,
      List<LocationData> locations,
      List<Long> locationIndices,
      List<FunctionData> functions,
      Attributes attributes,
      List<AttributeUnitData> attributeUnits,
      List<LinkData> links,
      List<String> stringTable,
      long dropFrames,
      long keepFrames,
      long timeNanos,
      long durationNanos,
      ValueTypeData periodType,
      long period,
      List<Long> comment,
      long defaultSampleType
  ) {
    return new AutoValue_ImmutableProfileData(
        sampleTypes, samples, mappings, locations, locationIndices, functions, attributes,
        attributeUnits, links, stringTable, dropFrames, keepFrames, timeNanos, durationNanos,
        periodType, period, comment, defaultSampleType);
  }

  ImmutableProfileData() {}
}
