/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.LabelData;
import io.opentelemetry.sdk.profile.data.SampleData;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
@AutoValue
@AutoValue.CopyAnnotations // inherit deprecations
@SuppressWarnings("deprecation") // generated code uses deprecated methods in e.g. equals()
public abstract class ImmutableSampleData implements SampleData {

  public static SampleData create(
      List<Long> locationIndices,
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      List<Long> values,
      List<LabelData> labels,
      List<Long> attributes,
      long link,
      List<Long> timestamps
  ) {
    return new AutoValue_ImmutableSampleData(
        locationIndices, locationsStartIndex, locationsLength, stacktraceIdIndex, values,
        labels, attributes, link, timestamps);
  }

  ImmutableSampleData() {}
}
