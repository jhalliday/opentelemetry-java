/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.profile;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Used to construct and emit profiles from a {@link Profiler}.
 *
 * <p>Obtain a {@link Profiler#profileBuilder()}, add properties using the setters, and emit the
 * profile by calling {@link #emit()}.
 */
public interface ProfileBuilder {

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the profile occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  ProfileBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}, using the instant.
   *
   * <p>The {@code timestamp} is the time at which the profile occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  ProfileBuilder setTimestamp(Instant instant);

  /**
   * Set the epoch {@code observedTimestamp}, using the timestamp and unit.
   *
   * <p>The {@code observedTimestamp} is the time at which the profile was observed. If unset, it
   * will be set to the {@code timestamp}. {@code observedTimestamp} may be different from {@code
   * timestamp} if profiles are being processed asynchronously (e.g. from a file or on a different
   * thread).
   */
  ProfileBuilder setObservedTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the {@code observedTimestamp}, using the instant.
   *
   * <p>The {@code observedTimestamp} is the time at which the profile was observed. If unset, it
   * will be set to the {@code timestamp}. {@code observedTimestamp} may be different from {@code
   * timestamp} if profiles are being processed asynchronously (e.g. from a file or on a different
   * thread).
   */
  ProfileBuilder setObservedTimestamp(Instant instant);

  /** Set the context. */
  ProfileBuilder setContext(Context context);

  /**
   * Sets attributes. If the {@link ProfileBuilder} previously contained a mapping for any of the
   * keys, the old values are replaced by the specified values.
   */
  @SuppressWarnings("unchecked")
  default ProfileBuilder setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /** Sets an attribute. */
  <T> ProfileBuilder setAttribute(AttributeKey<T> key, T value);

  /** Emit the profile. */
  void emit();
}
