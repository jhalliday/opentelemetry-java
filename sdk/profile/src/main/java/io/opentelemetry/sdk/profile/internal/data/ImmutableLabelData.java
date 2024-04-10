/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.LabelData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableLabelData implements LabelData {

  public static LabelData create(
      long keyIndex,
      long strIndex,
      long num,
      long numUnitIndex
  ) {
    return new AutoValue_ImmutableLabelData(keyIndex, strIndex, num, numUnitIndex);
  }

  ImmutableLabelData() {}
}
