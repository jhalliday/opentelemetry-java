/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteProfile implements ReadWriteProfile {

  private final ProfileLimits profileLimits;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final long timestampEpochNanos;
  private final long observedTimestampEpochNanos;
  private final SpanContext spanContext;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  private SdkReadWriteProfile(
      ProfileLimits profileLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      @Nullable AttributesMap attributes) {
    this.profileLimits = profileLimits;
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.timestampEpochNanos = timestampEpochNanos;
    this.observedTimestampEpochNanos = observedTimestampEpochNanos;
    this.spanContext = spanContext;
    this.attributes = attributes;
  }

  /** Create the profile with the given configuration. */
  static SdkReadWriteProfile create(
      ProfileLimits profileLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      @Nullable AttributesMap attributes) {
    return new SdkReadWriteProfile(
        profileLimits,
        resource,
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        spanContext,
        attributes);
  }

  @Override
  public <T> ReadWriteProfile setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (attributes == null) {
        attributes =
            AttributesMap.create(
                profileLimits.getMaxNumberOfAttributes(),
                profileLimits.getMaxAttributeValueLength());
      }
      attributes.put(key, value);
    }
    return this;
  }

  private Attributes getImmutableAttributes() {
    synchronized (lock) {
      if (attributes == null || attributes.isEmpty()) {
        return Attributes.empty();
      }
      return attributes.immutableCopy();
    }
  }

  @Override
  public ProfileData toProfileData() {
    synchronized (lock) {
      return SdkProfileData.create(
          resource,
          instrumentationScopeInfo,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          spanContext,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues());
    }
  }
}
