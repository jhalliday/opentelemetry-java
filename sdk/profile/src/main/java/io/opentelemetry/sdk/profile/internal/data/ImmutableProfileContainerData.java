/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile.internal.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileContainerData;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableProfileContainerData implements ProfileContainerData {

  @SuppressWarnings("TooManyParameters")
  public static ProfileContainerData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      byte[] profileId,
      long startTimeUnixNano,
      long endTimeUnixNano,
      Attributes attributes,
      int droppedAttributesCount,
      @Nullable String originalPayloadFormat,
      @Nullable byte[] originalPayload,
      ProfileData profile
  ) {
    return new AutoValue_ImmutableProfileContainerData(
        resource, instrumentationScopeInfo, profileId, startTimeUnixNano, endTimeUnixNano,
        attributes, droppedAttributesCount, originalPayloadFormat, originalPayload, profile);
  }

  ImmutableProfileContainerData() {}
}
