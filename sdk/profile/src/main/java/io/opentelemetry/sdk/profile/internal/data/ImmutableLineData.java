/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.LineData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableLineData implements LineData {

  public static LineData create(
      int functionIndex,
      int line,
      int column
  ) {
    return new AutoValue_ImmutableLineData(functionIndex, line, column);
  }

  ImmutableLineData() {}
}
