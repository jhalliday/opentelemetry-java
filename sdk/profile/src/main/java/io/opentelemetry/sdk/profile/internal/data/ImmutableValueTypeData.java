/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.AggregationTemporality;
import io.opentelemetry.sdk.profile.data.ValueTypeData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableValueTypeData implements ValueTypeData {

  public static ValueTypeData create(
      long type,
      long unit,
      AggregationTemporality aggregationTemporality
  ) {
    return new AutoValue_ImmutableValueTypeData(type, unit, aggregationTemporality);
  }

  ImmutableValueTypeData() {}
}
