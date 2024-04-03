/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.data;

import java.util.List;

/**
 * Each Sample records values encountered in some program context.
 * The program context is typically a stack trace, perhaps
 * augmented with auxiliary information like the thread-id, some
 * indicator of a higher level request being handled etc.
 * @see "pprofextended.proto::Sample"
 */
public interface SampleData {

  // TODO docs

  long[] getLocationIndices();
  long getLocationsStartIndex();
  long getLocationsLength();
  int getStacktraceIdIndex();
  long[] getValues();
  @Deprecated
  List<LabelData> getLabels();
  long[] getAttributes();
  long getLink();
  long[] getTimestamps();
}
