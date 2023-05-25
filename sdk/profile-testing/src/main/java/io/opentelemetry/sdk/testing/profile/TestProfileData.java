/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.profile;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.profile.data.ProfileData;
import io.opentelemetry.sdk.resources.Resource;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of {@link ProfileData}. */
@Immutable
@AutoValue
public abstract class TestProfileData implements ProfileData {

  /** Creates a new Builder for creating an {@link ProfileData} instance. */
  public static Builder builder() {
    return new AutoValue_TestProfileData.Builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(InstrumentationScopeInfo.empty())
        .setTimestamp(0, TimeUnit.NANOSECONDS)
        .setObservedTimestamp(0, TimeUnit.NANOSECONDS)
        .setSpanContext(SpanContext.getInvalid())
        .setAttributes(Attributes.empty())
        .setTotalAttributeCount(0);
  }

  TestProfileData() {}

  /** A {@code Builder} class for {@link TestProfileData}. */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestProfileData autoBuild();

    /** Create a new {@link ProfileData} instance from the data in this. */
    public TestProfileData build() {
      return autoBuild();
    }

    /** Set the {@link Resource}. */
    public abstract Builder setResource(Resource resource);

    /** Sets the {@link InstrumentationScopeInfo}. */
    public abstract Builder setInstrumentationScopeInfo(
        InstrumentationScopeInfo instrumentationScopeInfo);

    /**
     * Set the epoch {@code timestamp}, using the instant.
     *
     * <p>The {@code timestamp} is the time at which the profile occurred.
     */
    public Builder setTimestamp(Instant instant) {
      return setTimestampEpochNanos(
          TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano());
    }

    /**
     * Set the epoch {@code timestamp}, using the timestamp and unit.
     *
     * <p>The {@code timestamp} is the time at which the profile occurred.
     */
    public Builder setTimestamp(long timestamp, TimeUnit unit) {
      return setTimestampEpochNanos(unit.toNanos(timestamp));
    }

    /**
     * Set the epoch {@code timestamp}.
     *
     * <p>The {@code timestamp} is the time at which the profile occurred.
     */
    abstract Builder setTimestampEpochNanos(long epochNanos);

    /**
     * Set the {@code observedTimestamp}, using the instant.
     *
     * <p>The {@code observedTimestamp} is the time at which the profile was observed.
     */
    public Builder setObservedTimestamp(Instant instant) {
      return setObservedTimestampEpochNanos(
          TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano());
    }

    /**
     * Set the epoch {@code observedTimestamp}, using the timestamp and unit.
     *
     * <p>The {@code observedTimestamp} is the time at which the profile was observed.
     */
    public Builder setObservedTimestamp(long timestamp, TimeUnit unit) {
      return setObservedTimestampEpochNanos(unit.toNanos(timestamp));
    }

    /**
     * Set the epoch {@code observedTimestamp}.
     *
     * <p>The {@code observedTimestamp} is the time at which the profile was observed.
     */
    abstract Builder setObservedTimestampEpochNanos(long epochNanos);

    /** Set the span context. */
    public abstract Builder setSpanContext(SpanContext spanContext);

    /** Set the attributes. */
    public abstract Builder setAttributes(Attributes attributes);

    /** Set the total attribute count. */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
