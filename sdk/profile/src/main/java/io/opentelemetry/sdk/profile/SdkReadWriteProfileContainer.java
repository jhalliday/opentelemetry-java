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
import io.opentelemetry.sdk.profile.data.ProfileContainerData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteProfile implements ReadWriteProfile {

  private final ProfileLimits profileLimits;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;

  private final long timestampEpochNanos;
  private final long observedTimestampEpochNanos;
  @Nullable private final List<String> frames;
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
      @Nullable List<String> frames,
      SpanContext spanContext,
      @Nullable AttributesMap attributes) {
    this.profileLimits = profileLimits;
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.timestampEpochNanos = timestampEpochNanos;
    this.observedTimestampEpochNanos = observedTimestampEpochNanos;
    this.frames = frames;
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
      @Nullable List<String> frames,
      SpanContext spanContext,
      @Nullable AttributesMap attributes) {
    return new SdkReadWriteProfile(
        profileLimits,
        resource,
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        frames,
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
  public ProfileContainerData toProfileData() {
    synchronized (lock) {
      return SdkProfileContainerData.create(
          resource,
          instrumentationScopeInfo,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          frames == null ? Collections.emptyList() : frames,
          spanContext,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues());
    }
  }
}
