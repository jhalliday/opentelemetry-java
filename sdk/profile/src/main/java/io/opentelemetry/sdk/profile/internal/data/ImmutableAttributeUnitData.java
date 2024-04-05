/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.AttributeUnitData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableAttributeUnitData implements AttributeUnitData {

  public static AttributeUnitData create(
      long attributeKey,
      int unitIndex
  ) {
    return new AutoValue_ImmutableAttributeUnitData(attributeKey, unitIndex);
  }

  ImmutableAttributeUnitData() {}
}
