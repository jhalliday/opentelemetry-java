/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.profile.ProfileBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link ProfileBuilder}. */
final class SdkProfileBuilder implements ProfileBuilder {

  private final ProfilerSharedState profilerSharedState;
  private final ProfileLimits profileLimits;

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private long timestampEpochNanos;
  private long observedTimestampEpochNanos;

  @Nullable public List<String> frames;

  @Nullable private Context context;
  @Nullable private AttributesMap attributes;

  SdkProfileBuilder(
      ProfilerSharedState profilerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    this.profilerSharedState = profilerSharedState;
    this.profileLimits = profilerSharedState.getProfileLimits();
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  @Override
  public SdkProfileBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.timestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public SdkProfileBuilder setTimestamp(Instant instant) {
    this.timestampEpochNanos =
        TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  @Override
  public SdkProfileBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
    this.observedTimestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public SdkProfileBuilder setObservedTimestamp(Instant instant) {
    this.observedTimestampEpochNanos =
        TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  @Override
  public ProfileBuilder setFrames(List<String> frames) {
    this.frames = frames;
    return this;
  }

  @Override
  public SdkProfileBuilder setContext(Context context) {
    this.context = context;
    return this;
  }

  @Override
  public <T> SdkProfileBuilder setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (this.attributes == null) {
      this.attributes =
          AttributesMap.create(
              profileLimits.getMaxNumberOfAttributes(), profileLimits.getMaxAttributeValueLength());
    }
    this.attributes.put(key, value);
    return this;
  }

  @Override
  public void emit() {
    if (profilerSharedState.hasBeenShutdown()) {
      return;
    }
    Context context = this.context == null ? Context.current() : this.context;
    long observedTimestampEpochNanos =
        this.observedTimestampEpochNanos == 0
            ? this.profilerSharedState.getClock().now()
            : this.observedTimestampEpochNanos;
    profilerSharedState
        .getProfileProcessor()
        .onEmit(
            context,
            SdkReadWriteProfile.create(
                profilerSharedState.getProfileLimits(),
                profilerSharedState.getResource(),
                instrumentationScopeInfo,
                timestampEpochNanos,
                observedTimestampEpochNanos,
                frames,
                Span.fromContext(context).getSpanContext(),
                attributes));
  }
}
