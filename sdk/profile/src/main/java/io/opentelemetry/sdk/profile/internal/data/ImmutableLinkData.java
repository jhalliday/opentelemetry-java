/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.profile.data.LinkData;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableLinkData implements LinkData {

  public static LinkData create(
      byte[] traceId,
      byte[] spanId
  ) {
    return new AutoValue_ImmutableLinkData(traceId, spanId);
  }

  ImmutableLinkData() {}
}
