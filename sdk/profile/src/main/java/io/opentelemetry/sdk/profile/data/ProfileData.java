/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;
import java.util.List;

/**
 * Represents a complete profile, including sample types, samples,
 * mappings to binaries, locations, functions, string table, and additional metadata.
 * @see "pprofextended.proto::Profile"
 */
@Immutable
public interface ProfileData {

  // TODO docs

  List<ValueTypeData> getSampleTypes();
  List<SampleData> getSamples();
  List<MappingData> getMappings();
  List<LocationData> getLocations();
  long[] getLocationIndices();
  List<FunctionData> getFunctions();
  Attributes getAttributes();
  List<AttributeUnitData> getAttributeUnits();
  List<LinkData> getLinks();
  List<String> getStringTable();
  long getDropFrames();
  long getKeepFrames();
  long getTimeNanos();
  long getDurationNanos();
  ValueTypeData getPeriodType();
  long getPeriod();
  long[] getComment();
  long getDefaultSampleType();
}
