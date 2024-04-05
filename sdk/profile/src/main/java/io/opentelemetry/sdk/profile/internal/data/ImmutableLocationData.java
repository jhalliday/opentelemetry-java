/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.LineData;
import io.opentelemetry.sdk.profile.data.LocationData;
import javax.annotation.concurrent.Immutable;
import java.util.List;

@Immutable
@AutoValue
@AutoValue.CopyAnnotations // inherit deprecations
@SuppressWarnings("deprecation") // generated code uses deprecated methods in e.g. equals()
public abstract class ImmutableLocationData implements LocationData {

  public static LocationData create(
      long id,
      int mappingIndex,
      long address,
      List<LineData> lines,
      boolean folded,
      int typeIndex,
      List<Long> attributes
  ) {
    return new AutoValue_ImmutableLocationData(
        id, mappingIndex, address, lines, folded, typeIndex, attributes);
  }

  ImmutableLocationData() {}
}
